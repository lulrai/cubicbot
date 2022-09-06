package commands.normalCommands.bingo;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import commands.core.Cubic;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import commands.utils.Msg;
import commands.utils.UserPermission;

import java.util.concurrent.TimeUnit;

public class Verify extends Command {
    private EventWaiter waiter;

    public Verify() {
        this.name = "verify";
        this.aliases = new String[] { "v" };
        this.category = new Category("Bingo");
        this.ownerCommand = false;
        this.waiter = Cubic.getWaiter();
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getGuild().getCategoryById("756887929808224258") == null) return;

        boolean isFromBingo = event.getGuild().getCategoryById("756887929808224258").getTextChannels().parallelStream().anyMatch(c -> event.getTextChannel().getId().equals(c.getId()));
        if(!event.getGuild().getId().equals("240614697848537089") && !event.getGuild().getId().equals("705622006652993607") && !isFromBingo) return;

        if (event.getMessage().getMentionedUsers().isEmpty() ||
                !(UserPermission.isBotOwner(event.getAuthor())
                        || event.getMember().getRoles().parallelStream().anyMatch(r -> (r.getId().equals("909922595367968850") || r.getId().equals("756887929019957815")))))
            return;


        Message m = Msg.replyRet(event, "Validating bingo card of user " + event.getMessage().getMentionedUsers().get(0).getName() + "..");
        new ButtonMenu.Builder()
                .setDescription("Pick one of the options:\n" +
                        ":one: Normal\n" +
                        ":two: Blackout")
                .setEventWaiter(waiter)
                .addChoices(BingoItem.getEmojis(2))
                .addChoice(EmojiManager.getForAlias("x").getUnicode())
                .setUsers(event.getAuthor())
                .setTimeout(20, TimeUnit.SECONDS)
                .setColor(event.getMember().getColor())
                .setAction(em -> {
                    String option = EmojiParser.parseToAliases(em.getEmoji());
                    switch (option.trim()){
                        case ":one:" : {
                            m.delete().queue();
                            User u = event.getMessage().getMentionedUsers().get(0);
                            if(!GenerateBingo.bingoBoard.containsKey(u.getId())) {
                                Msg.replyTimed(event, "The provided user does not have a bingo card. Please generate one, if needed.", 5, TimeUnit.SECONDS);
                            }
                            else {
                                String[][] board = GenerateBingo.bingoBoard.get(u.getId()).getKey();
                                int[] rowCount = new int[5], colCount = new int[5], diaCountLeft = new int[5], diaCountRight = new int[5];
                                rowCount[2]++;
                                colCount[2]++;
                                diaCountLeft[2]++;
                                diaCountRight[2]++;
                                for (int i = 0; i < board.length; i++) {
                                    for (int j = 0; j < board.length; j++) {
                                        if (RollCommand.chosenImages.contains(board[i][j])) {
                                            rowCount[i]++;
                                            colCount[j]++;
                                            if (i == j && RollCommand.chosenImages.contains(board[i][j])) {
                                                diaCountLeft[i]++;
                                            }
                                            if ((i + j) == (board.length - 1) && RollCommand.chosenImages.contains(board[i][j])) {
                                                diaCountRight[j]++;
                                            }
                                        }
                                    }
                                }

                                boolean[] dia = {true, true};
                                boolean isComplete = false;
                                for (int i = 0; i < rowCount.length; i++) {
                                    if (rowCount[i] == 5 || colCount[i] == 5) {
                                        isComplete = true;
                                        break;
                                    }
                                    if (diaCountLeft[i] == 0) {
                                        dia[0] = false;
                                    }
                                    if (diaCountRight[i] == 0) {
                                        dia[1] = false;
                                    }
                                }

                                if (isComplete || dia[0] || dia[1]) {
                                    Msg.reply(event, u.getAsMention() + "'s board is a valid BINGO.");
                                } else {
                                    Msg.badTimed(event, "Invalid solution.", 5, TimeUnit.SECONDS);
                                }
                            }
                            break;
                        }
                        case ":two:" : {
                            m.delete().queue();
                            User u = event.getMessage().getMentionedUsers().get(0);
                            if(!GenerateBingo.bingoBoard.containsKey(u.getId())) {
                                Msg.replyTimed(event, "The provided user does not have a bingo card. Please generate one, if needed.", 5, TimeUnit.SECONDS);
                            }
                            else {
                                int count = 1;
                                String[][] board = GenerateBingo.bingoBoard.get(u.getId()).getKey();
                                for (String[] strings : board) {
                                    for (int j = 0; j < board.length; j++) {
                                        if (RollCommand.chosenImages.contains(strings[j])) {
                                            count++;
                                        }
                                    }
                                }
                                if (count == 25) {
                                    Msg.reply(event, u.getAsMention() + "'s board is a valid blackout BINGO.");
                                } else {
                                    Msg.badTimed(event, "Invalid solution.", 5, TimeUnit.SECONDS);
                                }
                            }
                            break;
                        }
                        default : {
                            m.delete().queue();
                            Msg.reply(event.getTextChannel(), event.getAuthor().getAsMention()+" Cancelled choosing an option.");
                            break;
                        }
                    }
                })
                .setFinalAction(me -> {}).build().display(m);
    }
}
