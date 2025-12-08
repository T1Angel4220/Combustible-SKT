@echo off
setlocal enabledelayedexpansion

title Sistema Combustible SKT - Inicio con MongoDB Atlas

REM Cambiar al directorio del script
cd /d "%~dp0"

REM Verificar que estamos en el directorio correcto
if not exist "src\auth-service\pom.xml" (
    echo ERROR: No se encontro el proyecto en el directorio actual.
    echo.
    echo Asegurate de ejecutar este script desde el directorio raiz del proyecto.
    echo Directorio actual: %CD%
    echo.
    pause
    exit /b 1
)

echo ============================================
echo Sistema Combustible SKT - Inicio Completo
echo Con MongoDB Atlas
echo ============================================
echo.
echo Directorio del proyecto: %CD%
echo.
echo Este script iniciara:
echo 1. Verificacion de conexion a MongoDB Atlas
echo 2. Auth Service (puerto 8085)
echo 3. Drivers Service (puerto 8081)
echo 4. Vehicles Service (puerto 8082)
echo 5. Routes Service (puerto 8083)
echo 6. Fuel Service (puerto 8084)
echo 7. Gateway Service (puerto 8090)
echo.
echo Cada servicio se abrira en una nueva ventana.
echo.
pause

REM ==========================================
REM PASO 1: Verificar conexion a MongoDB Atlas
REM ==========================================
echo.
echo [1/8] Verificando conexion a MongoDB Atlas...
echo.

REM Configurar URI de MongoDB Atlas
set "ATLAS_URI=mongodb+srv://907johan_db_user:piIe4vWfuADsnRM6@combustibleskt.4n4nf9z.mongodb.net/?retryWrites=true&w=majority&readPreference=secondaryPreferred&maxPoolSize=50&minPoolSize=10&appName=combustibleskt"

REM Verificar que mongosh este disponible
where mongosh >nul 2>&1
if errorlevel 1 (
    echo ADVERTENCIA: mongosh no esta instalado
    echo.
    echo Los servicios se iniciaran de todas formas, pero no se podra verificar la conexion.
    echo.
    echo Para instalar MongoDB Shell:
    echo https://www.mongodb.com/try/download/shell
    echo.
    choice /c SN /n /m "¿Deseas continuar sin verificar? [S/N]: "
    if errorlevel 2 exit /b 1
    goto skip_atlas_check
)

REM Probar conexion a MongoDB Atlas
echo Probando conexion a MongoDB Atlas...
echo Cluster: combustibleskt.4n4nf9z.mongodb.net
echo Usuario: 907johan_db_user
echo.

mongosh "!ATLAS_URI!" --quiet --eval "db.adminCommand('ping')" >nul 2>&1
if errorlevel 1 (
    echo ERROR: No se pudo conectar a MongoDB Atlas
    echo.
    echo Verifica:
    echo   1. Que tu IP este permitida en Network Access de MongoDB Atlas
    echo   2. Que el cluster este activo
    echo   3. Que las credenciales sean correctas
    echo   4. Que tengas conexion a Internet
    echo.
    choice /c SN /n /m "¿Deseas continuar de todas formas? [S/N]: "
    if errorlevel 2 exit /b 1
) else (
    echo OK - Conexion a MongoDB Atlas exitosa
)
echo.

:skip_atlas_check

REM ==========================================
REM PASO 2: Verificar datos existentes (opcional)
REM ==========================================
echo [2/8] Verificando datos existentes...
echo.

REM Verificar si mongosh esta disponible para contar documentos
where mongosh >nul 2>&1
if errorlevel 1 (
    echo Saltando verificacion de datos (mongosh no disponible)
    goto after_data_check
)

REM Verificar datos existentes en Atlas
echo Verificando datos existentes en MongoDB Atlas...
mongosh "!ATLAS_URI!" --quiet --eval "db = db.getSiblingDB('auth_db'); print('Usuarios:', db.users.countDocuments())" > temp_auth.txt 2>&1
mongosh "!ATLAS_URI!" --quiet --eval "db = db.getSiblingDB('drivers_db'); print('Choferes:', db.drivers.countDocuments())" > temp_drivers.txt 2>&1
mongosh "!ATLAS_URI!" --quiet --eval "db = db.getSiblingDB('vehicles_db'); print('Vehiculos:', db.vehicles.countDocuments())" > temp_vehicles.txt 2>&1
mongosh "!ATLAS_URI!" --quiet --eval "db = db.getSiblingDB('routes_db'); print('Rutas:', db.routes.countDocuments())" > temp_routes.txt 2>&1
mongosh "!ATLAS_URI!" --quiet --eval "db = db.getSiblingDB('fuel_db'); print('Registros:', db.fuel_consumptions.countDocuments())" > temp_fuel.txt 2>&1

