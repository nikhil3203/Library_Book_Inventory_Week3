#!/bin/bash
# ============================================================
#  build.sh — Library Book Inventory Build Script (Unix/macOS)
# ============================================================
#  Compiles all Java source files and packages the application
#  into an executable JAR.
#
#  Usage:   chmod +x build.sh && ./build.sh
#  Output:  dist/library-book-inventory.jar
# ============================================================

set -e

echo ""
echo "=========================================="
echo " Library Book Inventory — Build Script"
echo "=========================================="
echo ""

# --- Configuration ---
SRC_DIR="src/main/java"
OUT_DIR="build/classes"
DIST_DIR="dist"
JAR_NAME="library-book-inventory.jar"
MAIN_CLASS="library.Main"

# --- Step 1: Clean previous build ---
echo "[1/3] Cleaning previous build..."
rm -rf "$OUT_DIR" "$DIST_DIR"
mkdir -p "$OUT_DIR" "$DIST_DIR"
echo "      Done."

# --- Step 2: Compile source files ---
echo "[2/3] Compiling source files..."
find "$SRC_DIR" -name "*.java" > build/sources.txt
javac -d "$OUT_DIR" @build/sources.txt
echo "      Done. Compiled successfully."

# --- Step 3: Create executable JAR ---
echo "[3/3] Packaging JAR..."
echo "Main-Class: $MAIN_CLASS" > build/MANIFEST.MF
jar cfm "$DIST_DIR/$JAR_NAME" build/MANIFEST.MF -C "$OUT_DIR" .
echo "      Done. Created $DIST_DIR/$JAR_NAME"

# --- Summary ---
echo ""
echo "=========================================="
echo " BUILD SUCCESSFUL"
echo "=========================================="
echo " JAR: $DIST_DIR/$JAR_NAME"
echo " Run: java -jar $DIST_DIR/$JAR_NAME"
echo " Or:  ./run.sh"
echo "=========================================="
echo ""
