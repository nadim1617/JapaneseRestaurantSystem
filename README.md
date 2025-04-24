# Japanese Restaurant System

## Description
This is a Java-based restaurant management system for a Japanese restaurant. It allows users to view the menu, place orders, and view order history. Admins can log in to manage the menu items.

## Requirements
- Java 8 or higher
- MySQL Database
- JDBC driver for MySQL

## Setup Instructions

1. Clone this repository to your local machine.

   ```bash
   git clone https://github.com/<username>/JapaneseRestaurantSystem.git
   ```

2. Set up the MySQL database:
   - Create a database named `restaurant_db`.
   - Run the `restaurant.sql` file to create the required tables (`menu`, `orders`, `admin`).
   
   Example SQL command to create the database:
   ```sql
   CREATE DATABASE restaurant_db;
   USE restaurant_db;
   ```

3. Import the `restaurant.sql` file to create tables.

4. Run the Java program using your IDE or terminal.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
