package commands.information;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import commands.core.Cubic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import commands.utils.Msg;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HelpCommand extends Command {
    private static Map<String, EmbedBuilder> helpEmbeds = new HashMap<>();
    private EventWaiter waiter;
    /**TODO:
     * REDO THE ENTIRE COMMAND
     */

    public HelpCommand() {
        this.name = "help";
        this.aliases = new String[]{"halp"};
        this.category = new Category("Info");
        this.arguments = "";
        this.guildOnly = false;
        this.help = "Displays this message or detail about a certain command.";
        this.waiter = Cubic.getWaiter();
    }

    public static void addToHelp() {
        Map<String, ArrayList<Command>> settings = new HashMap<>();
        settings.put(":gear:", new ArrayList<>());
        settings.put(":hammer:", new ArrayList<>());
        settings.put(":robot_face:", new ArrayList<>());
        settings.put(":question:", new ArrayList<>());
        settings.put(":computer:", new ArrayList<>());
        settings.put(":house:", new ArrayList<>());
        settings.put(":picture_frame:", new ArrayList<>());
        for(Command command : Cubic.getCommandClient().getCommands()) {
            if(command.getCategory() == null) continue;
            String category = command.getCategory().getName();
            switch (category.toLowerCase()) {
                case "settings" : {
                    settings.get(":gear:").add(command);
                    break;
                }
                case "admin" : {
                    settings.get(":hammer:").add(command);
                    break;
                }
                case "cubic" : {
                    settings.get(":robot_face:").add(command);
                    break;
                }
                case "info" : {
                    settings.get(":question:").add(command);
                    break;
                }
                case "moderation" : {
                    settings.get(":computer:").add(command);
                    break;
                }
                case "normal" : {
                    settings.get(":house:").add(command);
                    break;
                }
                case "profile" : {
                    settings.get(":picture_frame:").add(command);
                    break;
                }
            }
        }
        for(String emoji : settings.keySet()) {
            switch(emoji.toLowerCase()) {
                case ":gear:" : { helpEmbeds.put(emoji, commandsToString(EmojiParser.parseToUnicode(":gear:")+" Settings",
                        "Configuring the settings for this guild/server requires `Administrator` permissions.",
                        settings.get(":gear:"))); break; }
                case ":hammer:" : { helpEmbeds.put(emoji, commandsToString(EmojiParser.parseToUnicode(":hammer:")+" Administrator",
                        "Requires `Administrator` permission for the user to be recognized as an administrator and use these commands.",
                        settings.get(":hammer:"))); break; }
                case ":robot_face:" : { helpEmbeds.put(emoji, commandsToString(EmojiParser.parseToUnicode(":robot_face:")+" Cubic Castles",
                        "Commands for cubic castles related things such as crafting items, checking status, and looking up items.",
                        settings.get(":robot_face:"))); break; }
                case ":question:" : { helpEmbeds.put(emoji, commandsToString(EmojiParser.parseToUnicode(":question:")+" Informative",
                        "Useful commands for you to use for various purposes to get more commands.information about certain stuffs.",
                        settings.get(":question:"))); break; }
                case ":computer:" : { helpEmbeds.put(emoji, commandsToString(EmojiParser.parseToUnicode(":computer:")+" Moderator",
                        "Requires `BAN` permission or `MANAGE` permission for the user to be recognized as a moderator.",
                        settings.get(":computer:"))); break; }
                case ":house:" : { helpEmbeds.put(emoji, commandsToString(EmojiParser.parseToUnicode(":house:")+" Misc",
                        "Just some random commands because why not.",
                        settings.get(":house:"))); break; }
                case ":picture_frame:" : { helpEmbeds.put(emoji, commandsToString(EmojiParser.parseToUnicode(":picture_frame:")+" Profile",
                        "Commands related to profile commands.information and other useful features relating to the profile system.",
                        settings.get(":picture_frame:"))); break; }
            }
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        String prefix = event.getClient().getPrefix();
        StringBuilder sb = new StringBuilder();
        String[] args = event.getArgs().split(" ", 1);
        Color c = Color.getHSBColor(48, 93, 94);
        Message displayMessage = event.getChannel().sendMessage(event.getAuthor().getAsMention()).complete();
        if (event.isFromType(ChannelType.TEXT)){
            c = event.getMember().getColor();
            if(event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) event.getMessage().delete().queue();
        }
        if (event.getArgs().isEmpty()) {
            sb.append("**__HELP COMMAND__**").append("\n").append("** **\n");
            sb.append("Please do not put the, [] = Optional, <> = Required, with the arguments. It will NOT work.").append("\n").append("** **\n");;
            sb.append("Prefix: `").append(prefix).append("`").append("\n").append("** **\n");
            sb.append(":gear: Settings Commands").append("\n").append("** **\n");
            sb.append(":hammer: Administrator Commands").append("\n").append("** **\n");
            sb.append(":robot: Cubic Castles Commands").append("\n").append("** **\n");
            sb.append(":question: Informative Commands").append("\n").append("** **\n");
            sb.append(":computer: Moderator Commands").append("\n").append("** **\n");
            sb.append(":house: Misc Commands").append("\n").append("** **\n");
            sb.append(":frame_photo: Profile Commands").append("\n").append("** **\n");
            sb.append("**Additional Help**").append("\n");
            sb.append("For more info about the commands, type: `").append(prefix).append("help <commandName>`. NOTE: Do not enter the prefix or the arguments.");

            ButtonMenu.Builder be = new ButtonMenu.Builder()
                    .setDescription(sb.toString())
                    .setEventWaiter(waiter)
                    .addChoice(EmojiManager.getForAlias("gear").getUnicode())
                    .addChoice(EmojiManager.getForAlias("hammer").getUnicode())
                    .addChoice(EmojiManager.getForAlias("robot_face").getUnicode())
                    .addChoice(EmojiManager.getForAlias("question").getUnicode())
                    .addChoice(EmojiManager.getForAlias("computer").getUnicode())
                    .addChoice(EmojiManager.getForAlias("house").getUnicode())
                    .addChoice(EmojiManager.getForAlias("picture_frame").getUnicode())
                    .addChoice(EmojiManager.getForAlias("x").getUnicode())
                    .setUsers(event.getAuthor())
                    .setColor(c)
                    .setTimeout(1, TimeUnit.MINUTES)
                    .setAction(em -> {
                        Color col = Color.CYAN;
                        if(event.isFromType(ChannelType.TEXT)){
                            col = event.getMember().getColor();
                        }
                        String option = EmojiParser.parseToAliases(em.getEmoji());
                        if(!helpEmbeds.containsKey(option))
                        {
                            displayMessage.editMessageEmbeds(Msg.createMessage(event.getTextChannel(), Color.RED, "Pick one of the emojis.", "")).queue();
                            return;
                        }
                        EmbedBuilder embed = helpEmbeds.get(option);
                        embed.setColor(col);
                        embed.setAuthor(event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator(), null, event.getAuthor().getEffectiveAvatarUrl());
                        displayMessage.editMessageEmbeds(embed.build()).queue();
                    })
                    .setFinalAction(me -> {
                        try {
                            if(displayMessage.isFromType(ChannelType.TEXT)) displayMessage.clearReactions().queue();
                        }catch (Exception ignored) { }
                    });
            be.build().display(displayMessage);
        } else if (args.length == 1) {
            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED, "Currently not supported part of the command. Still working on it..", "")).queue();
        } else {
            displayMessage.editMessageEmbeds(Msg.createMessage(event.getChannel(), Color.RED, "USAGE\" + \": \" + Constants.D_PREFIX + \"help <cmdName>", "")).queue();
        }
    }

    public static EmbedBuilder commandsToString(String title, String description, ArrayList<Command> commands){
        EmbedBuilder em = new EmbedBuilder();
        em.setTitle(title + " Commands");
        em.setColor(Color.getHSBColor(48, 93, 94));
        em.setDescription(description);
        for(Command cmd : commands) {
            em.addField(Cubic.settings.getString("prefix")+cmd.getName() + " " + cmd.getArguments(), cmd.getHelp(), false);
        }
        em.setFooter("You can type "+Cubic.settings.getString("prefix")+"help <commandName> to get more info about the command.");
        return em;
    }

}
