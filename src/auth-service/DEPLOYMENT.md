# Guía de Despliegue - Servicio de Autenticación SKT

## Requisitos Previos

- Java 17 o superior
- Maven 3.6 o superior
- MongoDB 4.4 o superior
- Docker (opcional)

## Opción 1: Despliegue Local

### 1. Configurar MongoDB

```bash
# Instalar MongoDB (Ubuntu/Debian)
sudo apt-get install mongodb

# O usar Docker
docker run -d --name mongodb \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=password \
  mongo:7.0
```

### 2. Configurar Variables de Entorno

```bash
export MONGO_USERNAME=admin
export MONGO_PASSWORD=password
export JWT_SECRET=mySecretKey1234567890123456789012345
export JWT_EXPIRATION=86400000
```

### 3. Compilar y Ejecutar

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar el servicio
mvn spring-boot:run -pl src/auth-service

# O ejecutar el JAR
cd src/auth-service
mvn clean package
java -jar target/auth-service-1.0.0.jar
```

## Opción 2: Despliegue con Docker

### 1. Construir la Imagen

```bash
# Desde la raíz del proyecto
docker build -f src/auth-service/Dockerfile -t auth-service .
```

### 2. Ejecutar con Docker Compose

```bash
cd src/auth-service
docker-compose up -d
```

### 3. Verificar el Despliegue

```bash
# Verificar que los contenedores estén ejecutándose
docker-compose ps

# Ver logs del servicio
docker-compose logs -f auth-service

# Verificar salud del servicio
curl http://localhost:8085/actuator/health
```

## Opción 3: Despliegue en Kubernetes

### 1. Crear ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: auth-service-config
data:
  MONGO_USERNAME: "admin"
  MONGO_PASSWORD: "password"
  JWT_SECRET: "mySecretKey1234567890123456789012345"
  JWT_EXPIRATION: "86400000"
```

### 2. Crear Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: auth-service-secrets
type: Opaque
data:
  mongo-password: cGFzc3dvcmQ=  # base64 encoded
  jwt-secret: bXlTZWNyZXRLZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1  # base64 encoded
```

### 3. Crear Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: auth-service:latest
        ports:
        - containerPort: 8085
        env:
        - name: MONGO_USERNAME
          valueFrom:
            configMapKeyRef:
              name: auth-service-config
              key: MONGO_USERNAME
        - name: MONGO_PASSWORD
          valueFrom:
            secretKeyRef:
              name: auth-service-secrets
              key: mongo-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: auth-service-secrets
              key: jwt-secret
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8085
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8085
          initialDelaySeconds: 30
          periodSeconds: 10
```

## Verificación del Despliegue

### 1. Health Check

```bash
curl http://localhost:8085/actuator/health
```

### 2. Probar Login

```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'
```

### 3. Ejecutar Script de Pruebas

```bash
# En Linux/Mac
./src/auth-service/test-auth-service.sh

# En Windows
bash src/auth-service/test-auth-service.sh
```

## Configuración de Producción

### Variables de Entorno Recomendadas

```bash
# Base de datos
MONGO_USERNAME=auth_user
MONGO_PASSWORD=password_segura_y_larga
MONGO_HOST=mongodb-cluster.internal
MONGO_PORT=27017
MONGO_DATABASE=auth_db

# JWT
JWT_SECRET=clave_secreta_muy_larga_y_compleja_para_produccion
JWT_EXPIRATION=3600000  # 1 hora

# Logging
LOG_LEVEL=INFO
SPRING_PROFILES_ACTIVE=prod
```

### Configuración de Seguridad

1. **Cambiar contraseñas por defecto**
2. **Usar JWT secrets complejos**
3. **Configurar HTTPS en producción**
4. **Implementar rate limiting**
5. **Configurar logs de auditoría**

## Monitoreo

### Métricas Disponibles

- `/actuator/health` - Estado del servicio
- `/actuator/info` - Información del servicio
- `/actuator/metrics` - Métricas detalladas

### Logs Importantes

```bash
# Ver logs en tiempo real
docker-compose logs -f auth-service

# Filtrar logs de autenticación
docker-compose logs auth-service | grep "AUTH"
```

## Troubleshooting

### Problemas Comunes

1. **Error de conexión a MongoDB**
   - Verificar que MongoDB esté ejecutándose
   - Verificar credenciales y URL de conexión

2. **Token JWT inválido**
   - Verificar que JWT_SECRET sea el mismo en todos los servicios
   - Verificar que el token no haya expirado

3. **Puerto ocupado**
   - Cambiar el puerto en application.yml
   - Verificar que no haya otros servicios usando el puerto 8085

### Comandos de Diagnóstico

```bash
# Verificar conectividad a MongoDB
mongo mongodb://admin:password@localhost:27017/auth_db

# Verificar logs del servicio
tail -f logs/auth-service.log

# Probar endpoints
curl -v http://localhost:8085/actuator/health
```

## Escalabilidad

### Escalado Horizontal

```bash
# Con Docker Compose
docker-compose up -d --scale auth-service=3

# Con Kubernetes
kubectl scale deployment auth-service --replicas=3
```

### Load Balancer

```yaml
apiVersion: v1
kind: Service
metadata:
  name: auth-service-lb
spec:
  selector:
    app: auth-service
  ports:
  - port: 80
    targetPort: 8085
  type: LoadBalancer
```
