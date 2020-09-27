package settingCommands;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import utils.Msg;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModLogSet extends Command {

    public ModLogSet() {
        this.name = "setmodlog";
        this.aliases = new String[]{};
        this.arguments = "";
        this.category = new Category("Settings");
        this.help = "Sets the current channel (where the command is used on) as a moderator log channel. Run the command again to remove the channel.";
        this.guildOnly = true;
    }

    public static String getModLogChannel(Guild guild) {
        String channelID = "";
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/servers/" + guild.getId()).toUri());
        guildDir.mkdirs();
        try {
            File outFile = new File(guildDir, "settings.txt");
            outFile.createNewFile();

            BufferedReader br = new BufferedReader(new FileReader(outFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(" ", 2);
                if (split[0].trim().equalsIgnoreCase("modlog")) {
                    channelID = split[1].trim();
                }
            }
            br.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return channelID;
    }

    public static void removeModLog(Guild guild, String channelID){
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/servers/" + guild.getId()).toUri());
        guildDir.mkdirs();
        try {
            File file = new File(guildDir, "settings.txt");
            file.createNewFile();

            File temp = File.createTempFile("sett", ".txt", file.getParentFile());

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(temp), Charset.defaultCharset()));

                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
                for (String line; (line = reader.readLine()) != null; ) {
                    if (line.split(" ")[0].equals("modlog")) {
                        line = line.replace("modlog " + channelID, "").trim();
                    }
                    if (!line.equals("")) // don't write out blank lines
                    {
                        writer.println(line);
                    }
                }
                reader.close();
            writer.close();
            file.delete();
            temp.renameTo(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        if (getModLogChannel(event.getGuild()).isEmpty()) {
            if (setModLogChannel(event, false)) {
                Msg.reply(event, "This channel has been set as a moderator log channel.");
            } else {
                Msg.bad(event, "Couldn't set this channel as a moderator log channel.");
            }
        } else {
            if (setModLogChannel(event, true)) {
                Msg.reply(event, "This channel has been removed as a moderator log channel.");
            } else {
                Msg.bad(event, "Couldn't remove this channel as a moderator log channel.");
            }
        }
    }

    private boolean setModLogChannel(CommandEvent event, boolean exists) {
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
                    if (line.split(" ")[0].equals("modlog")) {
                        line = line.replace("modlog " + event.getChannel().getId(), "").trim();
                    }
                    if (!line.equals("")) // don't write out blank lines
                    {
                        writer.println(line);
                    }
                }
                reader.close();
            } else {
                writer.println("modlog " + event.getChannel().getId());
            }

            writer.close();
            file.delete();
            temp.renameTo(file);
        } catch (Exception e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "ModLogSet.java");
            return false;
        }
        return true;
    }
}
