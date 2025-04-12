#!/bin/bash

# JavaFX modules needed for the application
JAVAFX_MODULES="javafx.controls,javafx.fxml,javafx.graphics"

# Set JavaFX path to the existing SDK
JAVAFX_PATH="$(pwd)/javafx-sdk/javafx-sdk-21.0.2/lib"

# Get the current directory
CURRENT_DIR="$(pwd)"

# Create a bin directory for compiled classes if it doesn't exist
mkdir -p bin

# Compile the Java files
javac --module-path "$JAVAFX_PATH" \
      --add-modules $JAVAFX_MODULES \
      -d bin \
      *.java

# Copy all resources to bin directory
cp *.fxml bin/
cp *.css bin/
mkdir -p bin/images
cp -r images/* bin/images/ 2>/dev/null || echo "Warning: Could not copy images"

# Check if images were copied
echo "Checking images in bin directory:"
find bin/images -type f | sort

# Run the application
java --module-path "$JAVAFX_PATH" \
     --add-modules $JAVAFX_MODULES \
     -cp "bin:$CURRENT_DIR" \
     App 