package cubicCastles.craftCommands;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CacheUtils;
import utils.Constants;

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

import static utils.Msg.bad;

public class CraftCommand extends Command {
    public static Map<String, Item> imgCache = new HashMap<>();

    public CraftCommand() {
        this.name = "craft";
        this.aliases = new String[]{"craftinfo", "ci", "recipe"};
        this.arguments = "itemName";
        this.category = new Category("Cubic Castles");
        this.cooldown = 5;
        this.ownerCommand = false;
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

        String givenName = event.getMessage().getContentRaw().split(" ", 2)[1].trim();
        EmbedBuilder em = new EmbedBuilder();
        MessageChannel channel = event.getTextChannel(); // = reference of a MessageChannel
        MessageBuilder message = new MessageBuilder();

        try {
            String basePicURL = "http://cubiccastles.com/recipe_html/";

            Path workingDir = Paths.get(System.getProperty("user.dir"));
            File guildDir = new File(workingDir.resolve("db/global").toUri());

            String title = "Craft Info";
            String itemType = "";
            String itemName = "";
            List<File> itemImage = new ArrayList<>();
            String itemDesc = "";
            StringBuilder otherWays = new StringBuilder();
            em.setTitle(title);
            em.setColor(Color.CYAN);

            StringBuilder suggestions = new StringBuilder();

            Document doc = Jsoup.parse(CacheUtils.getCache("craft"));


            boolean found = false;
            for (String key : imgCache.keySet()) {
                if (checkStrings(key, givenName)) {
                    msg.editMessage("Recipe found.. Fetching info and creating the image..").queue();
                    em.addField("Item Name", imgCache.get(key).getName(), true);
                    em.addField("Craft Type", imgCache.get(key).getType(), true);
                    em.setDescription(imgCache.get(key).getDesc());
                    File file = imgCache.get(key).getImage();
                    em.setImage("attachment://db/cache/"+URLEncoder.encode(file.getName(),"utf-8"));
                    msg.delete();
                    channel.sendFile(file, URLEncoder.encode(file.getName(),"utf-8")).embed(em.build()).queue();
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
                for (int i = 4; i < itemDiv.size() - 1; i++) {
                    Elements itemList = itemDiv.get(i).select("tr");
                    for (Element element : itemList) {
                        Elements td = element.select("td");
                        if (checkSimilarStrings(td.get(td.size() - 1).text().trim(), givenName)) {
                            suggestions.append("- ").append(td.get(td.size() - 1).text().trim()).append("\n");
                        }
                        if (!found)
                            if (checkStrings(td.get(td.size() - 1).text().trim(), givenName)) {
                                msg.editMessage("Recipe found.. Fetching info and creating the image..").queue();
                                itemType = doc.select("h2").get(i).text().replace((char) 160, ' ').trim();

                                if (itemType.trim().equalsIgnoreCase("Basic Crafting Recipes") ||
                                        itemType.trim().equalsIgnoreCase("Coloring Items with Dye") ||
                                        itemType.trim().equalsIgnoreCase("Cooking") ||
                                        itemType.trim().equalsIgnoreCase("Cut-O-Matik")) {

                                    List<String> splitIngridients = Arrays.asList(td.first().text().split("\\+ "));
                                    List<BufferedImage> itemImages = new ArrayList<>();
                                    List<BufferedImage> itemMult = initList();

                                    List<String> words = new ArrayList<>(splitIngridients);
                                    words.add(td.last().text().trim());
                                    itemName = getString(basePicURL, guildDir, td, itemImages, itemMult, words);
                                    itemImage.add(CraftImageUtil.getCompleted(splitIngridients.size(), itemImages, itemMult, words, itemType.trim(), itemName));
                                    itemDesc = "How to Craft " + itemName;
                                } else if (itemType.trim().equalsIgnoreCase("Crafting Recipes with Tools")) {

                                    List<String> splitIngridients = Arrays.asList(td.first().text().split("\\+ "));
                                    List<BufferedImage> itemImages = new ArrayList<>();
                                    List<BufferedImage> itemMult = initList();

                                    List<String> words = new ArrayList<>(splitIngridients);
                                    words.add(td.get(1).text().trim());
                                    itemName = getString(basePicURL, guildDir, td, itemImages, itemMult, words);
                                    itemImage.add(CraftImageUtil.getCompleted(splitIngridients.size(), itemImages, itemMult, words, itemType, itemName));
                                    itemDesc = "How to Craft " + itemName;
                                } else if (itemType.trim().equalsIgnoreCase("Forging Items") ||
                                        itemType.trim().equalsIgnoreCase("Ingredient Extraction") ||
                                        itemType.trim().equalsIgnoreCase("Distillation")) {

                                    String ingrUsed = td.select("span").first().text();
                                    String process = td.select("center").first().text();
                                    List<BufferedImage> itemImages = new ArrayList<BufferedImage>();
                                    List<BufferedImage> itemMult = initList();
                                    List<String> words = new ArrayList<String>();

                                    words.add(ingrUsed);
                                    words.add(process);
                                    itemName = getString(basePicURL, guildDir, td, itemImages, itemMult, words);
                                    itemImage.add(CraftImageUtil.getCompleted(1, itemImages, itemMult, words, itemType, itemName));
                                    itemDesc = "How to Craft " + itemName;
                                }

                                found = true;
                                break;
                            }
                    }
                }
                if (!found) {
                    if (suggestions.length() == 0) {
                        em.setDescription("Item Not Found.");
                    } else {
                        em.setDescription("Item Not Found. Did you mean any of these?\n\n" + suggestions);
                    }
                } else {
                    em.addField("Item Name", itemName, true);
                    em.addField("Craft Type", itemType, true);
                    em.setDescription(itemDesc);
                    File f = itemImage.get(0);
                    em.setImage("attachment://db/cache/"+URLEncoder.encode(f.getName(),"utf-8"));
                    msg.delete();
                    if (itemImage.size() > 1) {
                        for (int i = 1; i < itemImage.size(); i++) {
                            otherWays.append(itemImage.get(i)).append("\n");
                        }
                        em.addField("Other Ways", otherWays.toString(), true);
                    }
                    Item item = new Item(itemName, itemType, itemDesc, f);
                    imgCache.put(itemName.trim(), item);
                    channel.sendFile(f, URLEncoder.encode(f.getName(),"utf-8")).embed(em.build()).queue();
                    return;
                }
            }
            msg.editMessage(em.build()).queue();
        } catch (Exception e) {
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

}
