@echo off
setlocal enabledelayedexpansion

title Creando Estructura en MongoDB Atlas

echo ================================================
echo   CREANDO BASES DE DATOS Y COLECCIONES
echo   En MongoDB Atlas
echo ================================================
echo.
echo Este script creara:
echo - Bases de datos: auth_db, drivers_db, vehicles_db, routes_db, fuel_db
echo - Colecciones necesarias en cada base de datos
echo.
echo NO importara datos, solo creara la estructura.
echo.
pause

REM Verificar que mongosh esté disponible
where mongosh >nul 2>&1
if errorlevel 1 (
    echo ERROR: mongosh no esta instalado
    echo.
    echo Instala MongoDB Shell desde:
    echo https://www.mongodb.com/try/download/shell
    echo.
    pause
    exit /b 1
)

echo.
echo ================================================
echo   CONFIGURANDO CONEXION
echo ================================================
echo.
echo Cluster: combustibleskt.4n4nf9z.mongodb.net
echo Usuario: 907johan_db_user
echo.

set "ATLAS_URI=mongodb+srv://907johan_db_user:piIe4vWfuADsnRM6@combustibleskt.4n4nf9z.mongodb.net/?retryWrites=true&w=majority&appName=combustibleskt"

REM Probar conexión
echo Probando conexion a MongoDB Atlas...
mongosh "!ATLAS_URI!" --eval "db.adminCommand('ping')" >nul 2>&1
if errorlevel 1 (
    echo ERROR: No se pudo conectar a MongoDB Atlas
    echo.
    echo Verifica:
    echo   1. Que tu IP este permitida en Network Access
    echo   2. Que el cluster este activo
    echo   3. Que las credenciales sean correctas
    echo.
    pause
    exit /b 1
)

echo ✓ Conexion exitosa
echo.

echo ================================================
echo   CREANDO ESTRUCTURA
echo ================================================
echo.

REM Crear auth_db y coleccion usuarios
echo [1/5] Creando auth_db y coleccion usuarios...
mongosh "!ATLAS_URI!" --eval "db = db.getSiblingDB('auth_db'); db.createCollection('usuarios'); print('✓ auth_db.usuarios creada')"
if errorlevel 1 (
    echo   ✗ ERROR al crear auth_db
) else (
    echo   ✓ auth_db y coleccion usuarios creadas
)
echo.

REM Crear drivers_db y coleccion drivers
echo [2/5] Creando drivers_db y coleccion drivers...
mongosh "!ATLAS_URI!" --eval "db = db.getSiblingDB('drivers_db'); db.createCollection('drivers'); print('✓ drivers_db.drivers creada')"
if errorlevel 1 (
    echo   ✗ ERROR al crear drivers_db
) else (
    echo   ✓ drivers_db y coleccion drivers creadas
)
echo.

REM Crear vehicles_db y colecciones
echo [3/5] Creando vehicles_db y colecciones...
mongosh "!ATLAS_URI!" --eval "db = db.getSiblingDB('vehicles_db'); db.createCollection('vehicles'); db.createCollection('asignaciones_vehiculos'); db.createCollection('mantenimientos'); print('✓ vehicles_db: vehicles, asignaciones_vehiculos, mantenimientos creadas')"
if errorlevel 1 (
    echo   ✗ ERROR al crear vehicles_db
) else (
    echo   ✓ vehicles_db y colecciones creadas
)
echo.

REM Crear routes_db y coleccion routes
echo [4/5] Creando routes_db y coleccion routes...
mongosh "!ATLAS_URI!" --eval "db = db.getSiblingDB('routes_db'); db.createCollection('routes'); print('✓ routes_db.routes creada')"
if errorlevel 1 (
    echo   ✗ ERROR al crear routes_db
) else (
    echo   ✓ routes_db y coleccion routes creadas
)
echo.

REM Crear fuel_db y coleccion fuel_consumptions
echo [5/5] Creando fuel_db y coleccion fuel_consumptions...
mongosh "!ATLAS_URI!" --eval "db = db.getSiblingDB('fuel_db'); db.createCollection('fuel_consumptions'); print('✓ fuel_db.fuel_consumptions creada')"
if errorlevel 1 (
    echo   ✗ ERROR al crear fuel_db
) else (
    echo   ✓ fuel_db y coleccion fuel_consumptions creadas
)
echo.

echo ================================================
echo   VERIFICANDO ESTRUCTURA CREADA
echo ================================================
echo.

echo Listando bases de datos creadas...
mongosh "!ATLAS_URI!" --eval "db.adminCommand('listDatabases').databases.forEach(function(d) { if (d.name.match(/^(auth|drivers|vehicles|routes|fuel)_db$/)) print('✓ ' + d.name) })"
echo.

echo Verificando colecciones...
echo.
echo [auth_db] Colecciones:
mongosh "!ATLAS_URI!" --eval "db = db.getSiblingDB('auth_db'); db.getCollectionNames().forEach(function(c) { print('  - ' + c) })"
echo.

echo [drivers_db] Colecciones:
mongosh "!ATLAS_URI!" --eval "db = db.getSiblingDB('drivers_db'); db.getCollectionNames().forEach(function(c) { print('  - ' + c) })"
echo.

echo [vehicles_db] Colecciones:
mongosh "!ATLAS_URI!" --eval "db = db.getSiblingDB('vehicles_db'); db.getCollectionNames().forEach(function(c) { print('  - ' + c) })"
echo.

echo [routes_db] Colecciones:
mongosh "!ATLAS_URI!" --eval "db = db.getSiblingDB('routes_db'); db.getCollectionNames().forEach(function(c) { print('  - ' + c) })"
echo.

echo [fuel_db] Colecciones:
mongosh "!ATLAS_URI!" --eval "db = db.getSiblingDB('fuel_db'); db.getCollectionNames().forEach(function(c) { print('  - ' + c) })"
echo.

echo ================================================
echo   ESTRUCTURA CREADA EXITOSAMENTE
echo ================================================
echo.
echo Bases de datos creadas:
echo - auth_db (coleccion: usuarios)
echo - drivers_db (coleccion: drivers)
echo - vehicles_db (colecciones: vehicles, asignaciones_vehiculos, mantenimientos)
echo - routes_db (coleccion: routes)
echo - fuel_db (coleccion: fuel_consumptions)
echo.
echo Las bases de datos y colecciones estan listas para usar.
echo Ahora puedes ejecutar los servicios con el perfil 'atlas'.
echo.
echo Siguiente paso: Ejecutar start-all-services-atlas.bat
echo.
pause

