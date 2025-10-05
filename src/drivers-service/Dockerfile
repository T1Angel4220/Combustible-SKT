# Dockerfile para Drivers Service
FROM maven:3.9.4-openjdk-17-slim AS build

# Información del mantenedor
LABEL maintainer="Sistema SKT"
LABEL description="Drivers Service - Microservicio para gestión de choferes"

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven primero (para mejor caché)
COPY pom.xml ./pom.xml
COPY src/drivers-service/pom.xml ./src/drivers-service/pom.xml

# Copiar dependencias compartidas
COPY src/shared-lib ./src/shared-lib
COPY src/protos ./src/protos

# Copiar código fuente del drivers-service
COPY src/drivers-service ./src/drivers-service

# Compilar el proyecto
WORKDIR /app/src/drivers-service
RUN mvn clean package -DskipTests

# Imagen de runtime
FROM openjdk:17-jdk-slim

# Crear usuario no-root
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Crear directorio de trabajo
WORKDIR /app

# Copiar JAR compilado
COPY --from=build /app/src/drivers-service/target/drivers-service-1.0.0.jar app.jar

# Cambiar propietario del archivo
RUN chown spring:spring app.jar

# Cambiar a usuario no-root
USER spring:spring

# Exponer puertos
EXPOSE 8081 9091

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=docker
ENV DB_HOST=postgres-db
ENV DB_PORT=5432
ENV DB_NAME=drivers_db
ENV DB_USERNAME=drivers_user
ENV DB_PASSWORD=drivers_pass

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
