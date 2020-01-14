package utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class GlobalErrorLog {
    private static EmbedBuilder em = new EmbedBuilder();

    public static void normal(MessageReceivedEvent event, String command, String errorType, String reason){
        em.setColor(Color.YELLOW);
        em.setTitle("Simple Error in " + command);
        em.addField("Guild", event.getGuild().getName()+" ("+event.getGuild().getId()+" )", true);
        em.addField("Error Type", errorType, true);
        em.addField("Reason", reason, false);
        event.getJDA().getTextChannelById(Constants.BOT_ERROR_CHANNEL).sendMessage(em.build()).queue();
    }

    public static void concern(MessageReceivedEvent event, String command, String errorType, String reason){
        em.setColor(Color.ORANGE);
        em.setTitle("Concerning Error in " + command);
        em.addField("Guild", event.getGuild().getName()+" ("+event.getGuild().getId()+" )", true);
        em.addField("Error Type", errorType, true);
        em.addField("Reason", reason, false);
        event.getJDA().getTextChannelById(Constants.BOT_ERROR_CHANNEL).sendMessage(em.build()).queue();
    }

    public static void critical(MessageReceivedEvent event, String command, String errorType, String reason){
        em.setColor(Color.RED);
        em.setTitle("Critical Error in " + command);
        em.addField("Guild", event.getGuild().getName()+" ("+event.getGuild().getId()+" )", true);
        em.addField("Error Type", errorType, true);
        em.addField("Reason", reason, false);
        event.getJDA().getTextChannelById(Constants.BOT_ERROR_CHANNEL).sendMessage(em.build()).queue();
    }
}
