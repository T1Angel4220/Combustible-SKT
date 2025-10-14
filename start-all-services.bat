@echo off
echo ============================================
echo Sistema Combustible SKT - Inicio Completo
echo ============================================
echo.
echo Este script iniciara:
echo 1. MongoDB (sin autenticacion)
echo 2. Auth Service (puerto 8085)
echo 3. Drivers Service (puerto 8081)
echo 4. Vehicles Service (puerto 8082)
echo.
echo Cada servicio se abrira en una nueva ventana.
echo.
pause

REM ==========================================
REM PASO 1: Levantar MongoDB
REM ==========================================
echo.
echo [1/5] Iniciando MongoDB...
echo.

REM Detener MongoDB previo
docker stop mongodb-local 2>nul
docker rm mongodb-local 2>nul

REM Iniciar MongoDB
docker run -d --name mongodb-local -p 27017:27017 -v mongodb_data:/data/db mongo:7.0
if %errorlevel% neq 0 (
    echo ERROR: No se pudo iniciar MongoDB
    pause
    exit /b 1
)

REM Esperar a que MongoDB este listo
echo Esperando que MongoDB este listo...
set /a counter=0
:wait_mongo
set /a counter+=1
docker exec mongodb-local mongosh --quiet --eval "db.adminCommand('ping')" >nul 2>&1
if %errorlevel% equ 0 goto mongo_ready
if %counter% geq 30 goto mongo_timeout
ping 127.0.0.1 -n 2 >nul
goto wait_mongo

:mongo_timeout
echo ERROR: MongoDB no respondio
pause
exit /b 1

:mongo_ready
echo OK - MongoDB iniciado en puerto 27017
echo.

REM ==========================================
REM PASO 2: Compilar el proyecto (opcional)
REM ==========================================
echo [2/5] Compilando proyecto...
echo.
echo Deseas compilar el proyecto? (Recomendado si hay cambios)
echo [S] Si   [N] No (usar compilacion anterior)
choice /c SN /n /m "Selecciona una opcion: "

if errorlevel 2 goto skip_compile
if errorlevel 1 goto do_compile

:do_compile
echo.
echo Compilando con Maven (esto puede tomar 1-2 minutos)...
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Fallo la compilacion
    pause
    exit /b 1
)
echo OK - Compilacion exitosa
goto after_compile

:skip_compile
echo Saltando compilacion...

:after_compile
echo.

REM ==========================================
REM PASO 3: Iniciar Auth Service
REM ==========================================
echo [3/5] Iniciando Auth Service (puerto 8085)...
start "Auth Service - SKT" cmd /k "cd src\auth-service && echo Iniciando Auth Service... && mvn spring-boot:run"
echo OK - Auth Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 4: Iniciar Drivers Service
REM ==========================================
echo [4/5] Iniciando Drivers Service (puerto 8081)...
start "Drivers Service - SKT" cmd /k "cd src\drivers-service && echo Iniciando Drivers Service... && mvn spring-boot:run"
echo OK - Drivers Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 5: Iniciar Vehicles Service
REM ==========================================
echo [5/6] Iniciando Vehicles Service (puerto 8082)...
start "Vehicles Service - SKT" cmd /k "cd src\vehicles-service && echo Iniciando Vehicles Service... && mvn spring-boot:run"
echo OK - Vehicles Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 6: Iniciar Gateway Service
REM ==========================================
echo [6/6] Iniciando Gateway Service (puerto 8090)...
start "Gateway Service - SKT" cmd /k "cd src\gateway-service && echo Iniciando Gateway Service... && mvn spring-boot:run"
echo OK - Gateway Service iniciado en nueva ventana
echo.

REM ==========================================
REM Esperar a que los servicios esten listos
REM ==========================================
echo ============================================
echo Esperando que los servicios esten listos...
echo ============================================
echo.
echo Esto puede tomar 30-60 segundos...
echo.

REM Esperar 30 segundos para que Spring Boot inicie
echo Esperando inicializacion de Spring Boot...
for /l %%i in (30,-1,1) do (
    echo Tiempo restante: %%i segundos...
    ping 127.0.0.1 -n 2 >nul
)
echo.

REM ==========================================
REM Verificar servicios
REM ==========================================
echo Verificando estado de servicios...
echo.

echo [Auth Service - Puerto 8085]
curl -s http://localhost:8085/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   Estado: OK
) else (
    echo   Estado: Iniciando... (puede tardar mas)
)

echo.
echo [Drivers Service - Puerto 8081]
curl -s http://localhost:8081/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   Estado: OK
) else (
    echo   Estado: Iniciando... (puede tardar mas)
)

echo.
echo [Vehicles Service - Puerto 8082]
curl -s http://localhost:8082/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   Estado: OK
) else (
    echo   Estado: Iniciando... (puede tardar mas)
)

echo.
echo [Gateway Service - Puerto 8090]
curl -s http://localhost:8090/api/v1/gateway/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   Estado: OK
) else (
    echo   Estado: Iniciando... (puede tardar mas)
)

echo.
echo ============================================
echo Sistema Iniciado!
echo ============================================
echo.
echo Servicios disponibles:
echo.
echo  MongoDB:
echo    - Puerto: 27017
echo    - Conexion: mongodb://localhost:27017
echo.
echo  Auth Service:
echo    - Frontend: http://localhost:8085/
echo    - API: http://localhost:8085/api/auth
echo    - Health: http://localhost:8085/actuator/health
echo.
echo  Drivers Service:
echo    - API: http://localhost:8081/api/v1/drivers
echo    - gRPC: localhost:9091
echo    - Health: http://localhost:8081/actuator/health
echo.
echo  Vehicles Service:
echo    - Frontend: http://localhost:8082/vehicles.html
echo    - API: http://localhost:8082/api/v1/vehicles
echo    - gRPC: localhost:9092
echo    - Health: http://localhost:8082/actuator/health
echo.
echo  Gateway Service:
echo    - API: http://localhost:8090/api/v1/gateway
echo    - Drivers via Gateway: http://localhost:8090/api/v1/drivers
echo    - Health: http://localhost:8090/api/v1/gateway/health
echo    - Info: http://localhost:8090/api/v1/gateway/info
echo.
echo ============================================
echo.
echo Credenciales de prueba:
echo   Usuario: admin
echo   Password: admin123
echo.
echo ============================================
echo.
echo Deseas abrir el navegador en el Auth Service?
choice /c SN /n /m "[S] Si   [N] No: "
if errorlevel 2 goto skip_browser
if errorlevel 1 start http://localhost:8085/

:skip_browser
echo.
echo Para detener todos los servicios:
echo 1. Cierra las ventanas de cada servicio (Ctrl+C)
echo 2. Ejecuta: docker stop mongodb-local
echo.
echo O usa el script: stop-all-services.bat
echo.
pause

