@echo off
echo ===============================================
echo   AGREGANDO DATOS DE PRUEBA A VEHICLES_DB
echo ===============================================
echo.

echo Insertando vehículos con diferentes tipos de maquinaria...
echo.

REM Vehículo 1: Toyota Hilux - CAMION (Liviana) - Disponible
docker exec mongodb-local mongosh vehicles_db --eval "db.vehicles.insertOne({placa: 'ABC-123', marca: 'Toyota', modelo: 'Hilux', anio: 2023, tipoMaquinaria: 'CAMION', estadoOperativo: 'DISPONIBLE', capacidadTanque: 80, consumoPromedio: 12.5, kilometrajeActual: 15000, fechaCreacion: new Date(), fechaActualizacion: new Date(), activo: true})"

REM Vehículo 2: Ford Ranger - CAMION (Liviana) - Disponible
docker exec mongodb-local mongosh vehicles_db --eval "db.vehicles.insertOne({placa: 'DEF-456', marca: 'Ford', modelo: 'Ranger', anio: 2022, tipoMaquinaria: 'CAMION', estadoOperativo: 'DISPONIBLE', capacidadTanque: 75, consumoPromedio: 11.8, kilometrajeActual: 25000, fechaCreacion: new Date(), fechaActualizacion: new Date(), activo: true})"

REM Vehículo 3: Volvo Excavadora - EXCAVADORA (Pesada) - Disponible
docker exec mongodb-local mongosh vehicles_db --eval "db.vehicles.insertOne({placa: 'GHI-789', marca: 'Volvo', modelo: 'EC210', anio: 2023, tipoMaquinaria: 'EXCAVADORA', estadoOperativo: 'DISPONIBLE', capacidadTanque: 200, consumoPromedio: 18.5, kilometrajeActual: 5000, fechaCreacion: new Date(), fechaActualizacion: new Date(), activo: true})"

REM Vehículo 4: Caterpillar Volquete - VOLQUETE (Liviana) - Disponible
docker exec mongodb-local mongosh vehicles_db --eval "db.vehicles.insertOne({placa: 'JKL-012', marca: 'Caterpillar', modelo: '725C', anio: 2024, tipoMaquinaria: 'VOLQUETE', estadoOperativo: 'DISPONIBLE', capacidadTanque: 150, consumoPromedio: 15.2, kilometrajeActual: 3000, fechaCreacion: new Date(), fechaActualizacion: new Date(), activo: true})"

REM Vehículo 5: Komatsu Cargador - CARGADOR (Pesada) - Disponible
docker exec mongodb-local mongosh vehicles_db --eval "db.vehicles.insertOne({placa: 'MNO-345', marca: 'Komatsu', modelo: 'WA380', anio: 2023, tipoMaquinaria: 'CARGADOR', estadoOperativo: 'DISPONIBLE', capacidadTanque: 180, consumoPromedio: 16.8, kilometrajeActual: 8000, fechaCreacion: new Date(), fechaActualizacion: new Date(), activo: true})"

REM Vehículo 6: Liebherr Grúa - GRUA (Pesada) - En Mantenimiento
docker exec mongodb-local mongosh vehicles_db --eval "db.vehicles.insertOne({placa: 'PQR-678', marca: 'Liebherr', modelo: 'LTM 1050', anio: 2022, tipoMaquinaria: 'GRUA', estadoOperativo: 'MANTENIMIENTO', capacidadTanque: 250, consumoPromedio: 20.5, kilometrajeActual: 12000, fechaCreacion: new Date(), fechaActualizacion: new Date(), activo: true})"

echo.
echo ===============================================
echo   DATOS DE VEHICLES AGREGADOS EXITOSAMENTE!
echo ===============================================
echo.
echo Vehículos insertados: 6
echo Tipos de maquinaria: CAMION (2), EXCAVADORA (1), VOLQUETE (1), CARGADOR (1), GRUA (1)
echo Estados: DISPONIBLE (5), MANTENIMIENTO (1)
echo.
echo Verificando datos...
docker exec mongodb-local mongosh vehicles_db --eval "print('Total vehículos:', db.vehicles.countDocuments({}))"
echo.
echo Ahora prueba la API: GET http://localhost:8082/api/v1/vehicles
echo.
