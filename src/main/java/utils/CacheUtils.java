package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CacheUtils {
    public static Map<String, String> caches = new HashMap<>();

    private static String[] getUrlInfo(String type) {
        String[] url = new String[2];
        switch (type.toLowerCase()) {
            case "craft": {
                url[0] = "https://cubiccastles.com/recipe_html/recipes.html";
                url[1] = "4500";
                break;
            }
            case "item": {
                url[0] = "https://www.cubiccastles.com/recipe_html/recipes.html";
                url[1] = "5000";
                break;
            }
            case "perk": {
                url[0] = "https://www.cubiccastles.com/recipe_html/levelup.html";
                url[1] = "7100";
                break;
            }
            case "price": {
                url[0] = "https://forums2.cubiccastles.com/index.php?p=/discussion/4169/cubic-castles-prices/p1";
                url[1] = "5";
                break;
            }
            case "raffle": {
                url[0] = "https://forums2.cubiccastles.com";
                url[1] = "6000";
                break;
            }
            case "staff": {
                url[0] = "https://forums2.cubiccastles.com/index.php?p=/discussion/12/staff-list#latest";
                url[1] = "7200";
                break;
            }
            case "status": {
                url[0] = "https://www.cubiccastles.com/status.php";
                url[1] = "1";
                break;
            }
            case "forumrules": {
                url[0] = "https://forums2.cubiccastles.com/index.php?p=/discussion/8";
                url[1] = "1440";
                break;
            }
            case "gamerules": {
                url[0] = "http://108.61.22.243/store/rules.ml";
                url[1] = "1440";
                break;
            }
            case "news": {
                url[0] = "http://cubiccastles.com/news.txt";
                url[1] = "5";
                break;
            }
            case "event": {
                url[0] = "https://forums2.cubiccastles.com/index.php?p=/categories/events-contests";
                url[1] = "30";
            }
            case "newprice": {
                url[0] = "https://forums2.cubiccastles.com/index.php?p=/discussion/27821/cubic-castles-prices/p1";
                url[1] = "1440";
                break;
            }
        }
        return url;
    }

    public static String getCache(String type) {
        String data;
        if (caches.containsKey(getUrlInfo(type)[0])) {
            data = caches.get(getUrlInfo(type)[0]);
        } else {
            setData(getUrlInfo(type)[0], Integer.parseInt(getUrlInfo(type)[1]));
            data = caches.get(getUrlInfo(type)[0]);
        }
        return data;
    }

    private static void setData(String url, int time) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
        try {
            StringBuilder html = new StringBuilder();
            scheduler.scheduleAtFixedRate(() -> {
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
                }
            , 0, time, TimeUnit.MINUTES);
            while(html.toString().trim().isEmpty()) {
                Thread.sleep(1000);
            }
            caches.put(url, html.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
