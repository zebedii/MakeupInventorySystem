package inventory;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String URL =
            "jdbc:mysql://localhost:3306/makeup_inventory?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "admin123";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
