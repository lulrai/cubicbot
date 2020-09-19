package normalCommands.bingo;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import utils.Msg;

import java.util.concurrent.TimeUnit;

public class Verify extends Command {
    public Verify() {
        this.name = "verify";
        this.aliases = new String[] { "v" };
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        boolean isFromBingo = event.getGuild().getCategoryById("756887929808224258").getTextChannels().parallelStream().anyMatch(c -> event.getTextChannel().getId().equals(c.getId()));
        if(!event.getGuild().getId().equals("240614697848537089") && !event.getGuild().getId().equals("705622006652993607") && !isFromBingo) return;

        if (event.getMessage().getMentionedUsers().isEmpty() || !(event.getMember().hasPermission(Permission.ADMINISTRATOR) ||
                (event.getAuthor().getId().equals("169122787099672577")
                        || event.getAuthor().getId().equals("222488511385698304")
                        || event.getAuthor().getId().equals("195621535703105536")))) return;

        User u = event.getMessage().getMentionedUsers().get(0);
        if(!GenerateBingo.bingoBoard.containsKey(u.getId())) {
            Msg.replyTimed(event, "The provided user does not have a bingo card. Please generate one, if needed.", 5, TimeUnit.SECONDS);
        }
        else{
            String[][] board = GenerateBingo.bingoBoard.get(u.getId()).getKey();
            int[] rowCount = new int[5], colCount = new int[5], diaCountLeft = new int[5], diaCountRight = new int[5];
            rowCount[2]++;
            colCount[2]++;
            diaCountLeft[2]++;
            diaCountRight[2]++;
            for(int i = 0; i < board.length; i++) {
                for(int j = 0; j < board.length; j++) {
                    if(RollCommand.chosenImages.contains(board[i][j])) {
                        rowCount[i]++;
                        colCount[j]++;
                        if(i == j && RollCommand.chosenImages.contains(board[i][j])) {
                            diaCountLeft[i]++;
                        }
                        if ((i + j) == (board.length - 1) && RollCommand.chosenImages.contains(board[i][j])) {
                            diaCountRight[j]++;
                        }
                    }
                }
            }

//            System.out.println(Arrays.toString(rowCount) + " " + Arrays.toString(colCount) + " " + Arrays.toString(diaCountLeft) + " " + Arrays.toString(diaCountRight));

            boolean[] dia = {true, true};
            boolean isComplete = false;
            for(int i = 0; i < rowCount.length; i++){
                if(rowCount[i] == 5 || colCount[i] == 5){
                    isComplete = true;
                    break;
                }
                if(diaCountLeft[i] == 0){
                    dia[0] = false;
                }
                if(diaCountRight[i] == 0){
                    dia[1] = false;
                }
            }

            if(isComplete || dia[0] || dia[1]) {
                Msg.reply(event, u.getAsMention() +"'s board is a valid BINGO.");
            }
            else {
                Msg.badTimed(event, "Invalid solution.", 5, TimeUnit.SECONDS);
            }
        }
    }
}
