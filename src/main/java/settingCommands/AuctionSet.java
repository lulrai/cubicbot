package settingCommands;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import utils.GlobalErrorLog;
import utils.Msg;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AuctionSet extends Command {

    public AuctionSet() {
        this.name = "setauction";
        this.aliases = new String[]{"sa"};
        this.arguments = "";
        this.category = new Category("Settings");
        this.help = "Sets the current channel (where the command is used on) as an auction host-able channel. Run the command again to remove the channel.";
        this.guildOnly = true;
    }

    public static String getAuctionChannel(CommandEvent event) {
        String channelID = "";
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/servers/" + event.getGuild().getId()).toUri());
        guildDir.mkdirs();
        try {
            File outFile = new File(guildDir, "settings.txt");
            outFile.createNewFile();

            BufferedReader br = new BufferedReader(new FileReader(outFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(" ", 2);
                if (split[0].trim().equalsIgnoreCase("auction")) {
                    channelID = split[1].trim();
                }
            }
            br.close();
        } catch (IOException e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "AuctionSet.java");
        }
        return channelID;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (getAuctionChannel(event).isEmpty()) {
            if (setAuctionChannel(event, false)) {
                Msg.reply(event, "This channel has been set as an auction channel.");
            } else {
                Msg.bad(event, "Couldn't set this channel as an auction channel.");
            }
        } else {
            if (setAuctionChannel(event, true)) {
                Msg.reply(event, "This channel has been removed as an auction channel.");
            } else {
                Msg.bad(event, "Couldn't remove this channel as an auction channel.");
            }
        }
    }

    private boolean setAuctionChannel(CommandEvent event, boolean exists) {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/servers/" + event.getGuild().getId()).toUri());
        guildDir.mkdirs();
        try {
            File file = new File(guildDir, "settings.txt");
            file.createNewFile();

            File temp = File.createTempFile("sett", ".txt", file.getParentFile());

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(temp), Charset.defaultCharset()));

            if (exists) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
                for (String line; (line = reader.readLine()) != null; ) {
                    if (line.split(" ")[0].equals("auction")) {
                        line = line.replace("auction " + event.getChannel().getId(), "").trim();
                    }
                    if (!line.equals("")) // don't write out blank lines
                    {
                        writer.println(line);
                    }
                }
                reader.close();
            } else {
                writer.println("auction " + event.getChannel().getId());
            }

            writer.close();
            file.delete();
            temp.renameTo(file);
        } catch (IOException e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "AuctionSet.java");
            return false;
        }
        return true;
    }
}
