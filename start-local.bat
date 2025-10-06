@echo off
echo 🚀 Iniciando Base de Datos PostgreSQL con Docker...

REM Detener contenedores existentes
echo 🛑 Deteniendo contenedores existentes...
docker-compose -f docker-compose-local.yml down

REM Levantar solo PostgreSQL
echo 🔨 Levantando PostgreSQL...
docker-compose -f docker-compose-local.yml up -d

REM Esperar a que PostgreSQL esté listo
echo ⏳ Esperando a que PostgreSQL esté listo...
timeout /t 15 /nobreak

REM Verificar estado
echo 📊 Verificando estado de PostgreSQL...
docker-compose -f docker-compose-local.yml ps

echo.
echo ✅ PostgreSQL está listo!
echo 📋 Información de conexión:
echo    Host: localhost
echo    Puerto: 5432
echo    Base de datos: drivers_db
echo    Usuario: drivers_user
echo    Contraseña: drivers_pass
echo.
echo 🚗 Ahora puedes ejecutar el drivers-service localmente:
echo    cd src/drivers-service
echo    mvn spring-boot:run
echo.
echo 📋 Para ver logs de PostgreSQL:
echo    docker-compose -f docker-compose-local.yml logs -f postgres-db
