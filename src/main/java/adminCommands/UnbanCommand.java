package adminCommands;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.PermissionException;
import utils.Constants;
import utils.ErrorHandling;
import utils.Msg;
import utils.UserPermission;

public class UnbanCommand extends Command {
    public UnbanCommand() {
        this.name = "unban";
        this.aliases = new String[]{};
        this.arguments = "@user";
        this.category = new Category("Administrator");
        this.ownerCommand = false;
    }

    @Override
    public void execute(CommandEvent event) {
        if (event.getArgs().length() < 1) {
            Msg.bad(event, "USAGE" + ": " + Constants.D_PREFIX + "unban <userId>");
            return;
        }
        User target = event.getJDA().getUserById(event.getArgs().trim()) != null ? event.getJDA().getUserById(event.getArgs().trim()) : event.getMessage().getMentionedUsers().get(0);
        Member auth = event.getGuild().getMember(event.getAuthor());
        if (UserPermission.isAdmin(event, event.getAuthor()) && !auth.hasPermission(Permission.BAN_MEMBERS)) {
            ErrorHandling.USER_PERMISSION_ERROR.error(event);
            return;
        }
        try {
            Guild.Ban first = event.getGuild().retrieveBan(target).complete();
            if (first == null) {
                Msg.bad(event, "This user is not banned.");
                return;
            }
        } catch (PermissionException ex) {
            Msg.bad(event, "The bot doesn't have required permissions to perform this command. Missing Permission(s): " + ex.getPermission());
            return;
        }
        unban(event.getGuild().getMember(target), event.getGuild(), event.getTextChannel(), event.getMessage().getContentRaw());
    }

    public static void unban(Member toMember, Guild guild, TextChannel channel, String command) {
        try {
            try {
                guild.unban(toMember.getUser()).queue(v -> Msg.reply(channel, "@" + toMember.getUser().getName() + " has been unbanned."), t -> Msg.bad(channel, "Failed to unban the user."));
            }
            catch (PermissionException ex) {
                Msg.bad(channel, "The bot doesn't have necessary permission to mute the user. Requires Administrator permission.");
            }
        }
        catch (Exception e) {
            ExceptionHandler.handleException(e, command, "UnbanCommand.java");
        }
    }

}
