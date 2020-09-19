package cubicCastles.craftCommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import core.Cubic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import normalCommands.bingo.BingoItem;
import org.jetbrains.annotations.NotNull;
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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static normalCommands.bingo.BingoItem.emojiToNumber;
import static normalCommands.bingo.BingoItem.getEmojis;
import static utils.Msg.bad;

public class CraftCommand extends Command {
    public static Map<String, Item> imgCache = new HashMap<>();
    private EventWaiter waiter;

    public CraftCommand(EventWaiter waiter) {
        this.name = "craft";
        this.aliases = new String[]{"craftinfo", "ci", "recipe"};
        this.arguments = "itemName";
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
        this.waiter = waiter;
    }

    public static Map<Integer, List<String>> partition(final List<String> list, final int pageSize) {
        return IntStream.iterate(0, i -> i + pageSize).limit((list.size() + pageSize - 1) / pageSize).boxed().collect(Collectors.toMap(i -> i / pageSize, i -> list.subList(i, Integer.min(i + pageSize, list.size()))));
    }

    private static Boolean checkStrings(String str1, String str2) {
        if (str1.equalsIgnoreCase(str2)) {
            return true;
        } else return str1.replaceAll("[^A-Za-z0-9 ]", "").equalsIgnoreCase(str2);
    }

    private static Boolean checkSimilarStrings(String str1, String str2) {
        return str1.toLowerCase().contains(str2.toLowerCase());
    }

