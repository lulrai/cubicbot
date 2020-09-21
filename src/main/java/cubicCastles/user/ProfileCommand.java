package cubicCastles.user;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import utils.Msg;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ProfileCommand extends Command {
    public static ArrayList<Qbee> qbees = new ArrayList<>();

    public ProfileCommand() {
        this.name = "profile";
        this.aliases = new String[]{"pf", "qbee", "q"};
        this.category = new Category("Normal");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        Member m = null;
        EmbedBuilder em = new EmbedBuilder();
        if(!event.getMessage().getMentionedMembers().isEmpty()) {
            m = event.getMessage().getMentionedMembers().get(0);
        }
        else {
            if(event.getArgs().isEmpty()){
                m = event.getMember();
            }
            else{
                try {
                    m = event.getGuild().getMemberById(event.getArgs().trim());
                }catch (NumberFormatException ignored){
                }
            }
        }

        if(m == null) {
            Msg.bad(event, "Invalid user tagged or invalid user id given.");
            return;
        }
        em.setColor(m.getColor());
        em.setTitle(m.getEffectiveName()+"'s In-Game Profile");
        em.setThumbnail(m.getUser().getEffectiveAvatarUrl());
        String id = m.getId();

        event.getMessage().delete().queue();

        Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(id)).findFirst().orElse(null);
        if(qbee == null) {
            em.setDescription("No information regarding the user's profile.");
        }
        else {
            boolean hasInfo = false;
            if(qbee.getForumLink() != null && !qbee.getForumLink().isEmpty()) { em.setTitle(m.getEffectiveName()+"'s In-Game Profile", qbee.getForumLink()); };
            if(qbee.getAbout() != null && !qbee.getAbout().isEmpty()) { em.setDescription(qbee.getAbout()); hasInfo = true; }
            if(qbee.getGameName() != null && !qbee.getGameName().isEmpty()) { em.addField("In-game Name", qbee.getGameName(), true); hasInfo = true; }
            if(qbee.getLevel() != -1) { em.addField("Level", String.valueOf(qbee.getLevel()), true); hasInfo = true; }
            if(qbee.getJoinDate() != null && !qbee.getJoinDate().isEmpty()) { em.addField("Join Date", qbee.getJoinDate(), true); hasInfo = true; }
            if(qbee.getClan() != null && !qbee.getClan().isEmpty()) { em.addField("Clan", qbee.getClan(), true); hasInfo = true; }
            if(qbee.getFavoriteItem() != null && !qbee.getFavoriteItem().isEmpty()) { em.addField("Favorite Item", qbee.getFavoriteItem(), true); hasInfo = true; }
            if(!getFormattedTimeSinceJoin(qbee).isEmpty()) { em.addField("Time Since Join", getFormattedTimeSinceJoin(qbee), true); hasInfo = true; }
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
        event.getTextChannel().sendMessage(em.build()).queue();
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
