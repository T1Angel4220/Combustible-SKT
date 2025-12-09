# Dockerfile para Routes Service
FROM maven:3.9-eclipse-temurin-17 AS build

# Información del mantenedor
LABEL maintainer="Sistema SKT"
LABEL description="Routes Service - Microservicio para gestión de rutas"

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven primero (para mejor caché)
COPY pom.xml ./pom.xml
COPY src/routes-service/pom.xml ./src/routes-service/pom.xml

# Copiar dependencias compartidas
COPY src/shared-lib ./src/shared-lib
COPY src/protos ./src/protos

# Copiar código fuente del routes-service
COPY src/routes-service ./src/routes-service

# Instalar el proyecto padre en el repositorio local
RUN mvn clean install -N -DskipTests

# Compilar dependencias primero
WORKDIR /app/src/shared-lib
RUN mvn clean install -DskipTests

# Compilar protos
WORKDIR /app/src/protos
RUN mvn clean install -DskipTests

# Compilar el proyecto
WORKDIR /app/src/routes-service
RUN mvn clean package -DskipTests

# Imagen de runtime - Usando Eclipse Temurin (OpenJDK oficial)
FROM eclipse-temurin:17-jre-alpine

# Crear directorio de trabajo
WORKDIR /app

# Copiar JAR compilado
COPY --from=build /app/src/routes-service/target/routes-service-1.0.0.jar app.jar

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=render

# Exponer puerto
EXPOSE 8083

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=render"]

