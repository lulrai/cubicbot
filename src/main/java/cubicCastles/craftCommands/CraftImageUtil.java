package cubicCastles.craftCommands;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CraftImageUtil {
    private static BufferedImage getBackground(int numCards, int numPlus, String itemType) {
        BufferedImage background = null;

        BufferedImage orgBack = null;
        BufferedImage cards;
        BufferedImage plus;

        List<int[]> cardPos = new ArrayList<>();
        List<int[]> plusPos = new ArrayList<>();

        String cardURL;
        String backgroundURL;
        String plusURL = "https://i.imgur.com/UeCMs1O.png";

        try {
            switch (itemType.toLowerCase()) {
                case "layout1": {
                    cardURL = "https://i.imgur.com/LM2W5lP.png";
                    backgroundURL = "https://i.imgur.com/gZsuGFp.png";

                    final int[] cardPos1 = {5, 5};
                    final int[] cardPos2 = {170, 5};
                    final int[] cardPos3 = {333, 5};
                    final int[] cardPos4 = {493, 5};
                    cardPos.add(cardPos1);
                    cardPos.add(cardPos2);
                    cardPos.add(cardPos3);
                    cardPos.add(cardPos4);

                    final int[] resultPos = {602, 5};

                    final int[] plusPos1 = {122, 40};
                    final int[] plusPos2 = {285, 40};
                    final int[] plusPos3 = {447, 40};
                    plusPos.add(plusPos1);
                    plusPos.add(plusPos2);
                    plusPos.add(plusPos3);

                    orgBack = ImageIO.read(new URL(backgroundURL));
                    cards = ImageIO.read(new URL(cardURL));
                    plus = ImageIO.read(new URL(plusURL));

                    Graphics backG = orgBack.getGraphics();
                    backG.drawImage(cards, resultPos[0], resultPos[1], null);
                    for (int i = 0; i < numCards; i++) {
                        backG.drawImage(cards, cardPos.get(i)[0], cardPos.get(i)[1], null);
                    }

                    for (int i = 0; i < numPlus; i++) {
                        backG.drawImage(plus, plusPos.get(i)[0], plusPos.get(i)[1], null);
                    }
                    cards.flush();
                    plus.flush();
                    backG.dispose();
                    cardPos.clear();
                    plusPos.clear();
                    break;
                }
                case "layout2": {
                    backgroundURL = "https://i.imgur.com/0IYyitR.png";
                    cardURL = "https://i.imgur.com/w5aVgLF.png";

                    final int[] cardPos1 = {13, 10};
                    final int[] cardPos2 = {158, 10};
                    final int[] cardPos3 = {313, 10};
                    final int[] cardPos4 = {468, 10};
                    cardPos.add(cardPos1);
                    cardPos.add(cardPos2);
                    cardPos.add(cardPos3);
                    cardPos.add(cardPos4);

                    final int[] resultPos = {713, 10};
                    final int[] toolPos = {590, 10};

                    final int[] plusPos1 = {121, 43};
                    final int[] plusPos2 = {270, 43};
                    final int[] plusPos3 = {425, 43};
                    plusPos.add(plusPos1);
                    plusPos.add(plusPos2);
                    plusPos.add(plusPos3);

                    orgBack = ImageIO.read(new URL(backgroundURL));
                    cards = ImageIO.read(new URL(cardURL));
                    plus = ImageIO.read(new URL(plusURL));

                    Graphics backG = orgBack.getGraphics();

                    backG.drawImage(cards, resultPos[0], resultPos[1], null);
                    backG.drawImage(cards, toolPos[0], toolPos[1], null);

                    for (int i = 0; i < numCards; i++) {
                        backG.drawImage(cards, cardPos.get(i)[0], cardPos.get(i)[1], null);
                    }

                    for (int i = 0; i < numPlus; i++) {
                        backG.drawImage(plus, plusPos.get(i)[0], plusPos.get(i)[1], null);
                    }
                    cards.flush();
                    plus.flush();
                    backG.dispose();
                    cardPos.clear();
                    plusPos.clear();
                    break;
                }
                case "layout3": {
                    backgroundURL = "https://i.imgur.com/XEEWYei.png";
                    cardURL = "https://i.imgur.com/w5aVgLF.png";
                    String textBack = "https://i.imgur.com/8n7H55F.png";

                    final int[] cardPos1 = {13, 5};

                    final int[] resultPos = {538, 5};
                    final int[] textPos = {162, 43};

                    orgBack = ImageIO.read(new URL(backgroundURL));
                    cards = ImageIO.read(new URL(cardURL));
                    BufferedImage txtBack = ImageIO.read(new URL(textBack));

                    Graphics backG = orgBack.getGraphics();

                    backG.drawImage(cards, resultPos[0], resultPos[1], null);
                    backG.drawImage(txtBack, textPos[0], textPos[1], null);
                    backG.drawImage(cards, cardPos1[0], cardPos1[1], null);

                    cards.flush();
                    txtBack.flush();
                    backG.dispose();
                    break;
                }
            }
            background = orgBack;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return background;
    }

    static File getCompleted(int numCards, List<BufferedImage> itemImages, List<BufferedImage> itemMult, List<String> names, String itemType, String fileName) {
        BufferedImage orgBack;

        List<int[]> imagePos = new ArrayList<>();
        List<int[]> strPos = new ArrayList<>();
        List<int[]> multPos = new ArrayList<>();

        if (itemType.equalsIgnoreCase("Basic Crafting Recipes") ||
                itemType.equalsIgnoreCase("Coloring Items with Dye") ||
                itemType.equalsIgnoreCase("Cooking") ||
                itemType.equalsIgnoreCase("Cut-O-Matik")) {

            orgBack = getBackground(numCards, numCards - 1, "layout1");

            final int[] imgPos1 = {28, 20};
            final int[] imgPos2 = {195, 20};
            final int[] imgPos3 = {355, 20};
            final int[] imgPos4 = {517, 20};
            imagePos.add(imgPos1);
            imagePos.add(imgPos2);
            imagePos.add(imgPos3);
            imagePos.add(imgPos4);

            final int[] strPos1 = {28, 120};
            final int[] strPos2 = {203, 120};
            final int[] strPos3 = {359, 120};
            final int[] strPos4 = {520, 120};
            strPos.add(strPos1);
            strPos.add(strPos2);
            strPos.add(strPos3);
            strPos.add(strPos4);

            final int[] multPos1 = {10, 5};
            final int[] multPos2 = {175, 5};
            final int[] multPos3 = {338, 5};
            final int[] multPos4 = {498, 5};
            multPos.add(multPos1);
            multPos.add(multPos2);
            multPos.add(multPos3);
            multPos.add(multPos4);

            final int[] resultImgPos = {622, 20};

            final int[] resultStrPos = {616, 120};

            Graphics backG = orgBack.getGraphics();
            backG.setFont(backG.getFont().deriveFont(12f));
            backG.drawImage(itemImages.get(itemImages.size() - 1), resultImgPos[0], resultImgPos[1], null);
            backG.drawString(names.get(names.size() - 1), resultStrPos[0], resultStrPos[1]);
            for (int i = 0; i < numCards; i++) {
                backG.drawImage(itemImages.get(i), imagePos.get(i)[0], imagePos.get(i)[1], null);
                backG.drawString(names.get(i), strPos.get(i)[0], strPos.get(i)[1]);
                if (itemMult.get(i) != null) {
                    backG.drawImage(itemMult.get(i), multPos.get(i)[0], multPos.get(i)[1], null);
                }
            }

            backG.dispose();
        } else if (itemType.equalsIgnoreCase("Crafting Recipes with Tools")) {

            orgBack = getBackground(numCards, numCards - 1, "layout2");

            final int[] imgPos1 = {38, 25};
            final int[] imgPos2 = {184, 25};
            final int[] imgPos3 = {333, 25};
            final int[] imgPos4 = {500, 25};
            imagePos.add(imgPos1);
            imagePos.add(imgPos2);
            imagePos.add(imgPos3);
            imagePos.add(imgPos4);

            final int[] strPos1 = {40, 120};
            final int[] strPos2 = {198, 120};
            final int[] strPos3 = {350, 120};
            final int[] strPos4 = {490, 120};
            strPos.add(strPos1);
            strPos.add(strPos2);
            strPos.add(strPos3);
            strPos.add(strPos4);

            final int[] multPos1 = {14, 10};
            final int[] multPos2 = {160, 10};
            final int[] multPos3 = {313, 10};
            final int[] multPos4 = {468, 10};
            multPos.add(multPos1);
            multPos.add(multPos2);
            multPos.add(multPos3);
            multPos.add(multPos4);

            final int[] resultImgPos = {737, 20};

            final int[] resultStrPos = {734, 120};

            final int[] toolImgPos = {613, 20};

            final int[] toolStrPos = {611, 120};

            Graphics backG = orgBack.getGraphics();

            backG.setFont(backG.getFont().deriveFont(12f));
            backG.drawImage(itemImages.get(itemImages.size() - 1), resultImgPos[0], resultImgPos[1], null);
            backG.drawString(names.get(names.size() - 1), resultStrPos[0], resultStrPos[1]);

            backG.drawImage(itemImages.get(itemImages.size() - 2), toolImgPos[0], toolImgPos[1], null);
            backG.drawString(names.get(names.size() - 2), toolStrPos[0], toolStrPos[1]);

            for (int i = 0; i < numCards; i++) {
                backG.drawImage(itemImages.get(i), imagePos.get(i)[0], imagePos.get(i)[1], null);
                backG.drawString(names.get(i), strPos.get(i)[0], strPos.get(i)[1]);
                if (itemMult.get(i) != null) {
                    backG.drawImage(itemMult.get(i), multPos.get(i)[0], multPos.get(i)[1], null);
                }
            }

            backG.dispose();
        } else if (itemType.equalsIgnoreCase("Forging Items") ||
                itemType.equalsIgnoreCase("Ingredient Extraction") ||
                itemType.equalsIgnoreCase("Distillation")) {

            orgBack = getBackground(numCards, numCards - 1, "layout3");

            final int[] imgPos1 = {35, 25};

            final int[] strPos1 = {30, 120};

            final int[] multPos1 = {14, 5};

            final int[] resultImgPos = {578, 25};
            final int[] resultStrPos = {575, 120};
            final int[] processStrPos = {175, 66};

            Graphics backG = orgBack.getGraphics();
            backG.setFont(backG.getFont().deriveFont(12f));

            backG.drawImage(itemImages.get(itemImages.size() - 1), resultImgPos[0], resultImgPos[1], null);
            backG.drawString(names.get(names.size() - 1), resultStrPos[0], resultStrPos[1]);

            backG.drawImage(itemImages.get(0), imgPos1[0], imgPos1[1], null);
            backG.drawString(names.get(0), strPos1[0], strPos1[1]);
            if (itemMult.get(0) != null) {
                backG.drawImage(itemMult.get(0), multPos1[0], multPos1[1], null);
            }

            backG.drawString(names.get(1), processStrPos[0], processStrPos[1]);

            backG.dispose();
        } else {
            return null;
        }
        File outputfile = null;
        try {
            Path workingDir = Paths.get(System.getProperty("user.dir"));
            File cacheDir = new File(workingDir.resolve("db/cache/").toUri());
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            outputfile = new File(cacheDir, URLEncoder.encode(fileName + ".png", "utf-8"));
            ImageIO.write(orgBack, "png", outputfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputfile;
    }
}
