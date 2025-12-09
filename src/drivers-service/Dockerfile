# Dockerfile para Drivers Service
FROM maven:latest AS build

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

# Instalar el proyecto padre en el repositorio local
RUN mvn clean install -N -DskipTests

# Compilar dependencias primero
WORKDIR /app/src/shared-lib
RUN mvn clean install -DskipTests

# Compilar protos
WORKDIR /app/src/protos
RUN mvn clean install -DskipTests

# Compilar el proyecto
WORKDIR /app/src/drivers-service
RUN mvn clean package -DskipTests

# Imagen de runtime - Usando Eclipse Temurin (OpenJDK oficial)
FROM eclipse-temurin:17-jdk-alpine

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



# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=render

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=render"]
