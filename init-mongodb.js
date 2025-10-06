// Script de inicialización para MongoDB
// Este script se ejecuta al crear el contenedor por primera vez

// Crear base de datos para drivers service
db = db.getSiblingDB('drivers_db');

// Crear usuario para drivers service
db.createUser({
  user: 'drivers_user',
  pwd: 'drivers_pass',
  roles: [
    { role: 'readWrite', db: 'drivers_db' }
  ]
});

// Crear colecciones con índices
db.createCollection('drivers');

// Crear índices únicos para DNI y licencia
db.drivers.createIndex({ "dni": 1 }, { unique: true });
db.drivers.createIndex({ "licencia": 1 }, { unique: true });

// Crear índices para consultas frecuentes
db.drivers.createIndex({ "estado": 1 });
db.drivers.createIndex({ "tipo_maquinaria_asignada": 1 });
db.drivers.createIndex({ "activo": 1 });
db.drivers.createIndex({ "nombre": 1, "apellido": 1 });

// Insertar datos de ejemplo
db.drivers.insertMany([
  {
    nombre: "Juan",
    apellido: "Pérez",
    dni: "12345678",
    licencia: "LIC001",
    telefono: "+51987654321",
    email: "juan.perez@email.com",
    fecha_contratacion: new Date("2023-01-15"),
    estado: "ACTIVO",
    tipo_maquinaria_asignada: "CAMION",
    activo: true,
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    nombre: "María",
    apellido: "González",
    dni: "87654321",
    licencia: "LIC002",
    telefono: "+51987654322",
    email: "maria.gonzalez@email.com",
    fecha_contratacion: new Date("2023-02-20"),
    estado: "ACTIVO",
    tipo_maquinaria_asignada: "VOLQUETE",
    activo: true,
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    nombre: "Carlos",
    apellido: "Rodríguez",
    dni: "11223344",
    licencia: "LIC003",
    telefono: "+51987654323",
    email: "carlos.rodriguez@email.com",
    fecha_contratacion: new Date("2023-03-10"),
    estado: "ACTIVO",
    tipo_maquinaria_asignada: "EXCAVADORA",
    activo: true,
    created_at: new Date(),
    updated_at: new Date()
  }
]);

print("Base de datos drivers_db inicializada correctamente");
print("Usuario drivers_user creado");
print("Colección drivers creada con índices");
print("Datos de ejemplo insertados");
