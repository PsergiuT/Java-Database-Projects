package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnection {
    private static final String url;
    private static final String user;
    private static final String pass;

    static {
        Properties properties = Props.getProperties();
        if (properties == null) {
            throw new RuntimeException("Properties file not found.");
        }
        url = properties.getProperty("db.url");
        user = properties.getProperty("db.username");
        pass = properties.getProperty("db.password");
    }

    public static Connection getConnection() {
        try{
            Connection conn = DriverManager.getConnection(url, user, pass);
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
