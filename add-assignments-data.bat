@echo off
echo ===============================================
echo   AGREGANDO ASIGNACIONES DE VEHICULOS A CHOFERES
echo ===============================================
echo.

echo Creando asignaciones de vehículos a choferes...
echo.

REM Asignación 1: Juan Pérez -> Toyota Hilux (ABC-123)
docker exec mongodb-local mongosh vehicles_db --eval "var juan = db.getSiblingDB('drivers_db').drivers.findOne({nombre: 'Juan', apellido: 'Pérez'}); var toyota = db.vehicles.findOne({placa: 'ABC-123'}); if (juan && toyota) { db.asignaciones_vehiculos.insertOne({vehicle: {\$ref: 'vehicles', \$id: toyota._id}, choferId: juan._id.toString(), fechaAsignacion: new Date(), estado: 'ACTIVA', activo: true, fechaCreacion: new Date(), fechaActualizacion: new Date(), observaciones: 'Asignación inicial'}); db.vehicles.updateOne({_id: toyota._id}, {\$set: {estadoOperativo: 'EN_USO'}}); print('Asignado: ABC-123 -> Juan Pérez'); }"

REM Asignación 2: María García -> Volvo EC210 (GHI-789)
docker exec mongodb-local mongosh vehicles_db --eval "var maria = db.getSiblingDB('drivers_db').drivers.findOne({nombre: 'María', apellido: 'García'}); var volvo = db.vehicles.findOne({placa: 'GHI-789'}); if (maria && volvo) { db.asignaciones_vehiculos.insertOne({vehicle: {\$ref: 'vehicles', \$id: volvo._id}, choferId: maria._id.toString(), fechaAsignacion: new Date(), estado: 'ACTIVA', activo: true, fechaCreacion: new Date(), fechaActualizacion: new Date(), observaciones: 'Asignación inicial'}); db.vehicles.updateOne({_id: volvo._id}, {\$set: {estadoOperativo: 'EN_USO'}}); print('Asignado: GHI-789 -> María García'); }"

REM Asignación 3: Carlos López -> Caterpillar 725C (JKL-012)
docker exec mongodb-local mongosh vehicles_db --eval "var carlos = db.getSiblingDB('drivers_db').drivers.findOne({nombre: 'Carlos', apellido: 'López'}); var cat = db.vehicles.findOne({placa: 'JKL-012'}); if (carlos && cat) { db.asignaciones_vehiculos.insertOne({vehicle: {\$ref: 'vehicles', \$id: cat._id}, choferId: carlos._id.toString(), fechaAsignacion: new Date(), estado: 'ACTIVA', activo: true, fechaCreacion: new Date(), fechaActualizacion: new Date(), observaciones: 'Asignación inicial'}); db.vehicles.updateOne({_id: cat._id}, {\$set: {estadoOperativo: 'EN_USO'}}); print('Asignado: JKL-012 -> Carlos López'); }"

REM Asignación 4: Ana Martínez -> Komatsu WA380 (MNO-345)
docker exec mongodb-local mongosh vehicles_db --eval "var ana = db.getSiblingDB('drivers_db').drivers.findOne({nombre: 'Ana', apellido: 'Martínez'}); var komatsu = db.vehicles.findOne({placa: 'MNO-345'}); if (ana && komatsu) { db.asignaciones_vehiculos.insertOne({vehicle: {\$ref: 'vehicles', \$id: komatsu._id}, choferId: ana._id.toString(), fechaAsignacion: new Date(), estado: 'ACTIVA', activo: true, fechaCreacion: new Date(), fechaActualizacion: new Date(), observaciones: 'Asignación inicial'}); db.vehicles.updateOne({_id: komatsu._id}, {\$set: {estadoOperativo: 'EN_USO'}}); print('Asignado: MNO-345 -> Ana Martínez'); }"

REM Asignación 5: Luis Ramírez -> Ford Ranger (DEF-456)
docker exec mongodb-local mongosh vehicles_db --eval "var luis = db.getSiblingDB('drivers_db').drivers.findOne({nombre: 'Luis', apellido: 'Ramírez'}); var ford = db.vehicles.findOne({placa: 'DEF-456'}); if (luis && ford) { db.asignaciones_vehiculos.insertOne({vehicle: {\$ref: 'vehicles', \$id: ford._id}, choferId: luis._id.toString(), fechaAsignacion: new Date(), estado: 'ACTIVA', activo: true, fechaCreacion: new Date(), fechaActualizacion: new Date(), observaciones: 'Asignación inicial'}); db.vehicles.updateOne({_id: ford._id}, {\$set: {estadoOperativo: 'EN_USO'}}); print('Asignado: DEF-456 -> Luis Ramírez'); }"

echo.
echo ===============================================
echo   ASIGNACIONES AGREGADAS EXITOSAMENTE!
echo ===============================================
echo.
echo Asignaciones creadas: 5
echo - ABC-123 (Toyota Hilux) -> Juan Pérez
echo - GHI-789 (Volvo EC210) -> María García
echo - JKL-012 (Caterpillar 725C) -> Carlos López
echo - MNO-345 (Komatsu WA380) -> Ana Martínez
echo - DEF-456 (Ford Ranger) -> Luis Ramírez
echo.
echo Verificando asignaciones...
docker exec mongodb-local mongosh vehicles_db --eval "print('Total asignaciones:', db.asignaciones_vehiculos.countDocuments({}))"
echo.
