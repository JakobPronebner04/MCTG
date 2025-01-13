package persistence.db;

import java.sql.*;

public class DatabaseManager
{
    private static final String DBS_URL = "jdbc:postgresql://localhost:5432/mctg";
    private static final String USER = "postgres";
    private static final String PW = "Wuko1972";
    private Connection conn;

    public void connect() throws SQLException
    {
        this.conn = DriverManager.getConnection(DBS_URL, USER, PW);
    }

    public void disconnect() throws SQLException
    {
        if (conn != null && !conn.isClosed())
        {
            conn.close();
        }
    }

    public ResultSet executeQuery(String query, String... params) throws SQLException
    {
        PreparedStatement pstmt = conn.prepareStatement(query);

        for (int i = 0; i < params.length; i++)
        {
            pstmt.setString(i + 1, params[i]);
        }

        return pstmt.executeQuery();
    }

    public int executeUpdate(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(query);

        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }

        return pstmt.executeUpdate();
    }
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(autoCommit);
        } else {
            throw new IllegalStateException("No active database connection.");
        }
    }

    public void commit() throws SQLException {
        if (conn != null) {
            conn.commit();
        } else {
            throw new IllegalStateException("No active database connection.");
        }
    }

    public void rollback() throws SQLException {
        if (conn != null) {
            conn.rollback();
        } else {
            throw new IllegalStateException("No active database connection.");
        }
    }

    public boolean isConnected() throws SQLException
    {
        return conn != null && !conn.isClosed();
    }

}
