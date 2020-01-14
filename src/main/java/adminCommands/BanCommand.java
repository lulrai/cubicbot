package adminCommands;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import utils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BanCommand extends Command {
    public static Map<User, ScheduledFuture<?>> isBanned = new HashMap<>();
    private EventWaiter waiter;

    public BanCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "ban";
        this.aliases = new String[]{"banhammer"};
        this.arguments = "<@user> [time]";
        this.category = new Category("Administrator");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().length() < 1) {
            Msg.bad(event, "USAGE" + ": " + Constants.D_PREFIX + "ban <@user> <time(s)>");
            return;
        }
        if (event.getMessage().getMentionedUsers().isEmpty() || event.getMessage().getMentionedUsers() == null) {
            ErrorHandling.EMPTY_MENTION_ERROR.error(event);
            return;
        }
        User target = event.getMessage().getMentionedUsers().get(0);
        Member auth = event.getGuild().getMember(event.getAuthor());
        if (UserPermission.isAdmin(event, event.getAuthor()) && !auth.hasPermission(Permission.BAN_MEMBERS)) {
            ErrorHandling.USER_PERMISSION_ERROR.error(event);
            return;
        }
        try {
            Guild.Ban first = event.getGuild().retrieveBan(target).complete();
            if (first != null) {
                Msg.bad(event, "This user is already banned.");
                return;
            }
        } catch (PermissionException ex) {
            Msg.bad(event, "The bot doesn't have required permissions to perform this command. Missing Permission(s): " + ex.getPermission());
            return;
        }

        Msg.reply(event, "Are you sure you want to ban `" + target.getName() + "#" + target.getDiscriminator() + "`? \nType `Yes`, `No` or `Cancel`");

        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    String message = e.getMessage().getContentRaw();
                    if (message.equalsIgnoreCase("yes")) {
                        String[] args = event.getArgs().split(" ");

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

                            final int unBan = time;
                            try {
                                e.getGuild().ban(target, 7).queue((v) -> {
                                    if (unBan != 0) {
                                        isBanned.put(target, unban(event, unBan));
                                        Msg.reply(event, "@" + event.getMessage().getMentionedUsers().get(0).getName() + " " + "has been banned for " + ConversionUtils.secondsToTime(unBan) + ".");
                                    } else {
                                        Msg.reply(event, "@" + event.getMessage().getMentionedUsers().get(0).getName() + " " + "has been banned.");
                                    }
                                }, (t) -> Msg.bad(event, "Failed to ban the user."));

                                Logger.logInChannel(event, "ban", unBan);
                                //391330613858271252
                            } catch (PermissionException ex) {
                                Msg.bad(event, "The bot doesn't have necessary permission to ban the user.");
                            }
                        } catch (Exception ex) {
                            ExceptionHandler.handleException(event, ex, "BanCommand.java");
                        }
                    } else if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("cancel")) {
                        Msg.bad(event, "User was NOT banned.");
                    } else {
                        Msg.bad(event, "Invalid choice, ban cancelled.");
                    }
                },
                10, TimeUnit.SECONDS, () -> event.reply("Sorry, you took too long."));
    }

    private ScheduledFuture<?> unban(CommandEvent event, int unmuteTime) {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(2);
        return exec.schedule(() -> {
            new UnbanCommand().execute(event);
            Logger.logInChannel(event, "unban", unmuteTime);
        }, unmuteTime, TimeUnit.SECONDS);
    }
}
