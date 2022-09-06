package commands.cubicCastles.craftCommands;

import commands.botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import commands.core.Cubic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import commands.normalCommands.bingo.BingoItem;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import commands.utils.CacheUtils;
import commands.utils.Msg;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static commands.normalCommands.bingo.BingoItem.emojiToNumber;
import static commands.normalCommands.bingo.BingoItem.getEmojis;
import static commands.utils.Msg.bad;

public class CraftCommand extends Command {
    public static Map<String, Item> imgCache = new HashMap<>();
    private EventWaiter waiter;

    public CraftCommand() {
        this.name = "craft";
        this.aliases = new String[]{"craftinfo", "ci", "recipe"};
        this.arguments = "<itemName> [num]";
        this.help = "Displays info and the crafting process of the provided item name (for provided number of items, from 2 to 20000 (inclusive)).";
        this.category = new Category("cubic");
        this.waiter = Cubic.getWaiter();
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

    private static List<Integer> initMultList() {
        final List<Integer> l = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            l.add(null);
        }
        return l;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            bad(event, "USAGE: " + event.getClient().getPrefix() + "craft <item_name> [num]");
            return;
        }

        String givenName = event.getArgs().trim();
        int count = -1;
        try {
            count = Integer.parseInt(event.getArgs().substring(event.getArgs().lastIndexOf(" ") + 1));
            if(count <= 1 || count > 20000) count = -1;
            givenName = givenName.substring(0, event.getArgs().lastIndexOf(" ") + 1).trim();
        } catch (NumberFormatException ignored) { }

        Message msg = event.getChannel().sendMessage("Looking up the recipe..").complete();

