@echo off
echo Limpiando datos incorrectos y agregando datos correctos...

echo Limpiando coleccion vehicles...
docker exec mongodb-local mongosh vehicles_db --eval "db.vehicles.deleteMany({})"

echo Agregando datos de prueba con valores correctos del enum...
docker exec mongodb-local mongosh vehicles_db --eval "
db.vehicles.insertMany([
  {
    placa: 'ABC-123',
    marca: 'Toyota',
    modelo: 'Hilux',
    anio: 2023,
    tipoMaquinaria: 'CAMION',
    estadoOperativo: 'DISPONIBLE',
    capacidadTanque: 80,
    consumoPromedio: 12.5,
    kilometrajeActual: 15000,
    fechaCreacion: new Date(),
    fechaActualizacion: new Date(),
    activo: true
  },
  {
    placa: 'DEF-456',
    marca: 'Ford',
    modelo: 'Ranger',
    anio: 2022,
    tipoMaquinaria: 'CAMION',
    estadoOperativo: 'MANTENIMIENTO',
    capacidadTanque: 75,
    consumoPromedio: 11.8,
    kilometrajeActual: 25000,
    fechaCreacion: new Date(),
    fechaActualizacion: new Date(),
    activo: true
  },
  {
    placa: 'GHI-789',
    marca: 'Chevrolet',
    modelo: 'Silverado',
    anio: 2024,
    tipoMaquinaria: 'CAMION',
    estadoOperativo: 'EN_USO',
    capacidadTanque: 90,
    consumoPromedio: 13.2,
    kilometrajeActual: 5000,
    fechaCreacion: new Date(),
    fechaActualizacion: new Date(),
    activo: true
  }
])
"

echo Datos corregidos! Valores del enum EstadoOperativo:
echo - ACTIVO
echo - MANTENIMIENTO  
echo - FUERA_SERVICIO
echo - DISPONIBLE
echo - EN_USO
echo - RESERVADO

echo.
echo Ahora prueba la API: GET http://localhost:8082/api/v1/vehicles

pause
