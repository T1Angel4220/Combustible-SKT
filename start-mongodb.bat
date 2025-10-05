@echo off
echo ========================================
echo   Iniciando MongoDB para Drivers Service
echo ========================================

echo.
echo Deteniendo contenedores existentes...
docker-compose -f docker-compose-mongodb.yml down

echo.
echo Eliminando volúmenes anteriores (opcional)...
docker volume rm combustible-skt_mongodb_data 2>nul

echo.
echo Iniciando MongoDB...
docker-compose -f docker-compose-mongodb.yml up -d mongodb

echo.
echo Esperando a que MongoDB esté listo...
timeout /t 10 /nobreak >nul

echo.
echo Verificando estado de MongoDB...
docker-compose -f docker-compose-mongodb.yml ps

echo.
echo ========================================
echo   MongoDB iniciado correctamente
echo ========================================
echo.
echo Para conectarte a MongoDB:
echo   Host: localhost
echo   Puerto: 27017
echo   Usuario: admin
echo   Contraseña: admin123
echo   Base de datos: drivers_db
echo.
echo Para ver los logs de MongoDB:
echo   docker-compose -f docker-compose-mongodb.yml logs -f mongodb
echo.
echo Para detener MongoDB:
echo   docker-compose -f docker-compose-mongodb.yml down
echo.
pause
