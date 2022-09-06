package commands.botOwnerCommands;

import commands.core.Cubic;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.Arrays;

public class ExceptionHandler {
    public static void handleException(Exception e, String commandRun, String fileName){
        String exceptionType = e.getClass().getCanonicalName();
        String exceptionMessage = e.getMessage();
        String fullStackTrace = Arrays.toString(e.getStackTrace());

        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.RED);
        em.setTitle("EXCEPTION");
        em.addField("Exception Type", exceptionType, false);
        em.addField("Command Ran", commandRun, true);
        em.addField("File Name", fileName, true);
        if(exceptionMessage != null) em.addField("Exception Message", exceptionMessage, false);
            em.addField("Full Stack Trace", fullStackTrace.substring(0,1000)+"...", false);

        Cubic.getJDA().getTextChannelById("584488228749574160").sendMessageEmbeds(em.build()).queue();
    }
}
