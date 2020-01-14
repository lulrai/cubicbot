package utils;

import adminCommands.ModLogSet;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class Logger {
    public static void logInChannel(CommandEvent event, String type, int time) {
        String chanId = ModLogSet.getModLogChannel(event);
        if (chanId.isEmpty()) {
            return;
        }
        EmbedBuilder em = new EmbedBuilder();
        TextChannel channel = event.getJDA().getTextChannelById(chanId);
        if (channel == null) {
            ModLogSet.removeModLog(event, chanId);
            return;
        }
        switch (type.toLowerCase()) {
            case "unmutetime": {
                em.setColor(Color.GREEN);
                em.setTitle("Unmute - Time Over");
                builder(event, time, em, channel);
                break;
            }
            case "mute": {
                em.setColor(Color.ORANGE);
                em.setTitle("Mute");
                builder(event, time, em, channel);
                break;
            }
            case "unmute": {
                em.setColor(Color.GREEN);
                em.setTitle("Unmute");
                builder(event, time, em, channel);
                break;
            }
            case "unban": {
                em.setColor(Color.GREEN);
                em.setTitle("Unban - Time Over");
                builder(event, time, em, channel);
                break;
            }
            case "ban": {
                em.setColor(Color.RED);
                em.setTitle("Ban");
                builder(event, time, em, channel);
                break;
            }
            case "warn": {
                em.setColor(Color.BLUE);
                em.setTitle("Warn");
                builder(event, time, em, channel);
                break;
            }
        }
    }

    private static void builder(CommandEvent event, int time, EmbedBuilder em, TextChannel channel) {
        em.addField("User", event.getMessage().getMentionedUsers().get(0).getName() + "#" + event.getMessage().getMentionedUsers().get(0).getDiscriminator(), true);
        em.addField("Moderator", event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator(), true);
        if (time != 0) {
            em.addField("Time", ConversionUtils.secondsToTime(time), true);
        }
        channel.sendMessage(em.build()).queue();
    }
}
