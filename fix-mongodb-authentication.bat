@echo off
echo ========================================
echo   Solución al error de autenticación MongoDB
echo ========================================
echo.
echo PROBLEMA: El usuario auth_user no existe en MongoDB
echo porque los scripts de inicialización no se ejecutaron.
echo.
echo ERROR: Authentication failed
echo.
echo SOLUCIÓN: Reinicializar MongoDB completamente
echo.
echo ========================================
echo   Pasos para solucionar:
echo ========================================
echo.
echo 1. Detener MongoDB:
echo    docker-compose -f docker-compose-mongodb.yml down
echo.
echo 2. Eliminar el volumen de datos (IMPORTANTE):
echo    docker volume rm combustible-skt_mongodb_data
echo.
echo 3. Verificar que el volumen se eliminó:
echo    docker volume ls
echo.
echo 4. Reiniciar MongoDB (esto ejecutará los scripts de inicialización):
echo    docker-compose -f docker-compose-mongodb.yml up -d mongodb
echo.
echo 5. Esperar a que MongoDB se inicialice completamente (60 segundos):
echo    timeout /t 60 /nobreak
echo.
echo 6. Verificar que MongoDB está funcionando:
echo    docker-compose -f docker-compose-mongodb.yml ps
echo.
echo 7. Verificar que los usuarios se crearon:
echo    mongosh "mongodb://admin:admin123@localhost:27017/admin"
echo.
echo    Dentro de MongoDB, ejecutar:
echo    use auth_db
echo    db.getUsers()
echo.
echo 8. Verificar que hay datos en la colección usuarios:
echo    mongosh "mongodb://auth_user:auth_password@localhost:27017/auth_db"
echo.
echo    Dentro de MongoDB, ejecutar:
echo    db.usuarios.find().pretty()
echo.
echo 9. Si todo está bien, ejecutar el auth-service:
echo    cd src\auth-service
echo    mvn spring-boot:run
echo.
echo ========================================
echo   ¿Por qué ocurre esto?
echo ========================================
echo.
echo Los scripts de inicialización de MongoDB solo se ejecutan:
echo - La primera vez que se crea el contenedor
echo - Cuando se elimina el volumen de datos
echo.
echo Si el volumen ya existía de antes, los scripts NO se ejecutan.
echo Por eso necesitamos eliminar el volumen para reinicializar.
echo.
echo ========================================
echo   Archivos de inicialización:
echo ========================================
echo.
echo - init-mongodb.js (drivers_db)
echo - src/auth-service/mongo-init/01-init-auth-db.js (auth_db)
echo - src/vehicles-service/mongo-init/01-init-vehicles-db.js (vehicles_db)
echo.
pause

