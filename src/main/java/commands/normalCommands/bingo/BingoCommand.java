package commands.normalCommands.bingo;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.utils.Msg;
import commands.utils.UserPermission;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BingoCommand extends Command {
    public BingoCommand() {
        this.name = "card";
        this.aliases = new String[]{"bingocard", "cardinfo"};
        this.category = new Category("Bingo");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getGuild().getCategoryById("756887929808224258") == null) return;

        boolean isFromBingo = event.getGuild().getCategoryById("756887929808224258").getTextChannels().parallelStream().anyMatch(c -> event.getTextChannel().getId().equals(c.getId()));
        if(!event.getGuild().getId().equals("240614697848537089") && !event.getGuild().getId().equals("705622006652993607") && !isFromBingo) return;

        if((!event.getArgs().isEmpty() && !event.getMessage().getMentionedUsers().isEmpty()) && (UserPermission.isBotOwner(event.getAuthor())
                || event.getMember().getRoles().parallelStream().anyMatch(r -> (r.getId().equals("909922595367968850"))))){
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
                Msg.reply(event, event.getMember().getAsMention()+", you don't have a bingo card. Please generate one using the `"+ event.getClient().getPrefix() +"cgen` command.");
                return;
            }
            if(!event.getArgs().isEmpty() && event.getArgs().trim().matches("\\b[A-Ea-e]([1-5])\\b")){
                String[][] board = GenerateBingo.bingoBoard.get(event.getAuthor().getId()).getKey();
                int row = event.getArgs().toLowerCase().trim().charAt(0)-97;
                int col = Integer.parseInt(String.valueOf(event.getArgs().toLowerCase().trim().charAt(1)))-1;
                Msg.reply(event, event.getMember().getAsMention()+", the item is `"+board[row][col]+"` at the spot `" + event.getArgs().trim() + "`.");
            }
            else {
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
}
