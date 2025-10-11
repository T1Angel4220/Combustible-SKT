@echo off
echo ============================================
echo Reiniciando MongoDB (Sin Autenticacion)
echo ============================================
echo.

echo [1/5] Deteniendo contenedores previos...
docker stop mongodb-local 2>nul
docker rm mongodb-local 2>nul
echo OK - Contenedores previos eliminados
echo.

echo [2/5] Iniciando MongoDB SIN autenticacion...
docker run -d --name mongodb-local -p 27017:27017 -v mongodb_data:/data/db mongo:7.0
if %errorlevel% neq 0 (
    echo ERROR: No se pudo iniciar MongoDB
    pause
    exit /b 1
)
echo OK - Contenedor iniciado
echo.

echo [3/5] Esperando que MongoDB este listo...
set /a counter=0
:wait_loop
set /a counter+=1
docker exec mongodb-local mongosh --quiet --eval "db.adminCommand('ping')" >nul 2>&1
if %errorlevel% equ 0 goto mongodb_ready
if %counter% geq 30 goto mongodb_timeout
ping 127.0.0.1 -n 2 >nul
goto wait_loop

:mongodb_timeout
echo ERROR: MongoDB no respondio despues de 30 segundos
docker logs mongodb-local
pause
exit /b 1

:mongodb_ready
echo OK - MongoDB esta listo (tomo %counter% segundos)
echo.

echo [4/5] Verificando conexion...
docker exec mongodb-local mongosh --quiet --eval "db.runCommand('ping')"
echo.

echo [5/5] Informacion del contenedor:
docker ps --filter "name=mongodb-local" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.

echo ============================================
echo MongoDB iniciado exitosamente!
echo ============================================
echo.
echo Puerto: 27017
echo Sin autenticacion
echo Conexion: mongodb://localhost:27017
echo.
echo Puedes probarlo con:
echo - MongoDB Compass: mongodb://localhost:27017
echo - API Vehicles: http://localhost:8082/api/v1/vehicles
echo.
pause
