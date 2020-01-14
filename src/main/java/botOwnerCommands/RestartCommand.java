package botOwnerCommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import information.AutoUpdatingStatus;
import net.dv8tion.jda.api.Permission;
import utils.Msg;
import utils.UserPermission;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RestartCommand extends Command {
    public RestartCommand() {
        this.name = "restart";
        this.aliases = new String[]{"shutdown"};
        this.category = new Category("Owner");
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        Msg.reply(commandEvent, "Restarting..");

        if(AutoUpdatingStatus.m != null)
            AutoUpdatingStatus.m.delete().queue();

        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        final File currentJar = new File(workingDir.resolve("cubic-bot-0.1-jar-with-dependencies.jar").toUri());
        final File savePID = new File(workingDir.resolve("save_pid.txt").toUri());

            /* is it a jar file? */
        if(!currentJar.getName().endsWith(".jar"))
            return;

            /* Build command: java -jar application.jar */
        final ArrayList<String> command = new ArrayList<>();
        // nohup java -jar cubic-bot-0.1-jar-with-dependencies.jar & echo $! > save_pid.txt
        command.add("nohup");
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());
        command.add("&");
        command.add("echo");
        command.add("$!");
        command.add(">");
        command.add(savePID.getPath());


        final ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
            //System.out.println(builder.command());
        } catch (IOException e) {
            ExceptionHandler.handleException(commandEvent, e, "RestartCommand.java");
        }

        Msg.reply(commandEvent, "Restarted successfully.");
        System.exit(0);
    }
}
