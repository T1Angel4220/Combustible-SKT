# Servicio de Autenticación - SKT

Este servicio maneja la autenticación y autorización del sistema distribuido de control de combustible de la empresa SKT.

## Características

- **Autenticación JWT**: Tokens seguros con expiración configurable
- **Roles de Usuario**: Admin, Operador, Supervisor
- **Base de Datos**: MongoDB para persistencia
- **Seguridad**: Contraseñas encriptadas con BCrypt
- **Endpoints REST**: API completa para autenticación

## Tecnologías Utilizadas

- Spring Boot 3.2.0
- Spring Security
- Spring Data MongoDB
- JWT (JSON Web Tokens)
- BCrypt para encriptación de contraseñas

## Configuración

### Variables de Entorno

```yaml
# MongoDB
MONGO_USERNAME=admin
MONGO_PASSWORD=password

# JWT
JWT_SECRET=mySecretKey1234567890123456789012345
JWT_EXPIRATION=86400000  # 24 horas en milisegundos
```

### Puerto

El servicio se ejecuta en el puerto **8085**.

## Endpoints Disponibles

### Autenticación Pública

#### POST `/api/auth/login`
Autentica un usuario y devuelve un token JWT.

**Request:**
```json
{
  "usernameOrEmail": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tipoToken": "Bearer",
  "username": "admin",
  "email": "admin@skt.com",
  "nombre": "Administrador",
  "apellido": "Sistema",
  "rol": "ADMIN",
  "permisos": ["USUARIOS_CREAR", "USUARIOS_LEER", ...],
  "expiracion": "2024-01-02T10:30:00"
}
```

#### POST `/api/auth/register`
Registra un nuevo usuario en el sistema.

**Request:**
```json
{
  "username": "nuevo_usuario",
  "email": "usuario@skt.com",
  "password": "password123",
  "nombre": "Nombre",
  "apellido": "Apellido",
  "rol": "OPERADOR",
  "permisos": ["VEHICULOS_LEER"]
}
```

#### POST `/api/auth/validate`
Valida un token JWT.

**Request:**
```
POST /api/auth/validate?token=eyJhbGciOiJIUzUxMiJ9...
```

### Endpoints Protegidos

#### GET `/api/auth/me`
Obtiene información del usuario autenticado.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

#### GET `/api/auth/has-role/{rol}`
Verifica si el usuario tiene un rol específico.

#### GET `/api/auth/has-permission/{permiso}`
Verifica si el usuario tiene un permiso específico.

### Gestión de Usuarios (Solo Administradores)

#### GET `/api/users`
Obtiene todos los usuarios del sistema.

#### GET `/api/users/{username}`
Obtiene un usuario específico por username.

#### POST `/api/users`
Crea un nuevo usuario.

#### GET `/api/users/stats`
Obtiene estadísticas de usuarios.

## Roles y Permisos

### Roles Disponibles

1. **ADMIN**: Acceso completo al sistema
2. **SUPERVISOR**: Acceso a operaciones y reportes
3. **OPERADOR**: Acceso limitado a operaciones básicas

### Permisos por Rol

#### ADMIN
- `USUARIOS_CREAR`, `USUARIOS_LEER`, `USUARIOS_ACTUALIZAR`, `USUARIOS_ELIMINAR`
- `VEHICULOS_CREAR`, `VEHICULOS_LEER`, `VEHICULOS_ACTUALIZAR`, `VEHICULOS_ELIMINAR`
- `RUTAS_CREAR`, `RUTAS_LEER`, `RUTAS_ACTUALIZAR`, `RUTAS_ELIMINAR`
- `COMBUSTIBLE_CREAR`, `COMBUSTIBLE_LEER`, `COMBUSTIBLE_ACTUALIZAR`, `COMBUSTIBLE_ELIMINAR`
- `REPORTES_GENERAR`, `REPORTES_EXPORTAR`

#### SUPERVISOR
- `VEHICULOS_LEER`, `VEHICULOS_ACTUALIZAR`
- `RUTAS_LEER`, `RUTAS_ACTUALIZAR`
- `COMBUSTIBLE_LEER`, `COMBUSTIBLE_ACTUALIZAR`
- `REPORTES_GENERAR`, `REPORTES_EXPORTAR`

#### OPERADOR
- `VEHICULOS_LEER`
- `RUTAS_LEER`
- `COMBUSTIBLE_CREAR`, `COMBUSTIBLE_LEER`

## Usuarios por Defecto

El sistema crea automáticamente los siguientes usuarios:

| Username | Password | Rol | Email |
|----------|----------|-----|-------|
| admin | admin123 | ADMIN | admin@skt.com |
| supervisor | supervisor123 | SUPERVISOR | supervisor@skt.com |
| operador | operador123 | OPERADOR | operador@skt.com |
| test | test123 | OPERADOR | test@skt.com |

## Uso del Token JWT

### Incluir en Requests

```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
     http://localhost:8085/api/auth/me
```

### Validar Token

```bash
curl -X POST "http://localhost:8085/api/auth/validate?token=eyJhbGciOiJIUzUxMiJ9..."
```

## Estructura del Proyecto

```
src/auth-service/
├── src/main/java/com/skt/combustible/auth/
│   ├── application/service/          # Servicios de aplicación
│   │   ├── AuthService.java
│   │   ├── JwtService.java
│   │   └── DataInitializationService.java
│   ├── domain/                      # Entidades y DTOs
│   │   ├── entity/Usuario.java
│   │   └── dto/
│   │       ├── AuthResponse.java
│   │       ├── LoginRequest.java
│   │       └── RegisterRequest.java
│   ├── infrastructure/              # Infraestructura
│   │   ├── config/SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── UserManagementController.java
│   │   ├── filter/JwtAuthenticationFilter.java
│   │   └── repository/UsuarioRepository.java
│   └── AuthServiceApplication.java
└── src/main/resources/
    └── application.yml
```

## Ejecución

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar el servicio
mvn spring-boot:run

# O ejecutar el JAR
java -jar target/auth-service-1.0.0.jar
```

## Docker

```bash
# Construir imagen
docker build -t auth-service .

# Ejecutar contenedor
docker run -p 8085:8085 \
  -e MONGO_USERNAME=admin \
  -e MONGO_PASSWORD=password \
  -e JWT_SECRET=mySecretKey1234567890123456789012345 \
  auth-service
```

## Monitoreo

El servicio incluye endpoints de Actuator para monitoreo:

- `/actuator/health` - Estado del servicio
- `/actuator/info` - Información del servicio
- `/actuator/metrics` - Métricas del servicio
