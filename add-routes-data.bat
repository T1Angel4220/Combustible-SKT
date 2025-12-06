@echo off
echo ===============================================
echo   AGREGANDO DATOS DE PRUEBA A ROUTES_DB
echo ===============================================
echo.

echo Insertando rutas de prueba basadas en asignaciones existentes...
echo.

REM Script que obtiene asignaciones activas y crea rutas basadas en ellas
docker exec mongodb-local mongosh routes_db --eval "var drivers = db.getSiblingDB('drivers_db').drivers.find({}).toArray(); var vehicles = db.getSiblingDB('vehicles_db').vehicles.find({}).toArray(); var assignments = db.getSiblingDB('vehicles_db').asignaciones_vehiculos.find({estado: 'ACTIVA', activo: true}).toArray(); if (assignments.length < 3) { print('ERROR: Se requieren al menos 3 asignaciones activas'); } else { print('Encontradas ' + assignments.length + ' asignaciones activas'); }"

REM Ruta 1: Juan Pérez con Toyota Hilux (ABC-123) - PENDIENTE
docker exec mongodb-local mongosh routes_db --eval "var drivers = db.getSiblingDB('drivers_db').drivers.find({nombre: 'Juan', apellido: 'Pérez'}).toArray(); var vehicles = db.getSiblingDB('vehicles_db').vehicles.find({placa: 'ABC-123'}).toArray(); if (drivers.length > 0 && vehicles.length > 0) { db.routes.insertOne({codigo: 'RUT-001', nombreRuta: 'Ruta Norte', origen: 'Base Central', destino: 'Zona Norte A', distanciaKm: 85, duracionEstimadaHoras: 3.33, consumoEstimadoLitros: 23.4, horaInicio: '08:00', vehiculoId: vehicles[0]._id.toString(), choferId: drivers[0]._id.toString(), tipoMaquinaria: 'CAMION', estado: 'PENDIENTE', activa: true, observaciones: 'Ruta programada para entrega de materiales - Asignado: Juan Pérez con Toyota Hilux', createdAt: new Date(), updatedAt: new Date()}); print('RUT-001 insertada: Juan Pérez - ABC-123'); }"

REM Ruta 2: María García con Volvo EC210 (GHI-789) - EN_CURSO
docker exec mongodb-local mongosh routes_db --eval "var drivers = db.getSiblingDB('drivers_db').drivers.find({nombre: 'María', apellido: 'García'}).toArray(); var vehicles = db.getSiblingDB('vehicles_db').vehicles.find({placa: 'GHI-789'}).toArray(); if (drivers.length > 0 && vehicles.length > 0) { db.routes.insertOne({codigo: 'RUT-002', nombreRuta: 'Ruta Sur', origen: 'Base Central', destino: 'Zona Sur B', distanciaKm: 120, duracionEstimadaHoras: 4.75, consumoEstimadoLitros: 33.0, horaInicio: '07:00', vehiculoId: vehicles[0]._id.toString(), choferId: drivers[0]._id.toString(), tipoMaquinaria: 'EXCAVADORA', estado: 'EN_CURSO', activa: true, fechaInicio: new Date(), observaciones: 'Ruta en ejecución - Asignado: María García con Volvo EC210', createdAt: new Date(), updatedAt: new Date()}); print('RUT-002 insertada: María García - GHI-789'); }"

REM Ruta 3: Carlos López con Caterpillar 725C (JKL-012) - EN_CURSO
docker exec mongodb-local mongosh routes_db --eval "var drivers = db.getSiblingDB('drivers_db').drivers.find({nombre: 'Carlos', apellido: 'López'}).toArray(); var vehicles = db.getSiblingDB('vehicles_db').vehicles.find({placa: 'JKL-012'}).toArray(); if (drivers.length > 0 && vehicles.length > 0) { db.routes.insertOne({codigo: 'RUT-003', nombreRuta: 'Ruta Este', origen: 'Base Central', destino: 'Zona Este C', distanciaKm: 95, duracionEstimadaHoras: 3.75, consumoEstimadoLitros: 26.1, horaInicio: '09:00', vehiculoId: vehicles[0]._id.toString(), choferId: drivers[0]._id.toString(), tipoMaquinaria: 'VOLQUETE', estado: 'EN_CURSO', activa: true, fechaInicio: new Date(Date.now() - 3600000), observaciones: 'Ruta iniciada hace 1 hora - Asignado: Carlos López con Caterpillar 725C', createdAt: new Date(), updatedAt: new Date()}); print('RUT-003 insertada: Carlos López - JKL-012'); }"

