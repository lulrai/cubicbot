//
// Decompiled by Procyon v0.5.36
//

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
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import commands.utils.Msg;

import java.awt.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static commands.normalCommands.bingo.BingoItem.emojiToNumber;
import static commands.normalCommands.bingo.BingoItem.getEmojis;

@Deprecated
public class PriceCommand extends Command
{
    private static Map<String, String[]> priceMap;
    private static String recentUpdate;
    private EventWaiter waiter;

    public PriceCommand() {
        this.name = "price";
        this.aliases = new String[] { "prices", "pr" };
        this.arguments = "<itemName>";
        this.category = new Category("cubic");
        this.help = "Provides the price of the item with the item name provided. Uses [V's Forum Post](https://forums2.cubiccastles.com/discussion/30156/new-cubic-castles-prices-thread) for the prices.";
        this.waiter = Cubic.getWaiter();
        this.guildOnly = false;
    }

    @Override
    protected void execute(final CommandEvent event) {
        Message msg = event.getChannel().sendMessage("Looking up the commands.information..").complete();
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
                msg.editMessageEmbeds(em.build()).queue();
            }
            else {
                if (PriceCommand.priceMap.isEmpty()) {
                    final EmbedBuilder em = new EmbedBuilder();
                    em.setTitle("Command not functional");
                    em.setColor(Color.WHITE);
                    em.setDescription("The price command is currently not functional.");
                    em.addField("Reasons", "1. It could be because the forum is inaccessible/down. If that's the case, there is no fix for it until the forum is up.\n2. If the forum is up (please check first), please let the bot developer, `Raizusekku#2602` know of the issue as it could be a severe problem in the bot.", false);
                    msg.editMessageEmbeds(em.build()).queue();
                    return;
                }
                final String givenItem = event.getArgs().trim();
                if(givenItem.length() < 3){
                    Msg.bad(event.getChannel(), "The name provided is too short. Please provide at least 3 characters long argument.");
                    return;
                }
                runCommand(givenItem, event, msg);
            }
        }
        catch (InsufficientPermissionException ex) {
            event.getChannel().sendMessage(ex.getMessage()).queue();
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
            if (event.getMessage().getAuthor().getId().equals("169122787099672577") || event.getAuthor().getId().equals("747982101709586523") || event.getMessage().getAuthor().getId().equals("222488511385698304")){
                if (event.getMessage().getMentionedUsers().size() > 0 && event.getMessage().getMentionedUsers().get(0).getId().equals("747982101709586523")) {
                    em.setTitle("Absolutely Priceless");
                    em.setColor(Color.GREEN);
                    em.setDescription("I love you a lot, and you're priceless. So, suck it.");
                    msg.editMessageEmbeds(em.build()).queue();
                    return;
                }
                else if (event.getMessage().getMentionedUsers().size() > 0 && event.getMessage().getMentionedUsers().get(0).getId().equals("222488511385698304")) {
                    em.setTitle("Less Priceless");
                    em.setColor(Color.GREEN);
                    em.setDescription("I love you a lot too, and you're priceless too. But less priceless than I am, suck it.");
                    msg.editMessageEmbeds(em.build()).queue();
                    return;
                }
            }
            if (checkStrings(item, givenItem)) {
                em.setTitle(item + "'s Price");
                em.setColor(Color.GREEN);
                if(!PriceCommand.priceMap.get(item)[0].isEmpty()) {
                    try {
                        em.setThumbnail(PriceCommand.priceMap.get(item)[0]);
                    } catch(IllegalArgumentException e){
                        em.setThumbnail("https://www.cubiccastles.com/recipe_html/obscured.png");
                    }
                }
                em.addField("Price Range",
                        NumberFormat.getInstance().format(Integer.parseInt(PriceCommand.priceMap.get(item)[1])) +
                                ((PriceCommand.priceMap.get(item).length > 3) ? ("/" + PriceCommand.priceMap.get(item)[3] + "c") : "c") +
                                " - " +
                                NumberFormat.getInstance().format(Integer.parseInt(PriceCommand.priceMap.get(item)[2])) +
                                ((PriceCommand.priceMap.get(item).length > 3) ? ("/" + PriceCommand.priceMap.get(item)[4] + "c") : "c"), true);
                em.addField("Average Price", NumberFormat.getInstance().format(
                        (int)((Integer.parseInt(PriceCommand.priceMap.get(item)[1]) +
                                Integer.parseInt(PriceCommand.priceMap.get(item)[2])) / 2.0)) +
                        ((PriceCommand.priceMap.get(item).length > 3) ? ("/" + (int)((Integer.parseInt(PriceCommand.priceMap.get(item)[3]) + Integer.parseInt(PriceCommand.priceMap.get(item)[4])) / 2.0) + "c") : "c"), true);
                em.setFooter("Last Updated: " + PriceCommand.recentUpdate +" (Disclaimer: May not be up to date.)");
                msg.editMessage(event.getAuthor().getAsMention()).queue();
                msg.editMessageEmbeds(em.build()).queue();
                return;
            }
        }
        if (suggestionList.size() == 0) {
            em.setTitle("Price not Found");
            em.setColor(Color.RED);
            em.setDescription("Price not found. Please check your input. Type less word for `itemName` if needed, instead of more words.");
            em.addField("NOTE", "Know that not all prices for items exist. Please visit the forum price page and check if it's there.", false);
            em.setFooter("May take up to a day to update with new prices.");
            msg.editMessageEmbeds(em.build()).queue();
        }
        else if (event.getArgs().length() < 3) {
            em.setTitle("Argument not Long Enough");
            em.setColor(Color.RED);
            em.setDescription("Price not found. Please check your input. Type at least three letters for `itemName` and don't type too many if you don't know the exact name.");
            msg.editMessageEmbeds(em.build()).queue();
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
                        Msg.reply(event.getChannel(), "Cancelled choosing an item price.");
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
                .setFinalAction(me -> {});
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
        Document docu = Jsoup.parse(StatusCommand.getData("https://forums2.cubiccastles.com/discussion/32732/cubic-castles-prices-last-updated-10-14-2021"));
        final Pattern pricePattern = Pattern.compile("(.*?)([/0-9,c]+ *- *[/0-9,c]+)");
        Pattern urlPattern = Pattern.compile("<img[^>]*src=[\"']([^\"^']*)");
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

        Elements spoilers = mainPost.select("div.Spoiler");
        for(int i = 0; i < spoilers.size()-1; i++){
            Element each = spoilers.get(i);
            String[] items = each.getElementsByTag("b").html().split("\n");
            for(int j = 1; j < items.length; j++){
                isPerPrice = false;
                String[] itemSplit = items[j].split("<br>");
                Matcher match = pricePattern.matcher(itemSplit[0].trim());
                if(match.find()){
                    Matcher urlMatch = null;
                    if(itemSplit.length > 1){
                        urlMatch = urlPattern.matcher(itemSplit[1].trim());
                    }
                    if (match.group(2).contains("/")) {
                        isPerPrice = true;
                    }
                    if (isPerPrice) {
                        final String[] split = match.group(2).split(" *- *");
                        final String firstAPrice = (split[0].split("/")[0].replaceAll("[^\\d]", ""));
                        final String firstBPrice = StringUtils.isBlank(split[0].split("/")[1].replaceAll("[^\\d]", "")) ? "1" : (split[0].split("/")[1].replaceAll("[^\\d]", ""));
                        final String secondAPrice = (split[1].split("/")[0].replaceAll("[^\\d]", ""));
                        final String secondBPrice = StringUtils.isBlank(split[1].split("/")[1].replaceAll("[^\\d]", "")) ? "1" : (split[1].split("/")[1].replaceAll("[^\\d]", ""));
                        if (urlMatch != null && urlMatch.find()) {
                            String url = urlMatch.group(1);
                            PriceCommand.priceMap.put(Jsoup.parse(match.group(1).replaceAll("<b>", "").trim()).text(), new String[]{url, firstAPrice, secondAPrice, firstBPrice, secondBPrice});
                        } else {
                            PriceCommand.priceMap.put(Jsoup.parse(match.group(1).replaceAll("<b>", "").trim()).text(), new String[]{"", firstAPrice, secondAPrice, firstBPrice, secondBPrice});
                        }
                    }
                    else {
                        final String firstPrice = (match.group(2).split(" *- *")[0].replaceAll("[^\\d]", ""));
                        final String secondPrice = (match.group(2).split(" *- *")[1].replaceAll("[^\\d]", ""));
                        if (urlMatch != null && urlMatch.find()) {
                            String url = urlMatch.group(1);
                            PriceCommand.priceMap.put(Jsoup.parse(match.group(1).replaceAll("<b>", "").trim()).text(), new String[]{url, firstPrice, secondPrice});
                        } else {
                            PriceCommand.priceMap.put(Jsoup.parse(match.group(1).replaceAll("<b>", "").trim()).text(), new String[]{"", firstPrice, secondPrice});
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
