#!/bin/bash

# Script para iniciar el sistema con Docker
echo "🚀 Iniciando Sistema de Control de Combustible con Docker..."

# Detener contenedores existentes
echo "🛑 Deteniendo contenedores existentes..."
docker-compose down

# Construir y levantar los servicios
echo "🔨 Construyendo y levantando servicios..."
docker-compose up --build -d

# Esperar a que los servicios estén listos
echo "⏳ Esperando a que los servicios estén listos..."
sleep 30

# Verificar estado de los servicios
echo "📊 Verificando estado de los servicios..."
docker-compose ps

# Mostrar logs del drivers-service
echo "📋 Mostrando logs del drivers-service..."
docker-compose logs -f drivers-service
