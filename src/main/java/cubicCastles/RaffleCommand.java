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

import java.awt.*;

@Deprecated
public class RaffleCommand extends Command {
    public RaffleCommand() {
        this.name = "announcements";
        this.aliases = new String[]{"announcement"};
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
        this.cooldownScope = CooldownScope.GLOBAL;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        try {
            String title = "Cubic Castles Announcement";
            StringBuilder raffleInfo = new StringBuilder();

            em.setTitle(title);
            em.setColor(Color.CYAN);

            Document doc = Jsoup.parse(CacheUtils.getCache("raffle"));

            Elements div = doc.getElementsByClass("DismissMessage CasualMessage");
            for (Element info : div) {
                raffleInfo.append(info.ownText().replaceAll("\\*", "\\\\*")).append("\n\n");
            }
            em.setDescription(raffleInfo.toString().isEmpty() ? "No current announcements." : raffleInfo.toString());

            event.getTextChannel().sendMessage(em.build()).queue();

        } catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "RaffleCommand.java");
        }
    }

}