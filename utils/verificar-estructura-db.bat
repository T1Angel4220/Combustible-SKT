@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

title Verificar Estructura de Bases de Datos - SKT Combustible

echo ================================================
echo   VERIFICAR ESTRUCTURA DE BASES DE DATOS
echo   Sistema SKT Combustible
echo ================================================
echo.

REM Configuración de la cadena de conexión a MongoDB Atlas
set "ATLAS_BASE_URI=mongodb+srv://907johan_db_user:piIe4vWfuADsnRM6@combustibleskt.4n4nf9z.mongodb.net/?retryWrites=true&w=majority&readPreference=secondaryPreferred&maxPoolSize=50&minPoolSize=10&appName=combustibleskt"

echo Verificando conexión a MongoDB Atlas...
echo.

REM Verificar conexión
mongosh "%ATLAS_BASE_URI%" --eval "db.adminCommand('ping')" --quiet >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: No se pudo conectar a MongoDB Atlas
    echo Verifica que mongosh esté instalado y la cadena de conexión sea correcta
    pause
    exit /b 1
)
echo ✓ Conexión exitosa
echo.

echo ================================================
echo   LISTANDO TODAS LAS BASES DE DATOS
echo ================================================
echo.

mongosh "%ATLAS_BASE_URI%" --eval "db.adminCommand('listDatabases').databases.forEach(function(db) { print('Base de datos: ' + db.name + ' (tamaño: ' + (db.sizeOnDisk / 1024 / 1024).toFixed(2) + ' MB)'); });" --quiet

echo.
echo ================================================
echo   VERIFICANDO ESTRUCTURA DE CADA BASE DE DATOS
echo ================================================
echo.

echo [1] Verificando auth_db...
echo.
mongosh "%ATLAS_BASE_URI%" --eval "db = db.getSiblingDB('auth_db'); print('Colecciones en auth_db:'); db.getCollectionNames().forEach(function(coll) { var count = db.getCollection(coll).countDocuments(); print('  - ' + coll + ': ' + count + ' documentos'); });" --quiet
echo.

echo [2] Verificando drivers_db...
echo.
mongosh "%ATLAS_BASE_URI%" --eval "db = db.getSiblingDB('drivers_db'); print('Colecciones en drivers_db:'); db.getCollectionNames().forEach(function(coll) { var count = db.getCollection(coll).countDocuments(); print('  - ' + coll + ': ' + count + ' documentos'); });" --quiet
echo.

echo [3] Verificando vehicles_db...
echo.
mongosh "%ATLAS_BASE_URI%" --eval "db = db.getSiblingDB('vehicles_db'); print('Colecciones en vehicles_db:'); db.getCollectionNames().forEach(function(coll) { var count = db.getCollection(coll).countDocuments(); print('  - ' + coll + ': ' + count + ' documentos'); });" --quiet
echo.

echo [4] Verificando routes_db...
echo.
mongosh "%ATLAS_BASE_URI%" --eval "db = db.getSiblingDB('routes_db'); print('Colecciones en routes_db:'); db.getCollectionNames().forEach(function(coll) { var count = db.getCollection(coll).countDocuments(); print('  - ' + coll + ': ' + count + ' documentos'); });" --quiet
echo.

echo [5] Verificando fuel_db...
echo.
mongosh "%ATLAS_BASE_URI%" --eval "db = db.getSiblingDB('fuel_db'); print('Colecciones en fuel_db:'); db.getCollectionNames().forEach(function(coll) { var count = db.getCollection(coll).countDocuments(); print('  - ' + coll + ': ' + count + ' documentos'); });" --quiet
echo.

echo ================================================
echo   VERIFICANDO ESTRUCTURA DE DOCUMENTOS
echo ================================================
echo.

echo [auth_db] Estructura de un documento de usuarios (si existe):
echo.
mongosh "%ATLAS_BASE_URI%" --eval "db = db.getSiblingDB('auth_db'); var user = db.usuarios.findOne(); if (user) { print(JSON.stringify(user, null, 2)); } else { print('No hay documentos en la colección usuarios'); }" --quiet
echo.

echo [drivers_db] Estructura de un documento de drivers (si existe):
echo.
mongosh "%ATLAS_BASE_URI%" --eval "db = db.getSiblingDB('drivers_db'); var driver = db.drivers.findOne(); if (driver) { print(JSON.stringify(driver, null, 2)); } else { print('No hay documentos en la colección drivers'); }" --quiet
echo.

echo [vehicles_db] Estructura de un documento de vehicles (si existe):
echo.
mongosh "%ATLAS_BASE_URI%" --eval "db = db.getSiblingDB('vehicles_db'); var vehicle = db.vehicles.findOne(); if (vehicle) { print(JSON.stringify(vehicle, null, 2)); } else { print('No hay documentos en la colección vehicles'); }" --quiet
echo.

echo [routes_db] Estructura de un documento de routes (si existe):
echo.
mongosh "%ATLAS_BASE_URI%" --eval "db = db.getSiblingDB('routes_db'); var route = db.routes.findOne(); if (route) { print(JSON.stringify(route, null, 2)); } else { print('No hay documentos en la colección routes'); }" --quiet
echo.

echo [fuel_db] Estructura de un documento de fuel_consumptions (si existe):
echo.
mongosh "%ATLAS_BASE_URI%" --eval "db = db.getSiblingDB('fuel_db'); var fuel = db.fuel_consumptions.findOne(); if (fuel) { print(JSON.stringify(fuel, null, 2)); } else { print('No hay documentos en la colección fuel_consumptions'); }" --quiet
echo.

echo ================================================
echo   VERIFICACIÓN COMPLETA
echo ================================================
echo.
echo Si alguna colección no existe o está vacía, verifica:
echo   1. Que los nombres de las colecciones sean correctos
echo   2. Que los scripts de inserción se ejecuten correctamente
echo   3. Que no haya errores de permisos en MongoDB Atlas
echo.
pause

