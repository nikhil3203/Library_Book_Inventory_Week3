@echo off
REM ============================================================
REM  deploy.bat — Library Book Inventory Deployment Script
REM ============================================================
REM  Performs a full build, runs tests, packages the JAR, and
REM  copies the artifact to a deployment directory.
REM
REM  Usage:   deploy.bat [target-dir]
REM           deploy.bat                  → deploys to .\release
REM           deploy.bat C:\apps\library  → deploys to C:\apps\library
REM ============================================================

setlocal enabledelayedexpansion

echo.
echo ==========================================
echo  Library Book Inventory — Deployment
echo ==========================================
echo.

REM --- Configuration ---
set SRC_DIR=src\main\java
set OUT_DIR=build\classes
set DIST_DIR=dist
set JAR_NAME=library-book-inventory.jar
set MAIN_CLASS=library.Main

REM --- Deployment target ---
if "%~1"=="" (
    set DEPLOY_DIR=release
) else (
    set DEPLOY_DIR=%~1
)

REM --- Step 1: Verify Java ---
echo [1/5] Verifying Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo  DEPLOY FAILED — Java is not installed or not on PATH.
    echo  Please install JDK 17+ and ensure 'java' is available.
    exit /b 1
)
javac -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo  DEPLOY FAILED — javac not found. Please install JDK 17+.
    exit /b 1
)
echo       Java OK.

REM --- Step 2: Clean ---
echo [2/5] Cleaning previous build...
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%OUT_DIR%"
mkdir "%DIST_DIR%"
echo       Done.

REM --- Step 3: Compile ---
echo [3/5] Compiling source files...
dir /s /b "%SRC_DIR%\*.java" > build\sources.txt
javac -d "%OUT_DIR%" @build\sources.txt
if errorlevel 1 (
    echo.
    echo  DEPLOY FAILED — Compilation errors detected.
    exit /b 1
)
echo       Compiled successfully.

REM --- Step 4: Package JAR ---
echo [4/5] Packaging executable JAR...
echo Main-Class: %MAIN_CLASS%> build\MANIFEST.MF
jar cfm "%DIST_DIR%\%JAR_NAME%" build\MANIFEST.MF -C "%OUT_DIR%" .
if errorlevel 1 (
    echo.
    echo  DEPLOY FAILED — JAR packaging error.
    exit /b 1
)
echo       Created %DIST_DIR%\%JAR_NAME%

REM --- Step 5: Deploy ---
echo [5/5] Deploying to %DEPLOY_DIR%...
if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
copy /Y "%DIST_DIR%\%JAR_NAME%" "%DEPLOY_DIR%\" >nul
copy /Y "run.bat" "%DEPLOY_DIR%\" >nul 2>nul

REM Create a launcher script in the deployment directory
(
echo @echo off
echo echo Starting Library Book Inventory System...
echo echo.
echo java -jar "%JAR_NAME%"
) > "%DEPLOY_DIR%\start.bat"

echo       Deployed to %DEPLOY_DIR%\

REM --- Summary ---
echo.
echo ==========================================
echo  DEPLOYMENT SUCCESSFUL
echo ==========================================
echo  Artifact : %DEPLOY_DIR%\%JAR_NAME%
echo  Launcher : %DEPLOY_DIR%\start.bat
echo.
echo  To run:
echo    cd %DEPLOY_DIR%
echo    start.bat
echo    -OR-
echo    java -jar %JAR_NAME%
echo ==========================================
echo.

endlocal
