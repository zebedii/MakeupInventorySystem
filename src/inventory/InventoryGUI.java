package inventory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.util.ArrayList;

public class InventoryGUI extends JFrame {

    private JTextField txtName, txtShade, txtPrice, txtItems, txtSearch;
    private JComboBox<String> cbCategory;
    private JTable table;
    private DefaultTableModel model;

    private ProductDAO dao = new ProductDAO();

    public InventoryGUI() {
        setTitle("Makeup Inventory System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= FORM PANEL =================
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtName = new JTextField();
        txtShade = new JTextField();
        txtPrice = new JTextField();
        txtItems = new JTextField();

        cbCategory = new JComboBox<>(new String[]{
                "Lipstick", "Blush", "Foundation"
        });

        formPanel.add(new JLabel("Name:"));
        formPanel.add(txtName);

        formPanel.add(new JLabel("Category:"));
        formPanel.add(cbCategory);

        formPanel.add(new JLabel("Shade:"));
        formPanel.add(txtShade);

        formPanel.add(new JLabel("Price:"));
        formPanel.add(txtPrice);

        formPanel.add(new JLabel("No. of Items:"));
        formPanel.add(txtItems);

        add(formPanel, BorderLayout.NORTH);

        // ================= TABLE =================
        model = new DefaultTableModel(new String[]{
                "ID", "Name", "Category", "Shade", "Price", "No. of Items"
        }, 0);

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ================= BUTTON PANEL =================
        JPanel bottomPanel = new JPanel();

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnExport = new JButton("Export to Notepad");
        JButton btnLogout = new JButton("Logout");
        bottomPanel.add(btnLogout);


        txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("Search");

        bottomPanel.add(btnAdd);
        bottomPanel.add(btnUpdate);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnClear);

        bottomPanel.add(new JLabel("Search:"));
        bottomPanel.add(txtSearch);
        bottomPanel.add(btnSearch);

        bottomPanel.add(btnExport);

        bottomPanel.add(btnLogout);

        add(bottomPanel, BorderLayout.SOUTH);

        // Load data from DB
        refreshTable();

        // ================= EVENTS =================
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchProduct());
        btnExport.addActionListener(e -> exportToTxt());
        btnLogout.addActionListener(e -> logout());

        table.getSelectionModel().addListSelectionListener(e -> fillFieldsFromTable());
    }

    // ================= ADD =================
    private void addProduct() {
        try {
            // Input trapping (empty fields)
            if (txtName.getText().isEmpty() ||
                    txtShade.getText().isEmpty() ||
                    txtPrice.getText().isEmpty() ||
                    txtItems.getText().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            // Shade numbers only
            String shade = txtShade.getText();
            if (!shade.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                        "Shade must be numbers only (ex. 001).");
                return;
            }

            double price = Double.parseDouble(txtPrice.getText());
            int items = Integer.parseInt(txtItems.getText());

            // Non-negative check
            if (price < 0 || items < 0) {
                JOptionPane.showMessageDialog(this,
                        "Price and No. of Items must be non-negative.");
                return;
            }

            Product p = new Product(
                    txtName.getText(),
                    cbCategory.getSelectedItem().toString(),
                    shade,
                    price,
                    items
            );

            dao.addProduct(p);
            refreshTable();
            clearFields();
            JOptionPane.showMessageDialog(this, "Product added successfully!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Price must be a number and No. of Items must be an integer.");
        }
    }

    // ================= UPDATE =================
    private void updateProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        try {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());

            String shade = txtShade.getText();
            if (!shade.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                        "Shade must be numbers only (ex. 001).");
                return;
            }

            double price = Double.parseDouble(txtPrice.getText());
            int items = Integer.parseInt(txtItems.getText());

            Product p = new Product(
                    id,
                    txtName.getText(),
                    cbCategory.getSelectedItem().toString(),
                    shade,
                    price,
                    items
            );

            dao.updateProduct(p);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Product updated successfully!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format.");
        }
    }

    // ================= DELETE =================
    private void deleteProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to delete this product?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            dao.deleteProduct(id);
            refreshTable();
            clearFields();
            JOptionPane.showMessageDialog(this, "Product deleted successfully!");
        }
    }

    // ================= REFRESH TABLE =================
    private void refreshTable() {
        model.setRowCount(0);
        ArrayList<Product> products = dao.getAllProducts();

        for (Product p : products) {  // loop topic
            model.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getCategory(),
                    p.getShade(),
                    p.getPrice(),
                    p.getNoOfItems()
            });
        }
    }

    // ================= FILL FIELDS WHEN TABLE ROW CLICKED =================
    private void fillFieldsFromTable() {
        int row = table.getSelectedRow();
        if (row != -1) {
            txtName.setText(model.getValueAt(row, 1).toString());
            cbCategory.setSelectedItem(model.getValueAt(row, 2).toString());
            txtShade.setText(model.getValueAt(row, 3).toString());
            txtPrice.setText(model.getValueAt(row, 4).toString());
            txtItems.setText(model.getValueAt(row, 5).toString());
        }
    }

    // ================= CLEAR =================
    private void clearFields() {
        txtName.setText("");
        txtShade.setText("");
        txtPrice.setText("");
        txtItems.setText("");
        cbCategory.setSelectedIndex(0);
        txtSearch.setText("");
        table.clearSelection();
    }

    // ================= SEARCH =================
    private void searchProduct() {
        String key = txtSearch.getText().toLowerCase();
        model.setRowCount(0);

        ArrayList<Product> products = dao.getAllProducts();
        for (Product p : products) {
            if (p.getName().toLowerCase().contains(key)
                    || p.getCategory().toLowerCase().contains(key)
                    || p.getShade().toLowerCase().contains(key)) {

                model.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        p.getCategory(),
                        p.getShade(),
                        p.getPrice(),
                        p.getNoOfItems()
                });
            }
        }
    }

    // ================= EXPORT TO TXT (IOStream) =================
    private void exportToTxt() {
        try (FileWriter fw = new FileWriter("inventory_backup.txt")) {
            ArrayList<Product> products = dao.getAllProducts();

            for (Product p : products) {
                fw.write(
                        p.getId() + " | " +
                                p.getName() + " | " +
                                p.getCategory() + " | " +
                                p.getShade() + " | " +
                                p.getPrice() + " | " +
                                p.getNoOfItems() + "\n"
                );
            }

            JOptionPane.showMessageDialog(this,
                    "Exported successfully to inventory_backup.txt");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed.");
            e.printStackTrace();
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            new LoginGUI().setVisible(true); // go back to login
            dispose(); // close inventory window
        }
    }


    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventoryGUI().setVisible(true));
    }
}
