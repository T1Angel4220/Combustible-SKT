@echo off
echo ========================================
echo   🔐🚛 SKT Combustible - Sistema Arreglado
echo ========================================
echo.

echo 📊 1. Verificando MongoDB...
docker ps | findstr mongodb >nul
if %errorlevel% neq 0 (
    echo ❌ MongoDB no está ejecutándose
    echo Iniciando MongoDB...
    docker-compose -f docker-compose-mongodb.yml up -d mongodb
    timeout /t 10 /nobreak
    echo ✅ MongoDB iniciado
) else (
    echo ✅ MongoDB ya está ejecutándose
)

echo.
echo 🔐 2. Iniciando Auth Service...
cd src\auth-service
start "Auth Service" cmd /k "mvn spring-boot:run"
cd ..\..

echo.
echo ⏳ Esperando Auth Service (10 segundos)...
timeout /t 10 /nobreak

echo.
echo 🚛 3. Iniciando Vehicles Service...
cd src\vehicles-service
start "Vehicles Service" cmd /k "mvn spring-boot:run"
cd ..\..

echo.
echo ⏳ Esperando Vehicles Service (15 segundos)...
timeout /t 15 /nobreak

echo.
echo 🧪 4. Verificando servicios...
echo.
echo 🔐 Probando Auth Service...
curl -s http://localhost:8085/ >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Auth Service: OK
) else (
    echo ❌ Auth Service: ERROR
)

echo.
echo 🚛 Probando Vehicles Service...
curl -s http://localhost:8082/api/v1/vehicles/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Vehicles Service: OK
) else (
    echo ❌ Vehicles Service: ERROR
)

echo.
echo 🌐 5. Abriendo sistema...
start http://localhost:8085/

echo.
echo ========================================
echo   ✅ Sistema Arreglado - Listo para Usar
echo ========================================
echo.
echo 📋 Flujo de Prueba:
echo    1. Ve a http://localhost:8085/
echo    2. Login con: admin / admin123
echo    3. Click en "Gestión de Vehículos"
echo    4. ¡Ahora debería funcionar correctamente!
echo.
echo 🔧 URLs del Sistema:
echo    🔐 Auth Service:     http://localhost:8085/
echo    🚛 Vehicles Service: http://localhost:8082/vehicles-simple.html
echo.
echo 📝 Si hay problemas:
echo    - Revisa las ventanas de los servicios
echo    - Verifica la consola del navegador (F12)
echo    - Comprueba que MongoDB esté corriendo
echo.
pause
