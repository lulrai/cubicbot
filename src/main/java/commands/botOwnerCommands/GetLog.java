package commands.botOwnerCommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.utils.Msg;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GetLog extends Command {
    public GetLog() {
        this.name = "getlog";
        this.aliases = new String[] { "log" };
        this.category = new Category("Owner");
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        final Path workingDir = Paths.get(System.getProperty("user.dir"));
        final File nohup = new File(workingDir.resolve("nohup.out").toUri());
        if (!nohup.exists()) {
            Msg.bad(event, "Did not find any logs.");
            return;
        }
        Msg.reply(event, "Here is your log file:");
        event.getMessage().getTextChannel().sendFile(nohup).queue();
    }
}
