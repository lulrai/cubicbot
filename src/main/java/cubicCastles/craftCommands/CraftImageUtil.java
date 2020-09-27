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

class CraftImageUtil {
    private static List<BufferedImage> getImages(List<BufferedImage> itemImages, List<BufferedImage> itemMult, List<String> names) {
        List<BufferedImage> cards = new ArrayList<>();
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File globalDir = new File(workingDir.resolve("db/global/").toUri());

        for(int i = 0; i < itemImages.size(); i++) {
            BufferedImage card = null;
            try {
                card = ImageIO.read(new File(globalDir, "craftcard.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert card != null;
            Graphics backG = card.getGraphics();
            backG.setFont(backG.getFont().deriveFont(12f));

            int imageX = (card.getWidth() - itemImages.get(i).getWidth()) / 2;
            int imageY = (card.getHeight() - (int)(itemImages.get(i).getHeight()*1.7)) / 2;
            backG.drawImage(itemImages.get(i), imageX, imageY, null);

            FontMetrics fm = backG.getFontMetrics();
            String[] strs = names.get(i).trim().split(" ", 2);
            int textY = 100;
            for(String s : strs){
                int textX = (card.getWidth() - fm.stringWidth(s)) / 2;
                backG.drawString(s, textX, textY);
                textY += 20;
            }

            if (itemMult.get(i) != null) {
                backG.drawImage(itemMult.get(i), 2, 2, null);
            }

            backG.dispose();
            cards.add(card);
        }
        return cards;
    }


    private static BufferedImage getBackground(int numPlus, String itemType) {
        BufferedImage background = null;

        BufferedImage orgBack = null;
        BufferedImage plus;

        List<int[]> plusPos = new ArrayList<>();

        String backgroundURL;
        String plusURL = "https://i.imgur.com/UeCMs1O.png";

        try {
            switch (itemType.toLowerCase()) {
                case "layout1": {
                    backgroundURL = "https://i.imgur.com/gZsuGFp.png";

                    final int[] plusPos1 = {122, 40};
                    final int[] plusPos2 = {285, 40};
                    final int[] plusPos3 = {447, 40};
                    plusPos.add(plusPos1);
                    plusPos.add(plusPos2);
                    plusPos.add(plusPos3);

                    orgBack = ImageIO.read(new URL(backgroundURL));
                    plus = ImageIO.read(new URL(plusURL));

                    Graphics backG = orgBack.getGraphics();

                    for (int i = 0; i < numPlus; i++) {
                        backG.drawImage(plus, plusPos.get(i)[0], plusPos.get(i)[1], null);
                    }
                    plus.flush();
                    backG.dispose();
                    plusPos.clear();
                    break;
                }
                case "layout2": {
                    backgroundURL = "https://i.imgur.com/0IYyitR.png";

                    final int[] plusPos1 = {121, 43};
                    final int[] plusPos2 = {270, 43};
                    final int[] plusPos3 = {425, 43};
                    plusPos.add(plusPos1);
                    plusPos.add(plusPos2);
                    plusPos.add(plusPos3);

                    orgBack = ImageIO.read(new URL(backgroundURL));
                    plus = ImageIO.read(new URL(plusURL));

                    Graphics backG = orgBack.getGraphics();

                    for (int i = 0; i < numPlus; i++) {
                        backG.drawImage(plus, plusPos.get(i)[0], plusPos.get(i)[1], null);
                    }
                    plus.flush();
                    backG.dispose();
                    plusPos.clear();
                    break;
                }
                case "layout3": {
                    backgroundURL = "https://i.imgur.com/XEEWYei.png";
                    orgBack = ImageIO.read(new URL(backgroundURL));
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

        List<BufferedImage> images = getImages(itemImages, itemMult, names);

        List<int[]> cardPos = new ArrayList<>();

        if (itemType.equalsIgnoreCase("Basic Crafting Recipes") ||
                itemType.equalsIgnoreCase("Coloring Items with Dye") ||
                itemType.equalsIgnoreCase("Cooking") ||
                itemType.equalsIgnoreCase("Cut-O-Matik")) {

            orgBack = getBackground(numCards - 1, "layout1");

            final int[] cardPos1 = {5, 5};
            final int[] cardPos2 = {170, 5};
            final int[] cardPos3 = {333, 5};
            final int[] cardPos4 = {493, 5};
            cardPos.add(cardPos1);
            cardPos.add(cardPos2);
            cardPos.add(cardPos3);
            cardPos.add(cardPos4);

            final int[] resultPos = {602, 5};

            Graphics backG = orgBack.getGraphics();
            backG.setFont(backG.getFont().deriveFont(12f));
            backG.drawImage(images.get(images.size() - 1), resultPos[0], resultPos[1], null);
            for (int i = 0; i < numCards; i++) {
                backG.drawImage(images.get(i), cardPos.get(i)[0], cardPos.get(i)[1], null);
            }

            backG.dispose();
        } else if (itemType.equalsIgnoreCase("Crafting Recipes with Tools")) {

            orgBack = getBackground(numCards - 1, "layout2");

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

            Graphics backG = orgBack.getGraphics();

            backG.drawImage(images.get(images.size()-1), resultPos[0], resultPos[1], null);
            backG.drawImage(images.get(images.size()-2), toolPos[0], toolPos[1], null);

            for (int i = 0; i < numCards; i++) {
                backG.drawImage(images.get(i), cardPos.get(i)[0], cardPos.get(i)[1], null);
            }

            backG.dispose();
        } else if (itemType.equalsIgnoreCase("Forging Items") ||
                itemType.equalsIgnoreCase("Ingredient Extraction") ||
                itemType.equalsIgnoreCase("Distillation")) {

            orgBack = getBackground(numCards - 1, "layout3");
            String textBack = "https://i.imgur.com/8n7H55F.png";

            BufferedImage textBg = null;
            try {
                textBg = ImageIO.read(new URL(textBack));
            } catch (IOException e) {
                e.printStackTrace();
            }

            final int[] cardPos1 = {13, 5};
            final int[] resultPos = {538, 5};
            final int[] textPos = {162, 43};

            Graphics backG = orgBack.getGraphics();
            backG.setFont(backG.getFont().deriveFont(14f));

            backG.drawImage(images.get(images.size() - 1), resultPos[0], resultPos[1], null);
            backG.drawImage(images.get(0), cardPos1[0], cardPos1[1], null);

            FontMetrics fm = backG.getFontMetrics();
            int textY = 30;
            int textX = (textBg.getWidth() - fm.stringWidth(names.get(names.size()-1).trim())) / 2;
            Graphics textG = textBg.getGraphics();
            textG.setFont(backG.getFont().deriveFont(12f));
            textG.drawString(names.get(names.size()-1).trim(), textX, textY);

            backG.drawImage(textBg, textPos[0], textPos[1], null);

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
