package modCommands;

import adminCommands.BanCommand;
import settingCommands.ModLogSet;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import utils.*;

import java.awt.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WarningCommand extends Command {
    public WarningCommand() {
        this.name = "warn";
        this.aliases = new String[]{"badboy", "badgirl"};
        this.arguments = "<@user>";
        this.category = new Category("Moderation");
        this.help = "Warns the user and log it on the modlog channel, if present.\n" +
                "The warning points work as follows:\n" +
                "1 warning point  = no consequences\n" +
                "2 warning points = one day ban\n" +
                "3 warning points = five day ban\n" +
                "4 warning points = permanent ban";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.BLUE);
        em.setTitle("Warn");

        String[] args = event.getArgs().split(" ", 2);
        if (event.getAuthor().isBot()) return;
        if (args.length < 1) {
            Msg.bad(event, "USAGE" + ": " + Constants.D_PREFIX + "warn <@user> [reason]");
            return;
        }
        event.getMessage().getMentionedUsers();
        if (event.getMessage().getMentionedUsers().isEmpty()) {
            ErrorHandling.EMPTY_MENTION_ERROR.error(event);
            return;
        }
        Member m = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
        if (UserPermission.isMod(event, event.getAuthor())) {
            ErrorHandling.USER_PERMISSION_ERROR.error(event);
            return;
        }

        em.addField("User", m.getUser().getName() + "#" + m.getUser().getDiscriminator(), true);
        em.addField("Moderator", event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator(), true);

        if (WarningUtils.getWarnings(event.getGuild().getId()).get(m.getUser().getId()) != null) {
            int currentWarn = WarningUtils.getWarnings(event.getGuild().getId()).get(m.getUser().getId());
            int newWarn = 0;
            if (currentWarn == 2) {
                newWarn = currentWarn + 1;
                WarningUtils.setWarning(event.getGuild().getId(), m.getUser().getId(), currentWarn, newWarn);
                ban(event, "one", m.getUser(), 86399);
                Msg.reply(event, m.getUser().getName() + "#" + m.getUser().getDiscriminator() + " has been warned.");
                em.addField("Warning Points", Integer.toString(newWarn), true);
            } else if (currentWarn == 3) {
                newWarn = currentWarn + 1;
                WarningUtils.setWarning(event.getGuild().getId(), m.getUser().getId(), currentWarn, newWarn);
                ban(event, "five", m.getUser(), 431999);
                Msg.reply(event, m.getUser().getName() + "#" + m.getUser().getDiscriminator() + " has been warned.");
                em.addField("Warning Points", Integer.toString(newWarn), true);
            } else if (currentWarn == 4) {
                WarningUtils.setWarning(event.getGuild().getId(), m.getUser().getId(), currentWarn, newWarn);
                ban(event, "permanent", m.getUser(), 0);
                Msg.reply(event, m.getUser().getName() + "#" + m.getUser().getDiscriminator() + " has been warned.");
                em.addField("Warning Points", Integer.toString(currentWarn + 1), true);
            } else {
                newWarn = currentWarn + 1;
                WarningUtils.setWarning(event.getGuild().getId(), m.getUser().getId(), currentWarn, newWarn);
                Msg.reply(event, m.getUser().getName() + "#" + m.getUser().getDiscriminator() + " has been warned.");
                em.addField("Warning Points", Integer.toString(newWarn), true);
            }
        } else {
            WarningUtils.addWarning(event.getGuild().getId(), m.getUser().getId(), 1);
            Msg.reply(event, m.getUser().getName() + "#" + m.getUser().getDiscriminator() + " has been warned.");
            em.addField("Warning Points", Integer.toString(1), true);
        }

        if (args.length > 1) {
            em.addField("Reason", args[1], false);
        }

        String chanId = ModLogSet.getModLogChannel(event.getGuild());
        if (!chanId.isEmpty()) {
            TextChannel tc = event.getJDA().getTextChannelById(chanId);
            if(tc != null){
                tc.sendMessage(em.build()).queue();
            }
            else{
                ModLogSet.removeModLog(event.getGuild(), chanId);
            }
        }
    }

    private void ban(CommandEvent event, String type, User target, int time) {
        try {
            switch (type.toLowerCase()) {
                case "one":
                case "five": {
                    event.getGuild().ban(target, 7).queue((v) -> BanCommand.isBanned.put(target, unban(event, target, time)));
                    break;
                }
                case "permanent": {
                    event.getGuild().ban(target, 7).queue();
                    break;
                }
            }

            Logger.logInChannel(event, "ban", time);
        } catch (HierarchyException hierarchyException){
            Msg.bad(event, "Cannot ban user higher than the bot.");
        } catch (PermissionException ex) {
            Msg.bad(event, "The bot doesn't have necessary permission to ban the user. Requires Manage Channel permission.");
        }

    }

    private ScheduledFuture<?> unban(CommandEvent event, User target, int unbanTime) {
        return event.getGuild().unban(target).submitAfter(unbanTime, TimeUnit.SECONDS);
    }

}
