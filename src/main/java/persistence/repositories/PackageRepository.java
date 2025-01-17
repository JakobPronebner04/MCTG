package persistence.repositories;
import persistence.db.DatabaseManager;
import domain.models.Card;
import domain.models.Package;
import domain.models.Token;
import domain.models.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PackageRepository
{
    private static PackageRepository instance;

    private PackageRepository()
    {
    }

    public static PackageRepository getInstance() {
        if (instance == null) {
            instance = new PackageRepository();
        }
        return instance;
    }

    public synchronized boolean addPackage(Package pkg) throws SQLException {
        DatabaseManager db = new DatabaseManager();

        String addPackageCmd = "INSERT INTO packages (package_id) VALUES (?) ";
        String addCardCmd = "INSERT INTO packagecards (card_id,package_id,name,damage) VALUES (?, ?, ?, ?)";

        db.connect();
        Token token = new Token();
        String pkgID = token.generateRandomToken();
        int rowsAffectedPackage = db.executeUpdate(addPackageCmd,pkgID);

        if (rowsAffectedPackage > 0) {
            for (Card card : pkg.getCards()) {
                db.executeUpdate(
                        addCardCmd,
                        card.getId(),
                        pkgID,
                        card.getName(),
                        card.getDamage()
                );
            }
            db.disconnect();
            return true;
        }
        db.disconnect();
        return false;
    }

    public synchronized boolean getPackage(User user) throws SQLException {
        DatabaseManager db = new DatabaseManager();
        db.connect();
        String getFirstPackageIdCmd = """
                SELECT package_id\s
                FROM Packages\s
                ORDER BY package_id ASC\s
                LIMIT 1;
                """;
        ResultSet res = db.executeQuery(getFirstPackageIdCmd);
        if (res.next()) {
            String packageId = res.getString("package_id");

            String getPackageDetailsCmd = "SELECT p.package_id, pc.card_id, pc.name, pc.damage " +
                    "FROM Packages p " +
                    "LEFT JOIN Packagecards pc ON p.package_id = pc.package_id " +
                    "WHERE p.package_id = ?";

            res = db.executeQuery(getPackageDetailsCmd, packageId);
            List<Card> cards = new ArrayList<>();
            while (res.next()) {
                String cardId = res.getString("card_id");
                String name = res.getString("name");
                double damage = res.getDouble("damage");
                cards.add(new Card(name,damage,cardId));
            }
            for(Card card : cards) {
                String addUsercardCmd = "INSERT INTO usercards (card_id,user_id,name,damage) VALUES (?, ?, ?, ?)";
                int rowsAffectedCards = db.executeUpdate(addUsercardCmd,card.getId(),user.getId(),card.getName(),card.getDamage());
                if (rowsAffectedCards < 0) { return false; }
            }

            String deletePackagecardsCmd = "DELETE FROM Packagecards WHERE package_id = ?";
            db.executeUpdate(deletePackagecardsCmd, packageId);

            String deletePackageCmd = "DELETE FROM Packages WHERE package_id = ?";
            db.executeUpdate(deletePackageCmd, packageId);

            db.disconnect();
            return true;
        }
        db.disconnect();
        return false;
    }
}
