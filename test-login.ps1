# Script de Prueba de Login - SKT Combustible
# PowerShell version

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Script de Prueba de Login - SKT Combustible" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuración
$AUTH_SERVICE_URL = "http://localhost:8085"
$LOGIN_ENDPOINT = "$AUTH_SERVICE_URL/api/auth/login"

Write-Host "Verificando que el auth-service esté ejecutándose..." -ForegroundColor Yellow
Write-Host "URL del servicio: $AUTH_SERVICE_URL" -ForegroundColor Gray
Write-Host ""

# Verificar si el servicio está ejecutándose
try {
    $healthResponse = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/actuator/health" -Method GET -TimeoutSec 5
    Write-Host "✅ Auth-service está ejecutándose correctamente" -ForegroundColor Green
} catch {
    Write-Host "❌ Auth-service no está ejecutándose o no responde" -ForegroundColor Red
    Write-Host "Por favor, ejecuta el auth-service primero:" -ForegroundColor Yellow
    Write-Host "  cd src\auth-service" -ForegroundColor Gray
    Write-Host "  mvn spring-boot:run" -ForegroundColor Gray
    Write-Host ""
    Read-Host "Presiona Enter para continuar"
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Probando credenciales de login" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Función para probar login
function Test-Login {
    param(
        [string]$Username,
        [string]$Password,
        [string]$UserDescription
    )
    
    Write-Host "Probando login con usuario: $UserDescription" -ForegroundColor Yellow
    Write-Host "Usuario: $Username" -ForegroundColor Gray
    Write-Host "Contraseña: $Password" -ForegroundColor Gray
    Write-Host ""
    
    # Crear objeto JSON para el login
    $loginData = @{
        usernameOrEmail = $Username
        password = $Password
    } | ConvertTo-Json
    
    try {
        # Realizar petición de login
        $response = Invoke-RestMethod -Uri $LOGIN_ENDPOINT -Method POST -Body $loginData -ContentType "application/json" -TimeoutSec 10
        
        Write-Host "Respuesta del servidor:" -ForegroundColor Gray
        $response | ConvertTo-Json -Depth 3 | Write-Host -ForegroundColor White
        
        # Verificar si el login fue exitoso
        if ($response.token) {
            Write-Host "✅ Login exitoso para $UserDescription" -ForegroundColor Green
            Write-Host "Token recibido: $($response.token.Substring(0, 20))..." -ForegroundColor Gray
        } else {
            Write-Host "❌ Login fallido para $UserDescription" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ Error en la petición para $UserDescription" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        
        # Intentar obtener más detalles del error
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            Write-Host "Código de estado HTTP: $statusCode" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
}

# Probar diferentes usuarios
Test-Login -Username "admin" -Password "password" -UserDescription "Administrador"
Test-Login -Username "supervisor" -Password "password" -UserDescription "Supervisor"
Test-Login -Username "operador" -Password "password" -UserDescription "Operador"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Pruebas completadas" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Si todas las pruebas fueron exitosas, el sistema de autenticación" -ForegroundColor Green
Write-Host "está funcionando correctamente." -ForegroundColor Green
Write-Host ""
Write-Host "Si alguna prueba falló, verifica:" -ForegroundColor Yellow
Write-Host "1. Que MongoDB esté ejecutándose" -ForegroundColor Gray
Write-Host "2. Que el auth-service esté ejecutándose" -ForegroundColor Gray
Write-Host "3. Que los usuarios existan en la base de datos" -ForegroundColor Gray
Write-Host ""
Read-Host "Presiona Enter para continuar"
