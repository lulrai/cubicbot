package normalCommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import normalCommands.GenerateBingo;
import utils.Constants;
import utils.Msg;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BingoCommand extends Command {
    public BingoCommand() {
        this.name = "card";
        this.aliases = new String[]{"bingocard"};
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(!event.getGuild().getId().equals("240614697848537089") && !event.getGuild().getId().equals("365932526297939971") && !event.getGuild().getId().equals("705622006652993607")) return;

        if(!event.getArgs().isEmpty() && (event.getMember().hasPermission(Permission.ADMINISTRATOR) ||
                (event.getAuthor().getId().equals("169122787099672577")
                        || event.getAuthor().getId().equals("222488511385698304")
                        || event.getAuthor().getId().equals("195621535703105536")))){
            if(!event.getMessage().getMentionedUsers().isEmpty()){
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    if(!GenerateBingo.bingoBoard.containsKey(event.getMessage().getMentionedUsers().get(0).getId())){
                        Msg.reply(event, event.getMessage().getMentionedMembers().get(0).getAsMention()+" doesn't have a bingo card. Please generate one using `"+ Constants.D_PREFIX +"cgen <@user>` command.");
                        return;
                    }
                    ImageIO.write(GenerateBingo.bingoBoard.get(event.getMessage().getMentionedUsers().get(0).getId()), "png", os);

                    InputStream is = new ByteArrayInputStream(os.toByteArray());

                    event.getTextChannel().sendFile(is, "bingoboard.png").queue();
                    is.close();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(event.getArgs().trim().equalsIgnoreCase("clear")){
                GenerateBingo.bingoBoard.clear();
                Path workingDir = Paths.get(System.getProperty("user.dir"));
                File guildDir = new File(workingDir.resolve("db/cards").toUri());
                if(guildDir.exists()) {
                    for (File f : guildDir.listFiles()) {
                        f.delete();
                    }
                }
                Msg.reply(event,"Successfully cleared the cards!");
            }
        }
        else{
            if(!GenerateBingo.bingoBoard.containsKey(event.getAuthor().getId())){
                Msg.reply(event, event.getMember().getAsMention()+", you don't have a bingo card. Please generate one using `"+ Constants.D_PREFIX +"cgen` command.");
                return;
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(GenerateBingo.bingoBoard.get(event.getAuthor().getId()), "png", os);

                InputStream is = new ByteArrayInputStream(os.toByteArray());

                event.getTextChannel().sendFile(is, "bingoboard.png").queue();
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
