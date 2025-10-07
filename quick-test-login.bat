@echo off
echo ========================================
echo   Prueba Rápida de Login - SKT Combustible
echo ========================================
echo.

set AUTH_URL=http://localhost:8085/api/auth/login

echo Probando login con usuario: admin / password
echo.

echo Creando petición de login...
echo {"usernameOrEmail":"admin","password":"password"} > login.json

echo Enviando petición...
curl -X POST ^
     -H "Content-Type: application/json" ^
     -d @login.json ^
     -s ^
     %AUTH_URL%

echo.
echo.

echo Verificando respuesta...
findstr /C:"token" login.json > nul
if %errorlevel%==0 (
    echo ✅ Login exitoso!
) else (
    echo ❌ Login fallido. Verifica que:
    echo   1. El auth-service esté ejecutándose
    echo   2. MongoDB esté ejecutándose
    echo   3. Las credenciales sean correctas
)

echo.
echo Limpiando archivos temporales...
del login.json

echo.
echo ========================================
echo   Prueba completada
echo ========================================
pause
