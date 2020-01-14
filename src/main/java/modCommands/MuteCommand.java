package modCommands;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import utils.*;

import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MuteCommand extends Command {
    static Map<Guild, Map<Member, ScheduledFuture<?>>> isMuted = new HashMap<>();

    public MuteCommand() {
        this.name = "mute";
        this.aliases = new String[]{"moot"};
        this.arguments = "@user";
        this.category = new Category("Moderation");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        if (args.length < 1) {
            Msg.bad(event, "USAGE" + ": " + Constants.D_PREFIX + "mute <@user> <time(s)>");
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
            if (event.getTextChannel().getPermissionOverride(m) != null && event.getTextChannel().getPermissionOverride(m).getDenied().contains(Permission.MESSAGE_WRITE)) {
                Msg.bad(event, "This user is already muted.");
                return;
            }
        } catch (PermissionException ex) {
            Msg.bad(event, "The bot doesn't have required permissions to perform this command. Missing Permission(s): " + ex.getPermission());
            return;
        }


        try {
            int time = 0;
            if (args.length == 2) {
                try {
                    time = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    Msg.bad(event, "Invalid time, please enter a valid time in seconds.");
                    return;
                }
            }

            Member member = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
            String successful = "";


            if (member.getVoiceState().inVoiceChannel()) {
                try {
                    event.getGuild().deafen(member, true).queue();
                    event.getGuild().mute(member, true).queue();
                    successful = " both on text chat and voice chat";
                } catch (PermissionException e) {
                    successful = " but the bot doesn't have necessary permission to mute the user on Voice Chat";
                }
            }
            final int unmuteTime = time;
            try {
                for (TextChannel chan : event.getGuild().getTextChannels()) {
                    final Consumer<Throwable> throwableConsumer = t -> Msg.bad(event, "Failed to mute the user.");
                    if (chan.getPermissionOverride(member) != null) {
                        chan.getPermissionOverride(member).getManager().deny(Permission.MESSAGE_WRITE).queue(v -> {
                        }, throwableConsumer);
                    } else {
                        chan.createPermissionOverride(member).setDeny(Permission.MESSAGE_WRITE).queue(v -> {
                        }, throwableConsumer);
                    }
                }

                if (unmuteTime != 0) {
                    Map<Member, ScheduledFuture<?>> mp = new HashMap<>();
                    mp.put(member, unmute(event, unmuteTime));
                    isMuted.put(event.getGuild(), mp);
                    Msg.reply(event, "@" + event.getMessage().getMentionedUsers().get(0).getName() + " " + "has been muted" + successful + " for " + ConversionUtils.secondsToTime(unmuteTime) + ".");
                } else {
                    Msg.reply(event, "@" + event.getMessage().getMentionedUsers().get(0).getName() + " " + "has been muted" + successful + ".");
                }
                Logger.logInChannel(event, "mute", unmuteTime);
            } catch (PermissionException e) {
                Msg.bad(event, "The bot doesn't have necessary permission to mute the user. Requires Manage Channel permission.");
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(event, e, "MuteCommand.java");
        }
    }

    private ScheduledFuture<?> unmute(CommandEvent event, int unmuteTime) {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(2);
        return exec.schedule(() -> new UnmuteCommand().execute(event), unmuteTime, TimeUnit.SECONDS);
    }
}
