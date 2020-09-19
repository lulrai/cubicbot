package cubicCastles.user;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Qbee {
    private String userID;
    private Birthday birthday;
    private String gameName;
    private String joinDate;
    private String favoriteItem;
    private String about;
    private int level = -1;
    private String clan;
    private ArrayList<String> realms = new ArrayList<>();
    private ArrayList<String> overworlds = new ArrayList<>();

    public Qbee() {}

    public Qbee(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public Birthday getBirthday() {
        return birthday;
    }

    public void setBirthday(Birthday birthday) {
        this.birthday = birthday;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getJoinDate() { return joinDate; }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getFavoriteItem() {
        return favoriteItem;
    }

    public void setFavoriteItem(String favoriteItem) {
        this.favoriteItem = favoriteItem;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getClan() {
        return clan;
    }

    public void setClan(String clan) {
        this.clan = clan;
    }

    public ArrayList<String> getRealms() {
        return realms;
    }

    public void addRealm(String realm) {
        this.realms.add(realm);
    }

    public boolean removeRealm(String realmName) {
        int indexToRemove = -1;
        for(int i = 0; i < realms.size(); i++){
            if(realms.get(i).equalsIgnoreCase(realmName)){
                indexToRemove = i;
                break;
            }
        }
        if(indexToRemove == -1) return false;
        this.realms.remove(indexToRemove);
        return true;
    }

    public ArrayList<String> getOverworlds() {
        return overworlds;
    }

    public void addOverworld(String overworld) {
        this.overworlds.add(overworld);
    }

    public boolean removeOverworld(String overworld) {
        int indexToRemove = -1;
        for(int i = 0; i < overworlds.size(); i++){
            if(overworlds.get(i).equalsIgnoreCase(overworld)){
                indexToRemove = i;
                break;
            }
        }
        if(indexToRemove == -1) return false;
        this.overworlds.remove(indexToRemove);
        return true;
    }

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof Qbee){
            Qbee ptr = (Qbee) v;
            retVal = ptr.userID.equals(this.userID);
        }

        return retVal;
    }

    @Override
    public int hashCode() {
        return Math.toIntExact(((int)(Long.parseLong(this.userID)/Math.pow(10,10))+this.level));
    }



}
