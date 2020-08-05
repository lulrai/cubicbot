package normalCommands;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CacheUtils;

import java.awt.*;
import java.util.Random;

public class RollCommand extends Command {
    public RollCommand() {
        this.name = "roll";
        this.aliases = new String[] { "r" };
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(!event.getGuild().getId().equals("240614697848537089") && !event.getGuild().getId().equals("365932526297939971") && !event.getGuild().getId().equals("705622006652993607")) return;
        if(!(event.getMember().hasPermission(Permission.ADMINISTRATOR) ||
                (event.getAuthor().getId().equals("169122787099672577")
                        || event.getAuthor().getId().equals("222488511385698304")
                        || event.getAuthor().getId().equals("195621535703105536")))) return;

        try {
            String basePicURL = "http://cubiccastles.com/recipe_html/";

            Document doc = Jsoup.parse(CacheUtils.getCache("item"));

            Elements itemDiv = doc.select("div#CONTAINER > div.HIDE_CONTAINER");
            Elements itemList = itemDiv.get(2).select("tr > td");

            Random r = new Random();
            Element item = itemList.get(r.nextInt(itemList.size()));

            String itemName = item.text();
            String itemImage = basePicURL + item.select("img").attr("src");

            EmbedBuilder em = new EmbedBuilder();
            em.setColor(Color.YELLOW);
            em.setTitle("BINGO Item");
            em.setDescription(itemName);
            em.setImage(itemImage);
            em.setFooter("Mark this item off on your card if you have it.");

            if(event.getGuild().getId().equals("365932526297939971")) {
                event.getChannel().sendMessage(event.getGuild().getRoleById("727207508527284294").getAsMention()).embed(em.build()).queue();
            }
            else{
                event.getChannel().sendMessage(em.build()).queue();
            }

            event.getMessage().delete().queue();
        } catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "ItemCommand.java");
        }
    }
}
