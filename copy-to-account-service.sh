#!/bin/bash
# Bash script to copy existing code to account-service
# Run this script from the project root directory

echo "Copying source files to account-service..."

# Copy main source files
if [ -d "src/main" ]; then
    cp -r src/main/* account-service/src/main/
    echo "✓ Main source files copied"
else
    echo "✗ src/main directory not found"
    exit 1
fi

# Copy test files
if [ -d "src/test" ]; then
    cp -r src/test/* account-service/src/test/
    echo "✓ Test files copied"
else
    echo "⚠ src/test directory not found (optional)"
fi

# Remove old Application class if exists
OLD_APP_PATH="account-service/src/main/java/com/example/OnlineBankacilik/OnlineBankacilikApplication.java"
if [ -f "$OLD_APP_PATH" ]; then
    rm "$OLD_APP_PATH"
    echo "✓ Old Application class removed"
fi

echo ""
echo "Copy operation completed!"
echo "Next steps:"
echo "1. Update AccountServiceApplication.java if needed"
echo "2. Configure Git repository for Config Server"
echo "3. Start services in order: Config Server -> Account Service -> API Gateway"