REM Ruta 4: Ana Martínez con Komatsu WA380 (MNO-345) - PENDIENTE
docker exec mongodb-local mongosh routes_db --eval "var drivers = db.getSiblingDB('drivers_db').drivers.find({nombre: 'Ana', apellido: 'Martínez'}).toArray(); var vehicles = db.getSiblingDB('vehicles_db').vehicles.find({placa: 'MNO-345'}).toArray(); if (drivers.length > 0 && vehicles.length > 0) { db.routes.insertOne({codigo: 'RUT-004', nombreRuta: 'Ruta Oeste', origen: 'Base Central', destino: 'Zona Oeste D', distanciaKm: 110, duracionEstimadaHoras: 4.25, consumoEstimadoLitros: 30.3, horaInicio: '10:00', vehiculoId: vehicles[0]._id.toString(), choferId: drivers[0]._id.toString(), tipoMaquinaria: 'CARGADOR', estado: 'PENDIENTE', activa: true, observaciones: 'Ruta programada para mañana - Asignado: Ana Martínez con Komatsu WA380', createdAt: new Date(), updatedAt: new Date()}); print('RUT-004 insertada: Ana Martínez - MNO-345'); }"

REM Ruta 5: Luis Ramírez con Ford Ranger (DEF-456) - COMPLETADA
docker exec mongodb-local mongosh routes_db --eval "var drivers = db.getSiblingDB('drivers_db').drivers.find({nombre: 'Luis', apellido: 'Ramírez'}).toArray(); var vehicles = db.getSiblingDB('vehicles_db').vehicles.find({placa: 'DEF-456'}).toArray(); if (drivers.length > 0 && vehicles.length > 0) { db.routes.insertOne({codigo: 'RUT-005', nombreRuta: 'Ruta Centro', origen: 'Base Central', destino: 'Zona Centro', distanciaKm: 50, duracionEstimadaHoras: 2.0, consumoEstimadoLitros: 13.8, horaInicio: '06:00', vehiculoId: vehicles[0]._id.toString(), choferId: drivers[0]._id.toString(), tipoMaquinaria: 'CAMION', estado: 'COMPLETADA', activa: true, fechaInicio: new Date(Date.now() - 7200000), fechaFin: new Date(Date.now() - 3600000), observaciones: 'Ruta completada exitosamente - Asignado: Luis Ramírez con Ford Ranger', createdAt: new Date(), updatedAt: new Date()}); print('RUT-005 insertada: Luis Ramírez - DEF-456'); }"

echo.
echo ===============================================
echo   DATOS DE ROUTES AGREGADOS EXITOSAMENTE!
echo ===============================================
echo.
echo Rutas insertadas: 5
echo Estados: PENDIENTE (2), EN_CURSO (2), COMPLETADA (1)
echo.
echo Rutas basadas en asignaciones:
echo - RUT-001: Juan Pérez (ABC-123 Toyota Hilux) - PENDIENTE
echo - RUT-002: María García (GHI-789 Volvo EC210) - EN_CURSO
echo - RUT-003: Carlos López (JKL-012 Caterpillar 725C) - EN_CURSO
echo - RUT-004: Ana Martínez (MNO-345 Komatsu WA380) - PENDIENTE
echo - RUT-005: Luis Ramírez (DEF-456 Ford Ranger) - COMPLETADA
echo.
echo Verificando datos...
docker exec mongodb-local mongosh routes_db --eval "print('Total rutas:', db.routes.countDocuments({}))"
echo.
echo Ahora prueba las APIs:
echo - GET http://localhost:8083/api/v1/routes
echo - GET http://localhost:8083/api/v1/routes/stats
echo - Frontend: http://localhost:8083/routes.html
echo.
