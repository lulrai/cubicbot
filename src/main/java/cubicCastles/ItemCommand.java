package cubicCastles;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CacheUtils;
import utils.Constants;
import utils.Msg;

import java.awt.*;

public class ItemCommand extends Command {
    public ItemCommand() {
        this.name = "item";
        this.aliases = new String[]{"iteminfo", "itm"};
        this.arguments = "itemName";
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
    }

    public static String getRarity(String color) {
        String rarity = "";
        switch (color) {
            case "#9999FF": {
                rarity = "Uncommon";
                break;
            }
            case "green": {
                rarity = "Rare";
                break;
            }
            case "red": {
                rarity = "Very Rare";
                break;
            }
            case "#FFAA11": {
                rarity = "Special. Only available in quest or special event.";
                break;
            }
        }
        return rarity;
    }

    private static Boolean checkStrings(String str1, String str2) {
        if (str1.trim().equalsIgnoreCase(str2.trim())) {
            return true;
        } else return str1.trim().replaceAll("[^A-Za-z0-9 ]", "").equalsIgnoreCase(str2.trim());
	}

    private static Boolean checkSimilarStrings(String str1, String str2) {
		return str1.toLowerCase().contains(str2.toLowerCase());
	}

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            Msg.bad(event, "USAGE: " + Constants.D_PREFIX + "item <item_name>");
            return;
        }

        String givenItem = event.getMessage().getContentRaw().split(" ", 2)[1].trim();
        StringBuilder suggestions = new StringBuilder();

        EmbedBuilder em = new EmbedBuilder();
        try {
            String basePicURL = "http://cubiccastles.com/recipe_html/";

            String title = "Item Info";
            String itemType = "";
            String itemName = "";
            String itemImage = "";
            String itemRarity = "";
            em.setTitle(title);
            em.setColor(Color.CYAN);

            Document doc = Jsoup.parse(CacheUtils.getCache("item"));

            Elements itemDiv = doc.select("div#CONTAINER > div.HIDE_CONTAINER");
            boolean found = false;
            for (int i = 0; i < 4; i++) {
                Element itemList = itemDiv.get(i);
                for (Element item : itemList.select("tr > td")) {
                    if (checkSimilarStrings(item.text(), givenItem)) {
                        suggestions.append("- ").append(item.text().trim()).append("\n");
                    }
                    if (!found) {
                        if (checkStrings(item.text(), givenItem)) {
                            if (!item.select("font").attr("color").isEmpty()) {
                                itemRarity = getRarity(item.select("font").attr("color"));
                            }
                            itemName = item.text();
                            itemImage = basePicURL + item.select("img").attr("src");
                            itemType = doc.select("h2").get(i).text().trim();

                            found = true;
                            break;
                        }
                    }
                }
            }
            if (found) {
                em.addField("Name", itemName, false);
                if (!itemRarity.isEmpty()) {
                    em.addField("Rarity", itemRarity, false);
                }
                em.addField("Type", itemType, false);
                em.setThumbnail(itemImage);
            } else {
                if (suggestions.length() == 0) {
                    em.setDescription("Item Not Found.");
                } else {
                    em.setDescription("Item Not Found. Did you mean any of these?\n\n" + suggestions);
                }
            }

            event.getTextChannel().sendMessage(em.build()).queue();

        } catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "ItemCommand.java");
        }
    }

}
