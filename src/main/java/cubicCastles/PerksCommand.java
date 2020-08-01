package cubicCastles;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import utils.CacheUtils;
import utils.Msg;

import java.awt.*;

public class PerksCommand extends Command {
    public PerksCommand() {
        this.name = "perk";
        this.aliases = new String[]{"perks", "perkinfo"};
        this.arguments = "perkName";
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
    }

    private static String br2nl(String html) {
        if (html == null)
            return html;
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        try {
            String basePicURL = "http://cubiccastles.com/recipe_html/";

            em.setColor(Color.CYAN);

            Document doc = Jsoup.parse(CacheUtils.getCache("perk"));

            String title;
            StringBuilder perkName = new StringBuilder();

            Element mainCenter = doc.getElementsByTag("center").first();
            Element allPerksTag = mainCenter.getElementsByTag("center").last().getElementsByTag("tbody").first();
            Elements allTr = allPerksTag.getElementsByTag("tr");

            if (event.getArgs().isEmpty()) {
                title = "All Perks";
                for (Element tr : allTr) {
                    Element info = tr.children().last();
                    if (info != null) {
                        if (info.getElementsByTag("b").first() != null) {
                            perkName.append("- ").append(info.getElementsByTag("b").first().text()).append("\n");
                        }
                    }
                }
                em.setTitle(title);
                em.setDescription(perkName.toString());
            } else {
                String givenPerk = event.getArgs().trim();
                String perkImage = "";
                String perkDescription = "";

                for (Element tr : allTr) {
                    Element info = tr.children().last();
                    Element image = tr.children().first();
                    if (info != null && image != null) {
                        if (info.getElementsByTag("b").first() != null) {
                            if (givenPerk.equalsIgnoreCase(info.getElementsByTag("b").first().text().trim())) {
                                perkName = new StringBuilder(info.getElementsByTag("b").first().text());
                                perkImage = basePicURL + image.getElementsByTag("img").attr("src").trim();
                                perkDescription = br2nl(info.html()).replaceAll("&lt;", "<").replaceAll("&gt;", ">").replace(perkName.toString(), "").trim();
                                break;
                            }
                        }
                    }
                }

                if (perkName.length() == 0) {
                    Msg.bad(event, "Perk not found. Check all perks using no arguments or `perks` command.");
                    return;
                }
                em.setTitle("Perk Info");
                em.addField("Perk Name", perkName.toString(), true);
                em.addField("Description", perkDescription, false);
                em.setThumbnail(perkImage);
            }

            event.getTextChannel().sendMessage(em.build()).queue();

        } catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "PerksCommand.java");
        }
    }

}
