package domain.models;

public class Trade {
    private String id;
    private String cardToTrade;
    private String type;
    private double minimumDamage;

    public Trade() {}
    public Trade(String id, String cardToTrade, String type, double minimumDamage) {
        this.id = id;
        this.cardToTrade = cardToTrade;
        this.type = type;
        this.minimumDamage = minimumDamage;
    }
    public String getId() {return id;}
    public String getCardToTradeId() {return cardToTrade;}
    public String getType() {return type;}
    public double getMinimumDamage() {return minimumDamage;}
}
