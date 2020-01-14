package botOwnerCommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import utils.CacheUtils;
import utils.Msg;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class UpdateCache extends Command {
    public UpdateCache() {
        this.name = "refresh";
        this.aliases = new String[]{"reset"};
        this.category = new Category("Owner");
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        Msg.reply(event,"Clearing all cache..");

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
