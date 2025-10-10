@echo off
echo ========================================
echo   🚛 SKT Combustible - Vehicles Frontend
echo ========================================
echo.
echo Iniciando el servicio de vehículos con frontend...
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
echo 🔨 Compilando el proyecto...
cd src\vehicles-service
call mvn clean package -DskipTests

echo.
echo 🚀 Iniciando Vehicles Service...
start "Vehicles Service" mvn spring-boot:run

echo.
echo ⏳ Esperando a que el servicio inicie (15 segundos)...
timeout /t 15 /nobreak

echo.
echo 🌐 Abriendo frontend en el navegador...
start http://localhost:8082/vehicles.html

echo.
echo ========================================
echo   ✅ Frontend de Vehículos Iniciado
echo ========================================
echo.
echo 📋 Información:
echo    - Frontend: http://localhost:8082/vehicles.html
echo    - API REST: http://localhost:8082/api/v1/vehicles
echo    - Health Check: http://localhost:8082/api/v1/vehicles/health
echo    - MongoDB: localhost:27017
echo.
echo 📝 Para detener el servicio:
echo    - Presiona Ctrl+C en la ventana del servicio
echo    - O ejecuta: docker-compose -f docker-compose-mongodb.yml down
echo.
pause

