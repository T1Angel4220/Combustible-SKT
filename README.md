# Sistema Distribuido de Control de Combustible - SKT

Sistema distribuido basado en microservicios para la gestión de combustible de maquinaria liviana y pesada.

## 🏗️ Arquitectura

### Estilo Arquitectónico
- **Microservicios**: Servicios independientes desplegables y escalables
- **Comunicación**: gRPC entre microservicios (más eficiente que REST)
- **Bases de datos**: Desacopladas por servicio
- **API Gateway**: Exposición REST para servicios externos

### Servicios
1. **Drivers Service** (Puerto: 8081, gRPC: 9091) - Gestión de choferes
2. **Vehicles Service** (Puerto: 8082, gRPC: 9092) - Gestión de vehículos
3. **Routes Service** (Puerto: 8083, gRPC: 9093) - Gestión de rutas
4. **Fuel Service** (Puerto: 8084, gRPC: 9094) - Gestión de combustible
5. **Auth Service** (Puerto: 8085, gRPC: 9095) - Autenticación y autorización
6. **Gateway Service** (Puerto: 8080) - API Gateway

## 🚀 Stack Tecnológico

- **Backend**: Spring Boot 3.2.0
- **Base de Datos**: PostgreSQL 15
- **Comunicación**: gRPC + Protocol Buffers
- **Contenedores**: Docker
- **Java**: JDK 17
- **Autenticación**: JWT
- **Roles**: Admin, Operador, Supervisor

## 📦 Estructura del Proyecto

```
├── src/
│   ├── drivers-service/     # Servicio de Choferes
│   ├── vehicles-service/    # Servicio de Vehículos  
│   ├── routes-service/      # Servicio de Rutas
│   ├── fuel-service/        # Servicio de Combustible
│   ├── auth-service/         # Servicio de Autenticación
│   ├── gateway-service/     # API Gateway
│   ├── shared-lib/          # Librería compartida
│   └── protos/             # Definiciones Protocol Buffers
├── docker-compose.yml      # Configuración Docker
└── pom.xml                # Maven Parent POM
```

## 🛠️ Instalación y Ejecución

### Prerrequisitos
- JDK 17+
- Maven 3.8+
- Docker y Docker Compose
- PostgreSQL 15+ (si ejecutas fuera de Docker)

### Ejecución con Docker Compose

```bash
# Construir y ejecutar todos los servicios
docker-compose up --build

# Ejecutar en background
docker-compose up -d --build

# Detener servicios
docker-compose down
```

### Ejecución Local (Desarrollo)

1. **Ejecutar las bases de datos**
```bash
docker-compose up drivers-db vehicles-db routes-db fuel-db auth-db
```

2. **Compilar el proyecto padre**
```bash
mvn clean install
```

3. **Ejecutar cada microservicio individivamente**
```bash
# Terminal 1
mvn spring-boot:run -pl drivers-service

# Terminal 2  
mvn spring-boot:run -pl vehicles-service

# Terminal 3
mvn spring-boot:run -pl routes-service

# Terminal 4
mvn spring-boot:run -pl fuel-service

# Terminal 5
mvn spring-boot:run -pl auth-service

# Terminal 6
mvn spring-boot:run -pl gateway-service
```

## 🔧 Configuración

### Variables de Entorno
- `DB_USERNAME`: Usuario de PostgreSQL
- `DB_PASSWORD`: Contraseña de PostgreSQL  
- `JWT_SECRET`: Clave secreta para JWT (Auth Service)

### Puertos de Servicios
- **Gateway**: http://localhost:8080
- **Drivers Service**: http://localhost:8081 / gRPC:9091
- **Vehicles Service**: http://localhost:8082 / gRPC:9092
- **Routes Service**: http://localhost:8083 / gRPC:9093
- **Fuel Service**: http://localhost:8084 / gRPC:9094
- **Auth Service**: http://localhost:8085 / gRPC:9095

## 🗄️ Bases de Datos

Cada servicio tiene su propia base de datos PostgreSQL:
- `drivers_db` - Base de datos del servicio de choferes
- `vehicles_db` - Base de datos del servicio de vehículos
- `routes_db` - Base de datos del servicio de rutas
- `fuel_db` - Base de datos del servicio de combustible
- `auth_db` - Base de datos del servicio de autenticación

## 📋 Funcionalidades por Servicio

### Drivers Service
- Registro de choferes
- Consulta de disponibilidad
- Asignación por tipo de maquinaria

### Vehicles Service
- Clasificación: liviano o pesado
- Estado operativo del vehículo
- Asociación con choferes y rutas

### Routes Service
- Definición de rutas con distancias
- Asociación con vehículos y choferes
- Cálculo de consumo estimado

### Fuel Service
- Registro de consumo real por ruta
- Reportes por tipo de maquinaria
- Comparación estimado vs real

### Auth Service
- Autenticación con JWT
- Gestión de roles (Admin, Operador, Supervisor)
- Autorización por endpoints

## 🔐 Seguridad

- **Autenticación**: JWT (JSON Web Tokens)
- **Autorización**: Por endpoints usando interceptores gRPC
- **Roles**: Admin, Operador, Supervisor

## 📊 Tipos de Maquinaria

```java
public enum TipoMaquinaria {
    LIVIANA,  // Maquinaria liviana
    PESADA    // Maquinaria pesada
}
```

## 🏭 Capas de Cada Microservicio

Cada microservicio sigue la arquitectura de capas:
1- **Controllers**: Controllers gRPC
2- **Application**: Lógica de negocio
3- **Domain**: Entidades e interfaces
4- **Infrastructure**: Acceso a datos, clientes gRPC
5- **Persistence**: Base de datos

## 📈 Monitoreo

- **Health Checks**: Disponible en `/actuator/health`
- **Métricas**: Disponible en `/actuator/metrics`
- **Información**: Disponible en `/actuator/info`

## 📚 Documentación API

Una vez ejecutando el proyecto, la documentación estará disponible en:
- **Gateway**: http://localhost:8080/actuator/info
- **Swagger**: Integración futura con Swagger/OpenAPI

---

**Desarrollado por**: Sistema SKT  
**Versión**: 1.0.0  
**Fecha**: 2024
