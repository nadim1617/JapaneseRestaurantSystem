package restaurant;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Random;

@SuppressWarnings("unused")
public class JapaneseRestaurantGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    Connection conn;
    boolean isDarkMode = false;

    Font globalFont = new Font("SansSerif", Font.BOLD, 20);

    public JapaneseRestaurantGUI() {
        conn = getConnection();
        setupFrame();
        showLoginMenu();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JapaneseRestaurantGUI::new);
    }

    public Connection getConnection() {
        try {
        	return DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurant_db", "root", "");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Connection Failed!");
            return null;
        }
    }

    void setupFrame() {
        setTitle("🍣 Japanese Restaurant System");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    void applyTheme(Component comp) {
        if (comp instanceof JPanel || comp instanceof JScrollPane) {
            comp.setBackground(isDarkMode ? Color.DARK_GRAY : Color.WHITE);
        }
        if (comp instanceof JButton || comp instanceof JLabel || comp instanceof JTextArea || comp instanceof JTextField) {
            comp.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
            comp.setBackground(isDarkMode ? new Color(50, 50, 50) : Color.WHITE);
            comp.setFont(globalFont);
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyTheme(child);
            }
        }
    }

    void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        repaint();
    }

    void addSettingsMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu settings = new JMenu("⚙ Settings");
        JMenuItem toggle = new JMenuItem("Toggle Dark Mode");
        toggle.addActionListener(e -> {
            toggleDarkMode();
            applyTheme(this.getContentPane());
        });
        settings.add(toggle);
        menuBar.add(settings);
        setJMenuBar(menuBar);
    }

    @SuppressWarnings("serial")
	JPanel gradientPanel() {
        return new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                Color color1 = isDarkMode ? new Color(50, 50, 50) : new Color(255, 248, 220);
                Color color2 = isDarkMode ? Color.BLACK : new Color(255, 182, 193);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
    }

    void showLoginMenu() {
        getContentPane().removeAll();
        addSettingsMenu();

        JButton userBtn = new JButton("👤 User");
        JButton adminBtn = new JButton("🔐 Admin");

        Dimension btnSize = new Dimension(300, 70);
        userBtn.setPreferredSize(btnSize);
        adminBtn.setPreferredSize(btnSize);

        userBtn.addActionListener(e -> showUserPanel());
        adminBtn.addActionListener(e -> showAdminLogin());

        JPanel panel = gradientPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(userBtn, gbc);
        gbc.gridy++;
        panel.add(adminBtn, gbc);

        applyTheme(panel);
        setContentPane(panel);
        revalidate();
        repaint();
        setVisible(true);
    }

    void showUserPanel() {
        getContentPane().removeAll();
        addSettingsMenu();

        JTextArea area = new JTextArea();
        area.setFont(globalFont);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton view = new JButton("🍱 View Menu");
        JButton order = new JButton("🧾 Place Order");
        JButton history = new JButton("📖 Order History");
        JButton back = new JButton("🔙 Back");

        JButton[] buttons = {view, order, history, back};
        for (JButton btn : buttons) {
            btn.setPreferredSize(new Dimension(200, 60));
        }

        view.addActionListener(e -> {
            area.setText("");
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM menu")) {
                while (rs.next()) {
                    area.append(rs.getInt("id") + ". " + rs.getString("item_name") + " - Tk " + rs.getDouble("price") + "\n");
                }
            } catch (Exception ex) {
                area.setText("Failed to load menu.");
            }
        });

        order.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter your name:");
            String phone = JOptionPane.showInputDialog(this, "Phone number:");
            String itemId = JOptionPane.showInputDialog(this, "Menu item ID:");
            String quantity = JOptionPane.showInputDialog(this, "Quantity:");
            int qty = Integer.parseInt(quantity);
            double price;

            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM menu WHERE id = ?")) {
                ps.setInt(1, Integer.parseInt(itemId));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String itemName = rs.getString("item_name");
                    price = rs.getDouble("price");

                    int method = JOptionPane.showOptionDialog(this, "Choose Payment Method", "Payment",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                            new String[]{"bKash", "Nagad", "Cash"}, "bKash");

                    boolean success = false;
                    if (method == 2) {
                        int pin = new Random().nextInt(9000) + 1000;
                        String userPin = JOptionPane.showInputDialog(this, "Enter PIN from reception: " + pin);
                        success = Integer.parseInt(userPin) == pin;
                    } else {
                        success = true;
                    }

                    if (success) {
                        double total = price * qty;
                        PreparedStatement orderStmt = conn.prepareStatement(
                                "INSERT INTO orders (customer_name, phone_number, item_name, quantity, total_price) VALUES (?, ?, ?, ?, ?)");
                        orderStmt.setString(1, name);
                        orderStmt.setString(2, phone);
                        orderStmt.setString(3, itemName);
                        orderStmt.setInt(4, qty);
                        orderStmt.setDouble(5, total);
                        orderStmt.executeUpdate();

                        area.setText("🧾 Bill:\n\nName: " + name + "\nItem: " + itemName + "\nQty: " + qty + "\nTotal: Tk " + total);
                    } else {
                        area.setText("❌ Payment Failed.");
                    }
                } else {
                    area.setText("❌ Item not found.");
                }
            } catch (Exception ex) {
                area.setText("❌ Error placing order.");
            }
        });

        history.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter your name:");
            area.setText("");
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE customer_name = ?")) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    area.append(rs.getString("item_name") + " x" + rs.getInt("quantity") + " - Tk " + rs.getDouble("total_price") + "\n");
                }
            } catch (Exception ex) {
                area.setText("❌ Error loading history.");
            }
        });

        back.addActionListener(e -> showLoginMenu());

        JPanel btnPanel = new JPanel();
        for (JButton btn : buttons) btnPanel.add(btn);
        applyTheme(btnPanel);

        JScrollPane scroll = new JScrollPane(area);
        applyTheme(scroll);

        JPanel content = gradientPanel();
        content.setLayout(new BorderLayout());
        content.add(scroll, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        applyTheme(content);
        setContentPane(content);
        revalidate();
        repaint();
        setVisible(true);
    }



    void showAdminLogin() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(user);
        panel.add(new JLabel("Password:"));
        panel.add(pass);

        applyTheme(panel);

        int result = JOptionPane.showConfirmDialog(this, panel, "Admin Login", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = user.getText().trim();
            String password = new String(pass.getPassword()).trim();
            
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM admin WHERE username = ? AND password = ?")) {
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    showAdminPanel();
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Invalid credentials.");
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Log the exception
                JOptionPane.showMessageDialog(this, "❌ Login error: " + e.getMessage());
            }
        }
    }

    void showAdminPanel() {
        getContentPane().removeAll();
        addSettingsMenu();

        JTextArea output = new JTextArea();
        output.setFont(globalFont);
        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(output);

        JButton view = new JButton("📜 View Menu");
        JButton add = new JButton("➕ Add Item");
        JButton remove = new JButton("❌ Remove Item");
        JButton back = new JButton("🔙 Logout");

        // Create a panel with GridBagLayout
        JPanel btnPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make buttons fill the width
        gbc.insets = new Insets(10, 10, 10, 10); // Add some padding

        // Add buttons to the button panel with constraints
        for (int i = 0; i < 4; i++) {
            gbc.gridx = 0; // Column
            gbc.gridy = i; // Row
            btnPanel.add(new JButton[] {view, add, remove, back}[i], gbc);
        }

        // Add action listeners for buttons
        view.addActionListener(e -> {
            output.setText("");
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM menu")) {
                while (rs.next()) {
                    output.append(rs.getInt("id") + ". " + rs.getString("item_name") + " - Tk " + rs.getDouble("price") + "\n");
                }
            } catch (Exception ex) {
                output.setText("❌ Error loading menu.");
            }
        });

        add.addActionListener(e -> {
            String item = JOptionPane.showInputDialog(this, "Item name:");
            String price = JOptionPane.showInputDialog(this, "Price:");
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO menu(item_name, price) VALUES (?, ?)")) {
                ps.setString(1, item);
                ps.setDouble(2, Double.parseDouble(price));
                ps.executeUpdate();
                output.setText("✅ Item added.");
            } catch (Exception ex) {
                output.setText("❌ Error adding item.");
            }
        });

        remove.addActionListener(e -> {
            String id = JOptionPane.showInputDialog(this, "Enter item ID to remove:");
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM menu WHERE id = ?")) {
                ps.setInt(1, Integer.parseInt(id));
                int rows = ps.executeUpdate();
                output.setText(rows > 0 ? "✅ Item removed." : "❌ Item not found.");
            } catch (Exception ex) {
                output.setText("❌ Error removing item.");
            }
        });

        back.addActionListener(e -> showLoginMenu());

        // Apply theme to components
        applyTheme(scroll);
        applyTheme(btnPanel);

        // Add components to the main frame
        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        revalidate();
        repaint();
        setVisible(true);
    }
}
