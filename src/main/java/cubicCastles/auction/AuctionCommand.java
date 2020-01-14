package cubicCastles.auction;

import adminCommands.AuctionSet;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.Msg;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AuctionCommand extends Command {
    static Map<String, Map<Message, Auction>> auctions = new HashMap<>();

    private EventWaiter waiter;

    public AuctionCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "auction";
        this.aliases = new String[]{};
        this.category = new Category("Cubic Castles");
        this.cooldown = 1;
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        //396327915848990721
        if (AuctionSet.getAuctionChannel(event).equals(event.getChannel().getId()) && event.getGuild().getMember(event.getAuthor()).getRoles().stream().noneMatch(r -> r.getName().equalsIgnoreCase("AuctionBanned"))) {
            if (auctions.size() == 10) {
                Msg.badTimed(event, "Auction limit reached. There are 10 on-going auctions so please wait until one of them ends!", 10, TimeUnit.SECONDS);
                return;
            }
            event.getMessage().delete().queue();
            Msg.replyTimed(event, "Starting an auction.. Please enter the item you would like to auction or type in `cancel`.", 10, TimeUnit.SECONDS);
            waitForItem(event);
        } else {
            if (AuctionSet.getAuctionChannel(event).isEmpty()) {
                Msg.bad(event, "No auction channel set for this server.");
                return;
            }
            Msg.bad(event, "Not an auction channel. Use " + event.getGuild().getTextChannelById(AuctionSet.getAuctionChannel(event)).getAsMention() + " channel for that.");
        }
    }

    private void waitForItem(CommandEvent event) {
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if (e.getMessage().getContentRaw().equalsIgnoreCase("cancel")) {
                        e.getMessage().delete().queue();
                        Msg.replyTimed(event, "Auction has been cancelled.", 10, TimeUnit.SECONDS);
                    } else {
                        String item = e.getMessage().getContentRaw().trim();

                        if (item.length() > 200) {
                            Msg.badTimed(event, "Too long! Please make the item name shorter. So let's try that again..", 10, TimeUnit.SECONDS);
                            waitForItem(event);
                        } else {
                            Msg.replyTimed(event, "Please enter the start price and/or the end price for the auction or type in `cancel`.", 10, TimeUnit.SECONDS);
                            e.getMessage().delete().queue();
                            waitForPrice(event, item);
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Msg.replyTimed(event, "You took longer than 2 minutes to respond, " + event.getAuthor().getAsMention() + "! Auction has been cancelled.", 10, TimeUnit.SECONDS));
    }

    private void waitForPrice(CommandEvent event, String item) {
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if (e.getMessage().getContentRaw().equalsIgnoreCase("cancel")) {
                        e.getMessage().delete().queue();
                        Msg.replyTimed(event, "Auction has been cancelled.", 10, TimeUnit.SECONDS);
                    } else {
                        String[] split = e.getMessage().getContentRaw().split(" ");
                        if (split.length < 1) {
                            Msg.badTimed(event, "Please enter the price in the following format:\n"
                                    + "startPrice endPrice\n"
                                    + "Eg: 20 40", 10, TimeUnit.SECONDS);
                        } else {
                            if (split.length == 1) {
                                try {
                                    int start = Integer.parseInt(split[0]);
                                    if ((start < 0 || start > 1000000)) {
                                        Msg.badTimed(event, "The price is too high, please set it below 1,000,000 cubits.", 10, TimeUnit.SECONDS);
                                        return;
                                    }
                                } catch (NumberFormatException ex) {
                                    Msg.badTimed(event, "Make sure the price is numbers only, please.", 10, TimeUnit.SECONDS);
                                    return;
                                }
                                Msg.replyTimed(event, "Please enter the time in or type in `cancel`.", 10, TimeUnit.SECONDS);
                                e.getMessage().delete().queue();
                                waitForTime(event, item, split[0], "-1");
                            } else {
                                try {
                                    int start = Integer.parseInt(split[0]);
                                    int end = Integer.parseInt(split[1]);
                                    if ((start < 0 || start > 1000000) && (end < 0 || end > 1000000)) {
                                        Msg.badTimed(event, "The prices are too high, please set it below 1,000,000 cubits.", 10, TimeUnit.SECONDS);
                                        return;
                                    }
                                } catch (NumberFormatException ex) {
                                    Msg.badTimed(event, "Make sure the prices are numbers only, please.", 10, TimeUnit.SECONDS);
                                    return;
                                }
                                Msg.replyTimed(event, "Please enter the time in or type in `cancel`.", 10, TimeUnit.SECONDS);
                                e.getMessage().delete().queue();
                                waitForTime(event, item, split[0], split[1]);
                            }
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Msg.replyTimed(event, "You took longer than 2 minutes to respond, " + event.getAuthor().getAsMention() + "! Auction has been cancelled.", 10, TimeUnit.SECONDS));
    }

    private void waitForTime(CommandEvent event, String item, String startPrice, String endPrice) {
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if (e.getMessage().getContentRaw().equalsIgnoreCase("cancel")) {
                        e.getMessage().delete().queue();
                        Msg.replyTimed(event, "Auction has been cancelled.", 10, TimeUnit.SECONDS);
                    } else {
                        String val = e.getMessage().getContentRaw().toUpperCase().trim();
                        boolean min = false;
                        String trim = val.substring(0, val.length() - 1).trim();
                        if (val.endsWith("M")) {
                            min = true;
                            val = trim;
                        } else if (val.endsWith("S")) {
                            val = trim;
                        } else {
                            Msg.badTimed(event, "Please include either `M` or `S` at the end to denote minute or second.", 10, TimeUnit.SECONDS);
                            return;
                        }
                        int seconds;
                        try {
                            seconds = (min ? 60 : 1) * Integer.parseInt(val);
                            if (seconds < 30 || seconds > 60 * 60) {
                                Msg.badTimed(event, "Sorry! Auctions need to be at least 30 seconds long, and can't be longer than 1 hour. Please try again.", 10, TimeUnit.SECONDS);
                            } else {
                                e.getMessage().delete().queue();
                                createAuction(event, item, startPrice, endPrice, seconds, TimeUnit.SECONDS);
                            }
                        } catch (NumberFormatException ex) {
                            Msg.badTimed(event, "Hm. I can't seem to get a number from that. Can you try again?", 10, TimeUnit.SECONDS);
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Msg.replyTimed(event, "You took longer than 2 minutes to respond, " + event.getAuthor().getAsMention() + "! Auction has been cancelled.", 10, TimeUnit.SECONDS));
    }

    private void createAuction(CommandEvent event, String itemName, String startPrice, String endPrice, int time, TimeUnit type) {
        String id = getRandomId();
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setTitle(itemName);
        em.addField("Id", id, true);
        em.addField("Highest Bidder", event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator(), true);
        em.addField("Starting Price", startPrice + " cubits", true);
        em.addField("Current Price", startPrice + " cubits", true);
        em.setFooter("Auction by " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator(), null);
        event.getTextChannel().sendMessage("@here \n" + em.build()).queue(v -> {
            Map<Message, Auction> newAuc = new HashMap<>();
            newAuc.put(v, new Auction(itemName, startPrice, endPrice, startPrice, id, event.getAuthor(), event.getAuthor()));
            auctions.put(id, newAuc);
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(10);
            exec.schedule(() -> AuctionUpdater.endAuction(event, (Message) auctions.get(id).keySet().toArray()[0], itemName, id), time, type);
        }, t -> Msg.bad(event, "Failed to create an auction, please try again later."));
    }

    private String getRandomId() {
        String id = Integer.toString(new Random().nextInt(11));
        for (String key : auctions.keySet()) {
            if (key.equalsIgnoreCase(id)) {
                getRandomId();
            }
        }
        return id;
    }
}
