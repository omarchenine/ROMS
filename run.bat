@echo off

rem Set the JavaFX path - update this with your actual JavaFX path
set JAVAFX_PATH=C:\path\to\javafx-sdk-21.0.6

rem Set the MySQL connector path
set MYSQL_CONNECTOR=C:\path\to\mysql-connector-j-8.0.33.jar

rem Create bin directory if it doesn't exist
if not exist bin mkdir bin

rem Compile all the Java files
echo Compiling Java files...
javac --module-path "%JAVAFX_PATH%\lib" --add-modules javafx.controls,javafx.fxml -d bin src\ROMS\*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed.
    exit /b 1
)

rem Copy all non-Java files to bin directory
echo Copying resources...
if not exist bin\ROMS mkdir bin\ROMS
copy src\ROMS\*.fxml bin\ROMS\
copy src\ROMS\*.css bin\ROMS\
copy src\ROMS\*.sql bin\ROMS\

if not exist bin\images mkdir bin\images
xcopy /E images bin\images\

rem Run the application
echo Running the application...
java --module-path "%JAVAFX_PATH%\lib" --add-modules javafx.controls,javafx.fxml -cp bin;%MYSQL_CONNECTOR% ROMS.App 