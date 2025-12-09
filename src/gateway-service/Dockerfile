# Usando Maven para compilar
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar el archivo pom.xml
COPY pom.xml .

# Copiar el código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Imagen de runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar JAR compilado
COPY --from=build /app/src/gateway-service/target/gateway-service-1.0.0.jar app.jar

# Exponer el puerto
EXPOSE 8090

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=render

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=render"]
