package inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public boolean login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=? AND password=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            return rs.next(); // true if user found

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
