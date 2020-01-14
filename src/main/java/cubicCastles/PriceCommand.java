package cubicCastles;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import priceInfo.WebsiteItem;
import utils.CacheUtils;
import utils.Constants;

import java.awt.*;

public class PriceCommand extends Command {

    public PriceCommand() {
        this.name = "price";
        this.aliases = new String[]{"prices", "pr"};
        this.arguments = "itemsType";
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        try {
            String title = "";

            em.setColor(Color.CYAN);

            Message msg = event.getChannel().sendMessage("Looking up the information..").complete();

            Document docu = Jsoup.parse(CacheUtils.getCache("price"));
            Element post = docu.getElementsByClass("Message").first();

            if (event.getArgs().isEmpty()) {
                em.addField("Usage", "Here is how you can use this command:", false);
                em.addField("Pack Image", "To check the price of a certain pack, type in:\n"
                        + "`" + Constants.D_PREFIX + "price <packName>`", true);
                em.addField("Tip", "To check the names of the packs, please use `" + Constants.D_PREFIX + "pricepack`", false);
            } else {
                String priceImage = "";
                String givenArg = event.getArgs().trim();

                for (Element el : post.getElementsByClass("SpoilerTitle")) {
                    String name = el.getElementsByTag("span").first().ownText();
                    if (WebsiteItem.checkStrings(name, givenArg) ? WebsiteItem.checkStrings(name, givenArg) : WebsiteItem.checkSimilarStrings(name, givenArg)) {
                        title = name + " Price Info";
                        priceImage = el.parent().getElementsByClass("SpoilerText").first().getElementsByTag("img").last().attr("src");
                        break;
                    }
                }

                if (!title.isEmpty()) {
                    em.setTitle(title);
                    em.setDescription("[Click here for mobile](" + priceImage + ")");
                    em.setImage(priceImage);
                    em.setFooter("Prices created by Superxteme and Other Qbees", null);
                } else {
                    em.setDescription("Not a valid price pack. Use `.pricepack` command to view all the price packs.");
                }
            }

            msg.editMessage(em.build()).queue();

        } catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            ExceptionHandler.handleException(event, e, "PriceCommand.java");
        }
    }


}
