package commands.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CacheUtils {
    public static Map<String, String> caches = new HashMap<>();
    private static Map<String, String> urls = new HashMap<>();

    private static void initializeMap() {
        urls.put("craft", "https://www.cubiccastles.com/recipe_html/recipes.html");
        urls.put("item", "https://www.cubiccastles.com/recipe_html/recipes.html");
        urls.put("perk", "https://www.cubiccastles.com/recipe_html/levelup.html");
        urls.put("staff", "https://forums2.cubiccastles.com/index.php?p=/discussion/12/staff-list#latest");
        urls.put("forumrules", "https://forums2.cubiccastles.com/index.php?p=/discussion/8");
        urls.put("gamerules", "http://108.61.22.243/store/rules.ml");
        urls.put("news", "http://cubiccastles.com/news.txt");
    }

    public static String getCache(String type) {
        String data;
        if (caches.containsKey(type)) {
            data = caches.get(type);
        } else {
            setData();
            data = caches.get(type);
        }
        return data;
    }

    public static void setData() {
        initializeMap();
        for(String type : urls.keySet()) {
            try {
                StringBuilder html = new StringBuilder();
                try {
                    URL getUrl = new URL(urls.get(type));
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
                } catch (Exception ignored) {
                }
                while (html.toString().trim().isEmpty()) {
                    Thread.sleep(1000);
                }
                caches.put(type, html.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
