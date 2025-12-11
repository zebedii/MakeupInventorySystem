package inventory;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

public class UserDAO {

    public boolean login(String username, String password) {
        String sql = "SELECT password FROM users WHERE username=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String stored = rs.getString(1);
                String hashedInput = hash(password);
                return stored != null && (stored.equals(hashedInput) || stored.equals(password));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void register(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash(password));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String hash(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
