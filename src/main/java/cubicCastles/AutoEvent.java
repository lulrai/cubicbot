package cubicCastles;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import utils.CacheUtils;
import utils.Msg;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoEvent extends Command {
    public static String title = "";

    public AutoEvent() {
        this.name = "setevent";
        this.aliases = new String[]{};
        this.category = new Category("Cubic Castles");
        this.ownerCommand = true;
        this.cooldownScope = CooldownScope.GLOBAL;
    }

    public static void runUpdates(JDA jda) {


        EmbedBuilder em = new EmbedBuilder();
        try {
            em.setColor(Color.CYAN);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
            scheduler.scheduleAtFixedRate(() -> {
                Document doc = Jsoup.parse(CacheUtils.getCache("event"));
                Element div = doc.getElementsByClass("DataList Discussions").first();

                if (!title.equalsIgnoreCase(div.getElementsByClass("Title").first().text())) {
                    title = div.getElementsByClass("Title").first().text();
                    String postLink = div.getElementsByClass("Title").first().getElementsByTag("a").first().attr("abs:href");

                    Document p = Jsoup.parse(getData(postLink));
                    Element a = p.getElementsByClass("MessageList Discussion").first();
                    String post = fixBr(a.getElementsByClass("Message").html());
                    String image = p.getElementsByClass("Author").first().getElementsByTag("img").first().attr("abs:src");

                    em.setTitle(title);
                    em.setDescription(post + "\n" + "[Click here for the post](" + postLink + ")");
                    em.setThumbnail(image);

                    for (Guild g : jda.getGuilds()) {
                        String id = getEventChannel(g);
                        if (id != null) {
                            TextChannel tc = jda.getTextChannelById(id);
                            if (tc == null) {
                                setEventChannel(id, g.getId(), true);
                            } else {
                                jda.getTextChannelById(id).sendMessage(em.build()).queue();
                            }
                        }
                    }
                }
            }, 0, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getData(String url) {
        String data = "";
        try {
            URL getUrl = new URL(url);
            HttpURLConnection urlConn = (HttpURLConnection) getUrl.openConnection();
            urlConn.setUseCaches(true);
            urlConn.setRequestMethod("GET");
            urlConn.addRequestProperty("User-Agent", "Mozilla/4.76");
            urlConn.setDoOutput(true);
            urlConn.connect();
            InputStreamReader read = new InputStreamReader(urlConn.getInputStream());
            BufferedReader each = new BufferedReader(read);
            String line;
            while ((line = each.readLine()) != null) {
                data += line + "\n";
            }
            read.close();
            each.close();
            urlConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private static String fixBr(String html) {
        if (html == null)
            return "";
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        document.select("p").prepend("\\n");
        document.select("h2").prepend("\\n**").append("**\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

    private static String getEventChannel(Guild g) {
        String channelId = "";
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/servers/" + g.getId()).toUri());
        if (!guildDir.exists()) {
            return null;
        }
        try {
            File outFile = new File(guildDir, "settings.txt");
            if (!outFile.exists()) {
                return null;
            }

            BufferedReader br = new BufferedReader(new FileReader(outFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(" ", 2);
                if (split[0].trim().equalsIgnoreCase("event")) {
                    channelId = split[1].trim();
                }
            }
            br.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (channelId.isEmpty()) {
            return null;
        }
        return channelId;
    }

    private static boolean setEventChannel(String id, String guildId, boolean exists) {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/servers/" + guildId).toUri());
        guildDir.mkdirs();
        try {
            File file = new File(guildDir, "settings.txt");
            file.createNewFile();

            File temp = File.createTempFile("sett1", ".txt", file.getParentFile());

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(temp), Charset.defaultCharset()));

            if (exists) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
                for (String line; (line = reader.readLine()) != null; ) {
                    if (line.split(" ")[0].equals("event")) {
                        line = line.replace("event " + id, "").trim();
                    }
                    if (!line.equals("")) // don't write out blank lines
                    {
                        writer.println(line);
                    }
                }
                reader.close();
            } else {
                writer.println("event " + id);
            }

            writer.close();
            file.delete();
            temp.renameTo(file);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (getEventChannel(event.getGuild()) == null) {
            if (setEventChannel(event.getTextChannel().getId(), event.getGuild().getId(), false)) {
                Msg.reply(event, "This channel has been set as an event/contest update channel.");
            } else {
                Msg.bad(event, "Couldn't set this channel as an event/contest update channel.");
            }
        } else {
            if (setEventChannel(event.getTextChannel().getId(), event.getGuild().getId(), true)) {
                Msg.reply(event, "This channel has been removed as an event/contest update channel.");
            } else {
                Msg.bad(event, "Couldn't remove this channel as an event/contest update channel.");
            }
        }
    }

}
