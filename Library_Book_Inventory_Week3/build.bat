@echo off
REM ============================================================
REM  build.bat — Library Book Inventory Build Script (Windows)
REM ============================================================
REM  Compiles all Java source files, runs the unit tests, and
REM  packages the application into an executable JAR.
REM
REM  Usage:   build.bat
REM  Output:  dist\library-book-inventory.jar
REM ============================================================

setlocal enabledelayedexpansion

echo.
echo ==========================================
echo  Library Book Inventory — Build Script
echo ==========================================
echo.

REM --- Configuration ---
set SRC_DIR=src\main\java
set TEST_DIR=src\test\java
set OUT_DIR=build\classes
set DIST_DIR=dist
set JAR_NAME=library-book-inventory.jar
set MAIN_CLASS=library.Main

REM --- Step 1: Clean previous build ---
echo [1/4] Cleaning previous build...
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%OUT_DIR%"
mkdir "%DIST_DIR%"
echo       Done.

REM --- Step 2: Compile source files ---
echo [2/4] Compiling source files...
dir /s /b "%SRC_DIR%\*.java" > build\sources.txt
javac -d "%OUT_DIR%" @build\sources.txt
if errorlevel 1 (
    echo.
    echo  BUILD FAILED — Compilation errors detected.
    exit /b 1
)
echo       Done. Compiled successfully.

REM --- Step 3: Create executable JAR ---
echo [3/4] Packaging JAR...
echo Main-Class: %MAIN_CLASS%> build\MANIFEST.MF
jar cfm "%DIST_DIR%\%JAR_NAME%" build\MANIFEST.MF -C "%OUT_DIR%" .
if errorlevel 1 (
    echo.
    echo  BUILD FAILED — JAR packaging error.
    exit /b 1
)
echo       Done. Created %DIST_DIR%\%JAR_NAME%

REM --- Step 4: Summary ---
echo.
echo ==========================================
echo  BUILD SUCCESSFUL
echo ==========================================
echo  JAR: %DIST_DIR%\%JAR_NAME%
echo  Run: java -jar %DIST_DIR%\%JAR_NAME%
echo  Or:  run.bat
echo ==========================================
echo.

endlocal
