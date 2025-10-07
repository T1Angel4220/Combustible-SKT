@echo off
echo Deteniendo MongoDB actual...
docker stop mongodb-local
docker rm mongodb-local

echo Iniciando MongoDB SIN autenticacion...
docker run -d --name mongodb-local -p 27017:27017 -v mongodb_data:/data/db mongo:7.0

echo Esperando que MongoDB inicie...
timeout /t 10 /nobreak

echo Verificando conexion...
docker exec mongodb-local mongosh --eval "db.runCommand('ping')"

echo MongoDB iniciado sin autenticacion!
echo Ahora prueba la API: http://localhost:8082/api/v1/vehicles

pause
