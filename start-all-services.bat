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
echo 5. Routes Service (puerto 8083)
echo 6. Fuel Service (puerto 8084)
echo 7. Gateway Service (puerto 8090)
echo.
echo Cada servicio se abrira en una nueva ventana.
echo.
pause

REM ==========================================
REM PASO 1: Levantar MongoDB
REM ==========================================
echo.
echo [1/9] Iniciando MongoDB...
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
REM PASO 2: Verificar y cargar datos de prueba
REM ==========================================
echo [2/9] Verificando datos de prueba...
echo.

REM Verificar si ya existen datos
echo Verificando datos existentes...
docker exec mongodb-local mongosh auth_db --quiet --eval "print('Usuarios:', db.users.countDocuments())" > temp_auth.txt 2>&1
docker exec mongodb-local mongosh drivers_db --quiet --eval "print('Choferes:', db.drivers.countDocuments())" > temp_drivers.txt 2>&1
docker exec mongodb-local mongosh vehicles_db --quiet --eval "print('Vehiculos:', db.vehicles.countDocuments())" > temp_vehicles.txt 2>&1
docker exec mongodb-local mongosh routes_db --quiet --eval "print('Rutas:', db.routes.countDocuments())" > temp_routes.txt 2>&1
docker exec mongodb-local mongosh fuel_db --quiet --eval "print('Registros:', db.fuel_consumptions.countDocuments())" > temp_fuel.txt 2>&1

REM Leer los resultados
set /a auth_count=0
set /a drivers_count=0
set /a vehicles_count=0
set /a routes_count=0
set /a fuel_count=0

for /f %%i in (temp_auth.txt) do set auth_count=%%i
for /f %%i in (temp_drivers.txt) do set drivers_count=%%i
for /f %%i in (temp_vehicles.txt) do set vehicles_count=%%i
for /f %%i in (temp_routes.txt) do set routes_count=%%i
for /f %%i in (temp_fuel.txt) do set fuel_count=%%i

REM Limpiar archivos temporales
del temp_auth.txt 2>nul
del temp_drivers.txt 2>nul
del temp_vehicles.txt 2>nul
del temp_routes.txt 2>nul
del temp_fuel.txt 2>nul

echo Estado actual de la base de datos:
echo   - Usuarios (auth_db): %auth_count%
echo   - Choferes (drivers_db): %drivers_count%
echo   - Vehiculos (vehicles_db): %vehicles_count%
echo   - Rutas (routes_db): %routes_count%
echo   - Registros Combustible (fuel_db): %fuel_count%
echo.

REM Verificar si hay datos existentes
set /a total_data=%auth_count%+%drivers_count%+%vehicles_count%+%routes_count%+%fuel_count%

if %total_data% gtr 0 (
    echo ============================================
    echo   DATOS EXISTENTES DETECTADOS
    echo ============================================
    echo.
    echo Se encontraron datos en la base de datos.
    echo.
    echo Opciones disponibles:
    echo [1] Mantener datos existentes (recomendado)
    echo [2] Recargar todos los datos (eliminar y recrear)
    echo [3] Agregar datos faltantes (solo si faltan)
    echo.
    choice /c 123 /n /m "Selecciona una opcion [1-3]: "
    
    if errorlevel 3 goto add_missing_data
    if errorlevel 2 goto reload_all_data
    if errorlevel 1 goto keep_existing_data
) else (
    echo ============================================
    echo   BASE DE DATOS VACIA
    echo ============================================
    echo.
    echo No se encontraron datos. Se cargaran datos de prueba.
    goto load_initial_data
)

:keep_existing_data
echo Manteniendo datos existentes...
goto after_data_decision

:reload_all_data
echo ============================================
echo   RECARGANDO TODOS LOS DATOS
echo ============================================
echo.
echo ADVERTENCIA: Esto eliminara todos los datos existentes!
choice /c SN /n /m "¿Estas seguro? [S/N]: "
if errorlevel 2 goto keep_existing_data

echo Eliminando datos existentes...
docker exec mongodb-local mongosh auth_db --eval "db.users.deleteMany({})"
docker exec mongodb-local mongosh drivers_db --eval "db.drivers.deleteMany({})"
docker exec mongodb-local mongosh vehicles_db --eval "db.vehicles.deleteMany({})"
docker exec mongodb-local mongosh vehicles_db --eval "db.asignaciones_vehiculos.deleteMany({})"
docker exec mongodb-local mongosh routes_db --eval "db.routes.deleteMany({})"
docker exec mongodb-local mongosh fuel_db --eval "db.fuel_consumptions.deleteMany({})"
echo Datos existentes eliminados.
goto load_initial_data

