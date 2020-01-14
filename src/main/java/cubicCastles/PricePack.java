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
import utils.Constants;

import java.awt.*;

public class PricePack extends Command {
    public PricePack() {
        this.name = "pricepack";
        this.aliases = new String[]{"packs", "pack"};
        this.category = new Category("Cubic Castles");
        this.cooldown = 1;
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        try {
            String title;

            em.setColor(Color.CYAN);

            Document doc = Jsoup.parse(CacheUtils.getCache("price"));

            Element post = doc.getElementsByClass("Message").first();

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
            event.getTextChannel().sendMessage(em.build()).queue();

        } catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            ExceptionHandler.handleException(event, e, "PricePack.java");
        }
    }

}
