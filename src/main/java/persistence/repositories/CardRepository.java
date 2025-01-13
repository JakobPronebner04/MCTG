package persistence.repositories;
import persistence.db.DatabaseManager;
import domain.models.Card;
import domain.models.Deck;
import domain.models.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardRepository
{
    private static CardRepository instance;

    private CardRepository()
    {
    }

    public static CardRepository getInstance() {
        if (instance == null) {
            instance = new CardRepository();
        }
        return instance;
    }

    public String getCards(User user) throws SQLException {
        DatabaseManager db = new DatabaseManager();
        db.connect();

        String getCardPropertiesCmd = "SELECT * from usercards WHERE user_id = ?";

        ResultSet res = db.executeQuery(getCardPropertiesCmd,user.getId());
        if(!res.next())
        {
            db.disconnect();
            return "empty";
        }

        List<Card> cards = new ArrayList<>();
        while (res.next()) {
            String cardId = res.getString("card_id");
            String name = res.getString("name");
            double damage = res.getDouble("damage");
            cards.add(new Card(name, damage, cardId));
        }

        StringBuilder sb = new StringBuilder();

        for (Card card : cards) {
            sb.append("ID: ").append(card.getId()).append(" | ")
                    .append("Name: ").append(card.getName()).append(" | ")
                    .append("Damage: ").append(card.getDamage());

            sb.append(System.lineSeparator());
        }
        db.disconnect();
        return sb.toString();
    }

    public Optional<Deck> getDeck(User user) throws SQLException {
        DatabaseManager db = new DatabaseManager();
        db.connect();

        String getDeckCardsCmd = """
            SELECT
                ud.user_id,
                uc.card_id,
                uc.name,
                uc.damage,
                CASE
                    WHEN ud.card_id1 = uc.card_id THEN 'card1'
                    WHEN ud.card_id2 = uc.card_id THEN 'card2'
                    WHEN ud.card_id3 = uc.card_id THEN 'card3'
                    WHEN ud.card_id4 = uc.card_id THEN 'card4'
                END AS card_position
            FROM
                userdecks ud
            JOIN
                usercards uc ON uc.card_id IN (ud.card_id1, ud.card_id2, ud.card_id3, ud.card_id4)
            WHERE
                ud.user_id = ?;
            """;

        ResultSet res = db.executeQuery(getDeckCardsCmd, user.getId());

        if (!res.next()) return Optional.empty();

        Deck deck = new Deck();
        do {
            String name = res.getString("name");
            double damage = res.getDouble("damage");
            String cardID = res.getString("card_id");
            deck.addCard(new Card(name, damage, cardID));
        } while (res.next());

        return Optional.of(deck);
    }




    public boolean configureDeck(User user,List<String>card_IDs) throws SQLException {
        boolean ownsCards = checkCards(user.getId(), card_IDs);
        System.out.println(ownsCards);
       if (ownsCards) {
            String configureDeckCmd = "INSERT INTO userdecks (user_id, card_id1, card_id2, card_id3, card_id4) VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT (user_id) DO UPDATE SET " +
                    "card_id1 = EXCLUDED.card_id1, " +
                    "card_id2 = EXCLUDED.card_id2, " +
                    "card_id3 = EXCLUDED.card_id3, " +
                    "card_id4 = EXCLUDED.card_id4";
            DatabaseManager db = new DatabaseManager();
            db.connect();

            int changed = db.executeUpdate(configureDeckCmd,
                    user.getId(),
                    card_IDs.get(0),
                    card_IDs.get(1),
                    card_IDs.get(2),
                    card_IDs.get(3));
            if (changed < 1) return false;
            db.disconnect();
            return true;
        }
        return false;
    }

    private boolean checkCards(String user_id, List<String> card_IDs) throws SQLException {
        if(card_IDs.isEmpty() || card_IDs.size()< 4) return false;
        String checkCardsCmd = "SELECT COUNT(*) FROM usercards WHERE card_id in (?, ?, ?, ?) AND user_id = ?";
        DatabaseManager db = new DatabaseManager();
        db.connect();

        ResultSet res = db.executeQuery(checkCardsCmd,
                card_IDs.get(0),
                card_IDs.get(1),
                card_IDs.get(2),
                card_IDs.get(3),
                user_id);

        db.disconnect();
        if(!res.next()) return false;

        int cardsCount = res.getInt(1);
        return cardsCount == card_IDs.size();
    }
}
