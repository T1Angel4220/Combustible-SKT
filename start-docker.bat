@echo off
echo 🚀 Iniciando Sistema de Control de Combustible con Docker...

REM Detener contenedores existentes
echo 🛑 Deteniendo contenedores existentes...
docker-compose down

REM Construir y levantar los servicios
echo 🔨 Construyendo y levantando servicios...
docker-compose up --build -d

REM Esperar a que los servicios estén listos
echo ⏳ Esperando a que los servicios estén listos...
timeout /t 30 /nobreak

REM Verificar estado de los servicios
echo 📊 Verificando estado de los servicios...
docker-compose ps

REM Mostrar logs del drivers-service
echo 📋 Mostrando logs del drivers-service...
docker-compose logs -f drivers-service
