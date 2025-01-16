package persistence.repositories;

import persistence.db.DatabaseManager;
import domain.models.Token;
import domain.models.User;
import domain.models.UserProperties;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {

    private static UserRepository instance;

    private UserRepository() {
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public boolean addUser(User user) throws SQLException
    {
        DatabaseManager db = new DatabaseManager();

        String checkForUserCmd = "Select * from users where uname = ?";
        String addUserCmd = "Insert into users (user_id,uname, upw,coins) values (?, ?, ?, ?)";

        db.connect();
        ResultSet checkRes = db.executeQuery(checkForUserCmd,user.getUsername());
        Token token = new Token();
        String id = token.generateRandomToken();
        if(!checkRes.next())
        {
            int rowsAffected = db.executeUpdate(addUserCmd,id,user.getUsername(),user.getPassword(),20);
            db.disconnect();
            return rowsAffected > 0;
        }
        db.disconnect();
        return false;
    }

    public String getUser(User user) throws SQLException
    {
        DatabaseManager db = new DatabaseManager();
        String checkForUserCmd = "SELECT * FROM users WHERE uname = ? AND upw = ?";
        String updateTokenCmd = "Update users SET token = ? WHERE uname = ? AND upw = ?";

        db.connect();
        ResultSet checkRes = db.executeQuery(checkForUserCmd,user.getUsername(),user.getPassword());
        if(checkRes.next())
        {
            Token t = new Token();
            String strToken = t.generateTokenStr(user.getUsername());

            int rowsAffected = db.executeUpdate(updateTokenCmd,strToken,user.getUsername(),user.getPassword());
            db.disconnect();

            if(rowsAffected > 0)
            {
                return strToken;
            }
        }
        db.disconnect();
        return "";
    }
    public void updateCoins(User user) throws SQLException
    {
        int coins = user.getCoins();
        int costs = 5;
        if(coins == 0)
        {
            return;
        }

        DatabaseManager db = new DatabaseManager();
        db.connect();
        String updateCoinsCmd = "Update users SET coins = ? WHERE uname = ?";
        db.executeUpdate(updateCoinsCmd,coins-costs,user.getUsername());
        db.disconnect();

    }
    public boolean editData(User user, UserProperties up) throws SQLException {
        String changeupCmd = """
                INSERT INTO UserProperties (user_id, name, bio, image)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (user_id) DO UPDATE SET
                    name = EXCLUDED.name,
                    bio = EXCLUDED.bio,
                    image = EXCLUDED.image;
                """;
        DatabaseManager db = new DatabaseManager();
        db.connect();
        int res = db.executeUpdate(changeupCmd,user.getId(),
                up.getName(),
                up.getBio(),
                up.getImage());
        db.disconnect();
        return res > 0;
    }
    public Optional<User> getUserByToken(String token) throws SQLException
    {
        DatabaseManager db = new DatabaseManager();
        db.connect();
        String checkForUserCmd = "SELECT * FROM users WHERE token = ?";
        ResultSet res = db.executeQuery(checkForUserCmd,token);
        if(!res.next()){
            db.disconnect();
            return Optional.empty();
        }

        User user = new User(
                res.getString("uname"),
                res.getString("upw"));

        user.setCoins(res.getInt("coins"));
        user.setToken(token);
        user.setId(res.getString("user_id"));
        db.disconnect();
        return Optional.of(user);
    }

    public Optional<UserProperties> getProperties(User user) throws SQLException {
        DatabaseManager db = new DatabaseManager();
        db.connect();

        String getDeckCardsCmd = """
            Select * from userproperties where user_id = ?;
            """;

        ResultSet res = db.executeQuery(getDeckCardsCmd, user.getId());

        if (res.next()){
            UserProperties up = new UserProperties(
                    res.getString("name"),
                    res.getString("bio"),
                    res.getString("image")
            );
            db.disconnect();
            return Optional.of(up);
        }
        db.disconnect();
        return Optional.empty();
    }
}
