package botOwnerCommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import cubicCastles.PriceCommand;
import cubicCastles.craftCommands.CraftCommand;
import utils.CacheUtils;
import utils.Msg;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class UpdateCache extends Command {
    public UpdateCache() {
        this.name = "refresh";
        this.aliases = new String[]{"reset"};
        this.category = new Category("Owner");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!event.getAuthor().getId().equals("222488511385698304") && !event.getAuthor().getId().equals("643903506750898215")) {
            return;
        }

        Msg.reply(event,"Clearing all cache..");

        PriceCommand.populatePrices();
        CraftCommand.imgCache.clear();
        CacheUtils.caches.clear();

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File cacheDir = new File(workingDir.resolve("db/cache/").toUri());
        if(!cacheDir.exists()) return;
        for(File file: Objects.requireNonNull(cacheDir.listFiles())) {
            if (!file.isDirectory())
                file.delete();
        }

        Msg.reply(event, "Cleared cache.");
    }
}
