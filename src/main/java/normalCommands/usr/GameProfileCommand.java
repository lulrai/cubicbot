package normalCommands.usr;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import utils.Msg;

import java.awt.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class GameProfileCommand extends Command {
    public static ArrayList<Qbee> qbees = new ArrayList<>();

    public GameProfileCommand() {
        this.name = "profile";
        this.aliases = new String[]{"pf", "qbee", "q"};
        this.category = new Category("Profile");
        this.arguments = "[@user|id]";
        this.help = "Displays your own profile or a profile of another user.";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        User u = null;
        EmbedBuilder em = new EmbedBuilder();
        if(!event.getMessage().getMentionedUsers().isEmpty()) {
            u = event.getMessage().getMentionedUsers().get(0);
        }
        else {
            if(event.getArgs().isEmpty()){
                u = event.getAuthor();
            }
            else{
                try {
                    u = event.getJDA().retrieveUserById(event.getArgs().trim()).complete();
                }catch (NumberFormatException ignored){
                }
            }
        }

        if(u == null) {
            Msg.bad(event.getChannel(), "Invalid user tagged or invalid user id given.");
            return;
        }
        String title;
        if(event.isFromType(ChannelType.TEXT) && event.getGuild().getMember(u) != null) {
            em.setColor(event.getGuild().getMember(u).getColor());
            title = event.getGuild().getMember(u).getEffectiveName()+"'s In-Game Profile";
        }
        else{
            em.setColor(Color.WHITE);
            title = u.getName()+"'s In-Game Profile";
        }
        em.setTitle(title);
        em.setThumbnail(u.getEffectiveAvatarUrl());
        String id = u.getId();

        if(event.isFromType(ChannelType.TEXT)) event.getMessage().delete().queue();

        Qbee qbee = GameProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(id)).findFirst().orElse(null);
        if(qbee == null) {
            em.setDescription("No information regarding the user's profile.");
        }
        else {
            boolean hasInfo = false;
            if(qbee.getForumLink() != null && !qbee.getForumLink().isEmpty()) { em.setTitle(title, qbee.getForumLink()); };
            if(qbee.getAbout() != null && !qbee.getAbout().isEmpty()) { em.setDescription(qbee.getAbout()); hasInfo = true; }
            if(qbee.getGameName() != null && !qbee.getGameName().isEmpty()) { em.addField("In-game Name", qbee.getGameName(), true); hasInfo = true; }
            if(qbee.getLevel() != -1) { em.addField("Level", String.valueOf(qbee.getLevel()), true); hasInfo = true; }
            if(qbee.getJoinDate() != null && !qbee.getJoinDate().isEmpty()) { em.addField("Join Date (Month-Day-Year)", qbee.getJoinDate(), true); hasInfo = true; }
            if(qbee.getClan() != null && !qbee.getClan().isEmpty()) { em.addField("Clan", qbee.getClan(), true); hasInfo = true; }
            if(qbee.getFavoriteItem() != null && !qbee.getFavoriteItem().isEmpty()) { em.addField("Favorite Item", qbee.getFavoriteItem(), true); hasInfo = true; }
            if(!getFormattedTimeSinceJoin(qbee).isEmpty()) { em.addField("Joined Since", getFormattedTimeSinceJoin(qbee), true); hasInfo = true; }
            if(qbee.getBirthday() != null) { em.addField("Birthday", Birthday.numToMonth(qbee.getBirthday().getMonth()) + " " + qbee.getBirthday().getDay(), true); hasInfo = true; }
            if(qbee.getProfilePic() != null && !qbee.getProfilePic().isEmpty()) em.setThumbnail(qbee.getProfilePic());
            if(!qbee.getRealms().isEmpty()){
                StringBuilder sb = new StringBuilder();
                for(String realm : qbee.getRealms()) sb.append(realm).append("\n");
                em.addField("Realms", sb.toString().trim(), false);
                hasInfo = true;
            }
            if(!qbee.getOverworlds().isEmpty()){
                StringBuilder sb = new StringBuilder();
                for(String overworld : qbee.getOverworlds()) sb.append(overworld).append("\n");
                em.addField("Overworlds", sb.toString().trim(), true);
                hasInfo = true;
            }
            if(!hasInfo) em.setDescription("No information regarding the user's profile.");
        }
        em.setFooter("You are responsible for what you put on your profile. Inappropriate profiles can be deleted.");
        event.getChannel().sendMessage(em.build()).queue();
    }

    public String getFormattedTimeSinceJoin(Qbee q){
        if(q.getJoinDate() == null || q.getJoinDate().isEmpty()) return "";
        try {
            LocalDate join = LocalDate.parse(q.getJoinDate(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            Period period = Period.between(join, LocalDate.now());
            return period.getYears() + " year(s) " + period.getMonths() + " month(s) " + period.getDays() + " day(s)";
        }
        catch (Exception e) {
            System.out.println("=================== Broken json file for Qbee " + q.getUserID() + " for the time since join field. ====================");
            return "";
        }
    }
}
