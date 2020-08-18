package utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Msg {
    public static void reply(MessageReceivedEvent event, String message) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setDescription(message);
        event.getTextChannel().sendMessage(em.build()).queue();
    }

    public static void reply(CommandEvent event, String message) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setDescription(message);
        event.getTextChannel().sendMessage(em.build()).queue();
    }

    public static void reply(TextChannel mutedChannel, String message) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setDescription(message);
        mutedChannel.sendMessage(em.build()).queue();
    }

    public static void bad(MessageReceivedEvent event, String message) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.RED);
        em.setDescription(message);
        event.getTextChannel().sendMessage(em.build()).queue();
    }

    public static void bad(CommandEvent event, String message) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.RED);
        em.setDescription(message);
        event.getTextChannel().sendMessage(em.build()).queue();
    }

    public static void bad(TextChannel mutedChannel, String message) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.RED);
        em.setDescription(message);
        mutedChannel.sendMessage(em.build()).queue();
    }

    public static void replyTimed(CommandEvent event, String message, int timeTillDeletion, TimeUnit timeType) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setDescription(message);
        embedTimedMessage(event, em, timeTillDeletion, timeType);
    }

    public static void replyTimed(TextChannel channel, String message, int timeTillDeletion, TimeUnit timeType) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setDescription(message);
        embedTimedMessage(channel, em, timeTillDeletion, timeType);
    }

    public static void badTimed(CommandEvent event, String message, int timeTillDeletion, TimeUnit timeType) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.RED);
        em.setDescription(message);
        embedTimedMessage(event, em, timeTillDeletion, timeType);
    }

    public static void normal(CommandEvent event, String message) {
        event.getTextChannel().sendMessage(message).queue();
    }

    public static void normalTimedMessage(CommandEvent event, String message, int timeTillDeletion, TimeUnit timeType) {
        event.getTextChannel().sendMessage(message).queue(m -> {
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(20);
            exec.schedule(() -> m.delete().queue(), timeTillDeletion, timeType);
        });
    }

    public static void embedTimedMessage(CommandEvent event, EmbedBuilder embed, int timeTillDeletion, TimeUnit timeType) {
        event.getTextChannel().sendMessage(embed.build()).queue(m -> {
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(20);
            exec.schedule(() -> m.delete().queue(), timeTillDeletion, timeType);
        });
    }

    public static void embedTimedMessage(TextChannel channel, EmbedBuilder embed, int timeTillDeletion, TimeUnit timeType) {
        channel.sendMessage(embed.build()).queue(m -> {
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(20);
            exec.schedule(() -> m.delete().queue(), timeTillDeletion, timeType);
        });
    }
}
