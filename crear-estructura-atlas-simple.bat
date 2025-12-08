@echo off
title Creando Estructura en MongoDB Atlas

echo ================================================
echo   CREANDO BASES DE DATOS Y COLECCIONES
echo   En MongoDB Atlas (Version Simple)
echo ================================================
echo.

set "ATLAS_URI=mongodb+srv://907johan_db_user:piIe4vWfuADsnRM6@combustibleskt.4n4nf9z.mongodb.net/?retryWrites=true&w=majority&appName=combustibleskt"

echo Creando estructura en MongoDB Atlas...
echo.

REM Crear todas las bases de datos y colecciones en un solo comando
mongosh "%ATLAS_URI%" --eval ^
"db = db.getSiblingDB('auth_db'); db.createCollection('usuarios'); "^
"db = db.getSiblingDB('drivers_db'); db.createCollection('drivers'); "^
"db = db.getSiblingDB('vehicles_db'); db.createCollection('vehicles'); db.createCollection('asignaciones_vehiculos'); db.createCollection('mantenimientos'); "^
"db = db.getSiblingDB('routes_db'); db.createCollection('routes'); "^
"db = db.getSiblingDB('fuel_db'); db.createCollection('fuel_consumptions'); "^
"print('✓ Todas las bases de datos y colecciones creadas exitosamente')"

if errorlevel 1 (
    echo.
    echo ERROR: No se pudieron crear las bases de datos
    echo Verifica la conexion y los permisos
    pause
    exit /b 1
)

echo.
echo ================================================
echo   ESTRUCTURA CREADA
echo ================================================
echo.
echo Bases de datos y colecciones creadas:
echo - auth_db.usuarios
echo - drivers_db.drivers
echo - vehicles_db.vehicles
echo - vehicles_db.asignaciones_vehiculos
echo - vehicles_db.mantenimientos
echo - routes_db.routes
echo - fuel_db.fuel_consumptions
echo.
pause

