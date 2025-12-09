# Dockerfile para Auth Service
FROM maven:latest AS build

# Información del mantenedor
LABEL maintainer="Sistema SKT"
LABEL description="Auth Service - Microservicio para autenticación y autorización"

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven primero (para mejor caché)
COPY pom.xml ./pom.xml
COPY src/auth-service/pom.xml ./src/auth-service/pom.xml

# Copiar dependencias compartidas
COPY src/shared-lib ./src/shared-lib
COPY src/protos ./src/protos

# Copiar código fuente del auth-service
COPY src/auth-service ./src/auth-service

# Instalar el proyecto padre en el repositorio local
RUN mvn clean install -N -DskipTests

# Compilar dependencias primero
WORKDIR /app/src/shared-lib
RUN mvn clean install -DskipTests

# Compilar protos
WORKDIR /app/src/protos
RUN mvn clean install -DskipTests

# Compilar el proyecto
WORKDIR /app/src/auth-service
RUN mvn clean package -DskipTests

# Imagen de runtime
FROM openjdk:17-jdk-slim

# Crear usuario no-root
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Crear directorio de trabajo
WORKDIR /app

# Copiar JAR compilado
COPY --from=build /app/src/auth-service/target/auth-service-1.0.0.jar app.jar

# Cambiar propietario del archivo
RUN chown spring:spring app.jar

# Cambiar a usuario no-root
USER spring:spring

# Exponer puerto
EXPOSE 8085

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=render

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=render"]
