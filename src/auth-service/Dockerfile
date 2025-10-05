# Dockerfile para el servicio de autenticación SKT
FROM openjdk:17-jdk-slim

# Información del mantenedor
LABEL maintainer="Sistema SKT <sistema@skt.com>"
LABEL description="Servicio de autenticación y autorización para el sistema de control de combustible SKT"

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY src/auth-service/pom.xml src/auth-service/

# Copiar código fuente
COPY src/auth-service/src src/auth-service/src
COPY src/shared-lib src/shared-lib

# Instalar Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Compilar la aplicación
RUN mvn -f src/auth-service/pom.xml clean package -DskipTests

# Exponer puerto
EXPOSE 8085

# Variables de entorno por defecto
ENV MONGO_USERNAME=admin
ENV MONGO_PASSWORD=password
ENV JWT_SECRET=mySecretKey1234567890123456789012345
ENV JWT_EXPIRATION=86400000

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "src/auth-service/target/auth-service-1.0.0.jar"]
