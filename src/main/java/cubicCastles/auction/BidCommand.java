package cubicCastles.auction;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import utils.Msg;

import java.util.concurrent.TimeUnit;

public class BidCommand extends Command {
    public BidCommand() {
        this.name = "bid";
        this.aliases = new String[]{"addbid"};
        this.category = new Category("Cubic Castles");
        this.cooldown = 1;
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().trim().split(" ").length < 2) {
            Msg.badTimed(event, "Please include the following arguments, id and bid amount.\n"
                    + "Eg: .bid 2 20", 20, TimeUnit.SECONDS);
        } else {
            String id = event.getArgs().trim().split(" ")[0];
            String bidamt = event.getArgs().trim().split(" ")[1];
            event.getMessage().delete().queue();
            int newCurrent = 0;
            if (AuctionCommand.auctions.isEmpty()) {
                Msg.badTimed(event, "There are no current auctions running to bid.", 10, TimeUnit.SECONDS);
            } else if (!AuctionCommand.auctions.containsKey(id.trim())) {
                Msg.badTimed(event, "Provided auction id is not valid.", 10, TimeUnit.SECONDS);
            } else {
                Auction auc = AuctionCommand.auctions.get(id).get(AuctionCommand.auctions.get(id).keySet().toArray()[0]);
                Message message = (Message) AuctionCommand.auctions.get(id).keySet().toArray()[0];
                try {
                    int bid = Integer.parseInt(bidamt);
                    int current = Integer.parseInt(auc.getCurrentPrice());
                    if (bid > 500000) {
                        Msg.badTimed(event, "Sorry, bid amount is too high. Make it less than 500,000 cubits please.", 10, TimeUnit.SECONDS);
                        return;
                    }
                    newCurrent = current + bid;
                    if (!auc.getEndPrice().equals("-1")) {
                        if (newCurrent >= Integer.parseInt(auc.getEndPrice())) {
                            Msg.badTimed(event, "The bid limit for this auction has been reached. Ending the bid..", 10, TimeUnit.SECONDS);
                            auc.setCurrentPrice(Integer.toString(newCurrent));
                            auc.setUser(event.getAuthor());
                            AuctionUpdater.endAuction(event, (Message) AuctionCommand.auctions.get(id).keySet().toArray()[0], auc.getItemName(), id);
                            return;
                        }
                    }
                } catch (NumberFormatException ex) {
                    Msg.badTimed(event, "Not a valid character for bid amount. Please enter a number only.", 10, TimeUnit.SECONDS);
                    return;
                }

                //Sets new info
                auc.setCurrentPrice(Integer.toString(newCurrent));
                auc.setUser(event.getAuthor());

                if (AuctionUpdater.updateAuction(auc, message, auc.getItemName(), id, event.getAuthor(), auc.getStartPrice(), Integer.toString(newCurrent), auc.getBidOwner())) {
                    //Msg.replyTimed(event, event.getAuthor().getName()+", you bid "+bidamt+" cubits! Check out the new total bid amount.", 30, TimeUnit.SECONDS);
                } else {
                    Msg.badTimed(event, "Failed to bid, please report this or try again later.", 10, TimeUnit.SECONDS);
                }
            }
        }
    }
}
