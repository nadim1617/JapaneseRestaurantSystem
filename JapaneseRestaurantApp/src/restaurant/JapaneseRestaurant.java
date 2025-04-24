package restaurant;



import java.io.Console;
import java.sql.*;
import java.util.*;



public class JapaneseRestaurant {

    // Database connection
    public static Connection getConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/restaurant_db";
            String user = "root";
            String password = "";
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
            return null;
        }
    }

    
    
    // Clear screen method
    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    
    
    // View menu items
    public static void viewMenu() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM menu")) {
            System.out.println("\n--- Japanese Restaurant Menu ---");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ". " + rs.getString("item_name") + " - Tk " + rs.getDouble("price"));
            }
        } catch (Exception e) {
            System.out.println("Error displaying menu.");
        }
    }

    
    
    // Place order
    public static void placeOrder(String name, Scanner sc) {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM menu");

            System.out.println("\n🍣======= Menu =======🍣");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ". " + rs.getString("item_name") + " - " + rs.getDouble("price") + " BDT");
            }

            System.out.print("\n📱 Enter your phone number: ");
            String phone = sc.next();

            System.out.print("🛍️ Enter item ID to order: ");
            int itemId = sc.nextInt();
            System.out.print("🔢 Enter quantity: ");
            int quantity = sc.nextInt();

            // Payment Method
            System.out.println("\n💳 Choose Payment Method:");
            System.out.println("1. bKash");
            System.out.println("2. Nagad");
            System.out.println("3. Cash");
            System.out.print("Option: ");
            int payOption = sc.nextInt();

            boolean paymentSuccess = false;

            if (payOption == 1 || payOption == 2) {
                System.out.println("✅ Online payment received successfully via " + (payOption == 1 ? "bKash" : "Nagad"));
                paymentSuccess = true;
            } else if (payOption == 3) {
                int pin = new Random().nextInt(9000) + 1000;
                System.out.println("📟 Your 4-digit PIN from the reception desk: " + pin);
                System.out.print("🔐 Enter that PIN to confirm payment: ");
                int enteredPin = sc.nextInt();

                if (enteredPin == pin) {
                    System.out.println("💵 Cash payment confirmed.");
                    paymentSuccess = true;
                } else {
                    System.out.println("❌ Invalid PIN. Order cancelled.");
                    return;
                }
            } else {
                System.out.println("❌ Invalid payment method. Order cancelled.");
                return;
            }

            if (paymentSuccess) {
                rs = stmt.executeQuery("SELECT item_name, price FROM menu WHERE id = " + itemId);
                if (rs.next()) {
                    String itemName = rs.getString("item_name");
                    double price = rs.getDouble("price");
                    double total = price * quantity;

                    String sql = "INSERT INTO orders (customer_name, phone_number, item_name, quantity, total_price) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, name);
                    ps.setString(2, phone);
                    ps.setString(3, itemName);
                    ps.setInt(4, quantity);
                    ps.setDouble(5, total);
                    ps.executeUpdate();

                    System.out.println("\n🧾=========== Digital Bill ===========");
                    System.out.println("Customer: " + name);
                    System.out.println("Phone: " + phone);
                    System.out.println("Item: " + itemName);
                    System.out.println("Quantity: " + quantity);
                    System.out.println("Total: " + total + " BDT");
                    System.out.println("Payment: " + (payOption == 1 ? "bKash" : payOption == 2 ? "Nagad" : "Cash"));
                    System.out.println("\n🧾 Thank you for your order! Enjoy your meal.");
                    System.out.println("=====================================");
                } else {
                    System.out.println("❌ Item not found.");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to place order: " + e.getMessage());
        }
    }

    
    
    // View user order history
    public static void viewOrderHistory(String customerName) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE customer_name = ?");
            ps.setString(1, customerName);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n--- Order History for " + customerName + " ---");
            while (rs.next()) {
                System.out.println(rs.getString("item_name") + " - Tk " + rs.getDouble("total_price") + " on " + rs.getTimestamp("order_time"));
            }
        } catch (Exception e) {
            System.out.println("Error showing order history.");
        }
    }

    
    
    // Admin login check
    public static boolean adminLogin(String username, String password) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM admin WHERE username = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            System.out.println("Login error.");
            return false;
        }
    }

    
    
    // Add menu item
    public static void addMenuItem(Scanner sc) {
        try (Connection conn = getConnection()) {
            sc.nextLine();
            System.out.print("Enter new item name: ");
            String item = sc.nextLine();
            System.out.print("Enter price: ");
            double price = sc.nextDouble();

            PreparedStatement ps = conn.prepareStatement("INSERT INTO menu (item_name, price) VALUES (?, ?)");
            ps.setString(1, item);
            ps.setDouble(2, price);
            ps.executeUpdate();

            System.out.println("Item added successfully.");
        } catch (Exception e) {
            System.out.println("Error adding item.");
        }
    }

    
    
    // Remove menu item
    public static void removeMenuItem(Scanner sc) {
        viewMenu();
        try (Connection conn = getConnection()) {
            System.out.print("Enter item ID to remove: ");
            int id = sc.nextInt();

            PreparedStatement ps = conn.prepareStatement("DELETE FROM menu WHERE id = ?");
            ps.setInt(1, id);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Item removed.");
            } else {
                System.out.println("Item not found.");
            }
        } catch (Exception e) {
            System.out.println("Error removing item.");
        }
    }
    
    
    
    public static void showLoadingBar() {
        System.out.print("Loading ");
        for (int i = 0; i <= 20; i++) {
            try {
                Thread.sleep(100); // Delay for animation effect
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.print("#");
        }
        System.out.println("\n");
        try {
            Thread.sleep(500); // Slight pause after bar
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        clearScreen(); // Clear after loading
    }

    

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        showLoadingBar();
        
        int role;

        do {
            System.out.println("\n🍣==================================🍣");
            System.out.println("     Welcome to Japanese Restaurant");
            System.out.println("🍣==================================🍣");
            System.out.println("1. 👤 User");
            System.out.println("2. 🔐 Admin");
            System.out.println("0. ❎ Exit");
            System.out.print("Select an option: ");
            role = sc.nextInt();

            if (role == 1) {
                clearScreen();
                sc.nextLine();
                System.out.print("Enter your name: ");
                String name = sc.nextLine();

                int choice;
                do {
                    System.out.println("\n🎌========== User Panel ==========");
                    System.out.println("1. 🍱 View Menu");
                    System.out.println("2. 🧾 Place Order");
                    System.out.println("3. 📖 View Order History");
                    System.out.println("0. 🔙 Back to Main Menu");
                    System.out.print("Your choice: ");
                    choice = sc.nextInt();

                    switch (choice) {
                        case 1: viewMenu(); break;
                        case 2: placeOrder(name, sc); break;
                        case 3: viewOrderHistory(name); break;
                    }
                } while (choice != 0);

                clearScreen();  // Optional refresh before returning to main menu
            }

            else if (role == 2) {
                clearScreen();
                sc.nextLine();
                System.out.print("Enter Admin Username: ");
                String user = sc.nextLine();

                String pass = "";
                Console console = System.console();
                if (console != null) {
                    char[] passwordChars = console.readPassword("Enter Admin Password: ");
                    pass = new String(passwordChars);
                } else {
                    System.out.print("Enter Admin Password (⚠️ will be visible): ");
                    pass = sc.nextLine();
                }

                if (adminLogin(user, pass)) {
                    int choice;
                    do {
                        System.out.println("\n🎌========== Admin Panel ==========");
                        System.out.println("1. 📜 View Menu");
                        System.out.println("2. ➕ Add Menu Item");
                        System.out.println("3. ❌ Remove Menu Item");
                        System.out.println("0. 🔙 Logout to Main Menu");
                        System.out.print("Your choice: ");
                        choice = sc.nextInt();

                        switch (choice) {
                            case 1: viewMenu(); break;
                            case 2: addMenuItem(sc); break;
                            case 3: removeMenuItem(sc); break;
                        }
                    } while (choice != 0);
                    System.out.println("\n🔐 Admin logged out successfully.");
                    clearScreen();  // Optional refresh before returning to main menu
                } else {
                    System.out.println("❌ Invalid login.");
                }
            }

            else if (role != 0) {
                System.out.println("❗ Invalid selection. Please try again.");
            }

        } while (role != 0);

        System.out.println("\n🍥 Thank you for using Japanese Restaurant Management System!");
        System.out.println("Exiting... Sayonara! 🌸");
    }
}
