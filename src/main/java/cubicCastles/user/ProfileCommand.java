package cubicCastles.user;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import core.Cubic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ProfileCommand extends Command {
    public static ArrayList<Qbee> qbees = new ArrayList<>();

    public ProfileCommand() {
        this.name = "profile";
        this.aliases = new String[]{"pfp", "qbee", "q"};
        this.category = new Category("Normal");
        this.ownerCommand = false;
        this.cooldown = 10;
        this.cooldownScope = CooldownScope.USER;
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    protected void execute(CommandEvent event) {
        Member m;
        EmbedBuilder em = new EmbedBuilder();
        if(!event.getMessage().getMentionedUsers().isEmpty() && !event.getMessage().getMentionedUsers().get(0).isBot()) {
            m = event.getMessage().getMentionedMembers().get(0);
        }
        else {
            m = event.getMember();
        }
        em.setColor(m.getColor());
        em.setTitle(m.getEffectiveName()+"'s In-Game Profile");
        em.setThumbnail(m.getUser().getEffectiveAvatarUrl());

        Qbee qbee = ProfileCommand.qbees.parallelStream().filter(p -> p.getUserID().equals(m.getId())).findFirst().orElse(null);
        if(qbee == null) {
            em.setDescription("No information regarding the user's profile.");
        }
        else {
            if(qbee.getAbout() != null && !qbee.getAbout().isEmpty()) em.setDescription(qbee.getAbout());
            if(qbee.getGameName() != null && !qbee.getGameName().isEmpty()) em.addField("In-game Name", qbee.getGameName(), true);
            if(qbee.getLevel() != -1) em.addField("Level", String.valueOf(qbee.getLevel()), true);
            if(qbee.getJoinDate() != null && !qbee.getJoinDate().isEmpty()) em.addField("Join Date", qbee.getJoinDate(), true);
            if(qbee.getClan() != null && !qbee.getClan().isEmpty()) em.addField("Clan", qbee.getClan(), true);
            if(qbee.getFavoriteItem() != null && !qbee.getFavoriteItem().isEmpty()) em.addField("Favorite Item", qbee.getFavoriteItem(), true);
            if(!getFormattedTimeSinceJoin(qbee).isEmpty()) em.addField("Time Since Join", getFormattedTimeSinceJoin(qbee), true);
            if(qbee.getBirthday() != null) em.addField("Birthday", qbee.getBirthday().getMonth() + " " + qbee.getBirthday().getDay(), true);
            if(!qbee.getRealms().isEmpty()){
                StringBuilder sb = new StringBuilder();
                for(String realm : qbee.getRealms()) sb.append(realm).append("\n");
                em.addField("Realms", sb.toString().trim(), false);
            }
            if(!qbee.getOverworlds().isEmpty()){
                StringBuilder sb = new StringBuilder();
                for(String overworld : qbee.getOverworlds()) sb.append(overworld).append("\n");
                em.addField("Overworlds", sb.toString().trim(), true);
            }
        }
        event.getTextChannel().sendMessage(em.build()).queue();
    }

    public String getFormattedTimeSinceJoin(Qbee q){
        if(q.getJoinDate() == null || q.getJoinDate().isEmpty()) return "";
        LocalDate join = LocalDate.parse(q.getJoinDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Period period = Period.between(join, LocalDate.now());
        return period.getYears() + " year(s) " + period.getMonths() + " month(s) " + period.getDays() + " day(s)";
    }

    public static void checkForBirthdays(){
        for(Qbee q : ProfileCommand.qbees){
            User user = Cubic.getJDA().getUserById(q.getUserID());
            if(user.getMutualGuilds().size() == 0) return;
            if(q.getBirthday().getMonthNum() == LocalDate.now().getMonthValue() && q.getBirthday().getDayNum() == LocalDate.now().getDayOfMonth()){
                for(Guild guild : user.getMutualGuilds()){
                    Role birthdayRole = guild.getRoles().parallelStream().filter(r -> r.getName().toLowerCase().contains("birthday")).findFirst().orElse(null);
                    if(birthdayRole == null) return;
                    if(guild.getMember(Cubic.getJDA().getSelfUser()).canInteract(birthdayRole)) {
                        guild.addRoleToMember(guild.getMember(user), birthdayRole).queue();
                    }
                }
            }
            else {
                for(Guild guild : user.getMutualGuilds()){
                    Role birthdayRole = guild.getRoles().parallelStream().filter(r -> r.getName().toLowerCase().contains("birthday")).findFirst().orElse(null);
                    if(birthdayRole == null) return;
                    if(guild.getMember(Cubic.getJDA().getSelfUser()).canInteract(birthdayRole)) {
                        guild.removeRoleFromMember(guild.getMember(user), birthdayRole).queue();
                    }
                }
            }
        }
    }
}
