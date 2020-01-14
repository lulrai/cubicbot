package modCommands;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import utils.*;

import java.util.function.Consumer;

public class UnmuteCommand extends Command {
    public UnmuteCommand() {
        this.name = "unmute";
        this.aliases = new String[]{"unmoot"};
        this.arguments = "@user";
        this.category = new Category("Moderation");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        if (event.getAuthor().isBot()) return;
        if (args.length < 1) {
            Msg.bad(event, "USAGE" + ": " + Constants.D_PREFIX + "unmute <@user>");
            return;
        }
        if (event.getMessage().getMentionedUsers().isEmpty() || event.getMessage().getMentionedUsers() == null) {
            ErrorHandling.EMPTY_MENTION_ERROR.error(event);
            return;
        }
        Member m = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
        Member auth = event.getGuild().getMember(event.getAuthor());
        if (UserPermission.isMod(event, event.getAuthor()) && !auth.hasPermission(Permission.MANAGE_PERMISSIONS)) {
            ErrorHandling.USER_PERMISSION_ERROR.error(event);
            return;
        }
        try {
            if (event.getTextChannel().getPermissionOverride(m) == null || !event.getTextChannel().getPermissionOverride(m).getDenied().contains(Permission.MESSAGE_WRITE)) {
                Msg.bad(event, "This user is not muted.");
                return;
            }
        } catch (PermissionException ex) {
            Msg.bad(event, "The bot doesn't have required permissions to perform this command. Missing Permission(s): " + ex.getPermission());
            return;
        }
        try {
            Member member = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
            String successful = "";


            if (member.getVoiceState().inVoiceChannel()) {
                try {
                    event.getGuild().deafen(member, false).complete();
                    event.getGuild().mute(member, false).complete();
                    successful = " both on text chat and voice chat";
                } catch (PermissionException e) {
                    successful = " but the bot doesn't have necessary permission to unmute the user on Voice Chat";
                }
            }
            final String success = successful;
            try {
                for (TextChannel chan : event.getGuild().getTextChannels()) {
                    PermissionOverride pr;
                    if ((pr = chan.getPermissionOverride(member)) != null) {
                        final Consumer<Throwable> throwableConsumer = t -> Msg.bad(event, "Failed to unmute the user.");
                        if ((pr.getAllowed().size() + pr.getDenied().size()) > 1 && pr.getDenied().size() > 1) {
                            if (pr.getDenied().contains(Permission.MESSAGE_WRITE)) {
                                event.getTextChannel().getPermissionOverride(member).getManager().clear(Permission.MESSAGE_WRITE).queue(v -> {
                                }, throwableConsumer);
                            }
                        } else {
                            chan.getPermissionOverride(member).delete().queue(v -> {
                            }, throwableConsumer);
                        }
                    }
                }
                if (MuteCommand.isMuted.get(event.getGuild()).get(member) != null) {
                    MuteCommand.isMuted.get(event.getGuild()).get(member).cancel(true);
                }
                Logger.logInChannel(event, "unmute", 0);
                Msg.reply(event, "@" + event.getMessage().getMentionedUsers().get(0).getName() + " " + "has been unmuted" + success + ".");
            } catch (PermissionException e) {
                Msg.bad(event, "The bot doesn't have necessary permission to mute the user. Requires Manage Channel permission.");
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(event, e, "UnmuteCommand.java");
        }
    }
}
