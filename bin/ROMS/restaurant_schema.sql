-- MySQL Schema for Restaurant Management System

-- Create the database
CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

-- Create the Manager table
CREATE TABLE Manager (
    managerID VARCHAR(50) PRIMARY KEY,
    Name VARCHAR(100) NOT NULL
);

-- Create the Kitchen table
CREATE TABLE Kitchen (
    kitchen_id INT PRIMARY KEY AUTO_INCREMENT,
    managerID VARCHAR(50),
    FOREIGN KEY (managerID) REFERENCES Manager(managerID)
);

-- Create Category table
CREATE TABLE Category (
    title VARCHAR(50) PRIMARY KEY
);

-- Create the MenuItem table
CREATE TABLE MenuItem (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT DEFAULT 1,
    category_title VARCHAR(50),
    FOREIGN KEY (category_title) REFERENCES Category(title)
);

-- Create Ingredient table
CREATE TABLE Ingredient (
    title VARCHAR(100) PRIMARY KEY
);

-- Create Inventory table for tracking stock levels
CREATE TABLE Inventory (
    inventory_id INT PRIMARY KEY AUTO_INCREMENT,
    ingredient_title VARCHAR(100),
    current_quantity DECIMAL(10,2) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    min_threshold DECIMAL(10,2) NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ingredient_title) REFERENCES Ingredient(title)
);

-- Create Inventory Transaction table to track inventory changes
CREATE TABLE Inventory_Transaction (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    inventory_id INT NOT NULL,
    transaction_type ENUM('purchase', 'usage', 'waste', 'adjustment') NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    performed_by VARCHAR(50),
    FOREIGN KEY (inventory_id) REFERENCES Inventory(inventory_id),
    FOREIGN KEY (performed_by) REFERENCES Manager(managerID)
);

-- Create Supplier table
CREATE TABLE Supplier (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255)
);

-- Create Purchase Order table
CREATE TABLE Purchase_Order (
    po_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expected_delivery_date DATE,
    status ENUM('draft', 'ordered', 'partially_received', 'completed', 'cancelled') DEFAULT 'draft',
    total_amount DECIMAL(10,2),
    created_by VARCHAR(50),
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id),
    FOREIGN KEY (created_by) REFERENCES Manager(managerID)
);

-- Create Purchase Order Items table
CREATE TABLE Purchase_Order_Item (
    po_item_id INT PRIMARY KEY AUTO_INCREMENT,
    po_id INT NOT NULL,
    ingredient_title VARCHAR(100) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    received_quantity DECIMAL(10,2) DEFAULT 0,
    FOREIGN KEY (po_id) REFERENCES Purchase_Order(po_id),
    FOREIGN KEY (ingredient_title) REFERENCES Ingredient(title)
);

-- Create Ingredient_Supplier relation (many-to-many)
CREATE TABLE Ingredient_Supplier (
    ingredient_title VARCHAR(100),
    supplier_id INT,
    standard_unit_price DECIMAL(10,2),
    supplier_item_code VARCHAR(50),
    PRIMARY KEY (ingredient_title, supplier_id),
    FOREIGN KEY (ingredient_title) REFERENCES Ingredient(title),
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);

-- Create MenuItem_Ingredient relation (many-to-many)
CREATE TABLE MenuItem_Ingredient (
    item_id INT,
    ingredient_title VARCHAR(100),
    quantity_required DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (item_id, ingredient_title),
    FOREIGN KEY (item_id) REFERENCES MenuItem(item_id),
    FOREIGN KEY (ingredient_title) REFERENCES Ingredient(title)
);

-- Create Customer table
CREATE TABLE Customer (
    customer_ID INT PRIMARY KEY AUTO_INCREMENT,
    Address VARCHAR(255),
    Table_number INT,
    toDeliver BOOLEAN DEFAULT FALSE
);

-- Create Order table
CREATE TABLE `Order` (
    OrderID INT PRIMARY KEY AUTO_INCREMENT,
    Status VARCHAR(50) NOT NULL,
    Date DATE NOT NULL,
    customer_ID INT,
    kitchen_id INT,
    managerID VARCHAR(50),
    FOREIGN KEY (customer_ID) REFERENCES Customer(customer_ID),
    FOREIGN KEY (kitchen_id) REFERENCES Kitchen(kitchen_id),
    FOREIGN KEY (managerID) REFERENCES Manager(managerID)
);

-- Create Order_MenuItem relation (many-to-many)
CREATE TABLE Order_MenuItem (
    OrderID INT,
    item_id INT,
    quantity INT DEFAULT 1,
    PRIMARY KEY (OrderID, item_id),
    FOREIGN KEY (OrderID) REFERENCES `Order`(OrderID),
    FOREIGN KEY (item_id) REFERENCES MenuItem(item_id)
);

-- Create Kitchen_MenuItem relation (many-to-many)
CREATE TABLE Kitchen_MenuItem (
    kitchen_id INT,
    item_id INT,
    PRIMARY KEY (kitchen_id, item_id),
    FOREIGN KEY (kitchen_id) REFERENCES Kitchen(kitchen_id),
    FOREIGN KEY (item_id) REFERENCES MenuItem(item_id)
);

-- Create triggers to update inventory when orders are placed
DELIMITER //
CREATE TRIGGER update_inventory_after_order
AFTER INSERT ON Order_MenuItem
FOR EACH ROW
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE ing_title VARCHAR(100);
    DECLARE qty_required DECIMAL(10,2);
    DECLARE total_qty_required DECIMAL(10,2);
    DECLARE cur CURSOR FOR 
        SELECT mi.ingredient_title, mi.quantity_required * NEW.quantity
        FROM MenuItem_Ingredient mi
        WHERE mi.item_id = NEW.item_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO ing_title, qty_required;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Create inventory transaction record
        INSERT INTO Inventory_Transaction (inventory_id, transaction_type, quantity, notes)
        SELECT i.inventory_id, 'usage', qty_required, CONCAT('Order ID: ', NEW.OrderID)
        FROM Inventory i
        WHERE i.ingredient_title = ing_title;
        
        -- Update current quantity in inventory
        UPDATE Inventory
        SET current_quantity = current_quantity - qty_required
        WHERE ingredient_title = ing_title;
    END LOOP;
    
    CLOSE cur;
END //
DELIMITER ;