package normalCommands.bingo;

import botOwnerCommands.ExceptionHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.CacheUtils;
import utils.Msg;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BingoItem extends Command {
    static Map<Integer, String> largeItemPool = new HashMap<>();
    static Map<Integer, String> smallItemPool = new HashMap<>();
    private EventWaiter waiter;

    public BingoItem(EventWaiter waiter) {
        this.name = "bingoitem";
        this.aliases = new String[] { "bi" };
        this.category = new Category("Cubic Castles");
        this.ownerCommand = true;
        this.waiter = waiter;
    }

    private static Boolean checkSimilarStrings(String str1, String str2) {
        return str1.replaceAll("’", "'").toLowerCase().contains(str2.replaceAll("’", "'").toLowerCase());
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ", 2);
        if (args.length < 2) {
            Msg.badTimed(event, "Invalid command use.", 5, TimeUnit.SECONDS);
            return;
        }

        if (args[0].trim().equalsIgnoreCase("add")) {
            String item = args[1].trim().replaceAll("[^0-9a-zA-Z' ]", "");
            if (largeItemPool.containsValue(item)) {
                Msg.bad(event, "The provided item is already a part of the pool.");
            } else {
                try {
                    Document doc = Jsoup.parse(CacheUtils.getCache("item"));
                    String basePicURL = "http://cubiccastles.com/recipe_html/";

                    Elements itemDiv = doc.select("div#CONTAINER > div.HIDE_CONTAINER");
                    Elements itemList = itemDiv.get(2).select("tr > td");

                    Map<Integer, String[]> suggestions = new HashMap<>();
                    int suggCount = 0;

                    for(int i = 0; i < itemList.size(); i++){
                        String itemName = itemList.get(i).text().trim().replaceAll("[^0-9a-zA-Z' ]", "");
                        String itemImage = basePicURL + itemList.get(i).select("img").attr("src");

                        if(itemName.equalsIgnoreCase(item)){
                            largeItemPool.put(i, itemName);

                            addItemToDB(itemImage, itemName, i);

                            Msg.reply(event, "Added the item provided to the item pool.");
                            return;
                        }
                        else if(checkSimilarStrings(itemName, item)){
                            suggCount++;
                            suggestions.put(suggCount, new String[]{itemName, itemImage, String.valueOf(i)});
                            if(suggCount >= 5){
                                break;
                            }
                        }
                    }

                    StringBuilder sb = new StringBuilder();
                    for(int it : suggestions.keySet()){
                        sb.append(numberToEmoji(it)).append(" ").append(suggestions.get(it)[0]).append("\n");
                    }

                    new ButtonMenu.Builder()
                            .setDescription("Could not find an item with that name. Here are some possible suggestions.\n" +
                                    sb.toString().trim())
                            .setChoices(getEmojis(suggCount))
                            .addChoice(EmojiManager.getForAlias("x").getUnicode())
                            .setEventWaiter(waiter)
                            .setTimeout(20, TimeUnit.SECONDS)
                            .setColor(Color.orange)
                            .setAction(v -> {
                                if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":x:")) {
                                    Msg.replyTimed(event, "Cancelled adding item to the pool.", 5, TimeUnit.SECONDS);
                                }
                                else{
                                    int choice = emojiToNumber(EmojiParser.parseToAliases(v.getEmoji()));
                                    if(choice == 0){
                                        Msg.bad(event, "Invalid choice from the suggestion list.");
                                    }
                                    else {
                                        if (largeItemPool.containsValue(suggestions.get(choice)[0])) {
                                            Msg.bad(event, "The provided item is already a part of the pool.");
                                        } else {
                                            try {
                                                addItemToDB(suggestions.get(choice)[1], suggestions.get(choice)[0], Integer.parseInt(suggestions.get(choice)[2]));
                                                largeItemPool.put(Integer.parseInt(suggestions.get(choice)[2]), suggestions.get(choice)[0]);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            Msg.reply(event, "Added the item chosen to the item pool.");
                                        }
                                    }
                                }
                            })
                            .setFinalAction(me -> {
                                me.delete().queue();
                            }).build().display(event.getTextChannel());
                } catch (InsufficientPermissionException ex) {
                    event.getTextChannel().sendMessage(ex.getMessage()).queue();
                } catch (Exception e) {
                    ExceptionHandler.handleException(e, event.getMessage().getContentRaw(), "ItemCommand.java");
                }
            }
        } else if (args[0].trim().equalsIgnoreCase("remove")) {
            String item = args[1].trim().replaceAll("[^0-9a-zA-Z' ]", "");
            Map<Integer, String[]> suggestions = new HashMap<>();
            int suggCount = 0;

            for(int key : largeItemPool.keySet()){
                String itemName = largeItemPool.get(key).trim().replaceAll("[^0-9a-zA-Z' ]", "");
                if(itemName.equalsIgnoreCase(item)){
                    largeItemPool.remove(key);

                    try {
                        removeItemFromDB(itemName, key);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Msg.reply(event, "Removed the item provided from the item pool.");
                    return;
                }
                else if(checkSimilarStrings(itemName, item)){
                    suggCount++;
                    suggestions.put(suggCount, new String[]{itemName, String.valueOf(key)});
                    if(suggCount >= 5){
                        break;
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            for(int it : suggestions.keySet()){
                sb.append(numberToEmoji(it)).append(" ").append(suggestions.get(it)[0]).append("\n");
            }

            new ButtonMenu.Builder()
                    .setDescription("Could not find an item with that name. Here are some possible suggestions.\n" +
                            sb.toString().trim())
                    .setChoices(getEmojis(suggCount))
                    .addChoice(EmojiManager.getForAlias("x").getUnicode())
                    .setEventWaiter(waiter)
                    .setTimeout(20, TimeUnit.SECONDS)
                    .setColor(Color.orange)
                    .setAction(v -> {
                        if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":x:")) {
                            Msg.replyTimed(event, "Cancelled removing item from the pool.", 5, TimeUnit.SECONDS);
                        }
                        else{
                            int choice = emojiToNumber(EmojiParser.parseToAliases(v.getEmoji()));
                            if(choice == 0){
                                Msg.bad(event, "Invalid choice from the suggestion list.");
                            }
                            else {
                                try {
                                    removeItemFromDB(suggestions.get(choice)[0], Integer.parseInt(suggestions.get(choice)[1]));
                                    largeItemPool.remove(Integer.parseInt(suggestions.get(choice)[1]));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Msg.reply(event, "Removed the item chosen from the item pool.");
                            }
                        }
                    })
                    .setFinalAction(me -> {
                        me.delete().queue();
                    }).build().display(event.getTextChannel());
        } else if (args[0].trim().equalsIgnoreCase("generatePool")) {
            if(!smallItemPool.isEmpty()){
                new ButtonMenu.Builder()
                        .setDescription("The bingo pool isn't empty. Do you still want to clear it?")
                        .setChoices(EmojiManager.getForAlias("white_check_mark").getUnicode())
                        .addChoice(EmojiManager.getForAlias("x").getUnicode())
                        .setEventWaiter(waiter)
                        .setTimeout(20, TimeUnit.SECONDS)
                        .setColor(Color.orange)
                        .setAction(v -> {
                            if (EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":x:")) {
                                Msg.replyTimed(event, "Cancelled (re)generating pool.", 5, TimeUnit.SECONDS);
                            }
                            else if(EmojiParser.parseToAliases(v.getEmoji()).equalsIgnoreCase(":white_check_mark:")){
                                clearBingoPool();
                                try {
                                    int poolSize = Integer.parseInt(args[1]);
                                    if (poolSize > largeItemPool.size()) {
                                        Msg.bad(event, "The entered number is larger than the total items available.\n" +
                                                "Total items available: " + largeItemPool.size());
                                    } else {
                                        Msg.replyTimed(event, "Cleared existing pool and generating the pool of bingo of size " + poolSize + "...", 5, TimeUnit.SECONDS);
                                        Random random = new Random();
                                        int index = 0;
                                        Path workingDir = Paths.get(System.getProperty("user.dir"));
                                        File guildDir = new File(workingDir.resolve("db/cards/items").toUri());
                                        File outFile = new File(guildDir, "bingoPool.txt");
                                        if (!guildDir.exists()) {
                                            return;
                                        }
                                        outFile.createNewFile();
                                        FileWriter fw = new FileWriter(outFile, true);
                                        BufferedWriter bw = new BufferedWriter(fw);
                                        PrintWriter out = new PrintWriter(bw);
                                        while (smallItemPool.size() < poolSize) {
                                            int itemNum = random.nextInt(Collections.max(largeItemPool.keySet()));
                                            if (largeItemPool.containsKey(itemNum) && !smallItemPool.containsValue(largeItemPool.get(itemNum))) {
                                                smallItemPool.put(index, largeItemPool.get(itemNum));
                                                out.println(index+","+largeItemPool.get(itemNum));
                                                index++;
                                            }
                                        }
                                        out.close();
                                        bw.close();
                                        fw.close();
                                        Msg.reply(event, "Bingo pool generated of size " + poolSize + ".");
                                    }
                                } catch (NumberFormatException e) {
                                    Msg.badTimed(event, "Number entered is not valid.", 5, TimeUnit.SECONDS);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setFinalAction(me -> {
                            me.delete().queue();
                        }).build().display(event.getTextChannel());
            }
            else{
                try {
                    int poolSize = Integer.parseInt(args[1]);
                    if (poolSize > largeItemPool.size()) {
                        Msg.bad(event, "The entered number is larger than the total items available.\n" +
                                "Total items available: " + largeItemPool.size());
                    } else {
                        Msg.replyTimed(event, "Cleared existing pool and generating the pool of bingo of size " + poolSize + "...", 5, TimeUnit.SECONDS);
                        Random random = new Random();
                        int index = 0;
                        Path workingDir = Paths.get(System.getProperty("user.dir"));
                        File guildDir = new File(workingDir.resolve("db/cards/items").toUri());
                        File outFile = new File(guildDir, "bingoPool.txt");
                        if (!guildDir.exists()) {
                            return;
                        }
                        outFile.createNewFile();
                        FileWriter fw = new FileWriter(outFile, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw);
                        while (smallItemPool.size() < poolSize) {
                            int itemNum = random.nextInt(Collections.max(largeItemPool.keySet()));
                            if (largeItemPool.containsKey(itemNum) && !smallItemPool.containsValue(largeItemPool.get(itemNum))) {
                                smallItemPool.put(index, largeItemPool.get(itemNum));
                                out.println(index+","+largeItemPool.get(itemNum));
                                index++;
                            }
                        }
                        out.close();
                        bw.close();
                        fw.close();
                        Msg.reply(event, "Bingo pool generated of size " + poolSize + ".");
                    }
                } catch (NumberFormatException e) {
                    Msg.badTimed(event, "Number entered is not valid.", 5, TimeUnit.SECONDS);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Msg.badTimed(event, "Invalid command use.", 5, TimeUnit.SECONDS);
        }
    }

    private void clearBingoPool() {
        smallItemPool.clear();
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/cards/items").toUri());
        File outFile = new File(guildDir, "bingoPool.txt");
        if (!guildDir.exists()) {
            return;
        }
        outFile.delete();
    }

    private static void addItemToDB(String itemImage, String itemName, int itemID) throws IOException {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/cards/items").toUri());

        if (!guildDir.exists()) {
            guildDir.mkdirs();
        }
        File outFile = new File(guildDir, "itemPool.txt");
        File img = new File(guildDir, itemName+".png");
        ImageIO.write(ImageIO.read(new URL(itemImage)), "png", img);
        outFile.createNewFile();

        try (FileWriter fw = new FileWriter(outFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(itemID+","+itemName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void removeItemFromDB(String itemName, int itemID) throws IOException {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/cards/items").toUri());
        if (!guildDir.exists()) { return; }

        File img = new File(guildDir, itemName.trim()+".png");
        File inFile = new File(guildDir, "itemPool.txt");
        if(!inFile.exists()) { return; }
        if(img.exists()) { img.delete(); }

        File temp = File.createTempFile("tempool", ".txt", inFile.getParentFile());
        String charset = "UTF-8";
        String delete = itemID + "," + itemName.trim();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), charset));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(temp), charset));
        for (String line; (line = reader.readLine()) != null; ) {
            if (line.split(",")[0].trim().equalsIgnoreCase(String.valueOf(itemID))) {
                line = line.replace(delete, "").trim();
            }
            if (!line.equals("")) // don't write out blank lines
            {
                writer.println(line);
            }
        }
        reader.close();
        writer.close();

        inFile.delete();
        temp.renameTo(inFile);
    }

    public static String[] getEmojis(int numClasses) {
        ArrayList<String> emojis = new ArrayList<>();
        emojis.add(":one:");
        emojis.add(":two:");
        emojis.add(":three:");
        emojis.add(":four:");
        emojis.add(":five:");
        String[] emoji = new String[numClasses];
        for (int i = 0; i < numClasses; i++) {
            emoji[i] = EmojiManager.getForAlias(emojis.get(i)).getUnicode();
        }
        return emoji;
    }

    public static int emojiToNumber(String emoji) {
        switch (emoji) {
            case ":one:":
                return 1;
            case ":two:":
                return 2;
            case ":three:":
                return 3;
            case ":four:":
                return 4;
            case ":five:":
                return 5;
        }
        return 0;
    }

    public static String numberToEmoji(int emoji) {
        switch (emoji) {
            case 1:
                return ":one:";
            case 2:
                return ":two:";
            case 3:
                return ":three:";
            case 4:
                return ":four:";
            case 5:
                return ":five:";
        }
        return "";
    }

    public static void initializeItemPools(){
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/cards/items").toUri());
        if (!guildDir.exists()) return;
        File itemPool = new File(guildDir, "itemPool.txt");
        File bingoPool = new File(guildDir, "bingoPool.txt");
        if(!itemPool.exists()) return;
        if(!bingoPool.exists()) {
            try {
                bingoPool.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(itemPool));
            String line;
            while ((line = br.readLine()) != null) {
                largeItemPool.put(Integer.parseInt(line.split(",")[0]), line.split(",")[1]);
            }
            br = new BufferedReader(new FileReader(bingoPool));
            while ((line = br.readLine()) != null) {
                smallItemPool.put(Integer.parseInt(line.split(",")[0]), line.split(",")[1]);
            }
            br.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
