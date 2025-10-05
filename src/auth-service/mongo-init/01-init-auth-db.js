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

print('Base de datos de autenticación inicializada correctamente');
print('Usuario creado: auth_user');
print('Colección usuarios creada con índices');
