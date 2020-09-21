package information;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import utils.Constants;
import utils.Msg;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HelpCommand extends Command {
    private static Map<String, String> settingsHelp = new HashMap<>();
    private static Map<String, String> cubicHelp = new HashMap<>();
    private static Map<String, String> normalHelp = new HashMap<>();
    private static Map<String, String> informativeHelp = new HashMap<>();
    private static Map<String, String> modHelp = new HashMap<>();
    private static Map<String, String> adminHelp = new HashMap<>();
    private static Map<String, String> bingoHelp = new HashMap<>();
    public HelpCommand() {
        this.name = "help";
        this.aliases = new String[]{"halp"};
        this.category = new Category("Informative");
        this.ownerCommand = false;
        this.cooldown = 2;
    }

    private static void settingsHelp() {
        settingsHelp.put("setmodlog", "Sets the current channel (where the command is used on) as a moderator log channel. Run the command again to remove the channel.");
        settingsHelp.put("setauction", "Sets the current channel (where the command is used on) as an auction host-able channel. Run the command again to remove the channel.");
    }

    private static void cubicHelp() {
        cubicHelp.put("forumrules", "Shows the list of all the forum rules.");
        cubicHelp.put("gamerules", "Shows the entire list of game rules. (It's really long)");
        cubicHelp.put("item <itemName>", "Displays info about the provided item name.");
        cubicHelp.put("craft <itemName>", "Displays info and the crafting process of the provided item name.");
        cubicHelp.put("news [version_number]", "Provides information about a certain version/update of the game.");
        cubicHelp.put("staff", "Displays a list of current and retired Cubic Castles Staffs updated from the [Forum Post](http://forums2.cubiccastles.com/index.php?p=/discussion/12/staff-list#latest)");
        cubicHelp.put("perks [name]", "Displays all the perks available (w/o arguments) or information about the provided perk.");
        cubicHelp.put("price <itemName>", "Provides the price of the item with the item name provided. Uses [V's Forum Post](https://forums2.cubiccastles.com/index.php?p=/discussion/27821/cubic-castles-prices#latest) for the prices.");
        cubicHelp.put("oldprice <packName>", "Displays an image of the list of prices taken from the [Superxtreme's Forum Post](http://forums2.cubiccastles.com/index.php?p=/discussion/4169/cubic-castles-prices/p1)");
        cubicHelp.put("status", "Displays the current information about server status, number of players and current CC time.");
        cubicHelp.put("profile [@user]", "Displays your own profile or a profile of another user.");
        cubicHelp.put("profileedit", "Edit your profile to change, add, or remove certain aspects.");
        cubicHelp.put("auction", "Create an auction with the given information.");
        cubicHelp.put("bid <auction_post_id> <amount>", "Bids the provided amount to the bid post provided the proper id of the bid.");
    }

    private static void normalHelp() {
        normalHelp.put("imgur (imageAttachment)", "Uploads the attached image to imgur and provides you the link to it.");
    }

    private static void informativeHelp() {
        informativeHelp.put("help [cmd]", "Displays this message or detail about a certain command if argument is provided.");
        informativeHelp.put("botinfo", "Displays information about the bot.");
    }

    private static void modHelp() {
        modHelp.put("modtext <text>", "Sends a message with the given text as an embed with your role color.");
        modHelp.put("clear [@user] [num]", "Clears the given amount of messages in the channel with or w/o args.");
        modHelp.put("mute <@user> <time>", "Mutes the mentioned user in **the channel only** for the provided time, if provided.");
        modHelp.put("unmute <@user>", "Unmutes a mentioned-muted user.");
        modHelp.put("poll <topic> | [option1] | [option2]", "Creates a poll with the provided options or just the topic. Separate by using `|`.");
        modHelp.put("birthday", "Gives an option to get all the recorded birthdays of users in a server or a specific month.");
        modHelp.put("warn <@user> [reason]", "Warns the user and log it on the modlog channel, if present.\n"
                + "The warning points work as follows:\n"
                + "1 warning point  = no consequences\n"
                + "2 warning points = one day ban\n"
                + "3 warning points = five day ban\n"
                + "4 warning points = permanent ban");
    }

    private static void adminHelp() {
        adminHelp.put("command <add/remove> <name> [value]", "Allows you to add a command or remove a command.");
        adminHelp.put("ban <@user> [time]", "Ban the user based on the given time.");
        adminHelp.put("unban userid", "Unban the provided user's id.");
    }

    public static void addToHelp() {
        settingsHelp();
        cubicHelp();
        normalHelp();
        informativeHelp();
        modHelp();
        adminHelp();
    }

    @Override
    protected void execute(CommandEvent e) {
        String prefix = Constants.D_PREFIX;
        EmbedBuilder embed = new EmbedBuilder();
        String[] args = e.getArgs().split(" ");
        embed.setColor(Color.CYAN);
        if (e.getGuild().getMember(e.getJDA().getSelfUser()).hasPermission(Permission.MESSAGE_MANAGE)) {
            e.getMessage().delete().queue();
        }
        if (e.getArgs().isEmpty()) {
            embed.setThumbnail(e.getGuild().getSelfMember().getUser().getAvatarUrl());
            embed.setTitle(e.getGuild().getSelfMember().getUser().getName()).setDescription("Made By Raizusekku");
            embed.addField("More Help", "For more info about the commands, type: " + prefix + "help <cmd_type>", true);
            embed.addField("Index",
                    "[] = " + "Optional" + "\n" +
                            "<> = " + "Required" + "\n", false);
            embed.addField("Prefix", "`" + prefix + "`", false);
            embed.addField("Settings Commands", prefix + "help settings", false);
            embed.addField("Cubic Castles Commands", prefix + "help cubic", false);
            embed.addField("Fun/Normal Commands", prefix + "help fun/normal", false);
            embed.addField("Informative Commands", prefix + "help informative", false);
            embed.addField("Moderator Commands", prefix + "help mod", false);
            embed.addField("Admin Commands", prefix + "help admin", false);
            embed.setFooter("Requested by" + " " + e.getAuthor().getName(), e.getAuthor().getAvatarUrl());
            e.getTextChannel().sendMessage(embed.build()).queue(m -> {
                ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                exec.schedule(() -> m.delete().queue(), 20, TimeUnit.SECONDS);
            });
        } else if (args.length == 1) {
            String help;
            help = args[0];
            switch (help.toLowerCase().trim()) {
                case "settings": {
                    embed.setTitle("Settings Commands");
                    embed.setDescription("Configuring the settings for this guild/server requires `Bot Admin` role or Administrator permissions.");
                    for (String command : settingsHelp.keySet()) {
                        embed.addField(command, settingsHelp.get(command), false);
                    }
                    break;
                }
                case "cubic": {
                    embed.setTitle("Cubic Castles Commands");
                    embed.setDescription("Commands for cubic castles related things.");
                    for (String command : cubicHelp.keySet()) {
                        embed.addField(command, cubicHelp.get(command), false);
                    }
                    break;
                }
                case "fun/normal":
                case "fun":
                case "normal": {
                    embed.setTitle("Fun/Normal Commands");
                    embed.setDescription("Just some fun commands because why not.");
                    for (String command : normalHelp.keySet()) {
                        embed.addField(command, normalHelp.get(command), false);
                    }
                    break;
                }
                case "informative": {
                    embed.setTitle("Informative Commands");
                    embed.setDescription("Useful commands for you to use for various purposes.");
                    for (String command : informativeHelp.keySet()) {
                        embed.addField(command, informativeHelp.get(command), false);
                    }
                    break;
                }
                case "mod": {
                    embed.setTitle("Moderator Commands");
                    embed.setDescription("Requires `Bot Mod` or `Mod` or `Moderator` role for the user to be recognized as a moderator.");
                    for (String command : modHelp.keySet()) {
                        embed.addField(command, modHelp.get(command), false);
                    }
                    break;
                }
                case "admin": {
                    embed.setTitle("Administrator Commands");
                    embed.setDescription("Requires `Bot Admin` or `Admin` or `Administrator` role OR administrator permission for the user to be recognized as an administrator.");
                    for (String command : adminHelp.keySet()) {
                        embed.addField(command, adminHelp.get(command), false);
                    }
                    break;
                }
                default: {
                    Msg.badTimed(e, "Not a valid parameter.", 10, TimeUnit.SECONDS);
                    return;
                }
            }
            embed.setFooter("Requested by" + " " + e.getAuthor().getName(), e.getAuthor().getAvatarUrl());
            e.getTextChannel().sendMessage(embed.build()).queue(m -> {
                ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                exec.schedule(() -> m.delete().queue(), 1, TimeUnit.MINUTES);
            });
        } else {
            Msg.bad(e, "USAGE" + ": " + Constants.D_PREFIX + "help <cmd_type>");
        }
    }

}
