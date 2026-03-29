# PowerShell Backend Testing Guide

This guide provides a set of **PowerShell** commands to test all the API and Actuator endpoints of the backend application.

## 1. Environment Variables

First, define the base URL of your application in your PowerShell terminal:

```powershell
$baseUrl = "http://localhost:8080"
```

## 2. Public Endpoints (No Token Required)

### Health & Discovery

These endpoints work without any authentication.

```powershell
# Custom Health Check
Invoke-RestMethod -Uri "$baseUrl/api/health" -Method Get

# Actuator Discovery Page (List of all links)
Invoke-RestMethod -Uri "$baseUrl/actuator" -Method Get

# Actuator Health
Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get

# Actuator Info
Invoke-RestMethod -Uri "$baseUrl/actuator/info" -Method Get
```

### Authentication Endpoints

```powershell
# Test Registration (Sign Up)
# Note: Use a unique email each time for new users
$signupBody = @{
    nombre = "New Tester"
    email = "tester_$(Get-Random)@example.com"
    password = "password123"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/api/signup" -Method Post -Body $signupBody -ContentType "application/json"
```

## 3. Obtaining a JWT Token

To test protected endpoints, you must first log in to receive a token.

```powershell
# Sign In (Admin example)
$loginBody = @{
    email = "admin@example.com"
    password = "alf"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "$baseUrl/api/signin" -Method Post -Body $loginBody -ContentType "application/json"
$token = $response.accessToken
Write-Host "Token obtained: $token"
```

## 4. Protected Endpoints (Token Required)

Now use the `$token` variable to authenticate your requests.

### API User Management (Admin Only)

```powershell
# Get All Users
Invoke-RestMethod -Uri "$baseUrl/api/users" -Method Get -Headers @{Authorization = "Bearer $token"}

# Update a User (Replace {id} with an actual ID from the list above)
$updateBody = @{
    nombre = "Updated Name"
    email = "updated@example.com"
    password = "newpassword123"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/api/users/1" -Method Put -Body $updateBody -ContentType "application/json" -Headers @{Authorization = "Bearer $token"}
```

### Protected Actuator Endpoints (Admin Only)

```powershell
# Get Detailed Metrics
Invoke-RestMethod -Uri "$baseUrl/actuator/metrics" -Method Get -Headers @{Authorization = "Bearer $token"}

# Get Specific Metric (e.g., JVM Memory)
Invoke-RestMethod -Uri "$baseUrl/actuator/metrics/jvm.memory.used" -Method Get -Headers @{Authorization = "Bearer $token"}
```

---

> [!IMPORTANT]
> **Base Discovery**: The `/actuator` page is now public. If you are using a browser, you should be able to see the JSON results directly at `http://localhost:8080/actuator`.
