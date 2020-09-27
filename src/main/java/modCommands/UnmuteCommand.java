package modCommands;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import utils.*;

import java.util.Objects;
import java.util.function.Consumer;

public class UnmuteCommand extends Command {
    public UnmuteCommand() {
        this.name = "unmute";
        this.aliases = new String[]{"unmoot"};
        this.arguments = "<@user>";
        this.category = new Category("Moderation");
        this.help = "Unmutes a mentioned-muted user.";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        if (event.getAuthor().isBot()) return;
        if (args.length < 1) {
            Msg.bad(event, "USAGE" + ": " + Constants.D_PREFIX + "unmute <@user>");
            return;
        }
        if (event.getMessage().getMentionedUsers().isEmpty()) {
            ErrorHandling.EMPTY_MENTION_ERROR.error(event);
            return;
        } else {
            event.getMessage().getMentionedUsers();
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
            unmute(m, event.getMessage().getMember(), event.getGuild(), event.getTextChannel());
        } catch (Exception e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "UnmuteCommand.java");
        }
    }

    public static void unmute(Member toMember, Member fromMember, Guild guild, TextChannel mutedChannel) {
        String successful = "";

        if (toMember.getVoiceState() != null && toMember.getVoiceState().inVoiceChannel()) {
            try {
                guild.deafen(toMember, false).complete();
                guild.mute(toMember, false).complete();
                successful = " both on text chat and voice chat";
            } catch (PermissionException e) {
                successful = " but the bot doesn't have necessary permission to unmute the user on Voice Chat";
            }
        }
        final String success = successful;
        try {
            for (TextChannel chan : guild.getTextChannels()) {
                PermissionOverride pr;
                if ((pr = chan.getPermissionOverride(toMember)) != null) {
                    final Consumer<Throwable> throwableConsumer = t -> Msg.bad(mutedChannel, "Failed to unmute the user.");
                    if ((pr.getAllowed().size() + pr.getDenied().size()) > 1 && pr.getDenied().size() > 1) {
                        if (pr.getDenied().contains(Permission.MESSAGE_WRITE)) {
                            Objects.requireNonNull(mutedChannel.getPermissionOverride(toMember)).getManager().clear(Permission.MESSAGE_WRITE).queue(v -> {
                            }, throwableConsumer);
                        }
                    } else {
                        Objects.requireNonNull(chan.getPermissionOverride(toMember)).delete().queue(v -> {
                        }, throwableConsumer);
                    }
                }
            }
            if (MuteCommand.isMuted.get(guild).get(toMember) != null) {
                MuteCommand.isMuted.get(guild).get(toMember).cancel(true);
            }
            Logger.logInChannel(toMember, fromMember, guild, "unmute", 0);
            Msg.reply(mutedChannel, "@" + toMember.getUser().getName() + " " + "has been unmuted" + success + ".");
        } catch (PermissionException e) {
            Msg.bad(mutedChannel, "The bot doesn't have necessary permission to mute the user. Requires Manage Channel permission.");
        }
    }
}
