package inventory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class LoginGUI extends JFrame {

    private static final Color PRIMARY = new Color(0xFCF8F8);
    private static final Color SECONDARY = new Color(0xFBEFEF);
    private static final Color TERTIARY = new Color(0xF9DFDF);

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;
    private UserDAO userDAO = new UserDAO();

    public LoginGUI() {
        setTitle("Login - Makeup Inventory");
        setSize(520, 260);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PRIMARY);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(SECONDARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUser = new JTextField();
        txtPass = new JPasswordField();

        btnLogin = new JButton("Login");

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        formPanel.add(txtPass, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.weightx = 0; gbc.insets = new Insets(12, 4, 4, 4);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(btnLogin, gbc);

        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        logoPanel.setBackground(TERTIARY);
        JLabel logoLabel = createLogoLabel();
        JLabel nameLine1 = new JLabel("Makeup");
        JLabel nameLine2 = new JLabel("Inventory System");

        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLine1.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLine2.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameLine1.setFont(nameLine1.getFont().deriveFont(Font.BOLD, 18f));
        nameLine2.setFont(nameLine2.getFont().deriveFont(Font.PLAIN, 13f));

        nameLine1.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        logoPanel.add(logoLabel);
        logoPanel.add(nameLine1);
        logoPanel.add(nameLine2);

        JPanel contentPanel = new JPanel(new BorderLayout(12, 0));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        contentPanel.setBackground(PRIMARY);
        contentPanel.add(logoPanel, BorderLayout.WEST);
        contentPanel.add(formPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // Events
        btnLogin.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(btnLogin);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void doLogin() {
        String username = txtUser.getText();
        String password = new String(txtPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in all fields.");
            return;
        }

        if (failedAttempts >= MAX_ATTEMPTS) {
            JOptionPane.showMessageDialog(this, "Too many failed attempts. Please restart the app.");
            return;
        }

        if (userDAO.login(username, password)) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            new InventoryGUI().setVisible(true); // open inventory
            dispose(); // close login window
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
            failedAttempts++;
            if (failedAttempts >= MAX_ATTEMPTS) {
                btnLogin.setEnabled(false);
            }
        }
    }

    private JLabel createLogoLabel() {
        JLabel logoLabel;
        java.net.URL iconURL = getClass().getResource("/logo.png");
        if (iconURL != null) {
            ImageIcon rawIcon = new ImageIcon(iconURL);
            Image scaledImage = rawIcon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new ImageIcon(scaledImage));
        } else {
            logoLabel = new JLabel("Logo");
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            logoLabel.setVerticalAlignment(SwingConstants.CENTER);
        }
        logoLabel.setPreferredSize(new Dimension(140, 140));
        return logoLabel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}
