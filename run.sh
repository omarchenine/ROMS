#!/bin/bash

# JavaFX modules needed for the application
JAVAFX_MODULES="javafx.controls,javafx.fxml,javafx.graphics"

# Set JavaFX path to the existing SDK
JAVAFX_PATH="$(pwd)/bin/javafx-sdk/javafx-sdk-21.0.2/lib"

# MySQL connector path - Download this file and place it in the project root
MYSQL_CONNECTOR="$(pwd)/mysql-connector-j-8.0.33.jar"

# Get the current directory
CURRENT_DIR="$(pwd)"

# Create directories
mkdir -p bin
mkdir -p bin/ROMS
mkdir -p bin/images
mkdir -p bin/ROMS/images

# Check if MySQL connector exists
if [ ! -f "$MYSQL_CONNECTOR" ]; then
    echo "MySQL connector not found at $MYSQL_CONNECTOR"
    echo "Downloading MySQL connector..."
    curl -L -o mysql-connector-j-8.0.33.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar
    MYSQL_CONNECTOR="$(pwd)/mysql-connector-j-8.0.33.jar"
fi

# Compile the Java files with specific Java version
javac --module-path "$JAVAFX_PATH" \
      --add-modules $JAVAFX_MODULES \
      -source 17 \
      -target 17 \
      -cp "$MYSQL_CONNECTOR" \
      -d bin \
      *.java

# Copy all resources to bin directory
cp *.fxml bin/
cp *.css bin/
cp *.fxml bin/ROMS/
cp *.css bin/ROMS/
cp -r images/* bin/images/ 2>/dev/null || echo "Warning: Could not copy images"
cp -r images/* bin/ROMS/images/ 2>/dev/null || echo "Warning: Could not copy images to ROMS"

# Check if images were copied
echo "Checking images in bin directory:"
find bin/images -type f | sort

echo "Running Restaurant Operations Management System..."
# Run the application
java --module-path "$JAVAFX_PATH" \
     --add-modules $JAVAFX_MODULES \
     -cp "$MYSQL_CONNECTOR:bin:$CURRENT_DIR" \
     ROMS.App 