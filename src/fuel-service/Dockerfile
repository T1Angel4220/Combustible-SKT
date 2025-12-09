# Dockerfile para Fuel Service
FROM maven:3.9-eclipse-temurin-17 AS build

# Información del mantenedor
LABEL maintainer="Sistema SKT"
LABEL description="Fuel Service - Microservicio para gestión de combustible"

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven primero (para mejor caché)
COPY pom.xml ./pom.xml
COPY src/fuel-service/pom.xml ./src/fuel-service/pom.xml

# Copiar dependencias compartidas
COPY src/shared-lib ./src/shared-lib
COPY src/protos ./src/protos

# Copiar código fuente del fuel-service
COPY src/fuel-service ./src/fuel-service

# Instalar el proyecto padre en el repositorio local
RUN mvn clean install -N -DskipTests

# Compilar dependencias primero
WORKDIR /app/src/shared-lib
RUN mvn clean install -DskipTests

# Compilar protos
WORKDIR /app/src/protos
RUN mvn clean install -DskipTests

# Compilar el proyecto
WORKDIR /app/src/fuel-service
RUN mvn clean package -DskipTests

# Imagen de runtime - Usando Eclipse Temurin (OpenJDK oficial)
FROM eclipse-temurin:17-jre-alpine

# Crear directorio de trabajo
WORKDIR /app

# Copiar JAR compilado
COPY --from=build /app/src/fuel-service/target/fuel-service-1.0.0.jar app.jar

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=render

# Exponer puerto
EXPOSE 8084

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=render"]

