#!/usr/bin/env bash
# ============================================================
#  deploy.sh — Library Book Inventory Deployment Script (Unix)
# ============================================================
#  Performs a full build, packages the JAR, and copies the
#  artifact to a deployment directory.
#
#  Usage:   ./deploy.sh [target-dir]
#           ./deploy.sh                    → deploys to ./release
#           ./deploy.sh /opt/library-app   → deploys to /opt/library-app
# ============================================================

set -euo pipefail

echo ""
echo "=========================================="
echo " Library Book Inventory — Deployment"
echo "=========================================="
echo ""

# --- Configuration ---
SRC_DIR="src/main/java"
OUT_DIR="build/classes"
DIST_DIR="dist"
JAR_NAME="library-book-inventory.jar"
MAIN_CLASS="library.Main"
DEPLOY_DIR="${1:-release}"

# --- Step 1: Verify Java ---
echo "[1/5] Verifying Java installation..."
if ! command -v java &> /dev/null; then
    echo "  DEPLOY FAILED — Java is not installed or not on PATH."
    exit 1
fi
if ! command -v javac &> /dev/null; then
    echo "  DEPLOY FAILED — javac not found. Please install JDK 17+."
    exit 1
fi
echo "      Java OK."

# --- Step 2: Clean ---
echo "[2/5] Cleaning previous build..."
rm -rf "$OUT_DIR" "$DIST_DIR"
mkdir -p "$OUT_DIR" "$DIST_DIR"
echo "      Done."

# --- Step 3: Compile ---
echo "[3/5] Compiling source files..."
find "$SRC_DIR" -name "*.java" > build/sources.txt
javac -d "$OUT_DIR" @build/sources.txt
echo "      Compiled successfully."

# --- Step 4: Package JAR ---
echo "[4/5] Packaging executable JAR..."
echo "Main-Class: $MAIN_CLASS" > build/MANIFEST.MF
jar cfm "$DIST_DIR/$JAR_NAME" build/MANIFEST.MF -C "$OUT_DIR" .
echo "      Created $DIST_DIR/$JAR_NAME"

# --- Step 5: Deploy ---
echo "[5/5] Deploying to $DEPLOY_DIR..."
mkdir -p "$DEPLOY_DIR"
cp "$DIST_DIR/$JAR_NAME" "$DEPLOY_DIR/"

# Create launcher script
cat > "$DEPLOY_DIR/start.sh" << 'LAUNCHER'
#!/usr/bin/env bash
echo "Starting Library Book Inventory System..."
echo ""
DIR="$(cd "$(dirname "$0")" && pwd)"
java -jar "$DIR/library-book-inventory.jar"
LAUNCHER
chmod +x "$DEPLOY_DIR/start.sh"

echo "      Deployed to $DEPLOY_DIR/"

# --- Summary ---
echo ""
echo "=========================================="
echo " DEPLOYMENT SUCCESSFUL"
echo "=========================================="
echo " Artifact : $DEPLOY_DIR/$JAR_NAME"
echo " Launcher : $DEPLOY_DIR/start.sh"
echo ""
echo " To run:"
echo "   cd $DEPLOY_DIR"
echo "   ./start.sh"
echo "   -OR-"
echo "   java -jar $JAR_NAME"
echo "=========================================="
echo ""
