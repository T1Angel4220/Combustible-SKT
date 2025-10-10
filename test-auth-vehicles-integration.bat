@echo off
echo ========================================
echo   🔐🚛 Test: Integración Auth + Vehicles
echo ========================================
echo.
echo Probando la integración completa de autenticación...
echo.

REM Verificar que MongoDB esté corriendo
echo 📊 Verificando MongoDB...
docker ps | findstr mongodb >nul
if %errorlevel% neq 0 (
    echo ❌ MongoDB no está ejecutándose
    echo Iniciando MongoDB...
    docker-compose -f docker-compose-mongodb.yml up -d mongodb
    timeout /t 10 /nobreak
    echo ✅ MongoDB iniciado
) else (
    echo ✅ MongoDB ejecutándose
)

echo.
echo 🔐 Probando Auth Service...
timeout /t 3 /nobreak

REM Probar login
echo 📝 Probando login con credenciales admin...
curl -X POST http://localhost:8085/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"usernameOrEmail\":\"admin\",\"password\":\"admin123\"}" ^
  -s -o temp_login_response.json

echo.
echo 📄 Respuesta del login:
type temp_login_response.json
echo.

REM Extraer token (esto es básico, en producción usarías jq)
echo.
echo 🔍 Verificando si el token se generó correctamente...
findstr "token" temp_login_response.json >nul
if %errorlevel% equ 0 (
    echo ✅ Token generado correctamente
) else (
    echo ❌ Error: No se pudo generar el token
    echo Verifica que el Auth Service esté corriendo en puerto 8085
    goto :end
)

echo.
echo 🚛 Probando Vehicles Service...
timeout /t 3 /nobreak

REM Probar health check del vehicles service
echo 📝 Probando health check del Vehicles Service...
curl -X GET http://localhost:8082/api/v1/vehicles/health -s
if %errorlevel% equ 0 (
    echo ✅ Vehicles Service respondiendo
) else (
    echo ❌ Error: Vehicles Service no responde en puerto 8082
)

echo.
echo 🌐 Abriendo navegador para prueba manual...
start http://localhost:8085/

echo.
echo ========================================
echo   ✅ Test de Integración Completado
echo ========================================
echo.
echo 📋 Pasos para probar manualmente:
echo    1. Ve a http://localhost:8085/
echo    2. Login con: admin / admin123
echo    3. Click en "Gestión de Vehículos"
echo    4. Verifica que te redirija a la página de vehículos
echo    5. Verifica que muestre tu nombre de usuario
echo    6. Prueba crear/editar/eliminar vehículos
echo    7. Click en "Volver" para regresar al dashboard
echo.
echo 📝 Credenciales de Prueba:
echo    Usuario: admin
echo    Contraseña: admin123
echo.
echo 🔧 Si hay errores:
echo    - Verifica que ambos servicios estén corriendo
echo    - Revisa la consola del navegador (F12)
echo    - Verifica que MongoDB esté ejecutándose
echo.

:end
del temp_login_response.json 2>nul
pause
