package normalCommands.bingo;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import utils.Constants;
import utils.Msg;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UnmarkCommand extends Command {
    public UnmarkCommand() {
        this.name = "unmark";
        this.aliases = new String[]{"um"};
        this.category = new Category("Normal");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        boolean isFromBingo = event.getGuild().getCategoryById("756887929808224258").getTextChannels().parallelStream().anyMatch(c -> event.getTextChannel().getId().equals(c.getId()));
        if(!event.getGuild().getId().equals("240614697848537089") && !event.getGuild().getId().equals("705622006652993607") && !isFromBingo) return;

        Map<String, Integer[]> position = new HashMap<>();
        position.put("A1", new Integer[]{15+60, 273+40});
        position.put("A2", new Integer[]{260+60, 273+40});
        position.put("A3", new Integer[]{505+60, 273+40});
        position.put("A4", new Integer[]{750+60, 273+40});
        position.put("A5", new Integer[]{990+60, 273+40});

        position.put("B1", new Integer[]{15+60, 520+40});
        position.put("B2", new Integer[]{260+60, 520+40});
        position.put("B3", new Integer[]{505+60, 520+40});
        position.put("B4", new Integer[]{750+60, 520+40});
        position.put("B5", new Integer[]{990+60, 520+40});

        position.put("C1", new Integer[]{15+60, 762+40});
        position.put("C2", new Integer[]{260+60, 762+40});
        position.put("C3", new Integer[]{505+60, 762+40});
        position.put("C4", new Integer[]{750+60, 762+40});
        position.put("C5", new Integer[]{990+60, 762+40});

        position.put("D1", new Integer[]{15+60, 1005+40});
        position.put("D2", new Integer[]{260+60, 1005+40});
        position.put("D3", new Integer[]{505+60, 1005+40});
        position.put("D4", new Integer[]{750+60, 1005+40});
        position.put("D5", new Integer[]{990+60, 1005+40});

        position.put("E1", new Integer[]{15+60, 1250+40});
        position.put("E2", new Integer[]{260+60, 1250+40});
        position.put("E3", new Integer[]{505+60, 1250+40});
        position.put("E4", new Integer[]{750+60, 1250+40});
        position.put("E5", new Integer[]{990+60, 1250+40});

        String choice = event.getArgs().trim().toUpperCase();
        event.getMessage().delete().queue();
        if(!position.containsKey(choice) || choice.isEmpty()) {
            Msg.badTimed(event, "Invalid position to unmark, please supply an argument ranging from `A1` to `E5`.", 5, TimeUnit.SECONDS);
        }
        else if(!GenerateBingo.bingoBoard.containsKey(event.getAuthor().getId())) {
            Msg.badTimed(event, "You do not have a bingo card. Please generate one using `"+ Constants.D_PREFIX +"cgen` command.", 5, TimeUnit.SECONDS);
        }
        else {
            Map.Entry<String[][], BufferedImage> board = GenerateBingo.bingoBoard.get(event.getAuthor().getId());
            Path workingDir = Paths.get(System.getProperty("user.dir"));
            File guildDir = new File(workingDir.resolve("db/global").toUri());

            BufferedImage cross = null;
            BufferedImage bingoSlot = null;
            BufferedImage newImg = null;
            try {
                cross = ImageIO.read(new File(guildDir, "cross.png"));
                bingoSlot = ImageIO.read(new File(guildDir, "bingoslot.png"));
                newImg = ImageIO.read(new File(System.getProperty("user.dir")+"/db/cards/bingocards/", event.getAuthor().getId()+".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            Graphics gBoard = newImg.getGraphics();
            Dimension dim = GenerateBingo.getScaledDimension(new Dimension(cross.getWidth(), cross.getHeight()), new Dimension(bingoSlot.getWidth(), bingoSlot.getHeight()));

            for(int i = 0; i < board.getKey().length; i++){
                for(int j = 0; j < board.getKey()[0].length; j++){
                    String s = (char) (i+1 + 64) +""+(j+1);
                    int rgb = board.getValue().getRGB(position.get(s)[0], position.get(s)[1]);
                    int  red = (rgb & 0x00ff0000) >> 16;
                    int  green = (rgb & 0x0000ff00) >> 8;
                    int  blue = rgb & 0x000000ff;

                    if(red == 255 && green == 0 && blue == 0){
                        if(!choice.equalsIgnoreCase(s)) {
                            gBoard.drawImage(cross.getScaledInstance((int)dim.getWidth(), (int)dim.getHeight(), Image.SCALE_SMOOTH), position.get(s)[0]-60, position.get(s)[1]-40, null);
                        }
                    }
                    else if(choice.equalsIgnoreCase(s)) {
                        Msg.badTimed(event, "The position `"+choice+"` on the board is not marked.", 5, TimeUnit.SECONDS);
                        gBoard.dispose();
                        return;
                    }
                }
            }

            gBoard.dispose();
            board.getValue().flush();
            GenerateBingo.bingoBoard.put(event.getAuthor().getId(), new AbstractMap.SimpleEntry<>(board.getKey(), newImg));

            File markedCardDir = new File(workingDir.resolve("db/cards/markedbingocards/").toUri());
            if (!markedCardDir.exists()) markedCardDir.mkdirs();
            File outputImgMark = null;
            try {
                outputImgMark = new File(markedCardDir, URLEncoder.encode(event.getAuthor().getId() + ".png", "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                ImageIO.write(newImg, "png", outputImgMark);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(newImg, "png", os);                          // Passing: ​(RenderedImage im, String formatName, OutputStream output)
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream is = new ByteArrayInputStream(os.toByteArray());

            event.getTextChannel().sendMessage(event.getAuthor().getAsMention() + ", the spot `"+choice+"` has been unmarked. Here is your new board..").addFile(is,"board.png").queue();
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
