package commands.normalCommands.bingo;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.utils.Msg;
import commands.utils.UserPermission;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;

public class BingoClearCommand extends Command {
    public BingoClearCommand() {
        this.name = "bingoclear";
        this.aliases = new String[]{"bc"};
        this.category = new Category("Bingo");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getGuild().getCategoryById("756887929808224258") == null) return;

        boolean isFromBingo = event.getGuild().getCategoryById("756887929808224258").getTextChannels().parallelStream().anyMatch(c -> event.getTextChannel().getId().equals(c.getId()));
        if(!event.getGuild().getId().equals("705622006652993607") && !event.getGuild().getId().equals("240614697848537089") && !isFromBingo) return;

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        Path bingoCardsMarked = workingDir.resolve("db/cards/markedbingocards");
        Path bingoCards = workingDir.resolve("db/cards/bingocards");

        if(!event.getArgs().isEmpty() && (UserPermission.isBotOwner(event.getAuthor())
                || event.getMember().getRoles().parallelStream().anyMatch(r -> (r.getId().equals("909922595367968850"))))){
            if(event.getArgs().trim().equalsIgnoreCase("rolls")){
                File guildDir = new File(workingDir.resolve("db/cards").toUri());
                if (!guildDir.exists()) return;
                File outFile = new File(guildDir, "chosenItems.txt");
                if(!outFile.exists()) return;
                outFile.delete();
                RollCommand.chosenImages.clear();
                Msg.reply(event, "Cleared the database of all rolled bingo items!");
            }
            else if(event.getArgs().trim().equalsIgnoreCase("marks")){
                GenerateBingo.bingoBoard.clear();
                if(bingoCardsMarked.toFile().exists()) {
                    for (File f : bingoCardsMarked.toFile().listFiles()) {
                        f.delete();
                    }
                }
                if(bingoCards.toFile().exists()){
                    for(File f : bingoCards.toFile().listFiles()){
                        if(f.getName().endsWith(".png")) {
                            try {
                                copyFile(f, new File(bingoCardsMarked.resolve(f.getName()).toUri()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                GenerateBingo.initializeBingoCards();
                Msg.reply(event,"Successfully cleared all the cards of markings!");
            }
            else if(event.getArgs().trim().equalsIgnoreCase("cards")){
                GenerateBingo.bingoBoard.clear();
                if(bingoCardsMarked.toFile().exists()) {
                    for (File f : bingoCardsMarked.toFile().listFiles()) {
                        f.delete();
                    }
                }
                if(bingoCards.toFile().exists()) {
                    for (File f : bingoCards.toFile().listFiles()) {
                        f.delete();
                    }
                }
                Msg.reply(event,"Successfully cleared all the cards!");
            }
        } else {
            if(!GenerateBingo.bingoBoard.containsKey(event.getAuthor().getId())) {
                Msg.bad(event, "You do not have a bingo card. Please generate one using `"+ event.getClient().getPrefix() +"cgen` command.");
            }
            else{
                File clear = bingoCards.resolve(event.getAuthor().getId()+".png").toFile();
                File used = bingoCardsMarked.resolve(event.getAuthor().getId()+".png").toFile();
                used.delete();

                try{
                    copyFile(clear, used);
                }catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    GenerateBingo.bingoBoard.put(event.getAuthor().getId(), new AbstractMap.SimpleEntry<>(GenerateBingo.bingoBoard.get(event.getAuthor().getId()).getKey(), ImageIO.read(clear)));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Msg.reply(event,"Successfully cleared all the markings on your board!");

            }
        }
    }

    private static void copyFile(File in, File out) throws IOException {
        FileChannel inChannel = new
                FileInputStream(in).getChannel();
        FileChannel outChannel = new
                FileOutputStream(out).getChannel();
        inChannel.transferTo(0, inChannel.size(),
                    outChannel);
        inChannel.close();
        outChannel.close();
    }
}
