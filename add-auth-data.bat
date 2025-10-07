@echo off
echo Agregando datos de prueba a auth_db...

docker exec mongodb-local mongosh auth_db --eval "db.users.insertOne({username: 'admin', password: '\$2a\$10\$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', email: 'admin@email.com', roles: ['ADMIN'], enabled: true, accountNonExpired: true, accountNonLocked: true, credentialsNonExpired: true, fechaCreacion: new Date(), fechaActualizacion: new Date()})"

docker exec mongodb-local mongosh auth_db --eval "db.users.insertOne({username: 'user', password: '\$2a\$10\$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', email: 'user@email.com', roles: ['USER'], enabled: true, accountNonExpired: true, accountNonLocked: true, credentialsNonExpired: true, fechaCreacion: new Date(), fechaActualizacion: new Date()})"

echo Datos de auth agregados!
echo Usuarios de prueba:
echo - admin / admin123 (ADMIN)
echo - user / user123 (USER)
echo Ahora prueba la API:
echo POST http://localhost:8085/api/auth/login

pause
