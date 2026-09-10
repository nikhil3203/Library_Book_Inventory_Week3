#!/bin/bash
# ============================================================
#  run.sh — Library Book Inventory Launcher (Unix/macOS)
# ============================================================
#  Launches the application from the packaged JAR file.
#
#  Usage:   chmod +x run.sh && ./run.sh
#  Prereq:  Run build.sh first to create the JAR.
# ============================================================

JAR_PATH="dist/library-book-inventory.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo ""
    echo " ERROR: $JAR_PATH not found."
    echo " Please run build.sh first to compile and package the application."
    echo ""
    exit 1
fi

echo "Starting Library Book Inventory System..."
echo ""
java -jar "$JAR_PATH"
