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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MarkCommand extends Command {
    public MarkCommand() {
        this.name = "mark";
        this.aliases = new String[]{"m"};
        this.category = new Category("Normal");
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        boolean isFromBingo = event.getGuild().getCategoryById("756887929808224258").getTextChannels().parallelStream().anyMatch(c -> event.getTextChannel().getId().equals(c.getId()));
        if(!event.getGuild().getId().equals("240614697848537089") && !event.getGuild().getId().equals("705622006652993607") && !isFromBingo) return;

        Map<String, Integer[]> position = new HashMap<>();
        position.put("A1", new Integer[]{15, 273});
        position.put("A2", new Integer[]{260, 273});
        position.put("A3", new Integer[]{505, 273});
        position.put("A4", new Integer[]{750, 273});
        position.put("A5", new Integer[]{990, 273});

        position.put("B1", new Integer[]{15, 520});
        position.put("B2", new Integer[]{260, 520});
        position.put("B3", new Integer[]{505, 520});
        position.put("B4", new Integer[]{750, 520});
        position.put("B5", new Integer[]{990, 520});

        position.put("C1", new Integer[]{15, 762});
        position.put("C2", new Integer[]{260, 762});
        position.put("C3", new Integer[]{505, 762});
        position.put("C4", new Integer[]{750, 762});
        position.put("C5", new Integer[]{990, 762});

        position.put("D1", new Integer[]{15, 1005});
        position.put("D2", new Integer[]{260, 1005});
        position.put("D3", new Integer[]{505, 1005});
        position.put("D4", new Integer[]{750, 1005});
        position.put("D5", new Integer[]{990, 1005});

        position.put("E1", new Integer[]{15, 1250});
        position.put("E2", new Integer[]{260, 1250});
        position.put("E3", new Integer[]{505, 1250});
        position.put("E4", new Integer[]{750, 1250});
        position.put("E5", new Integer[]{990, 1250});

        String choice = event.getArgs().trim().toUpperCase();
        if(!position.containsKey(choice) || choice.isEmpty()) {
            Msg.badTimed(event, "Invalid position to mark, please supply an argument ranging from `A1` to `E5`.", 5, TimeUnit.SECONDS);
        }
        else if(!GenerateBingo.bingoBoard.containsKey(event.getAuthor().getId())) {
            Msg.badTimed(event, "You do not have a bingo card. Please generate one using `"+ Constants.D_PREFIX +"cgen` command.", 5, TimeUnit.SECONDS);
        }
        else {
            try {
                Map.Entry<String[][], BufferedImage> board = GenerateBingo.bingoBoard.get(event.getAuthor().getId());

//                int first = choice.toLowerCase().charAt(0) - 'a' + 1;
//                int second = Character.getNumericValue(choice.charAt(1));
//                if (!RollCommand.chosenImages.contains(board.getKey()[first-1][second-1])) {
//                    Msg.badTimed(event, "Cannot mark this item since it's not rolled yet.", 5, TimeUnit.SECONDS);
//                    return;
//                }

                Path workingDir = Paths.get(System.getProperty("user.dir"));
                File guildDir = new File(workingDir.resolve("db/global").toUri());
                BufferedImage cross = ImageIO.read(new File(guildDir, "cross.png"));
                BufferedImage bingoSlot = ImageIO.read(new File(guildDir, "bingoslot.png"));

                Graphics gBoard = board.getValue().getGraphics();
                Dimension dim = GenerateBingo.getScaledDimension(new Dimension(cross.getWidth(), cross.getHeight()), new Dimension(bingoSlot.getWidth(), bingoSlot.getHeight()));
                gBoard.drawImage(cross.getScaledInstance((int)dim.getWidth(), (int)dim.getHeight(), Image.SCALE_SMOOTH), position.get(choice)[0], position.get(choice)[1], null);
                gBoard.dispose();

                File markedCardDir = new File(workingDir.resolve("db/cards/markedbingocards/").toUri());
                if (!markedCardDir.exists()) markedCardDir.mkdirs();
                File outputImgMark = new File(markedCardDir, URLEncoder.encode(event.getAuthor().getId() + ".png", "utf-8"));
                ImageIO.write(board.getValue(), "png", outputImgMark);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(board.getValue(), "png", os);                          // Passing: ​(RenderedImage im, String formatName, OutputStream output)
                InputStream is = new ByteArrayInputStream(os.toByteArray());

                event.getMessage().delete().queue();
                event.getTextChannel().sendMessage(event.getAuthor().getAsMention() + ", the spot `"+choice+"` has been marked. Here is your new board..").addFile(is,"board.png").queue();

                is.close();
                os.close();
                cross.flush();
                bingoSlot.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
