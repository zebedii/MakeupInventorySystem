package inventory;

import java.sql.*;
import java.util.ArrayList;

public class ProductDAO {

    public void addProduct(Product p) {
        String sql = "INSERT INTO products(name, category, shade, price, no_of_items) VALUES(?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getShade());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getNoOfItems());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("shade"),
                        rs.getDouble("price"),
                        rs.getInt("no_of_items")
                );
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateProduct(Product p) {
        String sql = "UPDATE products SET name=?, category=?, shade=?, price=?, no_of_items=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getShade());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getNoOfItems());
            ps.setInt(6, p.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
