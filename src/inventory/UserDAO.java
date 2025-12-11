package inventory;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User loginUser(String username, String password) {
        String sql = "SELECT id, username, password, role FROM users WHERE username=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String stored = rs.getString("password");
                String hashedInput = hash(password);
                boolean match = stored != null && (stored.equals(hashedInput) || stored.equals(password));
                if (match) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            stored,
                            rs.getString("role")
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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

    public boolean createUser(String username, String password, String role) {
        String sql = "INSERT INTO users(username, password, role) VALUES(?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash(password));
            ps.setString(3, role);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> listEmployees(String searchKey) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, role FROM users WHERE role='employee'";
        if (searchKey != null && !searchKey.trim().isEmpty()) {
            sql += " AND LOWER(username) LIKE ?";
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (sql.contains("LIKE")) {
                ps.setString(1, "%" + searchKey.trim().toLowerCase() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            "", // password not returned here
                            rs.getString("role")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean updateUser(int id, String username, String password, String role) {
        boolean updatePassword = password != null && !password.isEmpty();
        String sql = updatePassword ?
                "UPDATE users SET username=?, password=?, role=? WHERE id=?" :
                "UPDATE users SET username=?, role=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            if (updatePassword) {
                ps.setString(2, hash(password));
                ps.setString(3, role);
                ps.setInt(4, id);
            } else {
                ps.setString(2, role);
                ps.setInt(3, id);
            }
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String hash(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
