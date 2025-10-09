@echo off
echo ========================================
echo   Solución al problema de datos vacíos
echo ========================================
echo.
echo PROBLEMA: La colección usuarios está vacía porque el script
echo de inicialización no incluía datos de ejemplo.
echo.
echo SOLUCIÓN:
echo.
echo 1. Detener los contenedores:
echo    docker-compose -f docker-compose-mongodb.yml down
echo.
echo 2. Eliminar el volumen de MongoDB para reinicializar:
echo    docker volume rm combustible-skt_mongodb_data
echo.
echo 3. Reiniciar los servicios:
echo    docker-compose -f docker-compose-mongodb.yml up -d
echo.
echo 4. Esperar a que se inicialice (30 segundos):
echo    timeout /t 30 /nobreak
echo.
echo 5. Conectarte a MongoDB:
echo    mongosh "mongodb://auth_user:auth_password@localhost:27017/auth_db"
echo.
echo 6. Verificar que ahora hay datos:
echo    db.usuarios.find().pretty()
echo.
echo ========================================
echo   Comandos de verificación:
echo ========================================
echo.
echo Una vez conectado, puedes usar estos comandos:
echo.
echo - Ver todos los usuarios:
echo   db.usuarios.find().pretty()
echo.
echo - Contar usuarios:
echo   db.usuarios.countDocuments()
echo.
echo - Buscar por rol:
echo   db.usuarios.find({rol: "ADMIN"}).pretty()
echo.
echo - Ver solo usuarios activos:
echo   db.usuarios.find({activo: true}).pretty()
echo.
echo ========================================
echo   Usuarios de ejemplo creados:
echo ========================================
echo.
echo Username: admin      Password: admin123      Rol: ADMIN
echo Username: supervisor Password: supervisor123 Rol: SUPERVISOR  
echo Username: operador   Password: operador123   Rol: OPERADOR
echo.
echo NOTA: Las contraseñas están hasheadas con BCrypt
echo.
pause



