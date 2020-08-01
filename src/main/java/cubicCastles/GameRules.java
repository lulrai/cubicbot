package cubicCastles;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;

public class GameRules extends Command {
    public GameRules() {
        this.name = "gamerules";
        this.aliases = new String[]{"gamerule"};
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        try {
            em.setColor(Color.CYAN);

            em.setTitle("Game Rules", "http://www.cubiccastles.com/terms_of_service.php");

            em.addField("Censored Words", "Cubic Castles is designed so everyone can play. That means many people of different backgrounds will be playing at the same time. As such some words are censored in chat messages, sign posts, and realm names. We realize this isn't necessary for everyone, but in order to keep the game as accessible to as many players as possible, we decided this was best, and didn't really detract from the basic game-play. Attempting to bypass the censoring by misspelling words is considered poor player behavior and could lead to a ban from the game. Likewise using innapropriate language in signs, player names, castle names or anywhere text shows up would also be considered violating the player guidelines.", true);
            em.addField("Mature Themes", "We respectfully ask players to use a little basic judgement in their designs, and their conduct, and treat your castles as public places. Please understand that anyone can come by and visit your castle. We'd really like everyone to be able to enjoy the game, and as some people are offended or too young for certain material, we would prefer that your designs remain \"family-friendly\"", true);
            em.addField("Spamming", "The chat bar and hollas exist so that players may communicate with each other. Spamming is repeatedly sending letters, messages, or nonsense, in a way that makes it difficult for other players to communicate. Spamming annoys other players and is considered bad behavior and could lead to a ban from the game. Posting messages to advertise products or websites would also be considered spamming.", true);
            em.addField("Scammers Thieves & Bullies", "Please play nice. It's inevitable in any on-line game that people will try to scam other players, or harass or bully other players. Again, we'd rather not have to play virtual police, but we will if forced to. Treat other players the way you'd like to be treated. Players who repeatedly steal from or bully other players would be considered as violating the player guidelines and could face temporary or permanent bans from the game.", true);
            em.addField("Cheating & Automation", "Don't use loopholes in the game, or use scripts, or 3rd party software to cheat the game in ways that will make it less fun for everyone. Anyone who is caught doing this sort of thing will be banned. You may get a warning if you're lucky. You may not. We will actively update the server to detect and shut down these sorts of cheats. Just because you get away with it one day doesn't mean it will work the next. If you value your account, then don't do this.\n\n" +
                    "If a bug is found in the game that allows a player to create free cubits, or free items, report it it cubic castles staff. We have systems in place to detect these problems and while it might seem like you can get away with it at first, attempting to take advantage of this kind of bug will most often lead to a permanent ban of you account. Be very cautious about accepting items or cubits from a player you think is cheating, as this could also lead to problems for your account even if you are not the one exploiting the bug.", true);
            em.addField("Games", "Malicious player made games are prohibited\n"
                    + "These include:\n"
                    + "1) Giveaways that require payment from participants.\n"
                    + "2) Gambling or games of chance that require cubits or item payments from participants.\n"
                    + "3) Games that require players to drop items as \"payment\" to play. (drop games)\n\n"
                    + "Games of skill such as creative building contests using foundation blocks, or parkour races may be allowed, but player complaints or reports of fake contests or undelivered prizes could lead to a permanent ban from the game for the organizers of these events. Moderators or admins may choose to shut these events down at their discretion if they determine the event to be malicious. We would encourage players not to join such contests unless they know the organizers well and trust them. You join at your own risk. Lost entry fees will not be returned.", true);
            em.addField("Play Smart!", "Don't give your personal details to other players. Report abusive behaviour or people you believe are trying to scam others in the game. Ideally everyone playing would be nice and treat other players well, but unfortunately this isn't always the case. So play smart, and exercise a little caution.\n\n" +
                    "Players are ultimately responsible for their in game items. The tools are in the game to protect your things. Cosmic Cow will not, in general, become involved in disputes between players, so if someone you trusted, steals your items you may lose them forever. We will attempt to ban troublemakers, and people who are repeatedly caught stealing, but we are not able to chase down each case on an individual basis.", true);
            em.addField("Selling Items", "You may not attempt to trade items, cubits or accounts in our game for real-life money or items, or for things in other games.", true);
            em.addField("Bans", "A Player in cubic castles who violates the player guidelines may be temporarily banned at the discretion of moderators or adminstrators. Players who repeatedly violate the player guidelines can be permanently banned from the game.", true);
            em.addField("Mods & Admins", "Cubic castles moderators (blue tag names) are dedicated players who have been trusted with some extra abilities to help other players in the game. They are volunteers and may not always be able to help you.Their goal is to maintain a positive, and fun environment for players. Mods have the power to ban players they feel are violating the player guidelines. Repeated bans could lead to a permanent ban. If you feel you were banned unfairly you can contact us directly at [support.cubiccastles.com](http://help.cubiccastles.com/support/index.php) \n\n"
                    + "Cubic Castles Administrators (red tag names) work for Cosmic Cow. While they may in some cases be able to help you in game, often if seen in game, they are there for a specific reason like testing a bug or helping a player who files a support ticket. If you have a support request please use the ticket system at [support.cubiccastles.com](http://help.cubiccastles.com/support/index.php) to get your proper turn.", true);

            em.setFooter("Terms of Service is subject to change.", null);

            event.getTextChannel().sendMessage(em.build()).queue();
        } catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "GameRules.java");
        }
    }
}