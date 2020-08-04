package cubicCastles;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import utils.CacheUtils;
import utils.ConversionUtils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StatusCommand extends Command {
    public StatusCommand() {
        this.name = "status";
        this.aliases = new String[]{"time", "stats"};
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        try {
            String title = "Cubic Castles Status";
            ArrayList<String> serverStats = new ArrayList<>();
            String time;

            em.setTitle(title);
            em.setColor(Color.CYAN);

            Document doc = Jsoup.parse(getData("https://www.cubiccastles.com/status.php"));

            JSONObject obj = new JSONObject(getData("https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?format=json&appid=317470"));


            Element div = doc.getElementsByTag("body").get(0);
            String[] text = Jsoup.parse(div.html().replace("<br>", "break")).text().split("break");
            List<String> str = Arrays.stream(text).map(s-> {
                if(s.length() >= 15) return s;
                return null;
            }).collect(Collectors.toList());
            for(String s : str){
                if(s != null && !s.isEmpty()) {
                    String[] split = s.trim().split(" ", 4);
                    int serverNum = 0;
                    int player = 0;
                    try{
                        player = Integer.parseInt(split[2].replaceAll("[^\\d]", ""));
                        serverNum = Integer.parseInt(split[0].substring(split[0].length()-3).replaceAll("[^\\d]", ""));
                    }catch (NumberFormatException ignored){}
                    serverStats.add(String.format("%s %02d %s %s %s", "Server", (serverNum >= 1000 ? serverNum - 1000 : serverNum), ":", split[1], (split[2].trim().equalsIgnoreCase("down!") ? " (Down for "+ ConversionUtils.secondsToTime(player) + ")" : "")));
                }
            }

            //Time
            TimeZone cc = TimeZone.getTimeZone("PST");
            SimpleDateFormat ccf = new SimpleDateFormat("`MM-dd-yyyy hh:mm:ss a z`");
            ccf.setTimeZone(cc);
            Calendar calcc = Calendar.getInstance(cc);
            time = ccf.format(calcc.getTime());

            em.addField("Time", time, true);

            em.addBlankField(true);
            //em.addField("Players", "Number of Users Online: "+players, true);
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < (serverStats.size()/2); i++){
                sb.append(serverStats.get(i)).append("\n");
            }
            em.addBlankField(true);
            em.addField("Servers", sb.toString(), true);
            sb = new StringBuilder();
            for(int i = (serverStats.size()/2); i < serverStats.size(); i++){
                sb.append(serverStats.get(i)).append("\n");
            }
            em.addField("", sb.toString(), true);

            event.getTextChannel().sendMessage(em.build()).queue();

        } catch (InsufficientPermissionException ex) {
            event.getTextChannel().sendMessage(ex.getMessage()).queue();
        } catch (Exception e) {
//            e.printStackTrace();
            ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "StatusCommand.java");
        }
    }

    public static String getData(String url) throws InterruptedException {
        StringBuilder html = new StringBuilder();
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
                html.append(line).append("\n");
            }
            read.close();
            each.close();
            urlConn.disconnect();
        }
        catch (Exception ignored){ }

        while(html.toString().trim().isEmpty()) {
            Thread.sleep(1000);
        }
        return html.toString();
    }

}