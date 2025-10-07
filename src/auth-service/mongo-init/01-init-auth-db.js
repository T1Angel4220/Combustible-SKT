// Script de inicialización para MongoDB del servicio de autenticación
// Este script se ejecuta automáticamente cuando se crea el contenedor de MongoDB

// Crear la base de datos de autenticación
db = db.getSiblingDB('auth_db');

// Crear usuario para la aplicación
db.createUser({
  user: 'auth_user',
  pwd: 'auth_password',
  roles: [
    {
      role: 'readWrite',
      db: 'auth_db'
    }
  ]
});

// Crear colección de usuarios
db.createCollection('usuarios');

// Crear índices para optimizar las consultas
db.usuarios.createIndex({ "username": 1 }, { unique: true });
db.usuarios.createIndex({ "email": 1 }, { unique: true });
db.usuarios.createIndex({ "rol": 1 });
db.usuarios.createIndex({ "activo": 1 });

// Insertar datos de ejemplo
db.usuarios.insertMany([
  {
    username: "admin",
    email: "admin@skt.com",
    password: "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi", // password: password
    nombre: "Administrador",
    apellido: "Sistema",
    rol: "ADMIN",
    activo: true,
    fecha_creacion: new Date(),
    ultimo_acceso: new Date()
  },
  {
    username: "supervisor",
    email: "supervisor@skt.com",
    password: "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi", // password: password
    nombre: "Supervisor",
    apellido: "General",
    rol: "SUPERVISOR",
    activo: true,
    fecha_creacion: new Date(),
    ultimo_acceso: new Date()
  },
  {
    username: "operador",
    email: "operador@skt.com",
    password: "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi", // password: password
    nombre: "Operador",
    apellido: "Campo",
    rol: "OPERADOR",
    activo: true,
    fecha_creacion: new Date(),
    ultimo_acceso: new Date()
  }
]);

print('Base de datos de autenticación inicializada correctamente');
print('Usuario creado: auth_user');
print('Colección usuarios creada con índices');
print('Datos de ejemplo insertados');
