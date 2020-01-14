package cubicCastles.craftCommands;

import java.io.File;

public class Item {
    private String name;
    private String type;
    private String desc;
    private File image;

    public Item(){ }
    public Item(String name, String type, String desc, File file){
        this.name = name;
        this.type = type;
        this.desc = desc;
        this.image = file;
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

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }
}
