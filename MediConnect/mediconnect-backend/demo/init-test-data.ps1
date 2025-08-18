# Set environment variables
$env:JWT_SECRET="your-secret-key-here"
$env:DB_URL="jdbc:postgresql://localhost:5432/demo"
$env:DB_USER="postgres"
$env:DB_PASSWORD="password"
$env:GEOCODING_API_KEY="your-geocoding-key"
$env:GEMINI_API_KEY="AIzaSyCy-W6FLBlVpbjy2Jr6ooFbI81XRopUlP0"

# Start the server in background
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run", "-DskipTests" -WorkingDirectory (Get-Location) -WindowStyle Hidden

# Wait for server to start
Write-Host "Waiting for server to start..."
Start-Sleep -Seconds 25

# Initialize test data
Write-Host "Initializing test data..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/test/init-data" -Method POST
    Write-Host "Test data initialized successfully: $($response.Content)"
} catch {
    Write-Host "Error initializing test data: $($_.Exception.Message)"
}

Write-Host "Server is running on http://localhost:8080"
Write-Host "Test data has been initialized. You can now test the slot booking feature!"
