@echo off
echo ========================================
echo   Script de Prueba de Login - SKT Combustible
echo ========================================
echo.

REM Configuración
set AUTH_SERVICE_URL=http://localhost:8085
set LOGIN_ENDPOINT=%AUTH_SERVICE_URL%/api/auth/login

echo Verificando que el auth-service esté ejecutándose...
echo URL del servicio: %AUTH_SERVICE_URL%
echo.

REM Verificar si el servicio está ejecutándose
curl -s -o nul -w "%%{http_code}" %AUTH_SERVICE_URL%/actuator/health > temp_status.txt
set /p HTTP_STATUS=<temp_status.txt
del temp_status.txt

if "%HTTP_STATUS%"=="200" (
    echo ✅ Auth-service está ejecutándose correctamente
) else (
    echo ❌ Auth-service no está ejecutándose o no responde
    echo Por favor, ejecuta el auth-service primero:
    echo   cd src\auth-service
    echo   mvn spring-boot:run
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Probando credenciales de login
echo ========================================
echo.

REM Función para probar login
:test_login
set USERNAME=%1
set PASSWORD=%2
set USER_DESC=%3

echo Probando login con usuario: %USER_DESC%
echo Usuario: %USERNAME%
echo Contraseña: %PASSWORD%
echo.

REM Crear archivo JSON temporal para el login
echo {^"usernameOrEmail^":^"%USERNAME%^",^"password^":^"%PASSWORD%^"} > login_request.json

REM Realizar petición de login
curl -X POST ^
     -H "Content-Type: application/json" ^
     -d @login_request.json ^
     -s ^
     -w "HTTP Status: %%{http_code}\n" ^
     %LOGIN_ENDPOINT% > login_response.txt

echo Respuesta del servidor:
type login_response.txt
echo.

REM Verificar si el login fue exitoso
findstr /C:"token" login_response.txt > nul
if %errorlevel%==0 (
    echo ✅ Login exitoso para %USER_DESC%
) else (
    echo ❌ Login fallido para %USER_DESC%
)

echo.
echo ========================================
echo.

REM Limpiar archivos temporales
del login_request.json
del login_response.txt

goto :eof

REM Probar diferentes usuarios
call :test_login "admin" "password" "Administrador"
call :test_login "supervisor" "password" "Supervisor"
call :test_login "operador" "password" "Operador"

echo ========================================
echo   Pruebas completadas
echo ========================================
echo.
echo Si todas las pruebas fueron exitosas, el sistema de autenticación
echo está funcionando correctamente.
echo.
echo Si alguna prueba falló, verifica:
echo 1. Que MongoDB esté ejecutándose
echo 2. Que el auth-service esté ejecutándose
echo 3. Que los usuarios existan en la base de datos
echo.
pause
