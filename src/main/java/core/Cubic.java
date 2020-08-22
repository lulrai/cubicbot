package core;

import adminCommands.*;
import botOwnerCommands.*;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.AboutCommand;
import cubicCastles.*;
import cubicCastles.auction.AuctionCommand;
import cubicCastles.auction.BidCommand;
import cubicCastles.craftCommands.CraftCommand;
import cubicCastles.craftCommands.Item;
import information.BotInfoCommand;
import information.HelpCommand;
import modCommands.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import normalCommands.bingo.*;
import normalCommands.ImgurCommand;
import cubicCastles.OldPriceCommand;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utils.Constants;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Cubic {
    private static JDA jda;

    public static void main(String[] args) throws LoginException, IllegalArgumentException, InterruptedException {
        BasicConfigurator.configure();
        //Logger.getRootLogger().setLevel(Level.ERROR);

        // Initialize bingo cards
        GenerateBingo.initializeBingoCards();
        RollCommand.initializeChosenCache();
        BingoItem.initializeItemPools();

        EventWaiter waiter = new EventWaiter();
        jda = new JDABuilder(AccountType.BOT)
//                .setToken(Constants.BOT_RELEASE_CODE)
                .setToken(Constants.BOT_TEST_CODE)
                .addEventListeners(commandClient(waiter).build(),waiter)
                .setStatus(OnlineStatus.DO_NOT_DISTURB).build();
        jda.setAutoReconnect(true);
        jda.getPresence().setActivity(Activity.watching("Cubic Castles!"));
        Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);
        jda.awaitStatus(JDA.Status.CONNECTED);

        autoClearCache();

        //Help Command
        HelpCommand.addToHelp();
    }

    private static void autoClearCache() {
        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
            scheduler.scheduleAtFixedRate(() -> {
                PriceCommand.populatePrices();
                for (Item i :CraftCommand.imgCache.values()) {
                    i.getImage().delete();
                }
                CraftCommand.imgCache.clear();

                Path workingDir = Paths.get(System.getProperty("user.dir"));
                File cacheDir = new File(workingDir.resolve("db/cache/").toUri());
                if(!cacheDir.exists()) return;
                for(File file: Objects.requireNonNull(cacheDir.listFiles())) {
                    if (!file.isDirectory())
                        file.delete();
                }

            }, 0, 1, TimeUnit.DAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CommandClientBuilder commandClient(EventWaiter waiter) {
        CommandClientBuilder client = new CommandClientBuilder();
        client.setPrefix(Constants.D_PREFIX)
                .setOwnerId(Constants.BOT_OWNER_ID)
                .setServerInvite(Constants.SERVER_INVITE)
                .setEmojis(Constants.SUCCESS, Constants.WARNING, Constants.ERROR)
                .useHelpBuilder(false)
                .addCommands(
                        //BotOwner
                        new EvalCommand(),
                        new UpdateCache(),
                        new RestartCommand(),
                        new GetLog(),
                        new Guilds(waiter),

                        new AboutCommand(Color.CYAN, "and I'm here to make your experience with Cubic Castles even better!",
                                new String[]{"Submitting Support Tickets", "Check Item Info", "Check Craft Info", "Check Staff List", "Check Perks Info", "Check Item Prices"},
                                Permission.BAN_MEMBERS, Permission.KICK_MEMBERS,
                                Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_READ,
                                Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_HISTORY, Permission.MESSAGE_EXT_EMOJI,
                                Permission.MESSAGE_MANAGE, Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS, Permission.VOICE_DEAF_OTHERS,
                                Permission.VOICE_MUTE_OTHERS, Permission.NICKNAME_CHANGE, Permission.NICKNAME_MANAGE),

                        //Info
                        new HelpCommand(),
                        new BotInfoCommand(),

                        //Normal
                        new ImgurCommand(),

                        // Bingo Commands
                        new GenerateBingo(waiter),
                        new BingoCommand(),
                        new RollCommand(),
                        new Verify(),
                        new MarkCommand(),
                        new UnmarkCommand(),
                        new BingoClearCommand(),
                        new BingoItem(waiter),

                        //Cubic Castles
                        new CraftCommand(waiter),
                        new ItemCommand(waiter),
                        new StaffListCommand(),
                        new PerksCommand(),
                        new StatusCommand(),
                        new RaffleCommand(),
                        new ProfileCommand(),
                        new AuctionCommand(waiter),
                        new BidCommand(),
                        new ForumRules(),
                        new GameRules(),
                        new NewsCommand(),

                        //Moderation
                        new ClearCommand(),
                        new MuteCommand(),
                        new UnmuteCommand(),
                        new PollCommand(),
                        new WarningCommand(),

                        //Administration
                        new BanCommand(waiter),
                        new UnbanCommand(),
                        new AuctionSet(),
                        new ModLogSet(),

                        //Prices
                        new OldPriceCommand(),
                        new PriceCommand(waiter)

                        //Tests
                );
        return client;
    }

    public static JDA getJDA() {
        return jda;
    }
}
