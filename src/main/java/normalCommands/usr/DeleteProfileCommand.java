package normalCommands.usr;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import core.Cubic;
import net.dv8tion.jda.api.entities.User;
import utils.Msg;
import utils.UserPermission;

import java.util.Collections;

public class DeleteProfileCommand extends Command {
    public DeleteProfileCommand(){
        this.name = "deleteprofile";
        this.aliases = new String[]{"dp"};
        this.category = new Category("Owner");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(!event.getAuthor().getId().equals("270011841869119488") && !UserPermission.isBotOwner(event.getAuthor())) return;

        User u;
        if(!event.getMessage().getMentionedUsers().isEmpty() && !event.getMessage().getMentionedUsers().get(0).isBot()) {
            u = event.getMessage().getMentionedUsers().get(0);
        }
        else {
            if(event.getArgs().isEmpty()){
                u = event.getAuthor();
            }
            else{
                u = Cubic.getJDA().retrieveUserById(event.getArgs().trim()).complete();
            }
        }

        if(u == null) {
            Msg.bad(event, "Invalid user tagged or invalid user id given.");
            return;
        }

        boolean isRemoved = GameProfileCommand.qbees.removeIf(p -> p.getUserID().equals(u.getId()));
        if(isRemoved) {
            Qbee qbee = new Qbee(u.getId(), u.getEffectiveAvatarUrl());
            int index = Collections.binarySearch(GameProfileCommand.qbees, qbee, new Qbee.QbeeSortingComparator());
            index = ~index;
            GameProfileCommand.qbees.add(index, qbee);
            ProfileReadWrite.updateUser(qbee);
            Msg.reply(event, "Successfully removed the user's profile.");
        }
        else {
            Msg.bad(event, "No profile found for " + u.getAsMention());
        }
    }
}
