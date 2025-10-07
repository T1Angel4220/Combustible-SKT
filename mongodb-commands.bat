@echo off
echo ========================================
echo   Comandos útiles para MongoDB
echo ========================================
echo.
echo Para conectarte a MongoDB con autenticación:
echo.
echo 1. Conectar como administrador:
echo    mongosh "mongodb://admin:admin123@localhost:27017/admin"
echo.
echo 2. Conectar a la base de datos de vehículos:
echo    mongosh "mongodb://vehicles_user:vehicles_pass@localhost:27017/vehicles_db"
echo.
echo 3. Conectar a la base de datos de drivers:
echo    mongosh "mongodb://drivers_user:drivers_pass@localhost:27017/drivers_db"
echo.
echo 4. Conectar a la base de datos de autenticación:
echo    mongosh "mongodb://auth_user:auth_password@localhost:27017/auth_db"
echo.
echo ========================================
echo   Comandos útiles dentro de MongoDB:
echo ========================================
echo.
echo Una vez conectado, puedes usar estos comandos:
echo.
echo - Ver todas las bases de datos:
echo   show dbs
echo.
echo - Cambiar a una base de datos:
echo   use vehicles_db
echo.
echo - Ver todas las colecciones:
echo   show collections
echo.
echo - Ver documentos de una colección:
echo   db.vehicles.find().pretty()
echo.
echo - Contar documentos:
echo   db.vehicles.countDocuments()
echo.
echo - Buscar por criterio:
echo   db.vehicles.find({estado: "DISPONIBLE"}).pretty()
echo.
echo ========================================
echo   Solución al error "Unauthorized":
echo ========================================
echo.
echo El error ocurre porque no estás autenticado.
echo Usa uno de los comandos de conexión de arriba
echo con las credenciales correctas.
echo.
pause