REM Leer los resultados
set /a auth_count=0
set /a drivers_count=0
set /a vehicles_count=0
set /a routes_count=0
set /a fuel_count=0

for /f "tokens=2" %%i in ('findstr /c:"Usuarios:" temp_auth.txt 2^>nul') do set auth_count=%%i
for /f "tokens=2" %%i in ('findstr /c:"Choferes:" temp_drivers.txt 2^>nul') do set drivers_count=%%i
for /f "tokens=2" %%i in ('findstr /c:"Vehiculos:" temp_vehicles.txt 2^>nul') do set vehicles_count=%%i
for /f "tokens=2" %%i in ('findstr /c:"Rutas:" temp_routes.txt 2^>nul') do set routes_count=%%i
for /f "tokens=2" %%i in ('findstr /c:"Registros:" temp_fuel.txt 2^>nul') do set fuel_count=%%i

REM Limpiar archivos temporales
del temp_auth.txt 2>nul
del temp_drivers.txt 2>nul
del temp_vehicles.txt 2>nul
del temp_routes.txt 2>nul
del temp_fuel.txt 2>nul

echo Estado actual de la base de datos en MongoDB Atlas:
echo   - Usuarios (auth_db): !auth_count!
echo   - Choferes (drivers_db): !drivers_count!
echo   - Vehiculos (vehicles_db): !vehicles_count!
echo   - Rutas (routes_db): !routes_count!
echo   - Registros Combustible (fuel_db): !fuel_count!
echo.

:after_data_check

REM ==========================================
REM PASO 3: Compilar el proyecto (opcional)
REM ==========================================
echo [3/8] Compilando proyecto...
echo.
echo Deseas compilar el proyecto? (Recomendado si hay cambios)
echo [S] Si   [N] No (usar compilacion anterior)
choice /c SN /n /m "Selecciona una opcion: "

if errorlevel 2 goto skip_compile
if errorlevel 1 goto do_compile

:do_compile
echo.
echo Compilando con Maven (esto puede tomar 1-2 minutos)...
echo Directorio: %CD%
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Fallo la compilacion
    echo Revisa los mensajes de error arriba.
    echo.
    pause
    exit /b 1
)
echo.
echo OK - Compilacion exitosa
goto after_compile

:skip_compile
echo Saltando compilacion...

:after_compile
echo.

REM ==========================================
REM PASO 4: Iniciar Auth Service
REM ==========================================
echo [4/8] Iniciando Auth Service (puerto 8085)...
start "Auth Service - SKT (Atlas)" cmd /k "cd /d %~dp0src\auth-service && echo ============================================ && echo Auth Service - MongoDB Atlas && echo ============================================ && echo. && echo Iniciando Auth Service con perfil 'atlas'... && echo. && mvn spring-boot:run -Dspring-boot.run.profiles=atlas || (echo. && echo ERROR: Fallo al iniciar Auth Service && echo Revisa los logs arriba para mas detalles && pause)"
echo OK - Auth Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 5: Iniciar Drivers Service
REM ==========================================
echo [5/8] Iniciando Drivers Service (puerto 8081)...
start "Drivers Service - SKT (Atlas)" cmd /k "cd /d %~dp0src\drivers-service && echo ============================================ && echo Drivers Service - MongoDB Atlas && echo ============================================ && echo. && echo Iniciando Drivers Service con perfil 'atlas'... && echo. && mvn spring-boot:run -Dspring-boot.run.profiles=atlas || (echo. && echo ERROR: Fallo al iniciar Drivers Service && echo Revisa los logs arriba para mas detalles && pause)"
echo OK - Drivers Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 6: Iniciar Vehicles Service
REM ==========================================
echo [6/8] Iniciando Vehicles Service (puerto 8082)...
start "Vehicles Service - SKT (Atlas)" cmd /k "cd /d %~dp0src\vehicles-service && echo ============================================ && echo Vehicles Service - MongoDB Atlas && echo ============================================ && echo. && echo Iniciando Vehicles Service con perfil 'atlas'... && echo. && mvn spring-boot:run -Dspring-boot.run.profiles=atlas || (echo. && echo ERROR: Fallo al iniciar Vehicles Service && echo Revisa los logs arriba para mas detalles && pause)"
echo OK - Vehicles Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 7: Iniciar Routes Service
REM ==========================================
echo [7/8] Iniciando Routes Service (puerto 8083)...
start "Routes Service - SKT (Atlas)" cmd /k "cd /d %~dp0src\routes-service && echo ============================================ && echo Routes Service - MongoDB Atlas && echo ============================================ && echo. && echo Iniciando Routes Service con perfil 'atlas'... && echo. && mvn spring-boot:run -Dspring-boot.run.profiles=atlas || (echo. && echo ERROR: Fallo al iniciar Routes Service && echo Revisa los logs arriba para mas detalles && pause)"
echo OK - Routes Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 8: Iniciar Fuel Service
REM ==========================================
echo [8/8] Iniciando Fuel Service (puerto 8084)...
start "Fuel Service - SKT (Atlas)" cmd /k "cd /d %~dp0src\fuel-service && echo ============================================ && echo Fuel Service - MongoDB Atlas && echo ============================================ && echo. && echo Iniciando Fuel Service con perfil 'atlas'... && echo. && mvn spring-boot:run -Dspring-boot.run.profiles=atlas || (echo. && echo ERROR: Fallo al iniciar Fuel Service && echo Revisa los logs arriba para mas detalles && pause)"
echo OK - Fuel Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 9: Iniciar Gateway Service
REM ==========================================
echo [9/8] Iniciando Gateway Service (puerto 8090)...
start "Gateway Service - SKT" cmd /k "cd /d %~dp0src\gateway-service && echo ============================================ && echo Gateway Service && echo ============================================ && echo. && echo Iniciando Gateway Service... && echo. && mvn spring-boot:run || (echo. && echo ERROR: Fallo al iniciar Gateway Service && echo Revisa los logs arriba para mas detalles && pause)"
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
echo [Routes Service - Puerto 8083]
curl -s http://localhost:8083/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   Estado: OK
) else (
    echo   Estado: Iniciando... (puede tardar mas)
)

