package modCommands;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import utils.Constants;
import utils.ErrorHandling;
import utils.Msg;
import utils.UserPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClearCommand extends Command {
    public ClearCommand() {
        this.name = "clear";
        this.aliases = new String[]{"clean"};
        this.arguments = "<@user> <num>";
        this.category = new Category("Moderation");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!event.getGuild().getMember(event.getJDA().getSelfUser()).hasPermission(Permission.MESSAGE_MANAGE)) {
            ErrorHandling.BOT_PERMISSION_ERROR.error(event);
            return;
        }
        if (!event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_PERMISSIONS) && UserPermission.isMod(event, event.getAuthor())) {
            ErrorHandling.USER_PERMISSION_ERROR.error(event);
            return;
        }

        int amt;
        String[] args = event.getArgs().split(" ");
        if (args.length < 1) {
            amt = 100;
            event.getMessage().delete().complete();
            event.getChannel().getHistory().retrievePast(amt).queue(messages -> {
                List<Message> toClean = messages;
                toClean.remove(event.getMessage());
                if (toClean.isEmpty()) {
                    Msg.bad(event, event.getAuthor().getName() + " No messages found matching the given criteria!");
                    return;
                }
                try {
                    if (toClean.size() == 1)
                        toClean.get(0).delete().queue(v -> Msg.normalTimedMessage(event, "@" + event.getAuthor().getName() + " " + "cleared " + toClean.size() + " messages.", 5, TimeUnit.SECONDS));
                    else
                        ((TextChannel) event.getChannel()).deleteMessages(toClean).queue(v -> Msg.normalTimedMessage(event, "@" + event.getAuthor().getName() + " " + "cleared " + toClean.size() + " messages.", 5, TimeUnit.SECONDS));
                } catch (IllegalArgumentException ex) {
                    Msg.bad(event, "Cannot delete messages older than 2 weeks.");
                }
            });
        } else if (args.length == 1) {
            if (!event.getMessage().getMentionedUsers().isEmpty()) {
                amt = 100;
                User u = event.getMessage().getMentionedUsers().get(0);
                event.getMessage().delete().complete();
                event.getTextChannel().getHistory().retrievePast(amt).queue(messages -> {
                    List<Message> toClean = new ArrayList<Message>();
                    for (Message m : messages) {
                        if (m.getAuthor().equals(u)) {
                            toClean.add(m);
                        }
                    }
                    toClean.remove(event.getMessage());
                    if (toClean.isEmpty()) {
                        Msg.bad(event, event.getAuthor().getName() + " No messages found matching the given criteria!");
                        return;
                    }
                    try {
                        if (toClean.size() == 1)
                            toClean.get(0).delete().queue(v -> Msg.normalTimedMessage(event, "@" + event.getAuthor().getName() + " " + "cleared " + toClean.size() + " messages by" + " @" + event.getMessage().getMentionedUsers().get(0).getName() + ".", 5, TimeUnit.SECONDS));
                        else
                            ((TextChannel) event.getChannel()).deleteMessages(toClean).queue(v -> Msg.normalTimedMessage(event, "@" + event.getAuthor().getName() + " " + "cleared " + toClean.size() + " messages by" + " @" + event.getMessage().getMentionedUsers().get(0).getName() + ".", 5, TimeUnit.SECONDS));
                    } catch (IllegalArgumentException ex) {
                        Msg.bad(event, "Cannot delete messages older than 2 weeks.");
                    }
                });
            } else {
                try {
                    amt = Integer.parseInt(args[0].trim());
                } catch (Exception e) {
                    Msg.bad(event, "USAGE" + ": " + "\n" +
                            Constants.D_PREFIX + "clear\n" +
                            Constants.D_PREFIX + "clear <num>\n" +
                            Constants.D_PREFIX + "clear <@user>\n" +
                            Constants.D_PREFIX + "clear <@user> <num>");
                    return;
                }
                if (amt < 2 || amt > 100) {
                    Msg.bad(event, "Please enter a number between 2 and 100.");
                    return;
                }
                event.getMessage().delete().complete();
                event.getChannel().getHistory().retrievePast(amt).queue(messages -> {
                    List<Message> toClean = messages;
                    toClean.remove(event.getMessage());
                    if (toClean.isEmpty()) {
                        Msg.bad(event, event.getAuthor().getName() + " No messages found matching the given criteria!");
                        return;
                    }
                    try {
                        if (toClean.size() == 1)
                            toClean.get(0).delete().queue(v -> Msg.normalTimedMessage(event, "@" + event.getAuthor().getName() + " " + "cleared " + toClean.size() + " messages.", 5, TimeUnit.SECONDS));
                        else
                            ((TextChannel) event.getChannel()).deleteMessages(toClean).queue(v -> Msg.normalTimedMessage(event, "@" + event.getAuthor().getName() + " " + "cleared " + toClean.size() + " messages.", 5, TimeUnit.SECONDS));
                    } catch (IllegalArgumentException ex) {
                        Msg.bad(event, "Cannot delete messages older than 2 weeks.");
                    }
                });
            }
        } else if (args.length == 2) {
            if (!event.getMessage().getMentionedUsers().isEmpty()) {
                User u = event.getMessage().getMentionedUsers().get(0);
                try {
                    amt = Integer.parseInt(args[1].trim());
                } catch (Exception e) {
                    Msg.bad(event, "USAGE" + ": " + "\n" +
                            Constants.D_PREFIX + "clear\n" +
                            Constants.D_PREFIX + "clear <num>\n" +
                            Constants.D_PREFIX + "clear <@user>\n" +
                            Constants.D_PREFIX + "clear <@user> <num>");
                    return;
                }
                if (amt < 2 || amt > 100) {
                    Msg.bad(event, "Please enter a number between 2 and 100.");
                    return;
                }
                event.getMessage().delete().complete();
                event.getTextChannel().getHistory().retrievePast(amt).queue(messages -> {
                    List<Message> toClean = new ArrayList<Message>();
                    for (Message m : messages) {
                        if (m.getAuthor().equals(u)) {
                            toClean.add(m);
                        }
                    }
                    toClean.remove(event.getMessage());
                    if (toClean.isEmpty()) {
                        Msg.bad(event, event.getAuthor().getName() + " No messages found matching the given criteria!");
                        return;
                    }
                    try {
                        if (toClean.size() == 1)
                            toClean.get(0).delete().queue(v -> Msg.normalTimedMessage(event, "@" + event.getAuthor().getName() + " " + "cleared " + toClean.size() + " messages by" + " @" + event.getMessage().getMentionedUsers().get(0).getName() + ".", 5, TimeUnit.SECONDS));
                        else
                            ((TextChannel) event.getChannel()).deleteMessages(toClean).queue(v -> Msg.normalTimedMessage(event, "@" + event.getAuthor().getName() + " " + "cleared " + toClean.size() + " messages by" + " @" + event.getMessage().getMentionedUsers().get(0).getName() + ".", 5, TimeUnit.SECONDS));
                    } catch (IllegalArgumentException ex) {
                        Msg.bad(event, "Cannot delete messages older than 2 weeks.");
                    }
                });
            } else {
                Msg.bad(event, "USAGE" + ": " + "\n" +
                        Constants.D_PREFIX + "clear\n" +
                        Constants.D_PREFIX + "clear <num>\n" +
                        Constants.D_PREFIX + "clear <@user>\n" +
                        Constants.D_PREFIX + "clear <@user> <num>");
            }
        } else {
            Msg.bad(event, "USAGE" + ": " + "\n" +
                    Constants.D_PREFIX + "clear\n" +
                    Constants.D_PREFIX + "clear <num>\n" +
                    Constants.D_PREFIX + "clear <@user>\n" +
                    Constants.D_PREFIX + "clear <@user> <num>");
        }
    }

}
