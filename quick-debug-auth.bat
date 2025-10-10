@echo off
echo ========================================
echo   🔍 Quick Debug: Autenticación
echo ========================================
echo.

echo 🚀 Iniciando servicios...
start "Auth Service" cmd /k "cd src\auth-service && mvn spring-boot:run"
timeout /t 5 /nobreak

start "Vehicles Service" cmd /k "cd src\vehicles-service && mvn spring-boot:run"
timeout /t 10 /nobreak

echo.
echo 🌐 Abriendo navegador...
start http://localhost:8085/

echo.
echo ========================================
echo   📋 Instrucciones de Debug
echo ========================================
echo.
echo 1. Haz login con: admin / admin123
echo 2. Abre la consola del navegador (F12)
echo 3. Click en "Gestión de Vehículos"
echo 4. Revisa los logs en la consola
echo.
echo 🔧 Si aparece el error "Debes iniciar sesión":
echo    - Revisa la consola para ver qué está pasando
echo    - Verifica que el token esté en localStorage
echo    - Comprueba que ambos servicios estén corriendo
echo.
echo 📝 URLs de Prueba:
echo    - Auth Service: http://localhost:8085/
echo    - Vehicles Simple: http://localhost:8082/vehicles-simple.html
echo.
pause
