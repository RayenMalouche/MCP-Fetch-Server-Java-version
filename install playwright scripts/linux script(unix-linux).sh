#!/bin/bash

echo "Installing Playwright browsers..."
echo "This may take a few minutes..."

# Install Playwright browsers
mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Playwright browsers installed successfully!"
    echo "You can now run: mvn clean package"
else
    echo ""
    echo "❌ Failed to install Playwright browsers"
    echo "Please check the error messages above"
fi

read -p "Press Enter to continue..."