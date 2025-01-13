package persistence.repositories;

import persistence.db.DatabaseManager;
import domain.models.Card;
import domain.models.Deck;
import domain.models.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GameRepository
{
    private static GameRepository instance;
    private final Map<String, String> autoWin;
    private final Map<String, String> specialities;
    private final Map<String, String> effective;
    private StringBuilder battleLogger;
    private GameRepository()
    {
        autoWin = new HashMap<>() {{
        put("Dragon", "Goblin");
        put("Wizard", "Ork");
        put("WaterSpell", "Knight");
        put("FireElf", "Dragon");
        }};
        specialities = new HashMap<>() {{
            put("Dragon", "The Goblin is too afraid to attack the Dragon.");
            put("Wizard", "Because the Wizard is controlling the Ork, the Ork can't deal any damage.");
            put("WaterSpell", "The heavy armor of the Knight made him drown in the water.");
            put("Kraken", "The Kraken is immune to every Spell");
            put("FireElf", "Because the FireElves have known the Dragons since they were little, the FireElf easily evades all attacks");
        }};
        effective = new HashMap<>() {{
                put("Water", "Fire");
                put("Fire", "Regular");
                put("Regular", "Water");
        }};
        battleLogger = new StringBuilder();
    }

    public static GameRepository getInstance() {
        if (instance == null) {
            instance = new GameRepository();
        }
        return instance;
    }

    public enum BattleOutcome {
        PLAYER1_WIN, PLAYER2_WIN, DRAW
    }

    public String start(User u1, User u2) throws SQLException {
        battleLogger.setLength(0);
        Deck player1_deck = u1.getDeck();
        Deck player2_deck = u2.getDeck();
        int round = 0;
        while (round < 100) {
            player1_deck.shuffle();
            player2_deck.shuffle();

            Card player1_card = player1_deck.getTopCard();
            Card player2_card = player2_deck.getTopCard();
            battleLogger.append("\n\n");
            battleLogger.append("Round ").append(round).append("\n");
            battleLogger.append("Player 1 card: ").append(player1_card.getName()).append(" -> ")
                    .append(player1_card.getDamage()).append(" -> ")
                    .append(player1_card.getElementType()).append("\n");
            battleLogger.append("Player 2 card: ").append(player2_card.getName()).append(" -> ")
                    .append(player2_card.getDamage()).append(" -> ")
                    .append(player2_card.getElementType()).append("\n\n");

            BattleOutcome result = fight(player1_card, player2_card);
            battleLogger.append("Result of fight: ").append(result).append("\n\n");
            if (result == BattleOutcome.PLAYER1_WIN) {
                player1_deck.addCard(player2_card);
                player1_deck.addCard(player1_card);
            } else if (result == BattleOutcome.PLAYER2_WIN) {
                player2_deck.addCard(player1_card);
                player2_deck.addCard(player2_card);
            }
            else {
                player1_deck.addCard(player1_card);
                player2_deck.addCard(player2_card);
            }

            battleLogger.append(u1.getUsername()).append("´s cards: ").append(player1_deck.getSize()).append("\n");
            battleLogger.append(u2.getUsername()).append("´s cards: ").append(player2_deck.getSize()).append("\n\n");

            if (player1_deck.getSize() == 0) {
                updateUserStats(u1,u2);
                battleLogger.append(u2.getUsername()).append(" won match against ").append(u1.getUsername()).append("\n");
                return battleLogger.toString();
            }
            if (player2_deck.getSize() == 0) {
                updateUserStats(u2,u1);
                battleLogger.append(u1.getUsername()).append(" won match against ").append(u2.getUsername());
                return battleLogger.toString();
            }
            round++;
        }
        battleLogger.append(u1.getUsername()).append(" won match against ").append(u2.getUsername());
        return battleLogger.toString();
    }

    private BattleOutcome fight(Card player1_card, Card player2_card) {
        if (player1_card.getName().equals("Kraken") && player2_card.getName().contains("Spell")) {
            battleLogger.append(specialities.get("Kraken")).append("\n");
            return BattleOutcome.PLAYER1_WIN;
        } else if (player2_card.getName().equals("Kraken") && player1_card.getName().contains("Spell")) {
            battleLogger.append(specialities.get("Kraken")).append("\n");
            return BattleOutcome.PLAYER2_WIN;
        }

        if (autoWin.containsKey(player1_card.getName()) && autoWin.get(player1_card.getName()).equals(player2_card.getName())) {
            battleLogger.append(specialities.get(player1_card.getName())).append("\n");
            return BattleOutcome.PLAYER1_WIN;
        } else if (autoWin.containsKey(player2_card.getName()) && autoWin.get(player2_card.getName()).equals(player1_card.getName())) {
            battleLogger.append(specialities.get(player2_card.getName())).append("\n");
            return BattleOutcome.PLAYER2_WIN;
        }

        if ((player1_card.isMonsterType() && player2_card.isMonsterType()) ||
                (player1_card.getElementType().equals(player2_card.getElementType()))) {
            return normalDmg(player1_card, player2_card);
        }

        return specialDmg(player1_card, player2_card);
    }

    private BattleOutcome normalDmg(Card player1_card, Card player2_card) {
        if (player1_card.getDamage() > player2_card.getDamage()) {
            battleLogger.append(player1_card.getName()).append(" beats ").append(player2_card.getName()).append("\n");
            return BattleOutcome.PLAYER1_WIN;
        }
        if (player2_card.getDamage() > player1_card.getDamage()) {
            battleLogger.append(player2_card.getName()).append(" beats ").append(player1_card.getName()).append("\n");
            return BattleOutcome.PLAYER2_WIN;
        }

        battleLogger.append("The Battle results in a draw\n");
        return BattleOutcome.DRAW;

    }

    private BattleOutcome specialDmg(Card player1_card, Card player2_card) {
        String card1_element = player1_card.getElementType();
        String card2_element = player2_card.getElementType();
        double card1Dmg = player1_card.getDamage();
        double card2Dmg = player2_card.getDamage();

        if (effective.containsKey(card1_element) && effective.get(card1_element).equals(card2_element)) {
            card1Dmg *= 2;
            card2Dmg /= 2;
            battleLogger.append(player1_card.getName()).append(" (").append(card1Dmg).append(") is very effective against ").append(player2_card.getName()).append(" (").append(card2Dmg).append(")!\n");
        } else if (effective.containsKey(card2_element) && effective.get(card2_element).equals(card1_element)) {
            card1Dmg /= 2;
            card2Dmg *= 2;
            battleLogger.append(player2_card.getName()).append(" (").append(card2Dmg).append(") is very effective against ").append(player1_card.getName()).append(" (").append(card1Dmg).append(")!\n");
        }

        if (card1Dmg > card2Dmg) {
            battleLogger.append(player1_card.getName()).append(" beats ").append(player2_card.getName()).append("\n");
            return BattleOutcome.PLAYER1_WIN;
        } else if (card1Dmg < card2Dmg) {
            battleLogger.append(player2_card.getName()).append(" beats ").append(player1_card.getName()).append("\n");
            return BattleOutcome.PLAYER2_WIN;
        }

        return BattleOutcome.DRAW;
    }
    private void updateUserStats(User loser, User winner) throws SQLException {
        String getStatsCmd = """
            SELECT wins, losses, elo FROM userstats WHERE user_id = ?
            """;
        String statsCmd = """
            INSERT INTO userstats (user_id, wins, losses, elo)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (user_id)
            DO UPDATE SET wins = EXCLUDED.wins, losses = EXCLUDED.losses, elo = EXCLUDED.elo;
            """;

        DatabaseManager db = new DatabaseManager();
        db.connect();

        ResultSet winnerStats = db.executeQuery(getStatsCmd, winner.getId());
        int winnerWins = 0, winnerLosses = 0, winnerElo = 1000;
        if (winnerStats.next()) {
            winnerWins = winnerStats.getInt("wins");
            winnerLosses = winnerStats.getInt("losses");
            winnerElo = winnerStats.getInt("elo");
        }

        ResultSet loserStats = db.executeQuery(getStatsCmd, loser.getId());
        int loserWins = 0, loserLosses = 0, loserElo = 1000;
        if (loserStats.next()) {
            loserWins = loserStats.getInt("wins");
            loserLosses = loserStats.getInt("losses");
            loserElo = loserStats.getInt("elo");
        }

        double winnerExpected = 1 / (1 + Math.pow(10, (loserElo - winnerElo) / 400.0));
        double loserExpected = 1 / (1 + Math.pow(10, (winnerElo - loserElo) / 400.0));
        int kFactor = 32;

        int newWinnerElo = (int) Math.round(winnerElo + kFactor * (1 - winnerExpected));
        int newLoserElo = (int) Math.round(loserElo + kFactor * (0 - loserExpected));

        db.executeUpdate(statsCmd, winner.getId(), winnerWins + 1, winnerLosses, newWinnerElo);

        db.executeUpdate(statsCmd, loser.getId(), loserWins, loserLosses + 1, newLoserElo);
        db.disconnect();
    }

    public String getStats(User user) throws SQLException
    {
        String userStatsCmd = """
                Select * from userstats where user_id = ?;
                """;
        DatabaseManager db = new DatabaseManager();
        db.connect();
        ResultSet userStats = db.executeQuery(userStatsCmd, user.getId());
        if (!userStats.next())
        {
            db.disconnect();
            return "No matches played yet! => elo:1000";
        }

        StringBuilder stats = new StringBuilder();
        stats.append("User stats:\n");
        stats.append("\tUser: ").append(user.getUsername()).append("\n");
        stats.append("\twins:").append(userStats.getInt("wins")).append("\n");
        stats.append("\tlosses:").append(userStats.getInt("losses")).append("\n");
        stats.append("\telo:").append(userStats.getInt("elo")).append("\n");
        db.disconnect();
        return stats.toString();
    }

    public String getScores() throws SQLException{
        boolean scoresAvailable = false;
        String scoresCmd = """
                SELECT\s
                    users.uname,
                    userstats.wins,
                    userstats.losses,
                    userstats.elo
                FROM\s
                    userstats
                JOIN\s
                    users
                ON\s
                    userstats.user_id = users.user_id
                ORDER BY\s
                    userstats.elo DESC;
               """;

        DatabaseManager db = new DatabaseManager();
        db.connect();

        ResultSet results = db.executeQuery(scoresCmd);

        StringBuilder scores = new StringBuilder();

        scores.append("Username").append('\t')
                .append("Wins").append("\t")
                .append("Losses").append("\t")
                .append("Elo").append("\t")
                .append("\n");

        while(results.next()) {
            scoresAvailable = true;
            int wins = results.getInt("wins");
            int losses = results.getInt("losses");
            int elo = results.getInt("elo");
            String username = results.getString("uname");
            scores.append(username).append("\t").append(wins).append("\t")
                    .append(losses).append("\t")
                    .append(elo).append("\n");
        }
        db.disconnect();
        if(!scoresAvailable) return scores.append("empty scores!").toString();
        return scores.toString();
    }

}