        try {
            if(givenName.length() < 3) {
                Msg.bad(event, "The name provided is too short. Please provide at least 3 characters long argument.");
                return;
            }
            runCommand(givenName, count, msg, event);
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "CraftCommand.java");
        }
    }

    @NotNull
    private String getString(String basePicURL, Elements td, List<BufferedImage> itemImages, List<BufferedImage> mults, List<Integer> itemMultNum, List<String> words, int cnt) throws IOException {
        String itemName;
        words.add(td.last().text().trim());

        int index = 0;
        for (Element eachtd : td) {
            if(!eachtd.select("span[id=ITEMCARD]").isEmpty()) {
                for (Element itemCard : eachtd.select("span[id=ITEMCARD]")) {
                    Element style = itemCard.select("img[style]").first();
                    if (style == null) {
                        String image;
                        try{
                            image = itemCard.getElementById("IMAGE_CONTAINER").getElementsByTag("img").first().attr("src").trim();
                        }catch (NullPointerException np){
                            image = "obscured.png";
                            cnt = -1;
                        }
                        if (cnt != -1) {
                            mults.add(index, createMultImage(cnt));
                        }
                        itemImages.add(ImageIO.read(new URL(basePicURL + image)));
                    } else {
                        String image;
                        int num = 1;
                        try{
                            image = itemCard.getElementById("IMAGE_CONTAINER").getElementsByTag("img").first().attr("src").trim();
                            num = Integer.parseInt(style.attr("src").replaceAll("[^0-9]", ""));
                        } catch (NullPointerException np){
                            image = "obscured.png";
                            cnt = -1;
                        }
                        itemImages.add(ImageIO.read(new URL(basePicURL + image)));
                        if (cnt != -1) {
                            mults.add(index, createMultImage(cnt * num));
                        } else {
                            itemMultNum.add(index, num);
                            mults.add(index, createMultImage(num));
                        }
                    }
                    index++;
                }
            }
        }
        Elements resultElements = td.select("span[id=ITEMCARD_MARGIN]");
        if(resultElements.size() > 1){
            String toolImage = resultElements.first().getElementById("IMAGE_CONTAINER").getElementsByTag("img").first().attr("src").trim();
            String resultImage = resultElements.last().getElementById("IMAGE_CONTAINER").getElementsByTag("img").first().attr("src").trim();
            itemImages.add(ImageIO.read(new URL(basePicURL + toolImage)));
            index++;
            if (cnt != -1) {
                mults.add(index, createMultImage(cnt));
            }
            itemImages.add(ImageIO.read(new URL(basePicURL + resultImage)));
        }
        else{
            String resultImage = resultElements.first().getElementById("IMAGE_CONTAINER").getElementsByTag("img").first().attr("src").trim();
            if (cnt != -1) {
                mults.add(index, createMultImage(cnt));
            }
            itemImages.add(ImageIO.read(new URL(basePicURL + resultImage)));
        }
        itemName = td.last().text().trim();
        return itemName;
    }

    private void runCommand(String givenName, int cnt, Message msg, CommandEvent event) throws IOException {
        EmbedBuilder em = new EmbedBuilder();

        String basePicURL = "https://cubiccastles.com/recipe_html/";

        String title = "Craft Info";
        String itemType;
        String itemName;
        String itemDesc;
        em.setTitle(title);
        em.setColor(Color.CYAN);

        ArrayList<String> suggestionList = new ArrayList<>();

        Document doc = Jsoup.parse(CacheUtils.getCache("craft"));

        for (String key : imgCache.keySet()) {
            if (checkStrings(key, givenName)) {
                msg.editMessage("Recipe found.. Fetching info and creating the image..").queue();
                em.addField("Item Name", imgCache.get(key).getName(), true);
                em.addField("Craft Type", imgCache.get(key).getType(), true);
                em.setDescription(imgCache.get(key).getDesc());
                ArrayList<BufferedImage> itemMults = new ArrayList<>();
                for(Integer i : imgCache.get(key).getItemMultNum()){
                    if(i == null) {
                        if(cnt != -1) itemMults.add(createMultImage(cnt));
                        else itemMults.add(null);
                    }
                    else {
                        if(cnt != -1) itemMults.add(createMultImage(cnt*i));
                        else itemMults.add(createMultImage(i));
                    }
                }
                File file = CraftImageUtil.getCompleted(imgCache.get(key).getSize(), imgCache.get(key).getItemImages(), itemMults, imgCache.get(key).getWords(), imgCache.get(key).getType(), imgCache.get(key).getName());
                Message image = Cubic.getJDA().getTextChannelById("740309750369091796").sendFile(file, URLEncoder.encode(file.getName(), "utf-8")).complete();
                file.delete();
                em.setImage(image.getAttachments().get(0).getUrl());
                msg.editMessage(event.getAuthor().getAsMention()).queue();
                msg.editMessageEmbeds(em.build()).queue();
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
            msg.editMessageEmbeds(em.build()).queue();
        }
        else if (checkStrings("sooth salve", givenName) || checkStrings("thermometer", givenName) || checkStrings("tissue", givenName) || checkStrings("spray kleen", givenName) || checkStrings("compound x", givenName) || checkStrings("compound y", givenName) || checkStrings("simple mask", givenName) || checkStrings("n95 mask", givenName)) {
            em.addField("Item Name", "Virus Update Crafting", true);
            em.addField("Craft Type", "Basic Crafting Recipes", true);
            em.setDescription("How to craft individual items from the Virus Update");
            em.setImage("https://media.discordapp.net/attachments/705622006652993610/712876818314821632/unknown.png");
            em.setFooter("Picture By -JZ- and V");
            msg.editMessage(event.getAuthor().getAsMention()).queue();
            msg.editMessageEmbeds(em.build()).queue();
        }
        else if (checkStrings("melted chocolate", givenName) || checkStrings("candy syrup", givenName) || checkStrings("gooey delight", givenName) || checkStrings("mint chocolate", givenName) || checkStrings("coco candy", givenName) || checkStrings("ultimate candy", givenName)){
            em.addField("Item Name", "Candy Crafting", true);
            em.addField("Craft Type", "Basic Crafting Recipes", true);
            em.setDescription("How to craft candies from the Candy Update");
            em.setImage("http://forums2.cubiccastles.com/uploads/editor/dj/9t4xv7nlu3sq.png");
            em.setFooter("Picture By *Joystick*");
            msg.editMessage(event.getAuthor().getAsMention()).queue();
            msg.editMessageEmbeds(em.build()).queue();
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
                        suggestionList.add(BingoItem.numberToEmoji(count) + " " + td.get(td.size()-1).text().trim());
                    }
                    if (checkStrings(td.get(td.size() - 1).text().trim(), givenName)) {
                        this.cooldown = 10;
                        this.cooldownScope = CooldownScope.USER;
                        msg.editMessage("Recipe found.. Fetching info and creating the image..").queue();
                        itemType = doc.select("h2").get(i).text().replace((char) 160, ' ').trim();

                        List<BufferedImage> itemImages = new ArrayList<>();
                        List<BufferedImage> itemMult = initList();
                        List<Integer> itemMultNum = initMultList();
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
                            itemName = getString(basePicURL, td, itemImages, itemMult, itemMultNum, words, cnt);
                        } else if (itemType.trim().equalsIgnoreCase("Crafting Recipes with Tools")) {

                            List<String> splitIngridients = Arrays.asList(td.first().text().split("\\+ "));
                            ingrSize = splitIngridients.size();
                            words.addAll(splitIngridients);
                            words.add(td.get(1).text().trim());
                            itemName = getString(basePicURL, td, itemImages, itemMult, itemMultNum, words, cnt);
                        } else if (itemType.trim().equalsIgnoreCase("Forging Items") ||
                                itemType.trim().equalsIgnoreCase("Ingredient Extraction") ||
                                itemType.trim().equalsIgnoreCase("Distillation")) {

                            String ingrUsed = td.select("span").first().text();
                            String process = td.select("center").first().text();

                            words.add(ingrUsed);
                            itemName = getString(basePicURL, td, itemImages, itemMult, itemMultNum, words, cnt);
                            words.add(process);

                            ingrSize = 1;
                        } else {
                            return;
                        }
                        itemDesc = "How to Craft " + itemName;

                        em.addField("Item Name", itemName, true);
                        em.addField("Craft Type", itemType, true);
                        em.setDescription(itemDesc);

                        File f = CraftImageUtil.getCompleted(ingrSize, itemImages, itemMult, words, itemType.trim(), itemName);
                        Message image = Cubic.getJDA().getTextChannelById("740309750369091796").sendFile(f, URLEncoder.encode(f.getName(), "utf-8")).complete();
                        f.delete();
                        em.setImage(image.getAttachments().get(0).getUrl());
                        Item item = new Item(itemName, itemType.trim(), itemDesc, ingrSize, itemImages, itemMultNum, words);
                        imgCache.put(itemName.trim(), item);
                        msg.editMessage(event.getAuthor().getAsMention()).queue();
                        msg.editMessageEmbeds(em.build()).queue();
                        return;
                    }
                }
            }
            if (suggestionList.size() == 0) {
                em.setDescription("Item Not Found.");
                msg.editMessageEmbeds(em.build()).queue();
            } else {
                this.cooldown = 10;
                this.cooldownScope = CooldownScope.USER;
                final Map<Integer, List<String>> paginatedCommands = partition(suggestionList, 5);
                buildMenu(event, cnt, paginatedCommands, 1, msg);
            }
        }
    }

    private void buildMenu(CommandEvent event, int cnt, Map<Integer, List<String>> suggestions, int pageNum, Message m){
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
                        if(m.isFromType(ChannelType.TEXT)) m.delete().queue();
                    }
                    else if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":small_red_triangle:")){
                        buildMenu(event, cnt, suggestions, pageNum+1, m);
                    }
                    else if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":small_red_triangle_down:")){
                        buildMenu(event, cnt, suggestions, pageNum-1, m);
                    }
                    else{
                        int choice = emojiToNumber(EmojiParser.parseToAliases(v.getEmoji()));
                        if(choice == 0){
                            Msg.bad(event.getTextChannel(), "Invalid choice from the suggestion list.");
                        }
                        else {
                            try {
                                runCommand(suggestions.get(pageNum-1).get(choice-1).replaceAll(":.+?:", "").trim(), cnt, m, event);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setFinalAction(me -> { });
        if(pageNum == 1 && suggestions.size() == 1){
            bm.addChoices(getEmojis(suggestions.get(pageNum-1).size()))
                    .addChoice(EmojiManager.getForAlias("x").getUnicode());
        }
        else {
            if (pageNum <= 1) {
                bm.addChoices(getEmojis(suggestions.get(pageNum - 1).size()))
                        .addChoice(EmojiManager.getForAlias("small_red_triangle").getUnicode())
                        .addChoice(EmojiManager.getForAlias("x").getUnicode());
            } else if (pageNum >= suggestions.size()) {
                bm.addChoices(getEmojis(suggestions.get(pageNum - 1).size()))
                        .addChoice(EmojiManager.getForAlias("small_red_triangle_down").getUnicode())
                        .addChoice(EmojiManager.getForAlias("x").getUnicode());
            } else {
                bm.addChoices(getEmojis(suggestions.get(pageNum - 1).size()))
                        .addChoice(EmojiManager.getForAlias("small_red_triangle_down").getUnicode())
                        .addChoice(EmojiManager.getForAlias("small_red_triangle").getUnicode())
                        .addChoice(EmojiManager.getForAlias("x").getUnicode());
            }
        }
        bm.build().display(m);
    }

    private static BufferedImage createMultImage(int count) {
        String text = NumberFormat.getNumberInstance(Locale.US).format(count) + "x";

        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.BOLD, 16);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text);
        int height = fm.getHeight();
        g2d.dispose();

        img = new BufferedImage((int) (width+(0.5*width)), (int) (height+(0.5*height)), BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        g2d.setColor(new Color(95, 95, 95));
        g2d.fillRoundRect(0, 0, img.getWidth(), img.getHeight(), 25, 25);
        Stroke oldStroke = g2d.getStroke();
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(0, 0, img.getWidth(), img.getHeight(), 25, 25);
        g2d.setStroke(oldStroke);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, (img.getWidth() - width)/2, (fm.getHeight()));
        g2d.dispose();
        return img;
    }
}
