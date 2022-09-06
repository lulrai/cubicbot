package commands.cubicCastles;

import commands.botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import commands.core.Cubic;
import commands.cubicCastles.craftCommands.CraftCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import commands.normalCommands.bingo.BingoItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import commands.utils.CacheUtils;
import commands.utils.Msg;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static commands.normalCommands.bingo.BingoItem.emojiToNumber;
import static commands.normalCommands.bingo.BingoItem.getEmojis;

public class ItemCommand extends Command {
    private EventWaiter waiter;
    public ItemCommand() {
        this.name = "item";
        this.aliases = new String[]{"iteminfo", "itm"};
        this.arguments = "<itemName>";
        this.help = "Displays info about the provided item name.";
        this.category = new Category("cubic");
        this.waiter = Cubic.getWaiter();
        this.guildOnly = false;
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
            Msg.bad(event.getChannel(), "USAGE: " + event.getClient().getPrefix() + "item <item_name>");
            return;
        }

        String givenItem = event.getMessage().getContentRaw().split(" ", 2)[1].trim();
        Message m = event.getChannel().sendMessage("Finding item..").complete();
        runCommand(givenItem, event, m);
    }

    public String runCommand(String givenItem, CommandEvent event, Message m){
        ArrayList<String> suggestionList = new ArrayList<>();

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
                        if(count == 5){
                            count = 0;
                        }
                        count++;
                        suggestionList.add(BingoItem.numberToEmoji(count) + " " + item.text().trim());
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
                        m.editMessage(event.getAuthor().getAsMention()).queue();
                        m.editMessageEmbeds(em.build()).queue();
                        return itemImage;
                    }
                }
            }
            if (suggestionList.size() == 0) {
                em.setDescription("Item Not Found.");
                m.editMessageEmbeds(em.build()).queue();
            } else {
                final Map<Integer, List<String>> paginatedCommands = CraftCommand.partition(suggestionList, 5);
                buildMenu(event, paginatedCommands, 1, m);
            }
        } catch (InsufficientPermissionException ex) {
            event.getChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "ItemCommand.java");
        }
        return "";
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
                        Msg.reply(event.getChannel(), "Cancelled choosing an item.");
                        if(m.isFromType(ChannelType.TEXT)) m.delete().queue();
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
                            Msg.bad(event.getChannel(), "Invalid choice from the suggestion list.");
                            if(m.isFromType(ChannelType.TEXT)) m.delete().queue();
                        }
                        else {
                            runCommand(suggestions.get(pageNum-1).get(choice-1).replaceAll(":.+?:", "").trim(), event, m);
                        }
                    }
                })
                .setFinalAction(me -> {
                    try {
                        if(me.isFromType(ChannelType.TEXT)) me.clearReactions().queue();
                    } catch (Exception ignored) {}
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
