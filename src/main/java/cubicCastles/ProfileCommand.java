package cubicCastles;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.validator.routines.UrlValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Msg;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

public class ProfileCommand extends Command {
    public ProfileCommand() {
        this.name = "profile";
        this.aliases = new String[]{"profiles", "id"};
        this.category = new Category("Cubic Castles");
        this.ownerCommand = false;
        this.cooldown = 3;
        this.cooldownScope = CooldownScope.GLOBAL;
    }

    private static String getJson(String userID) {
        URL obj;
        StringBuffer response = new StringBuffer();
        try {
            obj = uriWithPort(new URI("https://forums2.cubiccastles.com/index.php?p=/profile.json/" + URLEncoder.encode(userID, "UTF-8") + "/User")).toURL();
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setUseCaches(true);
            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (con.getResponseCode() == 404) {
                response.append("None");
                return String.valueOf(response);
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return result
        return String.valueOf(response);
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);

        if (event.getArgs().isEmpty()) {
            Msg.bad(event, "Invalid command argument. USAGE: .profile <userID>");
        } else {
            String userID = event.getArgs().replaceAll("spc", " ");

            String response = getJson(userID);

            //All the items

            if (response.equalsIgnoreCase("none")) {
                Msg.bad(event, "User not found. Make sure the provided userID or username is correct. If you have space before and after your username, please enter `spc` instead of actual space character.");
            } else {
                JSONObject obj = new JSONObject(response);

                JSONObject profile = obj.getJSONObject("Profile");

                em.setTitle(profile.getString("Name") + " Info");

                String photo = profile.getString("Photo");
                UrlValidator urlValidator = new UrlValidator();
                if (urlValidator.isValid(photo)) {
                    em.setThumbnail(photo);
                } else {
                    String[] split = profile.getString("Photo").split("/");
                    photo = photo.replaceAll(split[2], "p" + split[2]);
                    em.setThumbnail("http://forums2.cubiccastles.com/uploads/" + photo);
                }

                if (!profile.isNull("UserID") && !profile.getString("UserID").isEmpty()) {
                    em.addField("UserID", profile.getString("UserID"), true);
                }

                if (!profile.isNull("Location") && !profile.getString("Location").isEmpty()) {
                    em.addField("Location", profile.getString("Location"), true);
                }
                if (!profile.isNull("About") && !profile.getString("About").isEmpty()) {
                    em.addField("About", profile.getString("About"), true);
                }
                if (!profile.isNull("CountVisits") && !profile.getString("CountVisits").isEmpty()) {
                    em.addField("Visit Count", profile.getString("CountVisits"), true);
                }
                if (!profile.isNull("DateFirstVisit") && !profile.getString("DateFirstVisit").isEmpty()) {
                    em.addField("Join Date", profile.getString("DateFirstVisit"), true);
                }
                if (!profile.isNull("DateLastActive") && !profile.getString("DateLastActive").isEmpty()) {
                    em.addField("Last Active", profile.getString("DateLastActive"), true);
                }
                if (!profile.isNull("CountDiscussions") && !profile.getString("CountDiscussions").isEmpty()) {
                    em.addField("Discussions", profile.getString("CountDiscussions"), true);
                }
                if (!profile.isNull("CountComments") && !profile.getString("CountComments").isEmpty()) {
                    em.addField("Comments", profile.getString("CountComments"), true);
                }

                StringBuilder ranks = new StringBuilder();
                try {
                    JSONArray arrRoles = obj.getJSONArray("UserRoles");
                    for (Object temp : arrRoles) {
                        ranks.append(temp.toString()).append("\n");
                    }
                } catch (JSONException e) {
                    JSONObject objRoles = obj.getJSONObject("UserRoles");
                    for (String key : objRoles.keySet()) {
                        ranks.append(objRoles.getString(key)).append("\n");
                    }
                }

                em.addField("Roles", ranks.toString(), true);

                event.getTextChannel().sendMessage(em.build()).queue();
            }

        }
    }

    private static URI uriWithPort(URI uri) {
//        try {
//            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), 80,
//                    uri.getPath(), uri.getQuery(), uri.getFragment());
//        } catch (URISyntaxException e) {
//            return uri;
//        }
        return uri;
    }

}
