FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar el archivo pom.xml
COPY pom.xml .

# Copiar el código fuente
COPY src ./src

# Instalar Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Exponer el puerto
EXPOSE 8090

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=render

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/gateway-service-1.0.0.jar", "--spring.profiles.active=render"]
