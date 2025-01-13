package domain.models;

import domain.enums.ElementType;

public class Card{
    protected String name;
    private boolean monsterType;
    protected String id;
    private ElementType elementType;
    protected double damage;

    public Card(){
    }
    public Card(String name,double damage,String id){
        this.name = name;
        this.damage = damage;
        this.id = id;
        this.monsterType = determineCardType(this.name);
        this.elementType = determineElementType(this.name);
    }

    private ElementType determineElementType(String name){
        String normalized = name.toUpperCase();

        if (normalized.contains("WATER") || normalized.contains("KRAKEN")) {
            return ElementType.WATER;
        } else if (normalized.contains("FIRE") || normalized.contains("DRAGON") || normalized.contains("FIREELF")) {
            return ElementType.FIRE;
        } else {
            return ElementType.REGULAR;
        }
    }

    private boolean determineCardType(String name) {
        String normalized = name.toUpperCase().replace(" ", "").replace("_", "");
        return !normalized.contains("SPELL");
    }

    public double getDamage() { return this.damage; }
    public String getName() { return this.name; }
    public String getId() { return this.id; }
    public String getElementType() { return this.elementType.toString(); }
    public boolean isMonsterType() { return this.monsterType; }
    public String getCardType() { return monsterType ? "monster" : "spell"; }
}