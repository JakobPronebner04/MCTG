package domain.models;

public class User
{
    private String id;
    private String Username;
    private String Password;
    private String token;
    private Deck deck;
    private int coins;

    public User(){}
    public User(String Username, String Password)
    {
        this.Username = Username;
        this.Password = Password;
    }
    /*public User(String id, String Username, String Password, String token, int coins)
    {
        this.id = id;
        this.token = token;
        this.Username = Username;
        this.Password = Password;
        this.coins = coins;

    }*/

    public String getUsername()
    {
        return this.Username;
    }
    public String getPassword()
    {
        return this.Password;
    }
    public String getId(){return this.id;}
    public void setDeck(Deck deck){this.deck = deck;}
    public void setCoins(int coins){
        this.coins = coins;
    }
    public void setToken(String token){ this.token = token;}
    public void setId(String id){ this.id = id;}
    public String getToken()
    {
        return this.token;
    }
    public Deck getDeck()
    {
        return this.deck;
    }
    public int getCoins()
    {
        return this.coins;
    }
}
