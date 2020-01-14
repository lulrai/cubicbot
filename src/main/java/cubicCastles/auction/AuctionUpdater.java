package cubicCastles.auction;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class AuctionUpdater {

    static void endAuction(CommandEvent event, Message message, String itemName, String id) {
        User u = AuctionCommand.auctions.get(id).get(message).getUser();
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setTitle(itemName);
        em.setDescription("The auction has ended. The highest bidder was " + u.getName() + "#" + u.getDiscriminator() + " with " + AuctionCommand.auctions.get(id).get(message).getCurrentPrice() + " cubits as their bid!");
        em.setFooter("Auction by " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator(), null);
        message.getChannel().sendMessage(em.build()).queue(v -> {
            AuctionCommand.auctions.remove(id);
            message.delete().queue();
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(20);
            exec.schedule(() -> v.delete().queue(), 30, TimeUnit.MINUTES);
        });
    }

    static boolean updateAuction(Auction auc, Message message, String itemName, String id, User user, String startPrice, String newCurrent, User owner) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setTitle(itemName);
        em.addField("Id", id, true);
        em.addField("Highest Bidder", user.getName() + "#" + user.getDiscriminator(), true);
        em.addField("Starting Price", startPrice + " cubits", true);
        em.addField("Current Price", newCurrent + " cubits", true);
        em.setFooter("Auction by " + owner.getName() + "#" + owner.getDiscriminator(), null);
        message.getChannel().sendMessage(em.build()).queue(v -> {
            Map<Message, Auction> newAuc = new HashMap<>();
            newAuc.put(v, auc);
            AuctionCommand.auctions.put(id, newAuc);
        });
        message.delete().queue();
        return true;
    }

}
