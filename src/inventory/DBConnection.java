package inventory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/makeup_inventory?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "admin123";
    private static final String PROPERTIES_FILE = "db.properties";

    public static Connection getConnection() throws Exception {
        Properties props = new Properties();
        if (Files.exists(Paths.get(PROPERTIES_FILE))) {
            try (var in = Files.newInputStream(Paths.get(PROPERTIES_FILE))) {
                props.load(in);
            }
        }

        String url = System.getenv().getOrDefault("DB_URL",
                props.getProperty("db.url", DEFAULT_URL));
        String user = System.getenv().getOrDefault("DB_USER",
                props.getProperty("db.user", DEFAULT_USER));
        String pass = System.getenv().getOrDefault("DB_PASS",
                props.getProperty("db.pass", DEFAULT_PASS));

        return DriverManager.getConnection(url, user, pass);
    }
}
