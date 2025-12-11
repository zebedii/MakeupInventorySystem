package inventory;

public class TestDB {
    public static void main(String[] args) {
        try {
            DBConnection.getConnection();
            System.out.println("✅ Connected to MySQL successfully!");
        } catch (Exception e) {
            System.out.println("❌ Connection failed.");
            e.printStackTrace();
        }
    }
}
