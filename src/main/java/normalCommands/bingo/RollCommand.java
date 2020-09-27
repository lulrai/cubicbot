package normalCommands.bingo;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import core.Cubic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import utils.Msg;

import java.awt.*;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

public class RollCommand extends Command {
    public static ArrayList<String> chosenImages = new ArrayList<>();

    public RollCommand() {
        this.name = "roll";
        this.aliases = new String[] { "r" };
        this.category = new Category("Bingo");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getGuild().getCategoryById("756887929808224258") == null) return;

        boolean isFromBingo = event.getGuild().getCategoryById("756887929808224258").getTextChannels().parallelStream().anyMatch(c -> event.getTextChannel().getId().equals(c.getId()));
        if(!event.getGuild().getId().equals("240614697848537089") && !event.getGuild().getId().equals("705622006652993607") && !isFromBingo) return;

        if(!(event.getMember().hasPermission(Permission.ADMINISTRATOR) ||
                (event.getAuthor().getId().equals("169122787099672577")
                        || event.getAuthor().getId().equals("222488511385698304")
                        || event.getAuthor().getId().equals("195621535703105536")
                        || event.getAuthor().getId().equals("643903506750898215")))) return;

        try {
            if(BingoItem.smallItemPool.isEmpty()){
                Msg.bad(event, "The item pool is empty. Please generate one first.");
                return;
            }

            Path workingDir = Paths.get(System.getProperty("user.dir"));
            File guildDir = new File(workingDir.resolve("db/cards/items").toUri());

            Random r = new Random();

            String itemName = BingoItem.smallItemPool.get(r.nextInt(BingoItem.smallItemPool.size()));
            if(chosenImages.size() == BingoItem.smallItemPool.size()){
                Msg.bad(event, "This is literally impossible. Not sure how this managed to happen.." +
                        " but all the items from the pool is rolled yet no bingo has been achieved. Clear the rolls?");
                return;
            }
            while(chosenImages.contains(itemName)) {
                itemName = BingoItem.smallItemPool.get(r.nextInt(BingoItem.smallItemPool.size()));
            }

            storeItem(itemName);

            File bingoPool = new File(guildDir, itemName+".png");
            Message image = Cubic.getJDA().getTextChannelById("740309750369091796").sendFile(bingoPool, URLEncoder.encode(bingoPool.getName(),"utf-8")).complete();

            EmbedBuilder em = new EmbedBuilder();
            em.setColor(Color.YELLOW);
            em.setTitle("BINGO Item");
            em.setDescription(itemName);
            em.setImage(image.getAttachments().get(0).getUrl());
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

    private void storeItem(String item){
        chosenImages.add(item);

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/cards").toUri());
        if (!guildDir.exists()) {
            guildDir.mkdirs();
        }
        File outFile = new File(guildDir, "chosenItems.txt");
        try {
            outFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Couldn't create " + outFile.getName() + " file at " + outFile.getAbsolutePath());
        }

        try (FileWriter fw = new FileWriter(outFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(item);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initializeChosenCache(){
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/cards").toUri());
        if (!guildDir.exists()) return;
        File outFile = new File(guildDir, "chosenItems.txt");
        if(!outFile.exists()) return;

        try {
            BufferedReader br = new BufferedReader(new FileReader(outFile));
            String line;
            while ((line = br.readLine()) != null) {
                chosenImages.add(line);
            }
            br.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
