package commands.cubicCastles;

import commands.botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import commands.utils.CacheUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

public class NewsCommand extends Command {
    public NewsCommand() {
        this.name = "news";
        this.aliases = new String[]{"update", "updates"};
        this.category = new Category("cubic");
        this.arguments = "[version_id]";
        this.help = "Provides commands.information about a certain version/update of the game or general/recent update if no args provided.";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        try {
            String title = "Cubic Castles News/Updates";
            Map<String, String> map = new LinkedHashMap<>();

            em.setColor(Color.CYAN);

            Document doc = Jsoup.parse(CacheUtils.getCache("news"));
            String currentVersion = "";
            StringBuilder temp = new StringBuilder();

            String[] str = doc.text().replaceAll("~b", "**").split("(?=VERSION)");

            for (String s : str) {
                String[] split = s.split("(?= -)");
                for (String sp : split) {
                    String[] second = sp.trim().split("(?<=\\))");
                    if (second[0].startsWith("VERSION")) {
                        if (!currentVersion.isEmpty()) {
                            map.put(currentVersion, temp.toString());
                            temp = new StringBuilder();
                        }
                        if (second.length > 1) {
                            temp.append(second[1]).append("\n");

                        }
                        currentVersion = second[0].replaceAll("VERSION ", "VERSION: ");
                    } else {
                        if (second.length > 1) {
                            temp.append(second[1]).append("\n");

                        } else {
                            temp.append(sp).append("\n");
                        }
                    }
                }
            }


            if (event.getArgs().trim().isEmpty()) {
                em.setTitle(title);
                em.setDescription("To get the update commands.information for older updates, type `.news <versionNumber>` so for example, `.news 1.8.5`.");
                em.addBlankField(false);
                em.addField("Current Update", map.keySet().iterator().next() + "\n" + map.get(map.keySet().iterator().next()), false);
                em.addBlankField(false);

                int partitionSize = 10;
                List<List<String>> partitions = new LinkedList<>();
                ArrayList<String> message = new ArrayList<>(map.keySet());
                for (int i = 0; i < message.size(); i += partitionSize) {
                    partitions.add(message.subList(i,
                            i + Math.min(partitionSize, message.size() - i)));
                }

                em.addField("Other updates", "Here are other update versions:", false);
                for (List<String> partition : partitions) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : partition) {
                        sb.append(s).append("\n");
                    }
                    em.addField("", sb.toString(), false);
                }
            } else {
                String args = event.getArgs().trim();
                String key = map.keySet().parallelStream().anyMatch(s -> s.contains(" " + args + " ")) ? map.keySet().parallelStream().filter(s -> s.contains(" " + args + " ")).findFirst().get() : null;
                if (key != null && !key.isEmpty()) {
                    em.setTitle(key);
                    em.setDescription(map.get(key));
                } else {
                    em.setDescription("Update not found.");
                }
            }


            event.getChannel().sendMessageEmbeds(em.build()).queue();

        } catch (InsufficientPermissionException ex) {
            event.getChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "NewsCommand.java");
        }
    }
}
