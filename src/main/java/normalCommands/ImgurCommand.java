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
        this.arguments = "<attachment>";
        this.help = "Uploads the attached image to imgur and provides you the link to it.";
        this.cooldown = 30;
        this.cooldownScope = CooldownScope.USER;
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getMessage().getAttachments().isEmpty()) {
            Msg.bad(event.getChannel(), "No attachments found.");
            return;
        }
        if (event.getMessage().getAttachments().size() > 1) {
            Msg.bad(event.getChannel(), "Only one attachment at a time is supported.");
            return;
        }
        if (!event.getMessage().getAttachments().get(0).isImage()) {
            Msg.bad(event.getChannel(), "Only message attachments.");
            return;
        }

        File attach = new File("imgurFile.png");
        if (event.getMessage().getAttachments().get(0).downloadToFile(attach).isDone()) {
            event.getChannel().sendMessage("Here is your link to the image: " + ImageURLUtil.getImageURL(attach)).queue();
            attach.delete();
        } else {
            Msg.bad(event.getChannel(), "An error occured accessing the attached file. Please try again.");
        }
    }

}
