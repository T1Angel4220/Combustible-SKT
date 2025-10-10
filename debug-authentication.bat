@echo off
echo ========================================
echo   🔍 Debug: Sistema de Autenticación
echo ========================================
echo.
echo Verificando el estado de los servicios...
echo.

echo 📊 1. Verificando MongoDB...
docker ps | findstr mongodb >nul
if %errorlevel% equ 0 (
    echo ✅ MongoDB está ejecutándose
) else (
    echo ❌ MongoDB NO está ejecutándose
    echo Iniciando MongoDB...
    docker-compose -f docker-compose-mongodb.yml up -d mongodb
    timeout /t 5 /nobreak
)

echo.
echo 🔐 2. Verificando Auth Service...
curl -s http://localhost:8085/api/auth/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Auth Service está ejecutándose en puerto 8085
) else (
    echo ❌ Auth Service NO responde en puerto 8085
    echo Iniciando Auth Service...
    start "Auth Service" cmd /k "cd src\auth-service && mvn spring-boot:run"
    timeout /t 10 /nobreak
)

echo.
echo 🚛 3. Verificando Vehicles Service...
curl -s http://localhost:8082/api/v1/vehicles/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Vehicles Service está ejecutándose en puerto 8082
) else (
    echo ❌ Vehicles Service NO responde en puerto 8082
    echo Iniciando Vehicles Service...
    start "Vehicles Service" cmd /k "cd src\vehicles-service && mvn spring-boot:run"
    timeout /t 10 /nobreak
)

echo.
echo 🧪 4. Probando Login...
echo Realizando login de prueba...
curl -X POST http://localhost:8085/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"usernameOrEmail\":\"admin\",\"password\":\"admin123\"}" ^
  -s -o temp_login_debug.json

if exist temp_login_debug.json (
    echo ✅ Login realizado
    echo 📄 Respuesta del login:
    type temp_login_debug.json
    echo.
    
    echo 🔍 5. Probando Validación de Token...
    echo Extrayendo token para validación...
    
    REM Crear un archivo temporal con el token para validación
    echo {"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInBlcm1pc2lvbmVzIjpbXSwiYWN0aXZvIjp0cnVlLCJleHAiOjE3MzQ3MjI5ODB9.M88b3wimZ953AgKgY8VTnM_MG_iPN0h3yrcGjWpRnn8"} > temp_validate.json
    
    curl -X POST http://localhost:8085/api/auth/validate ^
      -H "Content-Type: application/json" ^
      -d @temp_validate.json ^
      -s -o temp_validate_response.json
    
    if exist temp_validate_response.json (
        echo 📄 Respuesta de validación:
        type temp_validate_response.json
        echo.
    )
    
    del temp_login_debug.json 2>nul
    del temp_validate.json 2>nul
    del temp_validate_response.json 2>nul
) else (
    echo ❌ Error en el login de prueba
)

echo.
echo 🌐 6. Abriendo navegador para prueba manual...
echo.
echo 📋 Instrucciones de Prueba:
echo    1. Abre la consola del navegador (F12)
echo    2. Ve a http://localhost:8085/
echo    3. Haz login con admin/admin123
echo    4. Verifica que aparezca el token en localStorage
echo    5. Click en "Gestión de Vehículos"
echo    6. Revisa los logs en la consola del navegador
echo.
echo 🔧 Si hay problemas:
echo    - Revisa los logs de la consola del navegador
echo    - Verifica que ambos servicios estén corriendo
echo    - Comprueba la red en las herramientas de desarrollador
echo.

start http://localhost:8085/

echo ========================================
echo   ✅ Debug Completado
echo ========================================
echo.
pause
