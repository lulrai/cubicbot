//
// Decompiled by Procyon v0.5.36
//

package cubicCastles;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import cubicCastles.craftCommands.CraftCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import normalCommands.bingo.BingoItem;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.Msg;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static normalCommands.bingo.BingoItem.emojiToNumber;
import static normalCommands.bingo.BingoItem.getEmojis;

public class PriceCommand extends Command
{
    private static Map<String, Integer[]> priceMap;
    private static String recentUpdate;
    private EventWaiter waiter;

    public PriceCommand(EventWaiter waiter) {
        this.name = "price";
        this.aliases = new String[] { "prices", "pr" };
        this.arguments = "itemName";
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
        this.waiter = waiter;
    }

    @Override
    protected void execute(final CommandEvent event) {
        final Message msg = event.getChannel().sendMessage("Looking up the information..").complete();
        try {
            if (event.getArgs().isEmpty()) {
                final EmbedBuilder em = new EmbedBuilder();
                em.setColor(Color.YELLOW);
                em.setTitle("Price Command");
                em.setDescription("Made and Updated by 'Dragon' and others");
                em.addField("Usage", "`.price <itemName>`", false);
                em.addField("Description", "Indicate the item name exactly to get the price of the item using the command above.Or, indicate part of the item name to view the list of items that has price listed. [DO NOT enter more if you don't know the name.]", false);
                em.addField("Example", "`.price books`", false);
                em.addField("Price List Last Updated", PriceCommand.recentUpdate, true);
                em.addField("Help Out!", "https://discord.gg/XW5DBvY", true);
                msg.editMessage(em.build()).queue();
            }
            else {
                if (PriceCommand.priceMap.isEmpty()) {
                    final EmbedBuilder em = new EmbedBuilder();
                    em.setTitle("Command not functional");
                    em.setColor(Color.WHITE);
                    em.setDescription("The price command is currently not functional.");
                    em.addField("Reasons", "1. It could be because the forum is inaccessible/down. If that's the case, there is no fix for it until the forum is up.\n2. If the forum is up (please check first), please let the bot developer, `Raizusekku#2602` know of the issue as it could be a severe problem in the bot.", false);
                    msg.editMessage(em.build()).queue();
                    return;
                }
                final String givenItem = event.getArgs().trim();
                if(givenItem.length() < 3){
                    Msg.bad(event, "The name provided is too short. Please provide at least 3 characters long argument.");
                    return;
                }
                runCommand(givenItem, event, msg);
            }
        }
        catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        }
        catch (Exception e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "PriceCommand.java");
        }
    }


    private void runCommand(String givenItem, CommandEvent event, Message msg){
        final EmbedBuilder em = new EmbedBuilder();
        ArrayList<String> suggestionList = new ArrayList<>();
        int count = 0;
        for (final String item : PriceCommand.priceMap.keySet()) {
            if (checkSimilarStrings(item, givenItem)) {
                if(count == 5){
                    count = 0;
                }
                count++;
                suggestionList.add(BingoItem.numberToEmoji(count) + " " + item.trim());
            }
            if (event.getMessage().getAuthor().getId().equals("169122787099672577") || event.getMessage().getAuthor().getId().equals("222488511385698304")){
                if (event.getMessage().getMentionedMembers().size() > 0 && event.getMessage().getMentionedMembers().get(0).getUser().getId().equals("169122787099672577")) {
                    em.setTitle("Absolutely Priceless");
                    em.setColor(Color.GREEN);
                    em.setDescription("I love you a lot, and you're priceless. So, suck it.");
                    msg.editMessage(em.build()).queue();
                    return;
                }
                else if (event.getMessage().getMentionedMembers().size() > 0 && event.getMessage().getMentionedMembers().get(0).getUser().getId().equals("222488511385698304")) {
                    em.setTitle("Less Priceless");
                    em.setColor(Color.GREEN);
                    em.setDescription("I love you a lot too, and you're priceless too. But less priceless than I am, suck it.");
                    msg.editMessage(em.build()).queue();
                    return;
                }
            }
            if (checkStrings(item, givenItem)) {
                em.setTitle(item + "'s Price");
                em.setColor(Color.GREEN);
                em.addField("Price Range", NumberFormat.getInstance().format(PriceCommand.priceMap.get(item)[0]) + ((PriceCommand.priceMap.get(item).length > 2) ? ("/" + PriceCommand.priceMap.get(item)[2] + "c") : "c") + " - " + NumberFormat.getInstance().format(PriceCommand.priceMap.get(item)[1]) + ((PriceCommand.priceMap.get(item).length > 2) ? ("/" + PriceCommand.priceMap.get(item)[3] + "c") : "c"), true);
                em.addField("Average Price", NumberFormat.getInstance().format((int)((PriceCommand.priceMap.get(item)[0] + PriceCommand.priceMap.get(item)[1]) / 2.0)) + ((PriceCommand.priceMap.get(item).length > 2) ? ("/" + (int)((PriceCommand.priceMap.get(item)[2] + PriceCommand.priceMap.get(item)[3]) / 2.0) + "c") : "c"), true);
                em.setFooter("Last Updated: " + PriceCommand.recentUpdate +" (Disclaimer: May not be up to date.)");
                msg.editMessage(event.getAuthor().getAsMention()).queue();
                msg.editMessage(em.build()).queue();
                return;
            }
        }
        if (suggestionList.size() == 0) {
            em.setTitle("Price not Found");
            em.setColor(Color.RED);
            em.setDescription("Price not found. Please check your input. Type less word for `itemName` if needed, instead of more words.");
            em.addField("NOTE", "Know that not all prices for items exist. Please visit the forum price page and check if it's there.", false);
            em.setFooter("May take up to a day to update with new prices.");
            msg.editMessage(em.build()).queue();
        }
        else if (event.getArgs().length() < 3) {
            em.setTitle("Argument not Long Enough");
            em.setColor(Color.RED);
            em.setDescription("Price not found. Please check your input. Type at least three letters for `itemName` and don't type too many if you don't know the exact name.");
            msg.editMessage(em.build()).queue();
        }
        else {
            final Map<Integer, List<String>> paginatedCommands = CraftCommand.partition(suggestionList, 5);
            buildMenu(event, paginatedCommands, 1, msg);
        }
    }

    private void buildMenu(CommandEvent event, Map<Integer, List<String>> suggestions, int pageNum, Message m){
        StringBuilder sb = new StringBuilder();
        for(String s : suggestions.get(pageNum-1)) sb.append(s).append("\n");
        ButtonMenu.Builder bm = new ButtonMenu.Builder()
                .setDescription("Possible Suggestions:\n" +
                        sb.toString().trim() + "\n\n" + "Page: " + pageNum + "/" + suggestions.size())
                .setEventWaiter(waiter)
                .setUsers(event.getAuthor())
                .setTimeout(20, TimeUnit.SECONDS)
                .setColor(Color.orange)
                .setAction(v -> {
                    if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":x:")) {
                        Msg.reply(event.getTextChannel(), "Cancelled choosing an item price.");
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
                            m.delete().queue();
                        }
                        else {
                            runCommand(suggestions.get(pageNum-1).get(choice-1).replaceAll(":.+?:", "").trim(), event, m);
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

    public static void populatePrices() {
        Document docu = null;
        try {
            docu = Jsoup.parse(StatusCommand.getData("https://forums2.cubiccastles.com/index.php?p=/discussion/27821/cubic-castles-prices/p1"));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        final Pattern pricePattern = Pattern.compile("(.*?)([/0-9,c]+ *- *[/0-9,c]+)");
        boolean isPerPrice;
        final Element mainPost = docu.getElementsByClass("Content MainContent").first();
        if (mainPost == null) {
            return;
        }
        PriceCommand.priceMap.clear();
        final Element ownerPost = mainPost.getElementsByClass("Discussion").first();
        final Pattern p = Pattern.compile("([0-9]*[0-9])(/)([0-9]*[0-9])(/)([0-9])+");
        final Matcher m = p.matcher(ownerPost.select("div.Message.userContent").text());
        if (m.find()) {
            PriceCommand.recentUpdate = m.group(0);
        }
        final Elements originalSpoilers = ownerPost.getElementsByClass("Spoiler");
        for (final Element each : originalSpoilers) {
            final String[] split3 =  Jsoup.parse(each.html().replaceAll("<\\s*b*>(.*?)<\\s*/\\s*b>", "").replaceAll("(<br */?>\\s*){2,}", "<br>").replaceAll("<br>", "boiReplaceThis")).text().split("boiReplaceThis");
            for (final String s : split3) {
                isPerPrice = false;
                if (!s.isEmpty()) {
                    final Matcher match = pricePattern.matcher(s.trim());
                    if (match.find()) {
                        if (match.group(2).contains("/")) {
                            isPerPrice = true;
                        }
                        if (isPerPrice) {
                            final String[] split = match.group(2).split(" *- *");
                            final int firstAPrice = Integer.parseInt(split[0].split("/")[0].replaceAll("[^\\d]", ""));
                            final int firstBPrice = StringUtils.isBlank(split[0].split("/")[1].replaceAll("[^\\d]", "")) ? 1 : Integer.parseInt(split[0].split("/")[1].replaceAll("[^\\d]", ""));
                            final int secondAPrice = Integer.parseInt(split[1].split("/")[0].replaceAll("[^\\d]", ""));
                            final int secondBPrice = StringUtils.isBlank(split[1].split("/")[1].replaceAll("[^\\d]", "")) ? 1 : Integer.parseInt(split[1].split("/")[1].replaceAll("[^\\d]", ""));
                            PriceCommand.priceMap.put(match.group(1).trim(), new Integer[] { firstAPrice, secondAPrice, firstBPrice, secondBPrice });
                        }
                        else {
                            final int firstPrice = Integer.parseInt(match.group(2).split(" *- *")[0].replaceAll("[^\\d]", ""));
                            final int secondPrice = Integer.parseInt(match.group(2).split(" *- *")[1].replaceAll("[^\\d]", ""));
                            PriceCommand.priceMap.put(match.group(1).trim(), new Integer[] { firstPrice, secondPrice });
                        }
                    }
                }
            }
        }
        final Element commentPost = docu.getElementsByClass("CommentsWrap").first();
        final Elements allComments = commentPost.getElementsByTag("ul").first().getElementsByTag("li");
        for (final Element comment : allComments) {
            final String authorName = comment.getElementsByClass("Author").first().text().trim();
            if (authorName.equals("'Dragon'")) {
                final Elements spoilers = comment.getElementsByClass("Spoiler");
                if (spoilers.size() == 0) {
                    continue;
                }
                for (final Element each2 : spoilers) {
                    final String[] split4 = Jsoup.parse(each2.html().replaceAll("<\\s*b*>(.*?)<\\s*/\\s*b>", "").replaceAll("(<br */?>\\s*){2,}", "<br>").replaceAll("<br>", "boiReplaceThis")).text().split("boiReplaceThis");
                    for (final String s2 : split4) {
                        isPerPrice = false;
                        if (!s2.isEmpty()) {
                            final Matcher match2 = pricePattern.matcher(s2.trim());
                            if (match2.find()) {
                                if (match2.group(2).contains("/")) {
                                    isPerPrice = true;
                                }
                                if (isPerPrice) {
                                    final String[] split2 = match2.group(2).split(" *- *");
                                    final int firstAPrice2 = Integer.parseInt(split2[0].split("/")[0].replaceAll("[^\\d]", ""));
                                    final int firstBPrice2 = StringUtils.isBlank(split2[0].split("/")[1].replaceAll("[^\\d]", "")) ? 1 : Integer.parseInt(split2[0].split("/")[1].replaceAll("[^\\d]", ""));
                                    final int secondAPrice2 = Integer.parseInt(split2[1].split("/")[0].replaceAll("[^\\d]", ""));
                                    final int secondBPrice2 = StringUtils.isBlank(split2[1].split("/")[1].replaceAll("[^\\d]", "")) ? 1 : Integer.parseInt(split2[1].split("/")[1].replaceAll("[^\\d]", ""));
                                    PriceCommand.priceMap.put(match2.group(1).trim(), new Integer[] { firstAPrice2, secondAPrice2, firstBPrice2, secondBPrice2 });
                                }
                                else {
                                    final int firstPrice2 = Integer.parseInt(match2.group(2).split(" *- *")[0].replaceAll("[^\\d]", ""));
                                    final int secondPrice2 = Integer.parseInt(match2.group(2).split(" *- *")[1].replaceAll("[^\\d]", ""));
                                    PriceCommand.priceMap.put(match2.group(1).trim(), new Integer[] { firstPrice2, secondPrice2 });
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static Boolean checkStrings(final String str1, final String str2) {
        if (str1.trim().equalsIgnoreCase(str2.trim())) {
            return true;
        }
        return str1.trim().replaceAll("[^A-Za-z0-9 ]", "").equalsIgnoreCase(str2.trim());
    }

    private static Boolean checkSimilarStrings(final String str1, final String str2) {
        return str1.toLowerCase().contains(str2.toLowerCase());
    }

    static {
        PriceCommand.priceMap = new HashMap<>();
        PriceCommand.recentUpdate = "";
    }
}
