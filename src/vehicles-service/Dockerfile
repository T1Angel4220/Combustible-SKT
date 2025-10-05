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
EXPOSE 8082 9092

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/vehicles-service-1.0.0.jar"]
