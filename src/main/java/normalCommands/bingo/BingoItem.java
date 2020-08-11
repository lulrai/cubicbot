package normalCommands.bingo;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CacheUtils;
import utils.Msg;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BingoItem extends Command {
    public BingoItem() {
        this.name = "bingoitem";
        this.aliases = new String[] { "bi" };
        this.category = new Category("Cubic Castles");
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String basePicURL = "http://cubiccastles.com/recipe_html/";

            Document doc = Jsoup.parse(CacheUtils.getCache("item"));

            Elements itemDiv = doc.select("div#CONTAINER > div.HIDE_CONTAINER");
            Elements itemList = itemDiv.get(2).select("tr > td");

            Msg.reply(event, "Starting to download pictures and creating a text file..");

            for(int i = 0; i < itemList.size(); i++){
                String itemName = itemList.get(i).text();
                String itemImage = basePicURL + itemList.get(i).select("img").attr("src");
                storeItem(i, itemName.replaceAll("[^0-9a-zA-Z' ]", ""), itemImage);
                Msg.reply(event, "ID: " + i + "\n" +
                        "Name: " + itemName);
                Thread.sleep(1000);
            }

            Msg.reply(event, "Finished downloading pictures and creating a text file.");
        } catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "ItemCommand.java");
        }
    }

    private void storeItem(int i, String itemName, String itemImage) throws IOException {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/cards/items").toUri());
        if (!guildDir.exists()) {
            guildDir.mkdirs();
        }
        File outFile = new File(guildDir, "itemPool.txt");
        File img = new File(guildDir, itemName+".png");
        ImageIO.write(ImageIO.read(new URL(itemImage)), "png", img);
        try {
            outFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Couldn't create " + outFile.getName() + " file at " + outFile.getAbsolutePath());
        }

        try (FileWriter fw = new FileWriter(outFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(i+","+itemName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
