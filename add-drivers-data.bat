@echo off
echo ===============================================
echo   AGREGANDO DATOS DE PRUEBA A DRIVERS_DB
echo ===============================================
echo.

echo Insertando choferes con nuevos enums EstadoOperativo...

docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'Juan', apellido: 'Pérez', dni: '12345678', licencia: 'LIC001', telefono: '987654321', email: 'juan.perez@skt.com', fechaContratacion: new Date('2024-01-15'), estado: 'DISPONIBLE', tipoMaquinariaAsignada: 'CAMION', activo: true, createdAt: new Date(), updatedAt: new Date()})"

docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'María', apellido: 'García', dni: '87654321', licencia: 'LIC002', telefono: '987654322', email: 'maria.garcia@skt.com', fechaContratacion: new Date('2024-02-01'), estado: 'ASIGNADO', tipoMaquinariaAsignada: 'EXCAVADORA', activo: true, createdAt: new Date(), updatedAt: new Date()})"

docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'Carlos', apellido: 'López', dni: '11223344', licencia: 'LIC003', telefono: '987654323', email: 'carlos.lopez@skt.com', fechaContratacion: new Date('2024-01-20'), estado: 'EN_RUTA', tipoMaquinariaAsignada: 'VOLQUETE', activo: true, createdAt: new Date(), updatedAt: new Date()})"

docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'Ana', apellido: 'Martínez', dni: '55667788', licencia: 'LIC004', telefono: '987654324', email: 'ana.martinez@skt.com', fechaContratacion: new Date('2024-03-01'), estado: 'DESCANSANDO', tipoMaquinariaAsignada: 'CARGADOR', activo: true, createdAt: new Date(), updatedAt: new Date()})"

docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'Pedro', apellido: 'Sánchez', dni: '99887766', licencia: 'LIC005', telefono: '987654325', email: 'pedro.sanchez@skt.com', fechaContratacion: new Date('2023-12-15'), estado: 'VACACIONES', tipoMaquinariaAsignada: null, activo: true, createdAt: new Date(), updatedAt: new Date()})"

echo.
echo ===============================================
echo   DATOS DE DRIVERS AGREGADOS EXITOSAMENTE!
echo ===============================================
echo.
echo Choferes insertados: 5
echo Estados utilizados: DISPONIBLE, ASIGNADO, EN_RUTA, DESCANSANDO, VACACIONES
echo.
echo Ahora prueba las APIs:
echo - GET http://localhost:8081/api/v1/drivers
echo - GET http://localhost:8081/api/v1/drivers/all
echo - Frontend: http://localhost:8081/drivers.html
echo.
pause
