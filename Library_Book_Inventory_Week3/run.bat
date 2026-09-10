@echo off
REM ============================================================
REM  run.bat — Library Book Inventory Launcher (Windows)
REM ============================================================
REM  Launches the application from the packaged JAR file.
REM
REM  Usage:   run.bat
REM  Prereq:  Run build.bat first to create the JAR.
REM ============================================================

setlocal

set JAR_PATH=dist\library-book-inventory.jar

if not exist "%JAR_PATH%" (
    echo.
    echo  ERROR: %JAR_PATH% not found.
    echo  Please run build.bat first to compile and package the application.
    echo.
    exit /b 1
)

echo Starting Library Book Inventory System...
echo.
java -jar "%JAR_PATH%"

endlocal