:add_missing_data
echo ============================================
echo   AGREGANDO DATOS FALTANTES
echo ============================================
echo.
if %auth_count% equ 0 (
    echo Cargando datos de autenticacion...
    call add-auth-data.bat >nul 2>&1
)
if %drivers_count% equ 0 (
    echo Cargando datos de choferes...
    call add-drivers-data.bat >nul 2>&1
)
if %vehicles_count% equ 0 (
    echo Cargando datos de vehiculos...
    call add-simple-data.bat >nul 2>&1
)
if %routes_count% equ 0 (
    echo Cargando asignaciones de vehiculos a choferes...
    call add-assignments-data.bat >nul 2>&1
    echo Cargando datos de rutas...
    call add-routes-data.bat >nul 2>&1
)
if %fuel_count% equ 0 (
    echo Cargando datos de combustible...
    call add-fuel-data.bat >nul 2>&1
)
echo Datos faltantes agregados.
goto after_data_decision

:load_initial_data
echo ============================================
echo   CARGANDO DATOS INICIALES
echo ============================================
echo.
echo [1/6] Cargando datos de autenticacion...
call add-auth-data.bat >nul 2>&1
echo OK - Usuarios de prueba creados

echo [2/6] Cargando datos de choferes...
call add-drivers-data.bat >nul 2>&1
echo OK - Choferes de prueba creados

echo [3/6] Cargando datos de vehiculos...
call add-simple-data.bat >nul 2>&1
echo OK - Vehiculos de prueba creados

echo [4/6] Cargando asignaciones de vehiculos a choferes...
call add-assignments-data.bat >nul 2>&1
echo OK - Asignaciones de prueba creadas

echo [5/6] Cargando datos de rutas...
call add-routes-data.bat >nul 2>&1
echo OK - Rutas de prueba creadas

echo [6/6] Cargando datos de combustible...
call add-fuel-data.bat >nul 2>&1
echo OK - Registros de combustible de prueba creados

echo.
echo ============================================
echo   DATOS DE PRUEBA CARGADOS EXITOSAMENTE
echo ============================================
echo.
echo Credenciales de acceso:
echo   Usuario: admin
echo   Password: admin123
echo.

:after_data_decision
echo.

REM ==========================================
REM PASO 3: Compilar el proyecto (opcional)
REM ==========================================
echo [3/9] Compilando proyecto...
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
REM PASO 4: Iniciar Auth Service
REM ==========================================
echo [4/9] Iniciando Auth Service (puerto 8085)...
start "Auth Service - SKT" cmd /k "cd src\auth-service && echo Iniciando Auth Service... && mvn spring-boot:run"
echo OK - Auth Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 5: Iniciar Drivers Service
REM ==========================================
echo [5/9] Iniciando Drivers Service (puerto 8081)...
start "Drivers Service - SKT" cmd /k "cd src\drivers-service && echo Iniciando Drivers Service... && mvn spring-boot:run"
echo OK - Drivers Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 6: Iniciar Vehicles Service
REM ==========================================
echo [6/9] Iniciando Vehicles Service (puerto 8082)...
start "Vehicles Service - SKT" cmd /k "cd src\vehicles-service && echo Iniciando Vehicles Service... && mvn spring-boot:run"
echo OK - Vehicles Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 7: Iniciar Routes Service
REM ==========================================
echo [7/9] Iniciando Routes Service (puerto 8083)...
start "Routes Service - SKT" cmd /k "cd src\routes-service && echo Iniciando Routes Service... && mvn spring-boot:run"
echo OK - Routes Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 8: Iniciar Fuel Service
REM ==========================================
echo [8/9] Iniciando Fuel Service (puerto 8084)...
start "Fuel Service - SKT" cmd /k "cd src\fuel-service && echo Iniciando Fuel Service... && mvn spring-boot:run"
echo OK - Fuel Service iniciado en nueva ventana
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM PASO 9: Iniciar Gateway Service
REM ==========================================
echo [9/9] Iniciando Gateway Service (puerto 8090)...
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
echo  MongoDB:
echo    - Puerto: 27017
echo    - Conexion: mongodb://localhost:27017
echo    - Bases de datos: auth_db, drivers_db, vehicles_db, routes_db, fuel_db
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
echo Credenciales de prueba:
echo   Usuario: admin
echo   Password: admin123
echo.
echo Datos de prueba incluidos:
echo   - 2 usuarios (admin, user)
echo   - 6 choferes con diferentes tipos de maquinaria
echo   - 6 vehiculos (CAMION, EXCAVADORA, VOLQUETE, CARGADOR, GRUA)
echo   - 5 asignaciones de vehiculos a choferes
echo   - 5 rutas de prueba (PENDIENTE, EN_CURSO, COMPLETADA)
echo   - 7 registros de consumo de combustible
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

