package normalCommands;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CacheUtils;
import utils.Constants;
import utils.Msg;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GenerateBingo extends Command {
    public static Map<String, BufferedImage> bingoBoard = new HashMap<>();
    private EventWaiter waiter;

    public GenerateBingo(EventWaiter waiter) {
        this.name = "cgen";
        this.aliases = new String[]{"gencard", "generatebingo"};
        this.category = new Category("Normal");
        this.ownerCommand = false;
        this.waiter = waiter;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(!event.getGuild().getId().equals("240614697848537089") && !event.getGuild().getId().equals("365932526297939971") && !event.getGuild().getId().equals("705622006652993607")) return;
        User user;
        if((event.getMember().hasPermission(Permission.ADMINISTRATOR) ||
                event.getAuthor().getId().equals("169122787099672577") || event.getAuthor().getId().equals("222488511385698304") || event.getAuthor().getId().equals("195621535703105536"))
                && !event.getMessage().getMentionedUsers().isEmpty()) {
                user = event.getMessage().getMentionedUsers().get(0);
                if(bingoBoard.get(user.getId()) != null){
                    new ButtonMenu.Builder()
                            .setDescription("There is a card for this user already. Are you sure you want to (re)generate another?")
                            .setChoices(EmojiManager.getForAlias("white_check_mark").getUnicode())
                            .addChoice(EmojiManager.getForAlias("x").getUnicode())
                            .setEventWaiter(waiter)
                            .setTimeout(20, TimeUnit.SECONDS)
                            .setColor(Color.orange)
                            .setAction(v -> {
                                if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":x:")) {
                                    Msg.replyTimed(event, "Cancelled (re)generating bingo card.", 5, TimeUnit.SECONDS);
                                }
                                else if(EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":white_check_mark:")){
                                    Message m = event.getTextChannel().sendMessage("Generating card... Please wait.").complete();
                                    generateCard(event, m, event.getMessage().getMentionedUsers().get(0));
                                }
                            })
                            .setFinalAction(me -> {
                                me.delete().queue();
                            }).build().display(event.getTextChannel());
                }
                else{
                    Message m = event.getTextChannel().sendMessage("Generating card... Please wait.").complete();
                    generateCard(event, m, event.getMessage().getMentionedUsers().get(0));
                }
        }
        else{
            return;
            /*
            user = event.getAuthor();
            if(bingoBoard.get(user.getId()) != null){
                Msg.reply(event,"You already have a bingo card. You cannot make another! Ask the admins or Snowy to create another. But here is your card.\n\n" +
                        "To check your card, use: `"+ Constants.D_PREFIX +"card` command.");
                return;
            }
            Message m = event.getTextChannel().sendMessage("Generating card... Please wait.").complete();
            generateCard(event, m, event.getAuthor());*/
        }

    }

    private void generateCard(CommandEvent event, Message m, User user){
        this.cooldown = 30;
        this.cooldownScope = CooldownScope.GLOBAL;
        try{
            String basePicURL = "http://cubiccastles.com/recipe_html/";

            Document doc = Jsoup.parse(CacheUtils.getCache("item"));

            Elements itemDiv = doc.select("div#CONTAINER > div.HIDE_CONTAINER");

            ArrayList<String> itemImages = new ArrayList<>();
            Element itemList = itemDiv.get(2);
            while(itemImages.size() < 24){
                Random rand = new Random();
                Element item = itemList.select("tr > td").get(rand.nextInt(itemList.select("tr > td").size()));
                String u = basePicURL + item.select("img").attr("src");
                if(!itemImages.contains(u)){
                    itemImages.add(u);
                }
            }

            itemImages.add(12, "placeHolder");

            BufferedImage board = generateBoard(itemImages);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(board, "png", os);                          // Passing: ​(RenderedImage im, String formatName, OutputStream output)
            InputStream is = new ByteArrayInputStream(os.toByteArray());

            m.delete().queue();

            Message mssg = event.getTextChannel().sendMessage(user.getAsMention()+"'s Bingo Card.").addFile(is, "bingoboard.png").complete();

            bingoBoard.put(user.getId(), board);
            addCard(user.getId(), board);

            user.openPrivateChannel().queue(c -> {
                EmbedBuilder em = new EmbedBuilder();
                em.setTitle("BINGO card");
                em.setColor(Color.ORANGE);
                em.setImage(mssg.getAttachments().get(0).getUrl());
                em.setFooter("Request assistance with admin, if you have any issues.");
                c.sendMessage(em.build()).queue();
                Msg.replyTimed(event, "Successfully DM'ed the card.", 5, TimeUnit.SECONDS);
            }, n -> Msg.replyTimed(event, "Could not send a DM. Please manually save the following card or retrieve it later using `\"+Constants.D_PREFIX+\"card` command.", 5, TimeUnit.SECONDS));

            itemImages.clear();
            is.close();
            os.close();
        }catch (Exception e){
            e.printStackTrace();
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "GenerateBingo.java");
        }
    }

    private void addCard(String id, BufferedImage board) throws IOException {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File cacheDir = new File(workingDir.resolve("db/cards/bingocards/").toUri());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File outputfile = new File(cacheDir, URLEncoder.encode(id + ".png", "utf-8"));
        ImageIO.write(board, "png", outputfile);
    }


    private ArrayList<BufferedImage> generateCards(ArrayList<String> itemImages) throws IOException {
        ArrayList<BufferedImage> cards = new ArrayList<>();
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/global").toUri());

        for(int i = 0; i < itemImages.size(); i++){
            BufferedImage bingoSlot = ImageIO.read(new File(guildDir, "bingoslot.png"));
            Graphics backG = bingoSlot.getGraphics();
            if(i == 12) {
                BufferedImage free = ImageIO.read(new File(guildDir, "free.png"));
                Dimension dim = getScaledDimension(new Dimension(free.getWidth(), free.getHeight()), new Dimension(bingoSlot.getWidth(), bingoSlot.getHeight()));
                backG.drawImage(free.getScaledInstance((int)dim.getWidth(), (int)dim.getHeight(), Image.SCALE_SMOOTH), 2, 2, null);
                free.flush();
            }
            else {
//                BufferedImage image = ImageIO.read(new File(guildDir, "test.png"));
                URL url = new URL(itemImages.get(i));
                BufferedImage image = ImageIO.read(url);
                double widthScale, heightScale;
                if(image.getWidth() < image.getHeight()){
                    widthScale = 1;
                    heightScale = image.getHeight()/(0.0+image.getWidth());
                }
                else{
                    widthScale = image.getWidth()/(0.0+image.getHeight());
                    heightScale = 1;
                }
                Dimension dim = getScaledDimension(new Dimension(image.getWidth()+(int)(50*widthScale), image.getHeight()+(int)(50*heightScale)), new Dimension(bingoSlot.getWidth(), bingoSlot.getHeight()));
                int x = (bingoSlot.getWidth() - (int)dim.getWidth()) / 2;
                int y = (bingoSlot.getHeight() - (int)dim.getHeight()) / 2;
                backG.drawImage(image.getScaledInstance((int)dim.getWidth(), (int)dim.getHeight(), Image.SCALE_SMOOTH), x, y, null);
                image.flush();
            }
            cards.add(bingoSlot);
            backG.dispose();
        }
        return cards;
    }

    private BufferedImage generateBoard(ArrayList<String> itemImages) throws IOException {
        ArrayList<BufferedImage> images = generateCards(itemImages);

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/global").toUri());

        BufferedImage background = ImageIO.read(new File(guildDir, "topslot.png"));
        Graphics backG = background.getGraphics();

        ArrayList<Integer[]> dimensions = new ArrayList<>();
        dimensions.add(new Integer[]{20,275});    // 1
        dimensions.add(new Integer[]{265,275});    // 2
        dimensions.add(new Integer[]{510,275});    // 3
        dimensions.add(new Integer[]{755,275});    // 4
        dimensions.add(new Integer[]{1000,275});    // 5

        dimensions.add(new Integer[]{20,520});    // 6
        dimensions.add(new Integer[]{265,520});    // 7
        dimensions.add(new Integer[]{510,520});    // 8
        dimensions.add(new Integer[]{755,520});    // 9
        dimensions.add(new Integer[]{1000,520});    // 10

        dimensions.add(new Integer[]{20,765});    // 11
        dimensions.add(new Integer[]{265,765});    // 12
        dimensions.add(new Integer[]{510,765});    // 13 (Special)
        dimensions.add(new Integer[]{755,765});    // 14
        dimensions.add(new Integer[]{1000,765});    // 15

        dimensions.add(new Integer[]{20,1010});    // 16
        dimensions.add(new Integer[]{265,1010});    // 17
        dimensions.add(new Integer[]{510,1010});    // 18
        dimensions.add(new Integer[]{755,1010});    // 19
        dimensions.add(new Integer[]{1000,1010});    // 20

        dimensions.add(new Integer[]{20,1255});    // 21
        dimensions.add(new Integer[]{265,1255});    // 22
        dimensions.add(new Integer[]{510,1255});    // 23
        dimensions.add(new Integer[]{755,1255});    // 24
        dimensions.add(new Integer[]{1000,1255});    // 25

        for(int i = 0; i < images.size(); i++){
            backG.drawImage(images.get(i), dimensions.get(i)[0], dimensions.get(i)[1], null);
        }

        for(BufferedImage img : images){
            img.flush();
        }
        images.clear();
        backG.dispose();
        return background;
    }

    private static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }

        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }

        return new Dimension(new_width, new_height);
    }

    public static void initializeBingoCards(){
        String[] args = new File(System.getProperty("user.dir")+"/db/cards/bingocards").list();
        if(args == null){
            return;
        }
        for (String arg : args) {
            try {
                BufferedImage bf = ImageIO.read(new File(System.getProperty("user.dir") + "/db/cards/bingocards/" + arg));
                bingoBoard.put(arg.replaceAll("[^0-9]", ""), bf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
