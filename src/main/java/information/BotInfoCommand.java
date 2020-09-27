package information;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import utils.ConversionUtils;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class BotInfoCommand extends Command {
    public BotInfoCommand() {
        this.name = "botinfo";
        this.aliases = new String[]{"botstats"};
        this.category = new Category("Info");
        this.arguments = "";
        this.help = "Displays information about the bot.";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setTitle("**" + event.getJDA().getSelfUser().getName() + " " + "Info" + "**", null);

        builder.setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

        builder.addField("Name:", event.getJDA().getSelfUser().getName(), true);

        builder.addField("Owner:", event.getJDA().getUserById(event.getClient().getOwnerId()).getName() + "#" + event.getJDA().getUserById(event.getClient().getOwnerId()).getDiscriminator(), true);

        builder.addField("Avatar Credit:", "Graceful Thunder#1856", true);

        builder.addField("Servers:", String.valueOf(event.getJDA().getGuilds().size()), true);

        builder.addField("Channels:", (event.getJDA().getTextChannels().size() + event.getJDA().getVoiceChannels().size()) + "\n"
                + "Text Channels:" + " " + event.getJDA().getTextChannels().size() + "\n"
                + "Voice Channels:" + " " + event.getJDA().getVoiceChannels().size(), true);

        builder.addField("Known users:", String.valueOf(event.getJDA().getUsers().size()), true);

        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        builder.addField("Uptime:", ConversionUtils.secondsToTime(rb.getUptime() / 1000), true);
        event.getChannel().sendMessage(builder.build()).complete();
    }

}
