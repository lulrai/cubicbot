package normalCommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import utils.ImageURLUtil;
import utils.Msg;

import java.io.File;

public class ImgurCommand extends Command {
    public ImgurCommand() {
        this.name = "imgur";
        this.aliases = new String[]{"img"};
        this.category = new Category("Normal");
        this.ownerCommand = false;
        this.cooldown = 3;
        this.cooldownScope = CooldownScope.GLOBAL;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getMessage().getAttachments().isEmpty()) {
            Msg.bad(event, "No attachments found.");
            return;
        }
        if (event.getMessage().getAttachments().size() > 1) {
            Msg.bad(event, "Only one attachment at a time is supported.");
            return;
        }
        if (!event.getMessage().getAttachments().get(0).isImage()) {
            Msg.bad(event, "Only message attachments.");
            return;
        }

        File attach = new File("imgurFile.png");
        if (event.getMessage().getAttachments().get(0).downloadToFile(attach).isDone()) {
            event.getTextChannel().sendMessage("Here is your link to the image: " + ImageURLUtil.getImageURL(attach)).queue();
            attach.delete();
        } else {
            Msg.bad(event, "An error occured accessing the attached file. Please try again.");
        }
    }

}
