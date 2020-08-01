package utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserPermission {
    public static boolean isBotOwner(User u) {
        return u.getId().equals(Constants.BOT_OWNER_ID);
    }

    public static boolean isAdmin(CommandEvent event, User u) {
        List<String> admins = new ArrayList<>(Arrays.asList("bot admin", "admin", "administrator"));
        Member m = event.getGuild().getMember(u);
        if(m == null) return false;
        boolean isAdmin = false;
        for (Role role : m.getRoles()) {
            if (admins.contains(role.getName().toLowerCase())) {
                isAdmin = true;
            }
        }
        return !m.hasPermission(Permission.ADMINISTRATOR) && !isAdmin && !m.isOwner();
    }

    public static boolean isMod(CommandEvent event, User u) {
        List<String> mods = new ArrayList<>(Arrays.asList("bot mod", "mod", "moderator", "discord moderator"));
        Member m = event.getGuild().getMember(u);
        if(m == null) return false;
        boolean isMod = false;
        for (Role role : m.getRoles()) {
            if (mods.contains(role.getName().toLowerCase())) {
                isMod = true;
            }
        }
        return !m.hasPermission(Permission.MANAGE_PERMISSIONS) && !isMod && isAdmin(event, u) && !m.isOwner();
    }
}
