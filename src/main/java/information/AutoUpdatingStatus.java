package information;

import cubicCastles.StatusCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import utils.ConversionUtils;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutoUpdatingStatus {
    public static Message m;
    public static void check(JDA jda) {
        TextChannel tc = jda.getTextChannelById("634897750982524939");
        MessageHistory mh = new MessageHistory(tc);
        if(mh.getRetrievedHistory().isEmpty()){
            update(tc);
            return;
        }
        mh.retrievePast(100).queue(messages -> {
            List<Message> toClean = new ArrayList<>();
            for (Message m : messages) {
                MessageEmbed em = m.getEmbeds().parallelStream().filter(f -> Objects.requireNonNull(f.getTitle()).equalsIgnoreCase("Status")).findFirst().orElse(null);
                if(em != null && !em.isEmpty()){
                    if (m.getAuthor().equals(jda.getSelfUser())) {
                        toClean.add(m);
                    }
                }
            }
            if (!toClean.isEmpty()) {
                try {
                    if (toClean.size() == 1)
                        toClean.get(0).delete().queue();
                    else
                        tc.deleteMessages(toClean).queue();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                };
            }
        });

        update(tc);
    }

    public static void update(TextChannel tc){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(() -> {
                    EmbedBuilder em = new EmbedBuilder();
                    try {
                        String title = "Status";
                        ArrayList<String> serverStats = new ArrayList<>();
                        int players = 0;
                        String time;

                        em.setTitle(title);
                        em.setColor(Color.decode("#036d7d"));

                        Document doc = Jsoup.parse(StatusCommand.getData("https://www.cubiccastles.com/status.php"));

                        Element div = doc.getElementsByTag("body").get(0);
                        String[] text = Jsoup.parse(div.html().replace("<br>", "break")).text().split("break");
                        List<String> str = Arrays.stream(text).map(s-> {
                            if(s.length() >= 15) return s;
                            return "";
                        }).collect(Collectors.toList());
                        for(String s : str){
                            if(!s.isEmpty()) {
                                String[] split = s.trim().split(" ", 4);
                                int serverNum = 0;
                                int player = 0;
                                try{
                                    player = Integer.parseInt(split[3].replaceAll("[^\\d]", ""));
                                    serverNum = Integer.parseInt(split[1].substring(split[1].length()-3).replaceAll("[^\\d]", ""));
                                    players += player > 1000 ? 0 : player;
                                }catch (NumberFormatException ignored){}
                                serverStats.add(String.format("%s %02d %s %s %s", "Server", (serverNum >= 1000 ? serverNum - 1000 : serverNum), ":", split[2], (split[2].trim().equalsIgnoreCase("down!") ? " (Down for "+ ConversionUtils.secondsToTime(player) + ")" : "")));
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
                        em.setFooter("This message updates every 10 seconds.");

                        if(m != null){
                            m = m.editMessage(em.build()).complete();
                        }
                        else{
                            m = tc.sendMessage(em.build()).complete();
                        }

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            , 0, 10, TimeUnit.SECONDS);
    }
}
