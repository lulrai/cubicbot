package cubicCastles;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import info.debatty.java.stringsimilarity.JaroWinkler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import utils.CacheUtils;
import utils.Constants;

import java.awt.*;

public class OldPriceCommand extends Command {

    public OldPriceCommand() {
        this.name = "pricepack";
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
                title = "Price Packs Info";
                String description = post.ownText();
                String lastUpdate = post.getElementsByTag("i").first().ownText();
                StringBuilder packNames = new StringBuilder();
                for (Element el : post.getElementsByClass("SpoilerTitle")) {
                    packNames.append("- ").append(el.getElementsByTag("span").first().ownText()).append("\n");
                }

                em.setTitle(title);
                em.setDescription(description);
                em.addField("Usage", Constants.D_PREFIX + "price <packName>", true);
                em.addField("Last Update", lastUpdate, true);
                em.addBlankField(false);
                em.addField("Pack Names", packNames.toString(), false);
                em.addBlankField(false);
                em.addField("Help Out", "If you want to help out with the prices, please fill out the respective form below and thank you. :D\n"
                                + "**Hats, Wings, Accessories, Critter Suits & Clothes:**\n"
                                + "https://goo.gl/forms/kri6AuDFLgbjIam33\n"
                                + "**Dungeon Pack, Race Pack & Wigs:**\n"
                                + "https://goo.gl/forms/vHZmAtn9MLjVBrWb2\n"
                                + "**New Year, Fool's Day, Easter & Summer:**\n"
                                + "https://goo.gl/forms/3duBLZ38inGim2ll1\n"
                                + "**Valentine's Day & Halloween:**\n"
                                + "https://goo.gl/forms/zPUEgXBD3w8iEHGg2\n"
                                + "**Thanksgiving Day & Christmas**\n"
                                + "https://goo.gl/forms/Oq5ubn7lYsdi0dCn1\n"
                                + "**Quest, Pets, Cars, Wands & Farm:**\n"
                                + "https://goo.gl/forms/wEDCNx7feSmf89md2\n"
                                + "**Easter Egg Hunt 2018:**\n"
                                + "https://goo.gl/forms/cC5zuqF48qC4sIZv2\n"
                                + "**Stamps, Sentries & Dehydrated Cubes**\n"
                                + "https://goo.gl/forms/xbhlGhvSABFxmXe32"
                        , false);


                em.setFooter("Created by Superxteme and Other Qbees", null);
            } else {
                String priceImage = "";
                String givenArg = event.getArgs().trim();

                for (Element el : post.getElementsByClass("SpoilerTitle")) {
                    String name = el.getElementsByTag("span").first().ownText();
                    if (checkStrings(name, givenArg) ? checkStrings(name, givenArg) : checkSimilarStrings(name, givenArg)) {
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
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "PriceCommand.java");
        }
    }

    public static boolean isSimilar(String str1,  String str2) {
        final JaroWinkler jw = new JaroWinkler();
        final double similarity = jw.similarity(str1, str2);
        return similarity > 0.8;
    }

    public static boolean checkStrings(final String str1, final String str2) {
        if (str1.equalsIgnoreCase(str2)) {
            return true;
        }
        return str1.replaceAll("[^A-Za-z0-9]", "").equalsIgnoreCase(str2);
    }

    public static boolean checkSimilarStrings(final String str1, final String str2) {
        if (str1.toLowerCase().contains(str2.toLowerCase())) {
            return true;
        }
        if (str1.toLowerCase().startsWith(str2.toLowerCase())) {
            return true;
        }
        return isSimilar(str1, str2);
    }

}
