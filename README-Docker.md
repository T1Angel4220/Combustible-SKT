# 🐳 Configuración Docker para Drivers Service

## 📋 Requisitos
- Docker Desktop instalado y ejecutándose
- Docker Compose v3.8+

## 🚀 Inicio Rápido

### Opción 1: Script Automático (Windows)
```bash
start-docker.bat
```

### Opción 2: Script Automático (Linux/Mac)
```bash
chmod +x start-docker.sh
./start-docker.sh
```

### Opción 3: Comandos Manuales
```bash
# Construir y levantar servicios
docker-compose up --build -d

# Ver logs
docker-compose logs -f drivers-service

# Verificar estado
docker-compose ps
```

## 🔧 Servicios Incluidos

### 📊 PostgreSQL Database
- **Puerto**: 5432
- **Base de datos**: `drivers_db`
- **Usuario**: `drivers_user`
- **Contraseña**: `drivers_pass`
- **Host**: `postgres-db` (interno) / `localhost` (externo)

### 🚗 Drivers Service
- **REST API**: http://localhost:8081
- **gRPC**: localhost:9091
- **Health Check**: http://localhost:8081/actuator/health

## 📡 Endpoints Disponibles

### REST API (Puerto 8081)
- `POST /api/v1/drivers` - Crear chofer
- `GET /api/v1/drivers/{id}` - Obtener chofer por ID
- `GET /api/v1/drivers/available` - Choferes disponibles
- `PUT /api/v1/drivers/{id}` - Actualizar chofer
- `DELETE /api/v1/drivers/{id}` - Desactivar chofer

### gRPC (Puerto 9091)
- `CreateDriver` - Crear chofer
- `GetDriverById` - Obtener por ID
- `GetAvailableDrivers` - Disponibles
- `UpdateDriver` - Actualizar

## 🛠️ Comandos Útiles

### Ver logs
```bash
# Todos los servicios
docker-compose logs -f

# Solo drivers-service
docker-compose logs -f drivers-service

# Solo base de datos
docker-compose logs -f postgres-db
```

### Acceder a la base de datos
```bash
# Conectar a PostgreSQL
docker-compose exec postgres-db psql -U drivers_user -d drivers_db

# Ejecutar consultas SQL
docker-compose exec postgres-db psql -U drivers_user -d drivers_db -c "SELECT * FROM common_schema.choferes;"
```

### Reiniciar servicios
```bash
# Reiniciar drivers-service
docker-compose restart drivers-service

# Reiniciar todo
docker-compose restart
```

### Detener servicios
```bash
# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v
```

## 🔍 Troubleshooting

### Error de conexión a base de datos
1. Verificar que PostgreSQL esté corriendo:
   ```bash
   docker-compose ps
   ```
2. Verificar logs de PostgreSQL:
   ```bash
   docker-compose logs postgres-db
   ```

### Error de compilación
1. Limpiar caché de Docker:
   ```bash
   docker system prune -f
   ```
2. Reconstruir sin caché:
   ```bash
   docker-compose build --no-cache
   ```

### Puerto ocupado
Si el puerto 8081 está ocupado, cambiar en `docker-compose.yml`:
```yaml
ports:
  - "8082:8081"  # Cambiar puerto externo
```

## 📊 Monitoreo

### Health Checks
- **Drivers Service**: http://localhost:8081/actuator/health
- **Base de datos**: Verificado automáticamente por Docker

### Métricas
- **Actuator**: http://localhost:8081/actuator/metrics
- **Info**: http://localhost:8081/actuator/info

## 🔐 Variables de Entorno

Las siguientes variables pueden ser configuradas:

```bash
# Base de datos
DB_HOST=postgres-db
DB_PORT=5432
DB_NAME=drivers_db
DB_USERNAME=drivers_user
DB_PASSWORD=drivers_pass

# Aplicación
SPRING_PROFILES_ACTIVE=docker
```

## 📁 Estructura de Archivos

```
├── docker-compose.yml          # Configuración de servicios
├── init-db.sql                # Script de inicialización de BD
├── start-docker.sh            # Script de inicio (Linux/Mac)
├── start-docker.bat           # Script de inicio (Windows)
├── src/drivers-service/
│   ├── Dockerfile             # Imagen del servicio
│   └── src/main/resources/
│       └── application-docker.yml  # Configuración para Docker
└── bd.sql                     # Schema de base de datos
```
