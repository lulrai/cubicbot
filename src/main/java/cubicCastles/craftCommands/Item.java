package cubicCastles.craftCommands;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Item {
    private List<String> words;
    private List<Integer> itemMultNum;
    private int size;
    private List<BufferedImage> itemImages;
    private String name;
    private String type;
    private String desc;
    private List<BufferedImage> itemCards;

    public Item(String itemName, String type, String itemDesc, int ingrSize, List<BufferedImage> itemImage, List<Integer> itemMultNum, List<String> words){
        this.name = itemName;
        this.type = type;
        this.desc = itemDesc;
        this.size = ingrSize;
        this.itemImages = itemImage;
        this.itemMultNum = itemMultNum;
        this.words = words;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public List<Integer> getItemMultNum() {
        return itemMultNum;
    }

    public void setItemMultNum(List<Integer> itemMultNum) {
        this.itemMultNum = itemMultNum;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<BufferedImage> getItemImages() {
        return itemImages;
    }

    public void setItemImages(List<BufferedImage> itemImages) {
        this.itemImages = itemImages;
    }

    public void setItemCards(ArrayList<BufferedImage> itemCards) {
        this.itemCards = itemCards;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<BufferedImage> getItemCards() {
        return itemCards;
    }

    public void addItemCards(BufferedImage card) {
        this.itemCards.add(card);
    }
}
