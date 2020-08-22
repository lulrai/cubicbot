package cubicCastles;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import normalCommands.bingo.BingoItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CacheUtils;
import utils.Constants;
import utils.Msg;

import java.awt.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import static normalCommands.bingo.BingoItem.emojiToNumber;
import static normalCommands.bingo.BingoItem.getEmojis;

public class ItemCommand extends Command {
    private EventWaiter waiter;
    public ItemCommand(EventWaiter waiter) {
        this.name = "item";
        this.aliases = new String[]{"iteminfo", "itm"};
        this.arguments = "itemName";
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
        this.waiter = waiter;
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
        } else return str1.trim().replaceAll("’", "'").replaceAll("[^A-Za-z0-9' ]", "").equalsIgnoreCase(str2.replaceAll("’", "'").replaceAll("[^A-Za-z0-9' ]", "").trim());
	}

    private static Boolean checkSimilarStrings(String str1, String str2) {
		return str1.replaceAll("’", "'").toLowerCase().contains(str2.replaceAll("’", "'").toLowerCase());
	}

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            Msg.bad(event, "USAGE: " + Constants.D_PREFIX + "item <item_name>");
            return;
        }

        String givenItem = event.getMessage().getContentRaw().split(" ", 2)[1].trim();
        runCommand(givenItem, event);
    }

    private void runCommand(String givenItem, CommandEvent event){
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
            int count = 0;
            for (int i = 0; i < 4; i++) {
                Element itemList = itemDiv.get(i);
                for (Element item : itemList.select("tr > td")) {
                    if (checkSimilarStrings(item.text(), givenItem) && count <= 8) {
                        count++;
                        suggestions.append(BingoItem.numberToEmoji(count)).append(" ").append(item.text().trim()).append("\n");
                    }
                    if (checkStrings(item.text(), givenItem)) {
                        if (!item.select("font").attr("color").isEmpty()) {
                            itemRarity = getRarity(item.select("font").attr("color"));
                        }
                        itemName = item.text();
                        itemImage = basePicURL + item.select("img").attr("src");
                        itemType = doc.select("h2").get(i).text().trim();
                        em.addField("Name", itemName, false);
                        if (!itemRarity.isEmpty()) {
                            em.addField("Rarity", itemRarity, false);
                        }
                        em.addField("Type", itemType, false);
                        em.setThumbnail(itemImage);
                        event.getTextChannel().sendMessage(em.build()).queue();
                        return;
                    }
                }
            }
            if (suggestions.length() == 0) {
                em.setDescription("Item Not Found.");
                event.getTextChannel().sendMessage(em.build()).queue();
            } else {
                if (suggestions.length() >= MessageEmbed.TEXT_MAX_LENGTH) {
                    em.setDescription("Too long of a suggestion list. Please type more characters so that the bot can suggest items.");
                    event.getTextChannel().sendMessage(em.build()).queue();
                } else {
                    new ButtonMenu.Builder()
                            .setDescription("Could not find an item with that name. Here are some possible suggestions.\n" +
                                    suggestions.toString().trim())
                            .setChoices(getEmojis(count))
                            .addUsers()
                            .addChoice(EmojiManager.getForAlias("x").getUnicode())
                            .setEventWaiter(waiter)
                            .setUsers(event.getAuthor())
                            .setTimeout(20, TimeUnit.SECONDS)
                            .setColor(Color.orange)
                            .setAction(v -> {
                                if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":x:")) {
                                    Msg.reply(event.getTextChannel(), "Cancelled choosing an item.");
                                }
                                else{
                                    int choice = emojiToNumber(EmojiParser.parseToAliases(v.getEmoji()));
                                    if(choice == 0){
                                        Msg.bad(event.getTextChannel(), "Invalid choice from the suggestion list.");
                                    }
                                    else {
                                        String[] split = suggestions.toString().replaceAll(":.+?:", "").split("\n");
                                        runCommand(split[choice-1].trim(), event);
                                    }
                                }
                            })
                            .setFinalAction(me -> {
                                me.delete().queue();
                            }).build().display(event.getTextChannel());
                }
            }
        } catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "ItemCommand.java");
        }
    }

}
