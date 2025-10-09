@echo off
echo ========================================
echo   Solución al error de autenticación MongoDB
echo ========================================
echo.
echo PROBLEMA: El auth-service no puede conectarse a MongoDB
echo porque no tiene las credenciales correctas configuradas.
echo.
echo ERROR: Command failed with error 13 (Unauthorized)
echo.
echo SOLUCIÓN APLICADA:
echo - Actualizada la configuración de MongoDB en auth-service
echo - Agregadas las credenciales de autenticación
echo.
echo ========================================
echo   Pasos para aplicar la solución:
echo ========================================
echo.
echo 1. Asegúrate de que MongoDB esté ejecutándose:
echo    docker-compose -f docker-compose-mongodb.yml up -d mongodb
echo.
echo 2. Espera a que MongoDB esté listo (30 segundos):
echo    timeout /t 30 /nobreak
echo.
echo 3. Verifica que MongoDB esté funcionando:
echo    docker-compose -f docker-compose-mongodb.yml ps
echo.
echo 4. Conéctate a MongoDB para verificar que los datos existen:
echo    mongosh "mongodb://auth_user:auth_password@localhost:27017/auth_db"
echo.
echo 5. Dentro de MongoDB, verifica que hay usuarios:
echo    db.usuarios.find().pretty()
echo.
echo 6. Si no hay datos, reinicia MongoDB con volúmenes limpios:
echo    docker-compose -f docker-compose-mongodb.yml down
echo    docker volume rm combustible-skt_mongodb_data
echo    docker-compose -f docker-compose-mongodb.yml up -d mongodb
echo.
echo 7. Ahora ejecuta el auth-service:
echo    cd src\auth-service
echo    mvn spring-boot:run
echo.
echo ========================================
echo   Configuración corregida:
echo ========================================
echo.
echo ANTES:
echo   uri: mongodb://localhost:27017/authdb
echo.
echo DESPUÉS:
echo   uri: mongodb://auth_user:auth_password@localhost:27017/auth_db
echo.
echo ========================================
echo   Credenciales de MongoDB:
echo ========================================
echo.
echo Base de datos: auth_db
echo Usuario: auth_user
echo Contraseña: auth_password
echo.
echo Base de datos: vehicles_db
echo Usuario: vehicles_user
echo Contraseña: vehicles_pass
echo.
echo Base de datos: drivers_db
echo Usuario: drivers_user
echo Contraseña: drivers_pass
echo.
pause



