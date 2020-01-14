package cubicCastles;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import utils.CacheUtils;

import java.awt.*;

public class StaffListCommand extends Command {
    public StaffListCommand() {
        this.name = "staffs";
        this.aliases = new String[]{"staff", "stafflist"};
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        try {
            String title = "Staff List";
            StringBuilder availableMods = new StringBuilder();
            StringBuilder unavailableMods = new StringBuilder();
            StringBuilder retiredMods = new StringBuilder();
            StringBuilder forumMods = new StringBuilder();
            StringBuilder unavailableForumMods = new StringBuilder();
            StringBuilder retiredForumMods = new StringBuilder();
            StringBuilder admins = new StringBuilder();

            em.setTitle(title);
            em.setColor(Color.CYAN);

            Document doc = Jsoup.parse(CacheUtils.getCache("staff"));

            Element post = doc.getElementsByClass("Message").first();

            //Available Mods
            Element ul1 = post.getElementsByTag("ul").first();
            for (Element el : ul1.getElementsByTag("li")) {
                availableMods.append(el.text().replaceAll("\\*", "\\\\*")).append("\n");
            }

            //Unavailable Mods
            Element ul2 = post.getElementsByTag("ul").get(1);
            for (Element el : ul2.getElementsByTag("li")) {
                unavailableMods.append(el.text().replaceAll("\\*", "\\\\*")).append("\n");
            }

            //Retired Mods
            Element ul3 = post.getElementsByTag("ul").get(2);
            for (Element el : ul3.getElementsByTag("li")) {
                retiredMods.append(el.text().replaceAll("\\*", "\\\\*")).append("\n");
            }

            //Forum Mods
            Element ul4 = post.getElementsByTag("ul").get(3);
            for (Element el : ul4.getElementsByTag("li")) {
                forumMods.append(el.text().replaceAll("\\*", "\\\\*")).append("\n");
            }

            //Unavailable Forum Mods
            Element ul5 = post.getElementsByTag("ul").get(4);
            for (Element el : ul5.getElementsByTag("li")) {
                unavailableForumMods.append(el.text().replaceAll("\\*", "\\\\*")).append("\n");
            }

            //Administrators
            Element ul7 = post.getElementsByTag("ul").get(5);
            for (Element el : ul7.getElementsByTag("li")) {
                admins.append(el.text().replaceAll("\\*", "\\\\*")).append("\n");
            }
            em.addField("Available Game Moderators", availableMods.toString(), true);
            em.addField("Unavailable Game Moderators", unavailableMods.toString(), true);
            em.addField("Retired Game Moderators", retiredMods.toString(), true);
            em.addField("Forum Moderators", forumMods.toString(), true);
            em.addField("Unavailable Forum Moderators", unavailableForumMods.toString(), false);
           //em.addField("Retired Forum Moderators", retiredForumMods.toString(), true);
            em.addField("Administrators", admins.toString(), true);

            event.getTextChannel().sendMessage(em.build()).queue();

        } catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            ExceptionHandler.handleException(event, e, "StaffListCommand.java");
        }
    }

}
