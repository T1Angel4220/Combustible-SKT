// Script de inicialización para MongoDB del servicio de vehículos
// Este script se ejecuta automáticamente cuando se crea el contenedor de MongoDB

// Crear la base de datos de vehículos
db = db.getSiblingDB('vehicles_db');

// Crear usuario para la aplicación
db.createUser({
  user: 'vehicles_user',
  pwd: 'vehicles_pass',
  roles: [
    {
      role: 'readWrite',
      db: 'vehicles_db'
    }
  ]
});

// Crear colección de vehículos
db.createCollection('vehicles');

// Crear índices para optimizar las consultas
db.vehicles.createIndex({ "placa": 1 }, { unique: true });
db.vehicles.createIndex({ "numero_serie": 1 }, { unique: true });
db.vehicles.createIndex({ "tipo": 1 });
db.vehicles.createIndex({ "estado": 1 });
db.vehicles.createIndex({ "activo": 1 });
db.vehicles.createIndex({ "marca": 1, "modelo": 1 });

// Insertar datos de ejemplo
db.vehicles.insertMany([
  {
    placa: "ABC-123",
    numero_serie: "VH001",
    marca: "Volvo",
    modelo: "FH16",
    tipo: "CAMION",
    año: 2023,
    capacidad_carga: 25000,
    estado: "DISPONIBLE",
    kilometraje: 15000,
    fecha_adquisicion: new Date("2023-01-15"),
    activo: true,
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    placa: "DEF-456",
    numero_serie: "VH002",
    marca: "Caterpillar",
    modelo: "320D",
    tipo: "EXCAVADORA",
    año: 2022,
    capacidad_carga: 20000,
    estado: "EN_MANTENIMIENTO",
    kilometraje: 25000,
    fecha_adquisicion: new Date("2022-08-20"),
    activo: true,
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    placa: "GHI-789",
    numero_serie: "VH003",
    marca: "Mercedes",
    modelo: "Actros",
    tipo: "VOLQUETE",
    año: 2023,
    capacidad_carga: 30000,
    estado: "DISPONIBLE",
    kilometraje: 8000,
    fecha_adquisicion: new Date("2023-03-10"),
    activo: true,
    created_at: new Date(),
    updated_at: new Date()
  }
]);

print('Base de datos de vehículos inicializada correctamente');
print('Usuario creado: vehicles_user');
print('Colección vehicles creada con índices');
print('Datos de ejemplo insertados');

