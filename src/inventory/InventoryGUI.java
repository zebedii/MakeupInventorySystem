package inventory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InventoryGUI extends JFrame {

    private static final Color PRIMARY = new Color(0xFCF8F8);
    private static final Color SECONDARY = new Color(0xFBEFEF);
    private static final Color TERTIARY = new Color(0xF9DFDF);
    private static final Color QUATERNARY = new Color(0xF5AFAF);

    private JTextField txtName, txtShade, txtPrice, txtItems, txtSearch;
    private JTextField txtMinPrice, txtMaxPrice;
    private JComboBox<String> cbCategory, cbFilterCategory;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblSummary;
    private JButton btnUndo;
    private static final String LOG_FILE = "inventory_log.txt";
    private static final DateTimeFormatter LOG_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ProductDAO dao = new ProductDAO();
    private Product lastDeleted;
    private List<Product> cachedProducts = new ArrayList<>();

    public InventoryGUI() {
        setTitle("Makeup Inventory System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PRIMARY);

        // ================= FORM PANEL =================
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(SECONDARY);

        txtName = new JTextField();
        txtShade = new JTextField();
        txtPrice = new JTextField();
        txtItems = new JTextField();
        txtName.setToolTipText("Product name");
        txtShade.setToolTipText("Numeric shade code (e.g., 001)");
        txtPrice.setToolTipText("Price (non-negative)");
        txtItems.setToolTipText("Number of items (non-negative integer)");

        cbCategory = new JComboBox<>(new String[]{
                "Lipstick", "Blush", "Foundation"
        });
        cbCategory.setToolTipText("Select product category");

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

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PRIMARY);
        topPanel.add(formPanel, BorderLayout.CENTER);

        // ================= TABLE =================
        model = new DefaultTableModel(new String[]{
                "ID", "Name", "Category", "Shade", "Price", "No. of Items"
        }, 0);

        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(PRIMARY);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
        table.setBackground(SECONDARY);
        table.setSelectionBackground(QUATERNARY);
        table.setSelectionForeground(Color.BLACK);

        // ================= FILTER / ACTION PANEL =================
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filterPanel.setBackground(TERTIARY);
        txtSearch = new JTextField(12);
        txtMinPrice = new JTextField(6);
        txtMaxPrice = new JTextField(6);
        cbFilterCategory = new JComboBox<>(new String[]{"All", "Lipstick", "Blush", "Foundation"});
        JButton btnSearch = new JButton("Search");
        JButton btnClearSearch = new JButton("Clear");
        JButton btnFilter = new JButton("Filter");
        JButton btnExportCsv = new JButton("Export CSV");
        JButton btnImportCsv = new JButton("Import CSV");

        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(txtSearch);
        filterPanel.add(btnSearch);
        filterPanel.add(btnClearSearch);
        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(cbFilterCategory);
        filterPanel.add(new JLabel("Min Price:"));
        filterPanel.add(txtMinPrice);
        filterPanel.add(new JLabel("Max Price:"));
        filterPanel.add(txtMaxPrice);
        filterPanel.add(btnFilter);
        filterPanel.add(btnExportCsv);
        filterPanel.add(btnImportCsv);
        topPanel.add(filterPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // ================= BUTTON PANEL =================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bottomPanel.setBackground(PRIMARY);
        actionPanel.setBackground(SECONDARY);
        rightPanel.setBackground(SECONDARY);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnExport = new JButton("Export to Notepad");
        JButton btnLogout = new JButton("Logout");
        btnUndo = new JButton("Undo Delete");
        btnUndo.setEnabled(false);
        lblSummary = new JLabel("Items: 0 | Value: 0.00");

        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        actionPanel.add(btnClear);
        actionPanel.add(btnUndo);
        actionPanel.add(btnExport);
        actionPanel.add(lblSummary);

        rightPanel.add(btnLogout);

        bottomPanel.add(actionPanel, BorderLayout.CENTER);
        bottomPanel.add(rightPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Load data from DB
        refreshTable();

        // ================= EVENTS =================
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchProduct());
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            txtMinPrice.setText("");
            txtMaxPrice.setText("");
            cbFilterCategory.setSelectedIndex(0);
            applyFilters();
        });
        btnFilter.addActionListener(e -> applyFilters());
        btnExport.addActionListener(e -> exportToTxt());
        btnExportCsv.addActionListener(e -> exportToCsv());
        btnImportCsv.addActionListener(e -> importFromCsv());
        btnLogout.addActionListener(e -> logout());
        btnUndo.addActionListener(e -> undoDelete());
        table.getSelectionModel().addListSelectionListener(e -> fillFieldsFromTable());

        // Keyboard shortcuts
        getRootPane().registerKeyboardAction(e -> logout(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        btnAdd.setMnemonic('A');
        btnUpdate.setMnemonic('U');
        btnDelete.setMnemonic('D');
        btnSearch.setMnemonic('S');
        btnLogout.setMnemonic('L');
    }

    // ================= ADD =================
    private void addProduct() {
        Product p = buildValidatedProduct(null);
        if (p == null) return;
        dao.addProduct(p);
        logAction("ADDED", p);
        refreshTable();
        clearFields();
        JOptionPane.showMessageDialog(this, "Product added successfully!");
    }

    // ================= UPDATE =================
    private void updateProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        Product p = buildValidatedProduct(id);
        if (p == null) return;
        p.setId(id);
        dao.updateProduct(p);
        logAction("UPDATED", p);
        refreshTable();
        JOptionPane.showMessageDialog(this, "Product updated successfully!");
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
            lastDeleted = new Product(
                    id,
                    model.getValueAt(row, 1).toString(),
                    model.getValueAt(row, 2).toString(),
                    model.getValueAt(row, 3).toString(),
                    Double.parseDouble(model.getValueAt(row, 4).toString()),
                    Integer.parseInt(model.getValueAt(row, 5).toString())
            );
            dao.deleteProduct(id);
            logAction("DELETED", lastDeleted);
            refreshTable();
            clearFields();
            btnUndo.setEnabled(true);
            JOptionPane.showMessageDialog(this, "Product deleted successfully!");
        }
    }

    // ================= REFRESH TABLE =================
    private void refreshTable() {
        cachedProducts = dao.getAllProducts();
        applyFilters();
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
        table.clearSelection();
        txtSearch.setText("");
        txtMinPrice.setText("");
        txtMaxPrice.setText("");
        cbFilterCategory.setSelectedIndex(0);
        applyFilters();
    }

    // ================= SEARCH =================
    private void searchProduct() {
        applyFilters();
    }

    // ================= EXPORT TO TXT (IOStream) =================
    private void exportToTxt() {
        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            try (FileWriter fw = new FileWriter("inventory_backup.txt")) {
                fw.write(sb.toString());
            }
            JOptionPane.showMessageDialog(this,
                    "Activity log exported to inventory_backup.txt");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed (no log yet?).");
            e.printStackTrace();
        }
    }

    private void exportToCsv() {
        try (FileWriter fw = new FileWriter("inventory.csv")) {
            fw.write("id,name,category,shade,price,no_of_items\n");
            for (Product p : cachedProducts) {
                fw.write(String.format("%d,%s,%s,%s,%.2f,%d%n",
                        p.getId(), escapeCsv(p.getName()), p.getCategory(), p.getShade(), p.getPrice(), p.getNoOfItems()));
            }
            JOptionPane.showMessageDialog(this, "Exported to inventory.csv");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "CSV export failed.");
            e.printStackTrace();
        }
    }

    private void importFromCsv() {
        try (BufferedReader br = new BufferedReader(new FileReader("inventory.csv"))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 6) continue;
                String name = unescapeCsv(parts[1]);
                String category = parts[2];
                String shade = parts[3];
                double price = Double.parseDouble(parts[4]);
                int items = Integer.parseInt(parts[5]);
                if (!dao.existsByNameAndShade(name, shade, null)) {
                    dao.addProduct(new Product(name, category, shade, price, items));
                }
            }
            refreshTable();
            JOptionPane.showMessageDialog(this, "Imported from inventory.csv");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "CSV import failed.");
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String key = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String category = cbFilterCategory.getSelectedItem().toString();

        Double minPrice = null;
        Double maxPrice = null;
        String minText = txtMinPrice.getText() == null ? "" : txtMinPrice.getText().trim();
        String maxText = txtMaxPrice.getText() == null ? "" : txtMaxPrice.getText().trim();

        if (!minText.isEmpty()) {
            try {
                minPrice = Double.parseDouble(minText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Min Price must be a number.");
                return;
            }
        }
        if (!maxText.isEmpty()) {
            try {
                maxPrice = Double.parseDouble(maxText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Max Price must be a number.");
                return;
            }
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            JOptionPane.showMessageDialog(this, "Min Price cannot be greater than Max Price.");
            return;
        }

        double min = minPrice != null ? minPrice : Double.NEGATIVE_INFINITY;
        double max = maxPrice != null ? maxPrice : Double.POSITIVE_INFINITY;

        model.setRowCount(0);
        List<Product> filtered = new ArrayList<>();
        for (Product p : cachedProducts) {
            boolean matchesSearch = key.isEmpty() ||
                    p.getName().toLowerCase().contains(key) ||
                    p.getCategory().toLowerCase().contains(key) ||
                    p.getShade().toLowerCase().contains(key);
            boolean matchesCategory = category.equals("All") || p.getCategory().equalsIgnoreCase(category);
            boolean matchesPrice = p.getPrice() >= min && p.getPrice() <= max;
            if (matchesSearch && matchesCategory && matchesPrice) {
                filtered.add(p);
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
        updateSummary(filtered);
    }

    private void updateSummary(List<Product> products) {
        int totalItems = products.stream().mapToInt(Product::getNoOfItems).sum();
        double totalValue = products.stream().mapToDouble(p -> p.getPrice() * p.getNoOfItems()).sum();
        lblSummary.setText(String.format("Items: %d | Value: %.2f", totalItems, totalValue));
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String unescapeCsv(String value) {
        String v = value;
        if (v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1).replace("\"\"", "\"");
        }
        return v;
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

    private Product buildValidatedProduct(Integer existingId) {
        String name = txtName.getText().trim();
        String shade = txtShade.getText().trim();
        String priceText = txtPrice.getText().trim();
        String itemsText = txtItems.getText().trim();
        String category = cbCategory.getSelectedItem().toString();

        if (name.isEmpty() || shade.isEmpty() || priceText.isEmpty() || itemsText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return null;
        }
        if (!shade.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Shade must be numbers only (ex. 001).");
            return null;
        }
        double price;
        int items;
        try {
            price = Double.parseDouble(priceText);
            items = Integer.parseInt(itemsText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price must be a number and No. of Items must be an integer.");
            return null;
        }
        if (price < 0 || items < 0) {
            JOptionPane.showMessageDialog(this, "Price and No. of Items must be non-negative.");
            return null;
        }
        if (dao.existsByNameAndShade(name, shade, existingId)) {
            JOptionPane.showMessageDialog(this, "A product with this name and shade already exists.");
            return null;
        }
        return new Product(name, category, shade, price, items);
    }

    private void undoDelete() {
        if (lastDeleted != null) {
            dao.addProduct(new Product(lastDeleted.getName(),
                    lastDeleted.getCategory(),
                    lastDeleted.getShade(),
                    lastDeleted.getPrice(),
                    lastDeleted.getNoOfItems()));
            logAction("RESTORED", lastDeleted);
            lastDeleted = null;
            btnUndo.setEnabled(false);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Last deleted product restored.");
        } else {
            JOptionPane.showMessageDialog(this, "No deleted product to restore.");
        }
    }

    private void logAction(String action, Product p) {
        String entry = String.format("%s | %s | ID:%d | %s | %s | Shade:%s | Price:%.2f | Items:%d%n",
                LocalDateTime.now().format(LOG_FORMAT),
                action,
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getShade(),
                p.getPrice(),
                p.getNoOfItems());
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(entry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventoryGUI().setVisible(true));
    }
}
