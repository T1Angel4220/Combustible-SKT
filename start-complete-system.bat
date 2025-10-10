@echo off
echo ========================================
echo   🚛 SKT Combustible - Sistema Completo
echo ========================================
echo.
echo Iniciando el sistema completo con autenticación y gestión de vehículos...
echo.

REM Verificar si MongoDB está ejecutándose
echo 📊 Verificando MongoDB...
docker ps | findstr mongodb >nul
if %errorlevel% neq 0 (
    echo ❌ MongoDB no está ejecutándose
    echo.
    echo Iniciando MongoDB...
    docker-compose -f docker-compose-mongodb.yml up -d mongodb
    timeout /t 10 /nobreak
    echo ✅ MongoDB iniciado
) else (
    echo ✅ MongoDB ya está ejecutándose
)

echo.
echo 🔨 Compilando proyectos...
cd src\auth-service
call mvn clean package -DskipTests
cd ..\vehicles-service
call mvn clean package -DskipTests
cd ..\..

echo.
echo 🔐 Iniciando Auth Service (Puerto 8085)...
start "Auth Service" cmd /k "cd src\auth-service && mvn spring-boot:run"

echo.
echo 🚛 Iniciando Vehicles Service (Puerto 8082)...
start "Vehicles Service" cmd /k "cd src\vehicles-service && mvn spring-boot:run"

echo.
echo ⏳ Esperando a que los servicios inicien (20 segundos)...
timeout /t 20 /nobreak

echo.
echo 🌐 Abriendo sistema en el navegador...
start http://localhost:8085/

echo.
echo ========================================
echo   ✅ Sistema Completo Iniciado
echo ========================================
echo.
echo 📋 Información de Servicios:
echo    🔐 Auth Service:     http://localhost:8085/
echo    🚛 Vehicles Service: http://localhost:8082/vehicles.html
echo    📊 MongoDB:          localhost:27017
echo.
echo 📝 Flujo de Trabajo:
echo    1. Inicia sesión en http://localhost:8085/
echo    2. Haz clic en "Gestión de Vehículos"
echo    3. Serás redirigido automáticamente a la gestión de vehículos
echo    4. Usa el botón "Volver" para regresar al dashboard principal
echo.
echo 📝 Para detener los servicios:
echo    - Presiona Ctrl+C en cada ventana del servicio
echo    - O ejecuta: docker-compose -f docker-compose-mongodb.yml down
echo.
echo 🎯 Credenciales de Prueba:
echo    Usuario: admin
echo    Contraseña: admin123
echo.
pause
