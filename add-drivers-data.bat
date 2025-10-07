@echo off
echo Agregando datos de prueba a drivers_db...

docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'Juan Pérez', apellido: 'García', cedula: '12345678', telefono: '0987654321', email: 'juan.perez@email.com', licencia: 'B-123456', fechaVencimientoLicencia: new Date('2025-12-31'), estado: 'ACTIVO', fechaCreacion: new Date(), fechaActualizacion: new Date(), activo: true})"

docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'María López', apellido: 'Rodríguez', cedula: '87654321', telefono: '0987654322', email: 'maria.lopez@email.com', licencia: 'C-654321', fechaVencimientoLicencia: new Date('2025-11-30'), estado: 'ACTIVO', fechaCreacion: new Date(), fechaActualizacion: new Date(), activo: true})"

echo Datos de drivers agregados!
echo Ahora prueba las APIs:
echo GET http://localhost:8081/api/v1/drivers
echo GET http://localhost:8082/api/v1/vehicles

pause
