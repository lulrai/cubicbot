package modCommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

public class ModText extends Command{
    public ModText() {
        this.name = "modtext";
        this.aliases = new String[]{"mt"};
        this.arguments = "";
        this.help = "Sends a message with the given text as an embed with your role color.";
        this.category = new Category("Moderation");
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getArgs().isEmpty() || !event.getMember().hasPermission(Permission.BAN_MEMBERS)){
            return;
        }
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(event.getMember().getColor());
        em.setDescription(event.getArgs().trim());
        event.getTextChannel().sendMessage(em.build()).queue();
        event.getMessage().delete().queue();
    }
}
