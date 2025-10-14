@echo off
title Sistema SKT - Inicio Rapido
echo ============================================
echo Sistema SKT - Inicio Rapido
echo ============================================
echo.

REM Detener MongoDB previo
docker stop mongodb-local 2>nul
docker rm mongodb-local 2>nul

REM Iniciar MongoDB
echo [MongoDB] Iniciando...
docker run -d --name mongodb-local -p 27017:27017 -v mongodb_data:/data/db mongo:7.0 >nul 2>&1

REM Esperar MongoDB
:wait_mongo
docker exec mongodb-local mongosh --quiet --eval "db.adminCommand('ping')" >nul 2>&1
if %errorlevel% neq 0 (
    ping 127.0.0.1 -n 2 >nul
    goto wait_mongo
)
echo [MongoDB] OK - Puerto 27017

REM Iniciar servicios en ventanas separadas
echo [Auth Service] Iniciando puerto 8085...
start "Auth Service" cmd /k "cd src\auth-service && mvn spring-boot:run"

ping 127.0.0.1 -n 3 >nul

echo [Drivers Service] Iniciando puerto 8081...
start "Drivers Service" cmd /k "cd src\drivers-service && mvn spring-boot:run"

ping 127.0.0.1 -n 3 >nul

echo [Vehicles Service] Iniciando puerto 8082...
start "Vehicles Service" cmd /k "cd src\vehicles-service && mvn spring-boot:run"

echo.
echo ============================================
echo Servicios Iniciando...
echo ============================================
echo.
echo Esperando 30 segundos para que Spring Boot inicie...
echo (Las ventanas mostraran el progreso)
echo.

REM Esperar 30 segundos
for /l %%i in (30,-1,1) do (
    <nul set /p "=%%i... "
    ping 127.0.0.1 -n 2 >nul
)
echo.
echo.

echo ============================================
echo Listo!
echo ============================================
echo.
echo URLs:
echo   Auth:     http://localhost:8085/
echo   Vehicles: http://localhost:8082/vehicles.html
echo   Drivers:  http://localhost:8081/drivers.html
echo   Gateway:  http://localhost:8090/api/v1/gateway/info
echo.
echo Credenciales: admin / admin123
echo.

REM Abrir navegador
start http://localhost:8085/

echo Navegador abierto. Cierra esta ventana cuando termines.
echo Para detener: Cierra las ventanas de servicios o usa stop-all-services.bat
echo.
pause >nul

