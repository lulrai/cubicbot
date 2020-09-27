package modCommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import utils.ErrorHandling;
import utils.Msg;
import utils.UserPermission;

public class PollCommand extends Command {
    private final static int REGIONAL_A = "\uD83C\uDDE6".codePointAt(0);

    public PollCommand() {
        this.name = "poll";
        this.aliases = new String[]{"polls"};
        this.arguments = "<topic> | [option1] | [option2]..";
        this.category = new Category("Moderation");
        this.help = "Creates a poll with the provided options or just the topic. Separate by using `|`.";
        this.guildOnly = false;
    }

    private static String formatQuestion(String str) {
        return "\uD83D\uDDF3 **" + str + "**";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (UserPermission.isMod(event, event.getAuthor())) {
            ErrorHandling.USER_PERMISSION_ERROR.error(event);
            return;
        }
        if (event.getArgs().length() < 1) {
            Msg.bad(event.getChannel(), "Please make sure there is at least one argument.");
            return;
        }
        String arg = event.getMessage().getContentRaw().split(" ", 2)[1].trim();
        String[] parts = arg.split("\\|");
        if (parts.length == 1) {
            event.getChannel().sendMessage(formatQuestion(arg)).queue(m ->
            {
                m.addReaction("\uD83D\uDC4D").queue();
                m.addReaction("\uD83D\uDC4E").queue();
            });
        } else {
            StringBuilder builder = new StringBuilder(formatQuestion(parts[0]));
            for (int i = 1; i < parts.length; i++) {
                String r = String.copyValueOf(Character.toChars(REGIONAL_A + i - 1));
                builder.append("\n").append(r).append(" ").append(parts[i].trim());
            }
            event.getChannel().sendMessage(builder.toString()).queue(m ->
            {
                for (int i = 1; i < parts.length; i++)
                    m.addReaction(String.copyValueOf(Character.toChars(REGIONAL_A + i - 1))).queue();
            });
        }
    }

}