echo.
echo [Fuel Service - Puerto 8084]
curl -s http://localhost:8084/actuator/health >nul 2>&1
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
echo Sistema Iniciado Exitosamente!
echo ============================================
echo.
echo Servicios disponibles:
echo.
echo  MongoDB Atlas:
echo    - Cluster: combustibleskt.4n4nf9z.mongodb.net
echo    - Usuario: 907johan_db_user
echo    - Bases de datos: auth_db, drivers_db, vehicles_db, routes_db, fuel_db
echo    - Replicacion: Habilitada (readPreference=secondaryPreferred)
echo.
echo  Auth Service:
echo    - Frontend: http://localhost:8085/
echo    - API: http://localhost:8085/api/auth
echo    - Health: http://localhost:8085/actuator/health
echo.
echo  Drivers Service:
echo    - Frontend: http://localhost:8081/drivers.html
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
echo  Routes Service:
echo    - Frontend: http://localhost:8083/routes.html
echo    - API: http://localhost:8083/api/v1/routes
echo    - gRPC: localhost:9093
echo    - Health: http://localhost:8083/actuator/health
echo.
echo  Fuel Service:
echo    - Frontend: http://localhost:8084/fuel.html
echo    - API: http://localhost:8084/api/v1/fuel
echo    - gRPC: localhost:9094
echo    - Health: http://localhost:8084/actuator/health
echo.
echo  Gateway Service:
echo    - Drivers via Gateway: http://localhost:8090/api/v1/drivers
echo    - Auth via Gateway: http://localhost:8090/api/v1/auth
echo    - Health: http://localhost:8090/actuator/health
echo.
echo ============================================
echo.
echo IMPORTANTE:
echo   - Todos los servicios estan conectados a MongoDB Atlas
echo   - Asegurate de que tu IP este permitida en Network Access
echo   - Las bases de datos se crean automaticamente al usar los servicios
echo   - Para crear la estructura manualmente, ejecuta: crear-estructura-atlas.bat
echo.
echo ============================================
echo.
echo Deseas abrir el navegador en el Auth Service?
choice /c SN /n /m "[S] Si   [N] No: "
if errorlevel 2 goto skip_browser
if errorlevel 1 start http://localhost:8085/

:skip_browser
echo.
echo ============================================
echo   SERVICIOS INICIADOS
echo ============================================
echo.
echo Los servicios estan ejecutandose en ventanas separadas.
echo Esta ventana permanecera abierta para monitoreo.
echo.
echo Para detener todos los servicios:
echo 1. Cierra las ventanas de cada servicio (Ctrl+C en cada una)
echo 2. O cierra esta ventana (los servicios seguiran corriendo)
echo.
echo O usa el script: stop-all-services.bat
echo.
echo Presiona cualquier tecla para cerrar esta ventana...
echo (Los servicios seguiran ejecutandose en sus propias ventanas)
pause >nul

