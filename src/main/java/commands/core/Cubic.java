package commands.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
//import commands.cubicCastles.PriceCommand;
import commands.cubicCastles.craftCommands.CraftCommand;
import commands.cubicCastles.craftCommands.Item;
import commands.information.HelpCommand;
import commands.normalCommands.bingo.*;
import commands.normalCommands.usr.ProfileReadWrite;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;
import commands.utils.CacheUtils;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Cubic {
    private static JDA jda;
    public static JSONObject settings = null;
    private static CommandClient commandClient;
    private static EventWaiter waiter = new EventWaiter();

    public static void main(String[] args) throws LoginException, IllegalArgumentException, InterruptedException, IOException {
        BasicConfigurator.configure();
        System.setProperty("http.agent", "Chrome");
        Logger.getRootLogger().setLevel(Level.WARN);

        Logger.getRootLogger().warn("Initializing data...");

        // Initialize bingo cards
        GenerateBingo.initializeBingoCards();
        RollCommand.initializeChosenCache();
        BingoItem.initializeItemPools();

        // Initialize Profiles
        ProfileReadWrite.loadAllUsers();

        // Clear cache
        autoClearCache();

        // Initialize Settings
        settings = getSettings();

        Logger.getRootLogger().warn("Done initializing data.");
        Logger.getRootLogger().warn("Initializing bot...");

        commandClient = commandClient().build();
        if(settings.getString("type").equalsIgnoreCase("prod")){
            jda = JDABuilder.create(
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_INVITES,
                    GatewayIntent.GUILD_VOICE_STATES,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.DIRECT_MESSAGE_REACTIONS)
                    .setToken(settings.getString("token"))
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .disableIntents(
                            GatewayIntent.GUILD_MESSAGE_TYPING,
                            GatewayIntent.GUILD_EMOJIS,
                            GatewayIntent.GUILD_BANS,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.DIRECT_MESSAGE_TYPING)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                    .addEventListeners(commandClient,waiter)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB).build();
        }
        else{
            jda = JDABuilder.createDefault(settings.getString("token-test"))
                    .addEventListeners(commandClient,waiter)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB).build();
        }
        jda.setAutoReconnect(true);
        jda.getPresence().setActivity(Activity.watching("Cubic Castles!"));
        Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);
        jda.awaitStatus(JDA.Status.CONNECTED);

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/cards/items").toUri());
        File outFile = new File(guildDir, "bingoPool.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(outFile));
            String line;
            while ((line = br.readLine()) != null) {
                boolean isPresent = false;
                for(String s : guildDir.list()){
                    if(s.replaceAll(".png","").equalsIgnoreCase(line.split(",")[1])){
                        isPresent = true;
                        break;
                    }
                }
                if(!isPresent){
                    Logger.getRootLogger().warn("Missing item image from the list: " + line);
                }
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        //Help Command
        HelpCommand.addToHelp();
        Logger.getRootLogger().warn("Done initializing bot.");
    }

    private static JSONObject getSettings() throws IOException {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File globalDir = new File(workingDir.resolve("db/global").toUri());
        File settings = new File(globalDir, "settings.json");

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        if(!settings.exists()){
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

            JSONObject sampleObject = new JSONObject();
            sampleObject.put("token", "");
            sampleObject.put("client-id", "");
            sampleObject.put("owner-id", "");
            sampleObject.put("prefix","");
            sampleObject.put("bot-invite", "");
            sampleObject.put("error-channel-id", "");
            sampleObject.put("coowner-ids", "");
            sampleObject.put("type", "");
            sampleObject.put("imgur-api", "");
            writer.writeValue(settings, sampleObject);
            System.err.println("No settings file found! A new settings.json file has been created in 'db/global' directory. Please fill it out before running the bot.");
            System.exit(-1);
            return null;
        }
        else {
            return mapper.readValue(settings, JSONObject.class);
        }
    }

    private static void autoClearCache() {
        try {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
//                PriceCommand.populatePrices();
                for (Item i : CraftCommand.imgCache.values()) {
                    i = null;
                }
                CraftCommand.imgCache.clear();
                Path workingDir = Paths.get(System.getProperty("user.dir"));
                File cacheDir = new File(workingDir.resolve("db/cache/").toUri());
                if(!cacheDir.exists()) return;
                for(File file: Objects.requireNonNull(cacheDir.listFiles())) {
                    if (!file.isDirectory())
                        file.delete();
                }
                CacheUtils.setData();
            }, 0,
                    TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CommandClientBuilder commandClient() {
        CommandClientBuilder client = new CommandClientBuilder();
        try {
            client.setPrefix(settings.getString("prefix"))
                    .setOwnerId(settings.getString("owner-id"))
                    .setCoOwnerIds(settings.getString("coowner-ids").split(","))
                    .setEmojis("\u2611","\u2622", "\u274C")
                    .useHelpBuilder(false)
                    .addCommands(getCommands())
                    .addSlashCommands(getSlashCommands());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }

    private static Command[] getCommands() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Reflections reflections = new Reflections("commands");
        Set<Class<? extends Command>> subTypes = reflections.getSubTypesOf(Command.class);

        ArrayList<Command> commands = new ArrayList<>();
        for (Class<? extends Command> theClass : subTypes) {
            if(theClass.isAssignableFrom(SlashCommand.class) || theClass.getAnnotation(Deprecated.class) != null) {
                continue;
            }
            commands.add(theClass.getDeclaredConstructor().newInstance());
            LoggerFactory.getLogger(theClass).debug("Loaded normal commands successfully!");
        }

        return commands.toArray(new Command[0]);
    }

    private static SlashCommand[] getSlashCommands() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Reflections reflections = new Reflections("commands");
        Set<Class<? extends SlashCommand>> subTypes = reflections.getSubTypesOf(SlashCommand.class);
        ArrayList<SlashCommand> commands = new ArrayList<>();

        for (Class<? extends SlashCommand> theClass : subTypes) {
            commands.add(theClass.getDeclaredConstructor().newInstance());
            LoggerFactory.getLogger(theClass).debug("Loaded slash commands successfully!");
        }

        return commands.toArray(new SlashCommand[0]);
    }

    public static JDA getJDA() {
        return jda;
    }

    public static CommandClient getCommandClient() {
        return commandClient;
    }

    public static EventWaiter getWaiter() {
        return waiter;
    }
}
