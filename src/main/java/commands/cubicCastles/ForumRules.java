package commands.cubicCastles;

import commands.botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import commands.utils.Msg;

import java.awt.*;

public class ForumRules extends Command {
    public ForumRules() {
        this.name = "forumrules";
        this.aliases = new String[]{"forumrule"};
        this.arguments = "";
        this.help = "Shows the list of all the forum rules.";
        this.category = new Category("cubic");
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        try {
            em.setColor(Color.CYAN);
            if (event.getArgs().isEmpty()) {
                em.setTitle("Forum Rules", "http://forums2.cubiccastles.com/index.php?p=/discussion/8/forum-rules#latest");
                em.setDescription("General rules on the forum that has to be followed. There are few categories of rules and they've been separated to prevent long/spammy text from the bot.\n\n"
                        + "Type .forumrules <type> to view the actual rule that falls under the category.");
                em.addBlankField(false);
                //em.setThumbnail("http://forums2.cubiccastles.com/uploads/forumrules.png");
                em.addField("Types of rules", "Here are the types of rules separated by their header.\n"
                        + "- Do's\n"
                        + "- Don'ts\n"
                        + "- Off-Topic\n"
                        + "- Trading\n"
                        + "- Warnings", false);
                em.addBlankField(false);
                em.addField("Note", "Any violation of the game guidelines and terms of service will act as a violation of the forum rules as well.\n\n" +
                        "Also, remember that what happens on other forums, stays on other forums.\n" +
                        "\n" +
                        "If you have any questions or having trouble understanding any of the above, feel free to private message an active forum moderator. They will do their best to help you.", false);

            } else {
                String givenArg = event.getArgs().trim();
                if (checkAndGetUrl(givenArg).isEmpty()) {
                    Msg.bad(event.getChannel(), "Invalid type. Please check the types by using .forumrules and make sure that you type in all the letters.");
                    return;
                } else {
                    em.setTitle("Rules", "http://forums2.cubiccastles.com/index.php?p=/discussion/8/forum-rules#latest");
                    em.setImage(checkAndGetUrl(givenArg));
                }
            }

            em.setFooter("Post Created by Horus", null);
            event.getChannel().sendMessageEmbeds(em.build()).queue();
        } catch (InsufficientPermissionException ex) {
            event.getChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "ForumRules.java");
        }
    }

    private String checkAndGetUrl(String str) {
        String url = "";
        str = str.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        switch (str) {
            case "dos": {
                url = "https://i.imgur.com/w4JsERl.png";
                break;
            }
            case "donts": {
                url = "https://i.imgur.com/QOowdYr.png?1";
                break;
            }
            case "offtopic": {
                url = "https://i.imgur.com/iuaepXs.png";
                break;
            }
            case "trading": {
                url = "https://i.imgur.com/QnGxSKu.png";
                break;
            }
            case "warnings": {
                url = "https://i.imgur.com/muy6cCY.png";
                break;
            }
        }
        return url;
    }
}
