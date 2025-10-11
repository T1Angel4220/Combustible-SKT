@echo off
echo ============================================
echo Agregando Datos de Prueba a MongoDB
echo ============================================
echo.

echo [1/3] Verificando conexion a MongoDB...
docker exec mongodb-local mongosh --quiet --eval "db.adminCommand('ping')" >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: MongoDB no esta corriendo
    echo Ejecuta primero: restart-mongodb-no-auth.bat
    pause
    exit /b 1
)
echo OK - MongoDB conectado
echo.

echo [2/3] Insertando datos de prueba...
docker exec mongodb-local mongosh vehicles_db --quiet --eval "db.vehicles.insertMany([{placa:'ABC-123',marca:'Toyota',modelo:'Hilux',anio:2023,tipoMaquinaria:'CAMION',estadoOperativo:'DISPONIBLE',capacidadTanque:80,consumoPromedio:12.5,kilometrajeActual:15000,fechaCreacion:new Date(),fechaActualizacion:new Date(),activo:true},{placa:'DEF-456',marca:'Ford',modelo:'Ranger',anio:2022,tipoMaquinaria:'CAMION',estadoOperativo:'MANTENIMIENTO',capacidadTanque:75,consumoPromedio:11.8,kilometrajeActual:25000,fechaCreacion:new Date(),fechaActualizacion:new Date(),activo:true}])"

if %errorlevel% equ 0 (
    echo OK - Datos insertados exitosamente
) else (
    echo WARNING: Puede que ya existan datos similares
)
echo.

echo [3/3] Verificando datos insertados...
docker exec mongodb-local mongosh vehicles_db --quiet --eval "print('Total vehiculos:', db.vehicles.countDocuments())"
echo.

echo ============================================
echo Datos de Prueba Agregados!
echo ============================================
echo.
echo Vehiculos creados:
echo - ABC-123 (Toyota Hilux) - DISPONIBLE
echo - DEF-456 (Ford Ranger)  - MANTENIMIENTO
echo.
echo Ahora puedes probar:
echo - API: http://localhost:8082/api/v1/vehicles
echo - Frontend: http://localhost:8082/vehicles.html
echo.
pause
