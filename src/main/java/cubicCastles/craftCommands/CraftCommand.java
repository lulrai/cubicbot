package cubicCastles.craftCommands;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import core.Cubic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.AttachmentOption;
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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
        this.cooldown = 5;
        this.ownerCommand = false;
        this.waiter = waiter;
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
            runCommand(givenName, msg, channel);
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "CraftCommand.java");
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

    private void runCommand(String givenName, Message msg, TextChannel channel) throws IOException {
        EmbedBuilder em = new EmbedBuilder();

        String basePicURL = "http://cubiccastles.com/recipe_html/";

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/global").toUri());

        String title = "Craft Info";
        String itemType;
        String itemName;
        List<File> itemImage = new ArrayList<>();
        String itemDesc;
        em.setTitle(title);
        em.setColor(Color.CYAN);

        StringBuilder suggestions = new StringBuilder();

        Document doc = Jsoup.parse(CacheUtils.getCache("craft"));

        for (String key : imgCache.keySet()) {
            if (checkStrings(key, givenName)) {
                msg.editMessage("Recipe found.. Fetching info and creating the image..").queue();
                em.addField("Item Name", imgCache.get(key).getName(), true);
                em.addField("Craft Type", imgCache.get(key).getType(), true);
                em.setDescription(imgCache.get(key).getDesc());
                File file = imgCache.get(key).getImage();
                em.setImage("attachment://db/cache/" + URLEncoder.encode(file.getName(), "utf-8"));
                msg.delete();
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
        } else {
            Elements itemDiv = doc.select("div#CONTAINER > div.HIDE_CONTAINER");
            int count = 0;
            for (int i = 4; i < itemDiv.size() - 1; i++) {
                Elements itemList = itemDiv.get(i).select("tr");
                for (Element element : itemList) {
                    Elements td = element.select("td");
                    if (checkSimilarStrings(td.get(td.size() - 1).text().trim(), givenName) && count <= 6) {
                        count++;
                        suggestions.append(BingoItem.numberToEmoji(count)).append(" ").append(td.get(td.size() - 1).text().trim()).append("\n");
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

                            List<String> splitIngridients = Arrays.asList(td.first().text().split("\\+ "));
                            ingrSize = splitIngridients.size();
                            words.addAll(splitIngridients);
                            words.add(td.last().text().trim());

                        } else if (itemType.trim().equalsIgnoreCase("Crafting Recipes with Tools")) {

                            List<String> splitIngridients = Arrays.asList(td.first().text().split("\\+ "));
                            ingrSize = splitIngridients.size();
                            words.addAll(splitIngridients);
                            words.add(td.get(1).text().trim());

                        } else if (itemType.trim().equalsIgnoreCase("Forging Items") ||
                                itemType.trim().equalsIgnoreCase("Ingredient Extraction") ||
                                itemType.trim().equalsIgnoreCase("Distillation")) {

                            String ingrUsed = td.select("span").first().text();
                            String process = td.select("center").first().text();

                            words.add(ingrUsed);
                            words.add(process);
                            ingrSize = 1;
                        } else {
                            return;
                        }
                        itemName = getString(basePicURL, guildDir, td, itemImages, itemMult, words);
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
                        msg.editMessage(em.build()).queue();

                        return;
                    }
                }
            }
            if (suggestions.length() == 0) {
                em.setDescription("Item Not Found.");
                msg.editMessage(em.build()).queue();
            } else {
                if (suggestions.length() >= MessageEmbed.TEXT_MAX_LENGTH) {
                    em.setDescription("Too long of a suggestion list. Please type more characters so that the bot can suggest items.");
                    msg.editMessage(em.build()).queue();
                } else {
                    new ButtonMenu.Builder()
                            .setDescription("Could not find an item with that name. Here are some possible suggestions.\n" +
                                    suggestions.toString().trim())
                            .setChoices(getEmojis(count))
                            .addChoice(EmojiManager.getForAlias("x").getUnicode())
                            .setEventWaiter(waiter)
                            .setTimeout(20, TimeUnit.SECONDS)
                            .setColor(Color.orange)
                            .setAction(v -> {
                                if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":x:")) {
                                    Msg.replyTimed(channel, "Cancelled choosing a crafting recipe.", 5, TimeUnit.SECONDS);
                                }
                                else{
                                    int choice = emojiToNumber(EmojiParser.parseToAliases(v.getEmoji()));
                                    if(choice == 0){
                                        Msg.bad(channel, "Invalid choice from the suggestion list.");
                                    }
                                    else {
                                        String[] split = suggestions.toString().replaceAll(":.+?:", "").split("\n");
                                        try {
                                            runCommand(split[choice-1], msg, channel);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            })
                            .setFinalAction(me -> {
                                me.delete().queue();
                            }).build().display(channel);
                }
            }
        }
    }
}
