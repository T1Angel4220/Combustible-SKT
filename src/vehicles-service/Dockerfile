# Usando Maven para compilar
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar archivos de configuración
COPY pom.xml .
COPY src/vehicles-service/pom.xml ./src/vehicles-service/pom.xml
COPY src/shared-lib ./src/shared-lib
COPY src/protos ./src/protos

# Copiar código fuente
COPY src/vehicles-service ./src/vehicles-service

# Instalar proyecto padre
RUN mvn clean install -N -DskipTests

# Compilar dependencias
WORKDIR /app/src/shared-lib
RUN mvn clean install -DskipTests

WORKDIR /app/src/protos
RUN mvn clean install -DskipTests

# Compilar vehicles-service - Usar package como auth-service
WORKDIR /app/src/vehicles-service
RUN mvn clean package -DskipTests

# Imagen de runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar JAR compilado
COPY --from=build /app/src/vehicles-service/target/vehicles-service-1.0.0.jar app.jar

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=render

# Exponer el puerto
EXPOSE 8082

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=render"]
