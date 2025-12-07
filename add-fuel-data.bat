@echo off
echo ===============================================
echo   AGREGANDO DATOS DE PRUEBA A FUEL_DB
echo ===============================================
echo.

echo Insertando registros de consumo de combustible basados en rutas existentes...
echo.

REM Obtener IDs de rutas, vehículos y choferes para crear registros de combustible
REM Registro 1: Para RUT-002 (María García - GHI-789) - EN_CURSO
docker exec mongodb-local mongosh fuel_db --eval "var routes = db.getSiblingDB('routes_db').routes.find({codigo: 'RUT-002'}).toArray(); if (routes.length > 0) { var route = routes[0]; db.fuel_consumptions.insertOne({fecha_hora: new Date(Date.now() - 7200000), cantidad_litros: 15.5, tipo_combustible: 'DIESEL', precio_por_litro: 1.25, costo_total: 19.38, vehiculo_id: route.vehiculoId, chofer_id: route.choferId, ruta_id: route._id.toString(), lectura_odometro_horas: 1250.5, observaciones: 'Carga de combustible al inicio de ruta RUT-002', activo: true, createdAt: new Date(), updatedAt: new Date()}); print('Registro 1 insertado: RUT-002 - 15.5L Diesel'); }"

REM Registro 2: Para RUT-003 (Carlos López - JKL-012) - EN_CURSO
docker exec mongodb-local mongosh fuel_db --eval "var routes = db.getSiblingDB('routes_db').routes.find({codigo: 'RUT-003'}).toArray(); if (routes.length > 0) { var route = routes[0]; db.fuel_consumptions.insertOne({fecha_hora: new Date(Date.now() - 10800000), cantidad_litros: 20.0, tipo_combustible: 'DIESEL', precio_por_litro: 1.25, costo_total: 25.00, vehiculo_id: route.vehiculoId, chofer_id: route.choferId, ruta_id: route._id.toString(), lectura_odometro_horas: 890.2, observaciones: 'Carga de combustible para ruta RUT-003', activo: true, createdAt: new Date(), updatedAt: new Date()}); print('Registro 2 insertado: RUT-003 - 20.0L Diesel'); }"

REM Registro 3: Para RUT-005 (Luis Ramírez - DEF-456) - COMPLETADA
docker exec mongodb-local mongosh fuel_db --eval "var routes = db.getSiblingDB('routes_db').routes.find({codigo: 'RUT-005'}).toArray(); if (routes.length > 0) { var route = routes[0]; db.fuel_consumptions.insertOne({fecha_hora: new Date(Date.now() - 10800000), cantidad_litros: 12.0, tipo_combustible: 'DIESEL', precio_por_litro: 1.25, costo_total: 15.00, vehiculo_id: route.vehiculoId, chofer_id: route.choferId, ruta_id: route._id.toString(), lectura_odometro_horas: 2100.0, observaciones: 'Carga de combustible para ruta RUT-005 (completada)', activo: true, createdAt: new Date(), updatedAt: new Date()}); print('Registro 3 insertado: RUT-005 - 12.0L Diesel'); }"

REM Registro 4: Para RUT-001 (Juan Pérez - ABC-123) - PENDIENTE (carga previa)
docker exec mongodb-local mongosh fuel_db --eval "var routes = db.getSiblingDB('routes_db').routes.find({codigo: 'RUT-001'}).toArray(); if (routes.length > 0) { var route = routes[0]; db.fuel_consumptions.insertOne({fecha_hora: new Date(Date.now() - 86400000), cantidad_litros: 30.0, tipo_combustible: 'DIESEL', precio_por_litro: 1.25, costo_total: 37.50, vehiculo_id: route.vehiculoId, chofer_id: route.choferId, ruta_id: route._id.toString(), lectura_odometro_horas: 1500.0, observaciones: 'Carga de combustible previa a ruta RUT-001', activo: true, createdAt: new Date(), updatedAt: new Date()}); print('Registro 4 insertado: RUT-001 - 30.0L Diesel'); }"

