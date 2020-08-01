package utils;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Iterator;
import java.util.Random;

public class ImageURLUtil {
    public static String getContentType(String url) {
        String contentType = "";
        try {
            URL urls = new URL(url);
            URLConnection u = urls.openConnection();
            contentType = u.getContentType().split("/")[0].trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contentType;
    }

    public static File getFileFromURL(String url) {
        int generatedInteger = new Random().nextInt();
        File imageurl = new File("urltourl" + Math.abs(generatedInteger) + ".png");
        try {
            BufferedImage image = ImageIO.read(new URL(url));

            ImageIO.write(image, "png", imageurl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageurl;
    }

    public static String getImageURL(File file) {
        String imageurl = "";
        try {
            String clientID = Constants.IMGURID;
            URL url = new URL("https://api.imgur.com/3/image");
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Authorization", "Client-ID " + clientID);
            urlConn.setDoOutput(true);
            urlConn.connect();

            //Encode image to data
            BufferedImage image;
            //read image
            image = ImageIO.read(file);
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArray);
            byte[] byteImage = byteArray.toByteArray();
            String dataImage = Base64.getEncoder().encodeToString(byteImage);
            String data = URLEncoder.encode("image", "UTF-8") + "="
                    + URLEncoder.encode(dataImage, "UTF-8");
            //Write to image and upload
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            wr.write(data);
            wr.flush();

            //Get input/reply
            InputStreamReader read = new InputStreamReader(urlConn.getInputStream());
            BufferedReader each = new BufferedReader(read);
            String line;
            while ((line = each.readLine()) != null) {
                JSONObject all = new JSONObject(line);
                JSONObject databack = all.getJSONObject("data");
                imageurl = databack.getString("link");
            }
            read.close();
            each.close();
            urlConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageurl;
    }

    public static String sendPost(boolean isOverlayRequired, String imageUrl, String language) throws Exception {

        URL obj = new URL("https://api.ocr.space/parse/image"); // OCR API Endpoints
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");


        JSONObject postDataParams = new JSONObject();

        postDataParams.put("apikey", "helloworld");//TODO Add your Registered API key
        postDataParams.put("isOverlayRequired", isOverlayRequired);
        postDataParams.put("url", imageUrl);
        postDataParams.put("language", language);


        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(getPostDataString(postDataParams));
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //return result
        return String.valueOf(response);
    }

    private static String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}
