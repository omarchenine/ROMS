# ROMS - Restaurant Operations Management System

A comprehensive database schema and management system for restaurant operations.

## Features

- **Menu Management**: Organize and maintain your restaurant's menu items and categories
- **Order Processing**: Track and manage customer orders and their status
- **Kitchen Operations**: Manage kitchen workflows and menu item preparation
- **Inventory Management**: 
  - Track ingredient stock levels
  - Manage suppliers
  - Create and track purchase orders
  - Automated inventory usage tracking
  - Threshold alerts for reordering

## Database Schema

The system uses a MySQL database with the following main components:

- Core entities: Menu Items, Categories, Ingredients, Orders, Customers
- Inventory tracking: Current stock levels, transaction history
- Supplier management: Supplier information and purchase orders
- Relational tables for many-to-many relationships

## Getting Started

1. Import the `restaurant_schema.sql` file into your MySQL environment
2. Configure database connection in your application
3. Start using the system to manage your restaurant operations

## Schema Diagram

(Schema diagram to be added)

## License

This project is licensed under the MIT License - see the LICENSE file for details. 