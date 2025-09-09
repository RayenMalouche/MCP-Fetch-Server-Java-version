@echo off
echo Installing Playwright browsers...
echo This may take a few minutes...

REM Install Playwright browsers
mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Playwright browsers installed successfully!
    echo You can now run: mvn clean package
) else (
    echo.
    echo ❌ Failed to install Playwright browsers
    echo Please check the error messages above
)

pause