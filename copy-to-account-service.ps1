# PowerShell script to copy existing code to account-service
# Run this script from the project root directory

Write-Host "Copying source files to account-service..." -ForegroundColor Green

# Copy main source files
if (Test-Path "src\main") {
    Copy-Item -Path "src\main\*" -Destination "account-service\src\main\" -Recurse -Force
    Write-Host "✓ Main source files copied" -ForegroundColor Green
} else {
    Write-Host "✗ src\main directory not found" -ForegroundColor Red
}

# Copy test files
if (Test-Path "src\test") {
    Copy-Item -Path "src\test\*" -Destination "account-service\src\test\" -Recurse -Force
    Write-Host "✓ Test files copied" -ForegroundColor Green
} else {
    Write-Host "⚠ src\test directory not found (optional)" -ForegroundColor Yellow
}

# Remove old Application class if exists
$oldAppPath = "account-service\src\main\java\com\example\OnlineBankacilik\OnlineBankacilikApplication.java"
if (Test-Path $oldAppPath) {
    Remove-Item $oldAppPath -Force
    Write-Host "✓ Old Application class removed" -ForegroundColor Green
}

Write-Host "`nCopy operation completed!" -ForegroundColor Green
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Update AccountServiceApplication.java if needed" -ForegroundColor White
Write-Host "2. Configure Git repository for Config Server" -ForegroundColor White
Write-Host "3. Start services in order: Config Server -> Account Service -> API Gateway" -ForegroundColor White

