package commands.botOwnerCommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.utils.ErrorHandling;
import net.dv8tion.jda.api.EmbedBuilder;
import commands.utils.UserPermission;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;

public class EvalCommand extends Command {
    public EvalCommand() {
        this.name = "eval";
        this.aliases = new String[]{};
        this.category = new Category("Owner");
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!UserPermission.isBotOwner(event.getAuthor())) {
            ErrorHandling.USER_PERMISSION_ERROR.error(event);
            return;
        }
        if (event.getArgs().length() <= 0) {
            return;
        }
        try {
            String guildId = "";
            String allArgs;
            if (event.getArgs().split(" ",2).length > 1 && event.getJDA().getGuildById(event.getArgs().split(" ",2)[0].trim()) != null) {
                guildId = event.getArgs().split(" ", 2)[0].trim();
                allArgs = event.getArgs().split(" ", 2)[1].trim();
            } else {
                allArgs = event.getArgs().trim();
            }
            Color c = Color.GREEN;
            EmbedBuilder em = new EmbedBuilder();
            ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
            se.put("e", event);
            se.put("event", event);
            se.put("jda", event.getJDA());
            se.put("guild", event.getGuild());
            se.put("channel", event.getChannel());
            se.put("chat", event.getTextChannel());
            se.put("author", event.getAuthor());
            se.put("member", event.getMember());
            se.put("message", event.getMessage());
            se.put("input", allArgs);
            se.put("selfUser", event.getJDA().getSelfUser());
            se.put("selfMember", event.getGuild() == null ? null : event.getGuild().getSelfMember());
            se.put("mentionedUsers", event.getMessage().getMentionedUsers());
            se.put("mentionedRoles", event.getMessage().getMentionedRoles());
            se.put("mentionedChannels", event.getMessage().getMentionedChannels());
            se.put("embed", em);
            se.put("shardinfo", event.getJDA().getShardInfo());
            se.put("green", c);
            se.put("Color", Color.class);
            try {
                if (!guildId.isEmpty()) {
                    se.put("guild", event.getJDA().getGuildById(guildId));
                    se.put("owner", event.getJDA().getGuildById(guildId).getOwner().getUser());
                    se.put("guildMembers", event.getJDA().getGuildById(guildId).getMembers());
                }
            } catch (NullPointerException ignored) {
            }
            try {
                Object value = se.eval(allArgs);
                if (value != null) {
                    event.getTextChannel().sendMessage("```java\n" + allArgs + " ```" + "Evaluated Successfully:\n```\n" + se.eval(allArgs) + " ```").complete();
                }
            } catch (Exception ex) {
                event.getTextChannel().sendMessage("```java\n" + allArgs + " ```" + "An exception was thrown:\n```\n" + ex + " ```").complete();
            }
        } catch (Exception ignored) {
        }
    }

}
