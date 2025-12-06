@echo off
echo ===============================================
echo   AGREGANDO DATOS DE PRUEBA A DRIVERS_DB
echo ===============================================
echo.

echo Insertando choferes con diferentes tipos de maquinaria...
echo.

REM Chofer 1: Juan Pérez - CAMION (Liviana)
docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'Juan', apellido: 'Pérez', dni: '12345678', licencia: 'LIC001', telefono: '987654321', email: 'juan.perez@skt.com', fechaContratacion: new Date('2024-01-15'), estado: 'DISPONIBLE', tipoMaquinariaAsignada: 'CAMION', activo: true, createdAt: new Date(), updatedAt: new Date()})"

REM Chofer 2: María García - EXCAVADORA (Pesada)
docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'María', apellido: 'García', dni: '87654321', licencia: 'LIC002', telefono: '987654322', email: 'maria.garcia@skt.com', fechaContratacion: new Date('2024-02-01'), estado: 'ASIGNADO', tipoMaquinariaAsignada: 'EXCAVADORA', activo: true, createdAt: new Date(), updatedAt: new Date()})"

REM Chofer 3: Carlos López - VOLQUETE (Liviana)
docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'Carlos', apellido: 'López', dni: '11223344', licencia: 'LIC003', telefono: '987654323', email: 'carlos.lopez@skt.com', fechaContratacion: new Date('2024-01-20'), estado: 'EN_RUTA', tipoMaquinariaAsignada: 'VOLQUETE', activo: true, createdAt: new Date(), updatedAt: new Date()})"

REM Chofer 4: Ana Martínez - CARGADOR (Pesada)
docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'Ana', apellido: 'Martínez', dni: '55667788', licencia: 'LIC004', telefono: '987654324', email: 'ana.martinez@skt.com', fechaContratacion: new Date('2024-03-01'), estado: 'DISPONIBLE', tipoMaquinariaAsignada: 'CARGADOR', activo: true, createdAt: new Date(), updatedAt: new Date()})"

REM Chofer 5: Pedro Sánchez - GRUA (Pesada)
docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'Pedro', apellido: 'Sánchez', dni: '99887766', licencia: 'LIC005', telefono: '987654325', email: 'pedro.sanchez@skt.com', fechaContratacion: new Date('2023-12-15'), estado: 'DISPONIBLE', tipoMaquinariaAsignada: 'GRUA', activo: true, createdAt: new Date(), updatedAt: new Date()})"

REM Chofer 6: Luis Ramírez - CAMION (Liviana) - Disponible
docker exec mongodb-local mongosh drivers_db --eval "db.drivers.insertOne({nombre: 'Luis', apellido: 'Ramírez', dni: '44332211', licencia: 'LIC006', telefono: '987654326', email: 'luis.ramirez@skt.com', fechaContratacion: new Date('2024-01-10'), estado: 'DISPONIBLE', tipoMaquinariaAsignada: 'CAMION', activo: true, createdAt: new Date(), updatedAt: new Date()})"

echo.
echo ===============================================
echo   DATOS DE DRIVERS AGREGADOS EXITOSAMENTE!
echo ===============================================
echo.
echo Choferes insertados: 6
echo Tipos de maquinaria: CAMION (2), EXCAVADORA (1), VOLQUETE (1), CARGADOR (1), GRUA (1)
echo Estados utilizados: DISPONIBLE (4), ASIGNADO (1), EN_RUTA (1)
echo.
echo Ahora prueba las APIs:
echo - GET http://localhost:8081/api/v1/drivers
echo - GET http://localhost:8081/api/v1/drivers/all
echo - Frontend: http://localhost:8081/drivers.html
echo.
