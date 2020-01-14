package cubicCastles.auction;

import net.dv8tion.jda.api.entities.User;

public class Auction {
    private String item;
    private String startPrice;
    private String endPrice;
    private String currentPrice;
    private String id;
    private User highestBidder;
    private User bidOwner;

    public Auction(String itemName, String startPrice, String endPrice, String currentPrice, String id, User highestBidder, User bidOwner) {
        this.item = itemName;
        this.startPrice = startPrice;
        this.endPrice = endPrice;
        this.currentPrice = currentPrice;
        this.id = id;
        this.highestBidder = highestBidder;
        this.bidOwner = bidOwner;
    }

    public String getItemName() {
        return this.item;
    }

    public void setItemName(String itemName) {
        this.item = itemName;
    }

    public String getStartPrice() {
        return this.startPrice;
    }

    public void setStartPrice(String startPrice) {
        this.startPrice = startPrice;
    }

    public String getEndPrice() {
        return this.endPrice;
    }

    public void setEndPrice(String endPrice) {
        this.endPrice = endPrice;
    }

    public String getCurrentPrice() {
        return this.currentPrice;
    }

    public void setCurrentPrice(String currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return this.highestBidder;
    }

    public void setUser(User user) {
        this.highestBidder = user;
    }

    public User getBidOwner() {
        return this.bidOwner;
    }

    public void setBidOwner(User user) {
        this.bidOwner = user;
    }
}
