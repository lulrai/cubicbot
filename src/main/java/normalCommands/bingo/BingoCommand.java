package normalCommands.bingo;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import utils.Constants;
import utils.Msg;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
                        Msg.reply(event, event.getMessage().getMentionedMembers().get(0).getAsMention()+" doesn't have a bingo card.");
                        return;
                    }
                    ImageIO.write(GenerateBingo.bingoBoard.get(event.getMessage().getMentionedUsers().get(0).getId()).getValue(), "png", os);

                    InputStream is = new ByteArrayInputStream(os.toByteArray());

                    event.getTextChannel().sendFile(is, "bingoboard.png").queue();
                    is.close();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            if(!GenerateBingo.bingoBoard.containsKey(event.getAuthor().getId())){
                Msg.reply(event, event.getMember().getAsMention()+", you don't have a bingo card. Please generate one using the `"+ Constants.D_PREFIX +"cgen` command.");
                return;
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(GenerateBingo.bingoBoard.get(event.getAuthor().getId()).getValue(), "png", os);

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
