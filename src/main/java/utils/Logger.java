package utils;

import adminCommands.ModLogSet;
import com.jagrosh.jdautilities.command.CommandEvent;
import core.Cubic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class Logger {
    public static void logInChannel(CommandEvent event, String type, int time) {
        String chanId = ModLogSet.getModLogChannel(event.getGuild());
        if (chanId.isEmpty()) {
            return;
        }
        EmbedBuilder em = new EmbedBuilder();
        TextChannel channel = event.getJDA().getTextChannelById(chanId);
        if (channel == null) {
            ModLogSet.removeModLog(event.getGuild(), chanId);
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

    public static void logInChannel(final Member toMember, final Member fromMember, final Guild guild, final String type, final int time) {
        final String chanId = ModLogSet.getModLogChannel(guild);
        if (chanId.isEmpty()) {
            return;
        }
        final EmbedBuilder em = new EmbedBuilder();
        final TextChannel channel = Cubic.getJDA().getTextChannelById(chanId);
        if (channel == null) {
            ModLogSet.removeModLog(guild, chanId);
            return;
        }
        final String lowerCase = type.toLowerCase();
        switch (lowerCase) {
            case "unmutetime": {
                em.setColor(Color.GREEN);
                em.setTitle("Unmute - Time Over");
                builder(toMember, fromMember, time, em, channel);
                break;
            }
            case "mute": {
                em.setColor(Color.ORANGE);
                em.setTitle("Mute");
                builder(toMember, fromMember, time, em, channel);
                break;
            }
            case "unmute": {
                em.setColor(Color.GREEN);
                em.setTitle("Unmute");
                builder(toMember, fromMember, time, em, channel);
                break;
            }
            case "unban": {
                em.setColor(Color.GREEN);
                em.setTitle("Unban - Time Over");
                builder(toMember, fromMember, time, em, channel);
                break;
            }
            case "ban": {
                em.setColor(Color.RED);
                em.setTitle("Ban");
                builder(toMember, fromMember, time, em, channel);
                break;
            }
            case "warn": {
                em.setColor(Color.BLUE);
                em.setTitle("Warn");
                builder(toMember, fromMember, time, em, channel);
                break;
            }
        }
    }

    private static void builder(final Member toMember, final Member fromMember, final int time, final EmbedBuilder em, final TextChannel channel) {
        em.addField("User", toMember.getUser().getName() + "#" + toMember.getUser().getDiscriminator(), true);
        em.addField("Moderator", fromMember.getUser().getName() + "#" + fromMember.getUser().getDiscriminator(), true);
        if (time != 0) {
            em.addField("Time", ConversionUtils.secondsToTime(time), true);
        }
        channel.sendMessage(em.build()).queue();
    }
}
