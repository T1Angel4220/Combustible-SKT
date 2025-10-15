@echo off
echo ============================================
echo   GESTION DE DATOS DE PRUEBA - SKT
echo ============================================
echo.

REM Verificar si MongoDB está corriendo
echo Verificando conexion a MongoDB...
docker exec mongodb-local mongosh --quiet --eval "db.adminCommand('ping')" >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: MongoDB no esta corriendo
    echo Ejecuta primero: start-all-services.bat
    pause
    exit /b 1
)
echo OK - MongoDB conectado
echo.

REM Verificar datos existentes
echo Verificando datos existentes...
docker exec mongodb-local mongosh auth_db --quiet --eval "print('Usuarios:', db.users.countDocuments())" > temp_auth.txt 2>&1
docker exec mongodb-local mongosh drivers_db --quiet --eval "print('Choferes:', db.drivers.countDocuments())" > temp_drivers.txt 2>&1
docker exec mongodb-local mongosh vehicles_db --quiet --eval "print('Vehiculos:', db.vehicles.countDocuments())" > temp_vehicles.txt 2>&1

REM Leer los resultados
set /a auth_count=0
set /a drivers_count=0
set /a vehicles_count=0

for /f %%i in (temp_auth.txt) do set auth_count=%%i
for /f %%i in (temp_drivers.txt) do set drivers_count=%%i
for /f %%i in (temp_vehicles.txt) do set vehicles_count=%%i

REM Limpiar archivos temporales
del temp_auth.txt 2>nul
del temp_drivers.txt 2>nul
del temp_vehicles.txt 2>nul

echo Estado actual de la base de datos:
echo   - Usuarios (auth_db): %auth_count%
echo   - Choferes (drivers_db): %drivers_count%
echo   - Vehiculos (vehicles_db): %vehicles_count%
echo.

echo ============================================
echo   OPCIONES DE GESTION DE DATOS
echo ============================================
echo.
echo [1] Ver datos existentes
echo [2] Agregar datos de prueba (sin eliminar existentes)
echo [3] Recargar todos los datos (eliminar y recrear)
echo [4] Eliminar todos los datos
echo [5] Agregar datos faltantes (solo si faltan)
echo [6] Salir
echo.
choice /c 123456 /n /m "Selecciona una opcion [1-6]: "

if errorlevel 6 goto exit_script
if errorlevel 5 goto add_missing_data
if errorlevel 4 goto delete_all_data
if errorlevel 3 goto reload_all_data
if errorlevel 2 goto add_test_data
if errorlevel 1 goto view_existing_data

:view_existing_data
echo.
echo ============================================
echo   DATOS EXISTENTES
echo ============================================
echo.
echo Usuarios en auth_db:
docker exec mongodb-local mongosh auth_db --eval "db.users.find({}, {username:1, email:1, roles:1, enabled:1}).pretty()"
echo.
echo Choferes en drivers_db:
docker exec mongodb-local mongosh drivers_db --eval "db.drivers.find({}, {nombre:1, apellido:1, dni:1, estado:1, activo:1}).pretty()"
echo.
echo Vehiculos en vehicles_db:
docker exec mongodb-local mongosh vehicles_db --eval "db.vehicles.find({}, {placa:1, marca:1, modelo:1, estadoOperativo:1, activo:1}).pretty()"
echo.
pause
goto menu_loop

:add_test_data
echo.
echo ============================================
echo   AGREGANDO DATOS DE PRUEBA
echo ============================================
echo.
echo Cargando datos de autenticacion...
call add-auth-data.bat >nul 2>&1
echo OK - Usuarios de prueba creados

echo Cargando datos de choferes...
call add-drivers-data.bat >nul 2>&1
echo OK - Choferes de prueba creados

echo Cargando datos de vehiculos...
call add-simple-data.bat >nul 2>&1
echo OK - Vehiculos de prueba creados

echo.
echo Datos de prueba agregados exitosamente!
echo.
pause
goto menu_loop

:reload_all_data
echo.
echo ============================================
echo   RECARGANDO TODOS LOS DATOS
echo ============================================
echo.
echo ADVERTENCIA: Esto eliminara todos los datos existentes!
choice /c SN /n /m "¿Estas seguro? [S/N]: "
if errorlevel 2 goto menu_loop

echo Eliminando datos existentes...
docker exec mongodb-local mongosh auth_db --eval "db.users.deleteMany({})"
docker exec mongodb-local mongosh drivers_db --eval "db.drivers.deleteMany({})"
docker exec mongodb-local mongosh vehicles_db --eval "db.vehicles.deleteMany({})"
echo Datos existentes eliminados.

echo Cargando nuevos datos...
call add-auth-data.bat >nul 2>&1
call add-drivers-data.bat >nul 2>&1
call add-simple-data.bat >nul 2>&1
echo.
echo Todos los datos recargados exitosamente!
echo.
pause
goto menu_loop

:delete_all_data
echo.
echo ============================================
echo   ELIMINANDO TODOS LOS DATOS
echo ============================================
echo.
echo ADVERTENCIA: Esto eliminara TODOS los datos de las bases de datos!
choice /c SN /n /m "¿Estas seguro? [S/N]: "
if errorlevel 2 goto menu_loop

echo Eliminando todos los datos...
docker exec mongodb-local mongosh auth_db --eval "db.users.deleteMany({})"
docker exec mongodb-local mongosh drivers_db --eval "db.drivers.deleteMany({})"
docker exec mongodb-local mongosh vehicles_db --eval "db.vehicles.deleteMany({})"
echo.
echo Todos los datos eliminados exitosamente!
echo.
pause
goto menu_loop

:add_missing_data
echo.
echo ============================================
echo   AGREGANDO DATOS FALTANTES
echo ============================================
echo.
if %auth_count% equ 0 (
    echo Cargando datos de autenticacion...
    call add-auth-data.bat >nul 2>&1
    echo OK - Usuarios de prueba creados
) else (
    echo Usuarios ya existen (%auth_count% encontrados)
)

if %drivers_count% equ 0 (
    echo Cargando datos de choferes...
    call add-drivers-data.bat >nul 2>&1
    echo OK - Choferes de prueba creados
) else (
    echo Choferes ya existen (%drivers_count% encontrados)
)

if %vehicles_count% equ 0 (
    echo Cargando datos de vehiculos...
    call add-simple-data.bat >nul 2>&1
    echo OK - Vehiculos de prueba creados
) else (
    echo Vehiculos ya existen (%vehicles_count% encontrados)
)

echo.
echo Datos faltantes agregados exitosamente!
echo.
pause
goto menu_loop

:menu_loop
echo.
echo Deseas realizar otra accion?
choice /c SN /n /m "[S] Si   [N] No: "
if errorlevel 2 goto exit_script
if errorlevel 1 goto :eof

:exit_script
echo.
echo ============================================
echo   GESTION DE DATOS COMPLETADA
echo ============================================
echo.
echo Credenciales de prueba:
echo   Usuario: admin
echo   Password: admin123
echo.
echo Para probar los servicios:
echo   - Auth: http://localhost:8085/
echo   - Drivers: http://localhost:8081/drivers.html
echo   - Vehicles: http://localhost:8082/vehicles.html
echo   - Gateway: http://localhost:8090/api/v1/drivers
echo.
pause
