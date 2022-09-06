package commands.normalCommands.bingo;

import commands.botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import commands.core.Cubic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import commands.utils.Msg;
import commands.utils.UserPermission;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class GenerateBingo extends Command {
    public static Map<String, Entry<String[][], BufferedImage>> bingoBoard = new HashMap<>();
    private EventWaiter waiter;

    public GenerateBingo() {
        this.name = "cgen";
        this.aliases = new String[]{"gencard", "generatebingo"};
        this.category = new Category("Bingo");
        this.ownerCommand = false;
        this.waiter = Cubic.getWaiter();
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getGuild().getCategoryById("756887929808224258") == null) return;

        boolean isFromBingo = event.getGuild().getCategoryById("756887929808224258").getTextChannels().parallelStream().anyMatch(c -> event.getTextChannel().getId().equals(c.getId()));
        if(!event.getGuild().getId().equals("240614697848537089") && !event.getGuild().getId().equals("705622006652993607") && !isFromBingo) return;

        if(BingoItem.smallItemPool.isEmpty()){
            Msg.bad(event, "The item pool is empty. Please generate one first.");
            return;
        }

        if (event.getMessage().getMentionedUsers().isEmpty() ||
                !(UserPermission.isBotOwner(event.getAuthor())
                        || event.getMember().getRoles().parallelStream().anyMatch(r -> (r.getId().equals("909922595367968850")))))
            return;

        User user;
        if(!event.getMessage().getMentionedUsers().isEmpty()) {
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
                else {
                    Message m = event.getTextChannel().sendMessage("Generating card... Please wait.").complete();
                    generateCard(event, m, event.getMessage().getMentionedUsers().get(0));
                }
        }
        else {
            user = event.getAuthor();
            if(bingoBoard.get(user.getId()) != null) {
                Msg.reply(event,"You already have a bingo card. You cannot make another! Ask any of the "+event.getGuild().getRoleById("756887929019957814").getAsMention()+" to create another. But here is your card.\n\n" +
                        "To check your card, use: `"+ event.getClient().getPrefix() +"card` command.");
                return;
            }
            Message m = event.getTextChannel().sendMessage("Generating card... Please wait.").complete();
            generateCard(event, m, event.getAuthor());
        }
    }

    private void generateCard(CommandEvent event, Message m, User user) {
        try{
            ArrayList<String> itemNames = new ArrayList<>();
            Random rand = new Random();
            while(itemNames.size() < 24){
                String itemName = BingoItem.smallItemPool.get(rand.nextInt(BingoItem.smallItemPool.size()));
                if(!itemNames.contains(itemName)){
                    itemNames.add(itemName);
                }
            }

            itemNames.add(12, "Free");

            Entry<String[][], BufferedImage> board = generateBoard(itemNames);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(board.getValue(), "png", os);                          // Passing: ​(RenderedImage im, String formatName, OutputStream output)
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
                em.setFooter("Request assistance with `snowy` or `WhimsicalFirefly`, if you have any issues.");
                c.sendMessageEmbeds(em.build()).queue(a -> Msg.replyTimed(event, "Successfully DM'ed the card.", 5, TimeUnit.SECONDS), f -> Msg.reply(event, "Could not send a DM. Please manually save the following card or retrieve it later using `\"+event.getClient().getPrefix()+\"card` command."));
            }, n -> Msg.replyTimed(event, "Could not send a DM. Please manually save the following card or retrieve it later using `\"+event.getClient().getPrefix()+\"card` command.", 5, TimeUnit.SECONDS));

            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "GenerateBingo.java");
        }
    }

    private void addCard(String id, Entry<String[][], BufferedImage> board) throws IOException {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File cardDir = new File(workingDir.resolve("db/cards/bingocards/").toUri());
        File markedCardDir = new File(workingDir.resolve("db/cards/markedbingocards/").toUri());
        if (!cardDir.exists()) cardDir.mkdirs();
        if (!markedCardDir.exists()) markedCardDir.mkdirs();

        File outputImgOrig = new File(cardDir, URLEncoder.encode(id + ".png", "utf-8"));
        File outputImgMark = new File(markedCardDir, URLEncoder.encode(id + ".png", "utf-8"));

        ImageIO.write(board.getValue(), "png", outputImgOrig);
        ImageIO.write(board.getValue(), "png", outputImgMark);

        File outputFileOrig = new File(cardDir, URLEncoder.encode(id + ".txt", "utf-8"));
        try {
            outputFileOrig.createNewFile();
        } catch (IOException e) {
            System.out.println("Couldn't create " + outputFileOrig.getName() + " file at " + outputFileOrig.getAbsolutePath());
        }

        FileWriter fw = new FileWriter(outputFileOrig, false);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);
        for (int i = 0; i < board.getKey().length; i++) {
            for(int j = 0; j < board.getKey()[0].length; j++) {
                out.println(board.getKey()[i][j]);
            }
        }
        out.close();
        bw.close();
        fw.close();
    }


    private Entry<String[][], ArrayList<BufferedImage>> generateCards(ArrayList<String> itemNames) throws IOException {
        ArrayList<BufferedImage> cards = new ArrayList<>();
        String[][] names = new String[5][5];

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/global").toUri());

        int row = 0;
        int col = 0;
        for(int i = 0; i < itemNames.size(); i++) {
            BufferedImage bingoSlot = ImageIO.read(new File(guildDir, "bingoslot.png"));
            Graphics backG = bingoSlot.getGraphics();
            if(i == 12) {
                BufferedImage free = ImageIO.read(new File(guildDir, "free.png"));
                names[2][2] = "Free";
                col++;
                Dimension dim = getScaledDimension(new Dimension(free.getWidth(), free.getHeight()), new Dimension(bingoSlot.getWidth(), bingoSlot.getHeight()));
                backG.drawImage(free.getScaledInstance((int)dim.getWidth(), (int)dim.getHeight(), Image.SCALE_SMOOTH), 2, 2, null);
                free.flush();
            }
            else {
                names[row][col] = itemNames.get(i);
                if(col == 4){
                    row++;
                    col = -1;
                }
                col++;
//                BufferedImage image = ImageIO.read(new File(guildDir, "test.png"));
                File itemDir = new File(workingDir.resolve("db/cards/items").toUri());
                BufferedImage image = ImageIO.read(new File(itemDir, itemNames.get(i)+".png"));
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
        return new AbstractMap.SimpleEntry<>(names, cards);
    }

    private Entry<String[][], BufferedImage> generateBoard(ArrayList<String> itemNames) throws IOException {
        Entry<String[][], ArrayList<BufferedImage>> images = generateCards(itemNames);

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

        for(int i = 0; i < images.getValue().size(); i++){
            backG.drawImage(images.getValue().get(i), dimensions.get(i)[0], dimensions.get(i)[1], null);
        }

        for(BufferedImage img : images.getValue()){
            img.flush();
        }
        images.getValue().clear();
        backG.dispose();
        return new AbstractMap.SimpleEntry<>(images.getKey(), background);
    }

    public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

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
        String[] bingoOrig = new File(System.getProperty("user.dir")+"/db/cards/bingocards").list();
        String[] bingoMark = new File(System.getProperty("user.dir")+"/db/cards/markedbingocards").list();
        if(bingoMark == null || bingoOrig == null){
            return;
        }
        for (String arg : bingoMark) {
            BufferedImage card = null;
            String[][] items = new String[5][5];
            if(arg.endsWith(".png")) {
                try {
                    card = ImageIO.read(new File(System.getProperty("user.dir") + "/db/cards/markedbingocards/" + arg));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File outFile = new File(System.getProperty("user.dir")+"/db/cards/bingocards/", arg.replaceAll("[^0-9]", "").trim() + ".txt");
                if(!outFile.exists()) continue;

                try {
                    BufferedReader br = new BufferedReader(new FileReader(outFile));
                    String line;
                    int i = 0;
                    int j = 0;
                    while ((line = br.readLine()) != null) {
                        if(j % 4 == 0 && j != 0) {
                            items[i][j] = line;
                            i++;
                            j = 0;
                        }
                        else {
                            if (i == 2 && j == 2) {
                                items[i][j] = "Free";
                            } else {
                                items[i][j] = line;
                            }
                            j++;
                        }
                    }
                    br.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if(card != null && items[0][0] != null) {
                Entry<String[][], BufferedImage> e = new AbstractMap.SimpleEntry<>(items, card);
                bingoBoard.put(arg.replaceAll("[^0-9]", ""), e);
            }
        }
    }
}