REM Registro 5: Para RUT-004 (Ana Martínez - MNO-345) - PENDIENTE (carga previa)
docker exec mongodb-local mongosh fuel_db --eval "var routes = db.getSiblingDB('routes_db').routes.find({codigo: 'RUT-004'}).toArray(); if (routes.length > 0) { var route = routes[0]; db.fuel_consumptions.insertOne({fecha_hora: new Date(Date.now() - 172800000), cantidad_litros: 25.0, tipo_combustible: 'DIESEL', precio_por_litro: 1.25, costo_total: 31.25, vehiculo_id: route.vehiculoId, chofer_id: route.choferId, ruta_id: route._id.toString(), lectura_odometro_horas: 1100.5, observaciones: 'Carga de combustible previa a ruta RUT-004', activo: true, createdAt: new Date(), updatedAt: new Date()}); print('Registro 5 insertado: RUT-004 - 25.0L Diesel'); }"

REM Registro 6: Consumo sin ruta asociada (mantenimiento)
docker exec mongodb-local mongosh fuel_db --eval "var vehicles = db.getSiblingDB('vehicles_db').vehicles.find({placa: 'ABC-123'}).toArray(); var drivers = db.getSiblingDB('drivers_db').drivers.find({nombre: 'Juan', apellido: 'Pérez'}).toArray(); if (vehicles.length > 0 && drivers.length > 0) { db.fuel_consumptions.insertOne({fecha_hora: new Date(Date.now() - 259200000), cantidad_litros: 5.0, tipo_combustible: 'DIESEL', precio_por_litro: 1.25, costo_total: 6.25, vehiculo_id: vehicles[0]._id.toString(), chofer_id: drivers[0]._id.toString(), ruta_id: null, lectura_odometro_horas: 1450.0, observaciones: 'Consumo durante mantenimiento del vehículo', activo: true, createdAt: new Date(), updatedAt: new Date()}); print('Registro 6 insertado: Sin ruta - 5.0L Diesel (mantenimiento)'); }"

REM Registro 7: Consumo adicional para RUT-002 (durante la ruta)
docker exec mongodb-local mongosh fuel_db --eval "var routes = db.getSiblingDB('routes_db').routes.find({codigo: 'RUT-002'}).toArray(); if (routes.length > 0) { var route = routes[0]; db.fuel_consumptions.insertOne({fecha_hora: new Date(Date.now() - 3600000), cantidad_litros: 18.0, tipo_combustible: 'DIESEL', precio_por_litro: 1.25, costo_total: 22.50, vehiculo_id: route.vehiculoId, chofer_id: route.choferId, ruta_id: route._id.toString(), lectura_odometro_horas: 1265.0, observaciones: 'Carga adicional durante ruta RUT-002', activo: true, createdAt: new Date(), updatedAt: new Date()}); print('Registro 7 insertado: RUT-002 - 18.0L Diesel (adicional)'); }"

echo.
echo ===============================================
echo   DATOS DE FUEL AGREGADOS EXITOSAMENTE!
echo ===============================================
echo.
echo Registros insertados: 7
echo Tipo de combustible: DIESEL (todos)
echo Precio por litro: $1.25
echo.
echo Distribución:
echo - Registros con ruta asociada: 6
echo - Registros sin ruta (mantenimiento): 1
echo - Total litros: 125.5 L
echo - Costo total: $156.88
echo.
echo Verificando datos...
docker exec mongodb-local mongosh fuel_db --eval "print('Total registros:', db.fuel_consumptions.countDocuments({}))"
docker exec mongodb-local mongosh fuel_db --eval "var total = db.fuel_consumptions.aggregate([{$group: {_id: null, total: {$sum: '$cantidad_litros'}}}]).toArray(); if (total.length > 0) { print('Total litros:', total[0].total); }"
echo.
echo Ahora prueba las APIs:
echo - GET http://localhost:8084/api/v1/fuel
echo - GET http://localhost:8084/api/v1/fuel/stats
echo - Frontend: http://localhost:8084/fuel.html
echo.

