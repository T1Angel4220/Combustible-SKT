@echo off
echo ========================================
echo   Iniciando Vehicles Service
echo ========================================

echo.
echo Deteniendo contenedores existentes...
docker-compose -f docker-compose-mongodb.yml down

echo.
echo Eliminando volúmenes anteriores (opcional)...
docker volume rm combustible-skt_mongodb_data 2>nul

echo.
echo Iniciando MongoDB y Vehicles Service...
docker-compose -f docker-compose-mongodb.yml up -d mongodb vehicles-service

echo.
echo Esperando a que los servicios estén listos...
timeout /t 15 /nobreak >nul

echo.
echo Verificando estado de los servicios...
docker-compose -f docker-compose-mongodb.yml ps

echo.
echo ========================================
echo   Vehicles Service iniciado correctamente
echo ========================================
echo.
echo Servicios disponibles:
echo   MongoDB:
echo     Host: localhost
echo     Puerto: 27017
echo     Usuario: admin
echo     Contraseña: admin123
echo     Base de datos: vehicles_db
echo.
echo   Vehicles Service:
echo     REST API: http://localhost:8082
echo     gRPC: localhost:9092
echo     Health Check: http://localhost:8082/actuator/health
echo.
echo Para ver los logs del Vehicles Service:
echo   docker-compose -f docker-compose-mongodb.yml logs -f vehicles-service
echo.
echo Para ver los logs de MongoDB:
echo   docker-compose -f docker-compose-mongodb.yml logs -f mongodb
echo.
echo Para detener todos los servicios:
echo   docker-compose -f docker-compose-mongodb.yml down
echo.
echo Para reiniciar solo el Vehicles Service:
echo   docker-compose -f docker-compose-mongodb.yml restart vehicles-service
echo.
pause
