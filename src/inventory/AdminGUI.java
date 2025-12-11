package inventory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.util.List;

public class AdminGUI extends JFrame {

    private static final Color PRIMARY = new Color(0xFCF8F8);
    private static final Color SECONDARY = new Color(0xFBEFEF);
    private static final Color TERTIARY = new Color(0xF9DFDF);
    private static final Color QUATERNARY = new Color(0xF5AFAF);

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel model;
    private final UserDAO userDAO = new UserDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private int selectedId = -1;

    public AdminGUI() {
        setTitle("Admin - Employee Management");
        setSize(1000, 600);
        setMinimumSize(new Dimension(900, 520));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PRIMARY);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(SECONDARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUser = new JTextField();
        txtPass = new JPasswordField();

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnExportCsv = new JButton("Export Inventory CSV");
        JButton btnExportTxt = new JButton("Export Inventory Notepad");
        JButton btnLogout = new JButton("Logout");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        formPanel.add(txtPass, gbc);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        buttonRow.setBackground(SECONDARY);
        buttonRow.add(btnAdd);
        buttonRow.add(btnUpdate);
        buttonRow.add(btnDelete);
        buttonRow.add(btnClear);
        buttonRow.add(btnExportCsv);
        buttonRow.add(btnExportTxt);
        buttonRow.add(btnLogout);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PRIMARY);
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonRow, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Username", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionBackground(QUATERNARY);
        table.setSelectionForeground(Color.BLACK);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(PRIMARY);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchPanel.setBackground(TERTIARY);
        txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("Search");
        JButton btnClearSearch = new JButton("Clear");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClearSearch);
        add(searchPanel, BorderLayout.SOUTH);

        loadEmployees(null);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
                txtUser.setText(model.getValueAt(row, 1).toString());
                txtPass.setText("");
            }
        });

        btnAdd.addActionListener(e -> addEmployee());
        btnUpdate.addActionListener(e -> updateEmployee());
        btnDelete.addActionListener(e -> deleteEmployee());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> loadEmployees(txtSearch.getText()));
        btnClearSearch.addActionListener(e -> { txtSearch.setText(""); loadEmployees(null); });
        btnExportCsv.addActionListener(e -> exportInventoryCsv());
        btnExportTxt.addActionListener(e -> exportInventoryTxt());
        btnLogout.addActionListener(e -> {
            new LoginGUI().setVisible(true);
            dispose();
        });

        getRootPane().registerKeyboardAction(e -> {
                    new LoginGUI().setVisible(true);
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadEmployees(String search) {
        model.setRowCount(0);
        List<User> users = userDAO.listEmployees(search);
        for (User u : users) {
            model.addRow(new Object[]{u.getId(), u.getUsername(), u.getRole()});
        }
    }

    private void addEmployee() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required.");
            return;
        }
        if (userDAO.createUser(user, pass, "employee")) {
            JOptionPane.showMessageDialog(this, "Employee added.");
            clearForm();
            loadEmployees(null);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add employee (maybe duplicate?).");
        }
    }

    private void updateEmployee() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username is required.");
            return;
        }
        if (userDAO.updateUser(selectedId, user, pass, "employee")) {
            JOptionPane.showMessageDialog(this, "Employee updated.");
            clearForm();
            loadEmployees(null);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update employee.");
        }
    }

    private void deleteEmployee() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected employee?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        if (userDAO.deleteUser(selectedId)) {
            JOptionPane.showMessageDialog(this, "Employee deleted.");
            clearForm();
            loadEmployees(null);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete employee.");
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtUser.setText("");
        txtPass.setText("");
        table.clearSelection();
    }

    private void exportInventoryCsv() {
        try (FileWriter fw = new FileWriter("inventory.csv")) {
            fw.write("id,name,category,shade,price,no_of_items\n");
            var products = productDAO.getAllProducts();
            for (Product p : products) {
                fw.write(String.format("%d,%s,%s,%s,%.2f,%d%n",
                        p.getId(),
                        escapeCsv(p.getName()),
                        escapeCsv(p.getCategory()),
                        escapeCsv(p.getShade()),
                        p.getPrice(),
                        p.getNoOfItems()));
            }
            JOptionPane.showMessageDialog(this, "Exported to inventory.csv");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed.");
            e.printStackTrace();
        }
    }

    private void exportInventoryTxt() {
        try (FileWriter fw = new FileWriter("inventory_backup.txt")) {
            var products = productDAO.getAllProducts();
            for (Product p : products) {
                fw.write(String.format("%d | %s | %s | %s | %.2f | %d%n",
                        p.getId(),
                        p.getName(),
                        p.getCategory(),
                        p.getShade(),
                        p.getPrice(),
                        p.getNoOfItems()));
            }
            JOptionPane.showMessageDialog(this, "Exported to inventory_backup.txt");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed.");
            e.printStackTrace();
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

