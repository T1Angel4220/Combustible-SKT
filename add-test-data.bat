@echo off
echo Agregando datos de prueba a MongoDB...

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
  }
])
"

echo Datos agregados! Ahora prueba la API nuevamente.
echo GET http://localhost:8082/api/v1/vehicles

pause
