@echo off
echo Configurando variables de entorno para MongoDB...
set SPRING_DATA_MONGODB_URI=mongodb://vehicles_user:vehicles_pass@localhost:27017/vehicles_db?authSource=admin
set SPRING_DATA_MONGODB_DATABASE=vehicles_db
set SPRING_DATA_MONGODB_USERNAME=vehicles_user
set SPRING_DATA_MONGODB_PASSWORD=vehicles_pass
set SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=admin

echo Variables configuradas:
echo SPRING_DATA_MONGODB_URI=%SPRING_DATA_MONGODB_URI%
echo SPRING_DATA_MONGODB_DATABASE=%SPRING_DATA_MONGODB_DATABASE%
echo SPRING_DATA_MONGODB_USERNAME=%SPRING_DATA_MONGODB_USERNAME%
echo SPRING_DATA_MONGODB_PASSWORD=%SPRING_DATA_MONGODB_PASSWORD%
echo SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=%SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE%

echo.
echo Iniciando vehicles-service...
cd /d "E:\Aplicaciones Distribuidas\Combustible-SKT\src\vehicles-service"
mvn spring-boot:run

pause

