package persistence.repositories;
import domain.enums.TradeState;
import domain.models.*;
import persistence.db.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;


public class TradeRepository
{
    private static TradeRepository instance;

    private TradeRepository()
    {
    }

    public static TradeRepository getInstance() {
        if (instance == null) {
            instance = new TradeRepository();
        }
        return instance;
    }
    public boolean addTrade(User user,Trade trade) throws SQLException {
        boolean availableCard = checkCardAvailable(user,trade);
        if (availableCard) {
            DatabaseManager db = new DatabaseManager();
            db.connect();
            String insertTrade = """
                    INSERT INTO trades (id,cardtotrade,type,minimumdamage) VALUES (?,?,?,?);
                    """;
            int res = db.executeUpdate(insertTrade,trade.getId(),trade.getCardToTradeId(),trade.getType(),trade.getMinimumDamage());
            db.disconnect();

            if(res > 0) return true;
        }
        return false;
    }

    private boolean checkCardAvailable(User user,Trade trade) throws SQLException {
        String cardValidCmd = """
                Select * from usercards where user_id=? and card_id=?
                """;
        DatabaseManager db = new DatabaseManager();
        db.connect();

        ResultSet result = db.executeQuery(cardValidCmd,user.getId(),trade.getCardToTradeId());
        if(result.next()) {
            db.disconnect();
            return true;
        }
        db.disconnect();
        return false;
    }

    public Optional<Trade> getTradeById(String tradeId) throws SQLException {
        DatabaseManager db = new DatabaseManager();
        db.connect();
        ResultSet result = db.executeQuery("SELECT * FROM trades WHERE id=?",tradeId);
        if(result.next()) {
            Trade trade = new Trade(result.getString("id"),
                    result.getString("cardtotrade"),
                    result.getString("type"),
                    result.getInt("minimumdamage"));
            db.disconnect();
            return Optional.of(trade);
        }
        db.disconnect();
        return Optional.empty();
    }

    public Optional<Card> getCardToTradeById(User user,String changeCard) throws SQLException {
        DatabaseManager db = new DatabaseManager();
        db.connect();
        ResultSet result = db.executeQuery("SELECT * FROM usercards WHERE card_id = ? AND user_id = ?",changeCard,user.getId());
        if(result.next())
        {
            Card c = new Card(
                    result.getString("name"),
                    result.getDouble("damage"),
                    result.getString("card_id"));
            db.disconnect();
            return Optional.of(c);
        }
        db.disconnect();
        return Optional.empty();
    }

    public String getTrades() throws SQLException
    {
        boolean empty = true;
        String trades = """
        SELECT\s
                trades.id,
                usercards.name,
                usercards.damage,
                usercards.card_id,
                trades.type,
                trades.minimumdamage
        FROM\s
            trades
        JOIN\s
            usercards
        ON\s
        trades.cardtotrade = usercards.card_id
        """;
     DatabaseManager db = new DatabaseManager();
     db.connect();
     ResultSet res = db.executeQuery(trades);
     StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-50s %-30s %-30s%n", "ID:", "Card to get:", "Requirements:"));
        sb.append("-".repeat(103)).append("\n");
     while(res.next()) {
         String id = res.getString("id");

         Card c = new Card(res.getString("name"),
                           res.getDouble("damage"),
                           res.getString("card_id"));

         String type = res.getString("type");
         String minimumDamage = res.getString("minimumdamage");

         appendRow(sb, id, "Name: " + c.getName(), "Type: " + type);
         appendRow(sb, "", "Damage: " + c.getDamage(), "Minimumdamage: " + minimumDamage);
         appendRow(sb, "", "Type: " + c.getCardType(), "");
         appendRow(sb, "", "Element type: " + c.getElementType(), "");
         sb.append("-".repeat(103)).append("\n");
         empty = false;

     }
     db.disconnect();
     if(empty) return "empty";
     return sb.toString();
    }

    private void appendRow(StringBuilder sb, String col1, String col2, String col3) {
        sb.append(String.format("%-50s %-30s %-20s %-1s%n", col1, col2, col3,"|"));
    }

    public TradeState startTrade(User user, Trade tradeOffer, Card accepterCard) throws SQLException {

        if(accepterCard.getCardType().equals(tradeOffer.getType())
                && accepterCard.getDamage() >= tradeOffer.getMinimumDamage())
        {
            Card offererCard = getOffererCard(tradeOffer.getCardToTradeId()).get();

            String accepterId = user.getId();
            String offererId = getUserIdByCardId(tradeOffer.getCardToTradeId());

            if(offererId.isEmpty()) return TradeState.NO_SUCCESS;

            if(offererId.equals(accepterId)) return TradeState.SAME_USER;
            DatabaseManager db = new DatabaseManager();
            db.connect();
            db.setAutoCommit(false);

            if (!executeTrade(db, offererCard, accepterCard, accepterId, offererId, tradeOffer.getId())) {
                db.rollback();
                return TradeState.NO_SUCCESS;
            }

            db.commit();
            return TradeState.SUCCESS;
        }
        return TradeState.NO_SUCCESS;
    }

    private String getUserIdByCardId(String cardId) throws SQLException {
        DatabaseManager db = new DatabaseManager();
        db.connect();
        String query = "SELECT user_id FROM usercards WHERE card_id=?";
        ResultSet result = db.executeQuery(query,cardId);
        db.disconnect();
        if(!result.next()) return "";
        return result.getString("user_id")==null ? "" : result.getString("user_id");
    }
    private Optional<Card> getOffererCard(String offererCardId) throws SQLException {
        DatabaseManager db = new DatabaseManager();
        db.connect();
        ResultSet result = db.executeQuery("SELECT * FROM usercards WHERE card_id=?",offererCardId);
        if(!result.next()) return Optional.empty();
        return Optional.of(new Card(result.getString("name"),
                result.getDouble("damage"),
                result.getString("card_id")));
    }
    private boolean executeTrade(DatabaseManager db, Card offererCard, Card accepterCard,
                                 String accepterId, String offererId, String tradeId) throws SQLException {
        String deleteQuery = "DELETE FROM usercards WHERE card_id = ? OR card_id = ?";
        String insertQuery = "INSERT INTO usercards (card_id, user_id, name, damage) VALUES (?, ?, ?, ?)";
        String tradeDeleteQuery = "DELETE FROM trades WHERE id = ?";

        int res = db.executeUpdate(deleteQuery, offererCard.getId(), accepterCard.getId());
        if (res < 2) return false;

        res = db.executeUpdate(insertQuery, offererCard.getId(), accepterId, offererCard.getName(), offererCard.getDamage());
        if (res < 1) return false;

        res = db.executeUpdate(insertQuery, accepterCard.getId(), offererId, accepterCard.getName(), accepterCard.getDamage());
        if (res < 1) return false;

        res = db.executeUpdate(tradeDeleteQuery, tradeId);
        return res >= 1;
    }

}