    private static List<BufferedImage> initList() {
        final List<BufferedImage> l = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            l.add(null);
        }
        return l;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            bad(event, "USAGE: " + Constants.D_PREFIX + "craft <item_name>");
            return;
        }

        Message msg = event.getChannel().sendMessage("Looking up the recipe..").complete();
        TextChannel channel = event.getTextChannel(); // = reference of a MessageChannel
        String givenName = event.getMessage().getContentRaw().split(" ", 2)[1].trim();

        try {
            if(givenName.length() < 3){
                Msg.bad(event, "The name provided is too short. Please provide at least 3 characters long argument.");
                return;
            }
            runCommand(givenName, msg, channel, event);
        } catch (Exception e) {
            e.printStackTrace();
            //ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "CraftCommand.java");
        }
    }

    @NotNull
    private String getString(String basePicURL, File guildDir, Elements td, List<BufferedImage> itemImages, List<BufferedImage> itemMult, List<String> words) throws IOException {
        String itemName;
        words.add(td.last().text().trim());

        int index = 0;
        for (Element eachtd : td) {
            for (Element img : eachtd.select("img")) {
                String style = img.attr("style");
                String image = img.attr("src");
                List<String> dni = Arrays.asList("2x.png", "3x.png", "4x.png", "5x.png", "6x.png", "obscured.png");
                if (!dni.contains(image)) {
                    itemImages.add(ImageIO.read(new URL(basePicURL + image)));
                    index++;
                }
                if (!style.isEmpty()) {
                    itemMult.add(index, ImageIO.read(new File(guildDir, image)));
                }
            }
        }
        itemName = td.last().text().trim();
        return itemName;
    }

    private void runCommand(String givenName, Message msg, TextChannel channel, CommandEvent event) throws IOException {
        EmbedBuilder em = new EmbedBuilder();

        String basePicURL = "https://cubiccastles.com/recipe_html/";

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/global").toUri());

        String title = "Craft Info";
        String itemType;
        String itemName;
        List<File> itemImage = new ArrayList<>();
        String itemDesc;
        em.setTitle(title);
        em.setColor(Color.CYAN);

//        StringBuilder suggestions = new StringBuilder();
        ArrayList<String> suggestionList = new ArrayList<>();

        Document doc = Jsoup.parse(CacheUtils.getCache("craft"));

        for (String key : imgCache.keySet()) {
            if (checkStrings(key, givenName)) {
                msg.editMessage("Recipe found.. Fetching info and creating the image..").queue();
                em.addField("Item Name", imgCache.get(key).getName(), true);
                em.addField("Craft Type", imgCache.get(key).getType(), true);
                em.setDescription(imgCache.get(key).getDesc());
                File file = imgCache.get(key).getImage();
                em.setImage("attachment://db/cache/" + URLEncoder.encode(file.getName(), "utf-8"));
                channel.sendFile(file, URLEncoder.encode(file.getName(), "utf-8")).embed(em.build()).queue();
                return;
            }
        }
        if (checkStrings("easter egg", givenName)) {
            em.addField("Item Name", "Easter Egg", true);
            em.addField("Craft Type", "Basic Crafting Recipes", true);
            em.setDescription("How to Craft Easter Egg\n"
                    + "Link for full post: [Click here](http://forums2.cubiccastles.com/index.php?p=/discussion/19605/full-easter-pow-gift-egg-crafting-guide/p1)");
            em.setImage("http://forums2.cubiccastles.com/uploads/editor/uk/lp45t6zaccd4.png");
            em.setFooter("Picture By Gdog", "http://forums2.cubiccastles.com/uploads/userpics/400/nDTXMQJ83XKVS.jpg");
            msg.editMessage(event.getAuthor().getAsMention()).queue();
            msg.editMessage(em.build()).queue();
        }
        else if (checkStrings("sooth salve", givenName) || checkStrings("thermometer", givenName) || checkStrings("tissue", givenName) || checkStrings("spray kleen", givenName) || checkStrings("compound x", givenName) || checkStrings("compound y", givenName) || checkStrings("simple mask", givenName) || checkStrings("n95 mask", givenName)) {
            em.addField("Item Name", "Virus Update Crafting", true);
            em.addField("Craft Type", "Basic Crafting Recipes", true);
            em.setDescription("How to craft individual items from the Virus Update");
            em.setImage("https://media.discordapp.net/attachments/705622006652993610/712876818314821632/unknown.png");
            em.setFooter("Picture By -JZ- and V");
            msg.editMessage(event.getAuthor().getAsMention()).queue();
            msg.editMessage(em.build()).queue();
        }
        else if (checkStrings("melted chocolate", givenName) || checkStrings("candy syrup", givenName) || checkStrings("gooey delight", givenName) || checkStrings("mint chocolate", givenName) || checkStrings("coco candy", givenName) || checkStrings("ultimate candy", givenName)){
            em.addField("Item Name", "Candy Crafting", true);
            em.addField("Craft Type", "Basic Crafting Recipes", true);
            em.setDescription("How to craft candies from the Candy Update");
            em.setImage("http://forums2.cubiccastles.com/uploads/editor/dj/9t4xv7nlu3sq.png");
            em.setFooter("Picture By *Joystick*");
            msg.editMessage(event.getAuthor().getAsMention()).queue();
            msg.editMessage(em.build()).queue();
        }
        else {
            Elements itemDiv = doc.select("div#CONTAINER > div.HIDE_CONTAINER");
            int count = 0;
            for (int i = 4; i < itemDiv.size() - 1; i++) {
                Elements itemList = itemDiv.get(i).select("tr");
                for (Element element : itemList) {
                    Elements td = element.select("td");
                    if (checkSimilarStrings(td.get(td.size() - 1).text().trim(), givenName)) {
                        if(count == 5){
                            count = 0;
                        }
                        count++;
                        //suggestions.append(BingoItem.numberToEmoji(count)).append(" ").append(td.get(td.size() - 1).text().trim()).append("\n");
                        suggestionList.add(BingoItem.numberToEmoji(count) + " " + td.get(td.size()-1).text().trim());
                    }
                    if (checkStrings(td.get(td.size() - 1).text().trim(), givenName)) {
                        msg.editMessage("Recipe found.. Fetching info and creating the image..").queue();
                        itemType = doc.select("h2").get(i).text().replace((char) 160, ' ').trim();

                        List<BufferedImage> itemImages = new ArrayList<>();
                        List<BufferedImage> itemMult = initList();
                        int ingrSize;
                        List<String> words = new ArrayList<>();

                        if (itemType.trim().equalsIgnoreCase("Basic Crafting Recipes") ||
                                itemType.trim().equalsIgnoreCase("Coloring Items with Dye") ||
                                itemType.trim().equalsIgnoreCase("Cooking") ||
                                itemType.trim().equalsIgnoreCase("Cut-O-Matik")) {

                            List<String> splitIngridients = Arrays.asList(td.first().text().trim().split("\\+", -1));
                            ingrSize = splitIngridients.size();
                            words.addAll(splitIngridients);
                            words.add(td.last().text().trim());
                            itemName = getString(basePicURL, guildDir, td, itemImages, itemMult, words);
                        } else if (itemType.trim().equalsIgnoreCase("Crafting Recipes with Tools")) {

                            List<String> splitIngridients = Arrays.asList(td.first().text().split("\\+ "));
                            ingrSize = splitIngridients.size();
                            words.addAll(splitIngridients);
                            words.add(td.get(1).text().trim());
                            itemName = getString(basePicURL, guildDir, td, itemImages, itemMult, words);
                        } else if (itemType.trim().equalsIgnoreCase("Forging Items") ||
                                itemType.trim().equalsIgnoreCase("Ingredient Extraction") ||
                                itemType.trim().equalsIgnoreCase("Distillation")) {

                            String ingrUsed = td.select("span").first().text();
                            String process = td.select("center").first().text();

                            words.add(ingrUsed);
                            itemName = getString(basePicURL, guildDir, td, itemImages, itemMult, words);
                            words.add(process);

                            ingrSize = 1;
                        } else {
                            return;
                        }
                        itemImage.add(CraftImageUtil.getCompleted(ingrSize, itemImages, itemMult, words, itemType.trim(), itemName));
                        itemDesc = "How to Craft " + itemName;

                        em.addField("Item Name", itemName, true);
                        em.addField("Craft Type", itemType, true);
                        em.setDescription(itemDesc);

                        File f = itemImage.get(0);
                        Message image = Cubic.getJDA().getTextChannelById("740309750369091796").sendFile(f, URLEncoder.encode(f.getName(), "utf-8")).complete();

                        em.setImage(image.getAttachments().get(0).getUrl());
                        Item item = new Item(itemName, itemType, itemDesc, f);
                        imgCache.put(itemName.trim(), item);
                        msg.editMessage(event.getAuthor().getAsMention()).queue();
                        msg.editMessage(em.build()).queue();
                        return;
                    }
                }
            }
            if (suggestionList.size() == 0) {
                em.setDescription("Item Not Found.");
                msg.editMessage(em.build()).queue();
            } else {
                final Map<Integer, List<String>> paginatedCommands = partition(suggestionList, 5);
                buildMenu(event, paginatedCommands, 1, msg);
            }
        }
    }

    private void buildMenu(CommandEvent event, Map<Integer, List<String>> suggestions, int pageNum, Message m){
        StringBuilder sb = new StringBuilder();
        for(String s : suggestions.get(pageNum-1)) sb.append(s).append("\n");
        ButtonMenu.Builder bm = new ButtonMenu.Builder()
                .setDescription("Could not find an item with that name. Here are some possible suggestions.\n" +
                        sb.toString().trim() + "\n\n" + "Page: " + pageNum + "/" + suggestions.size())
                .setEventWaiter(waiter)
                .setUsers(event.getAuthor())
                .setTimeout(20, TimeUnit.SECONDS)
                .setColor(Color.orange)
                .setAction(v -> {
                    if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":x:")) {
                        Msg.reply(event.getTextChannel(), "Cancelled choosing a crafting recipe.");
                        m.delete().queue();
                    }
                    else if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":small_red_triangle:")){
                        buildMenu(event, suggestions, pageNum+1, m);
                    }
                    else if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":small_red_triangle_down:")){
                        buildMenu(event, suggestions, pageNum-1, m);
                    }
                    else{
                        int choice = emojiToNumber(EmojiParser.parseToAliases(v.getEmoji()));
                        if(choice == 0){
                            Msg.bad(event.getTextChannel(), "Invalid choice from the suggestion list.");
                        }
                        else {
                            try {
                                runCommand(suggestions.get(pageNum-1).get(choice-1).replaceAll(":.+?:", "").trim(), m, event.getTextChannel(), event);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setFinalAction(me -> {
                    try {
                        me.clearReactions().queue();
                    }catch (Exception ignored) { }
                });
        if(pageNum == 1 && suggestions.size() == 1){
            bm.addChoices(getEmojis(suggestions.get(pageNum-1).size()))
                    .addChoice(EmojiManager.getForAlias("x").getUnicode());
        }
        else {
            this.cooldown = 10;
            this.cooldownScope = CooldownScope.USER;
            if (pageNum <= 1) {
                bm.addChoices(getEmojis(suggestions.get(pageNum - 1).size()))
                        .addChoice(EmojiManager.getForAlias("small_red_triangle").getUnicode())
                        .addChoice(EmojiManager.getForAlias("x").getUnicode());
            } else if (pageNum >= suggestions.size()) {
                bm.addChoice(EmojiManager.getForAlias("small_red_triangle_down").getUnicode())
                        .addChoices(getEmojis(suggestions.get(pageNum - 1).size()))
                        .addChoice(EmojiManager.getForAlias("x").getUnicode());
            } else {
                bm.addChoice(EmojiManager.getForAlias("small_red_triangle_down").getUnicode())
                        .addChoices(getEmojis(suggestions.get(pageNum - 1).size()))
                        .addChoice(EmojiManager.getForAlias("small_red_triangle").getUnicode())
                        .addChoice(EmojiManager.getForAlias("x").getUnicode());
            }
        }
        bm.build().display(m);
    }
}
