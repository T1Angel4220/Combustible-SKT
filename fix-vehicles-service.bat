@echo off
echo ========================================
echo   🚛 Fix: Vehicles Service
echo ========================================
echo.

echo 🔍 Verificando si el Vehicles Service está corriendo...
curl -s http://localhost:8082/api/v1/vehicles/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Vehicles Service ya está corriendo en puerto 8082
    goto :test
) else (
    echo ❌ Vehicles Service NO está corriendo
)

echo.
echo 🚀 Iniciando Vehicles Service...
cd src\vehicles-service
start "Vehicles Service" cmd /k "mvn spring-boot:run"
cd ..\..

echo.
echo ⏳ Esperando a que el servicio inicie (15 segundos)...
timeout /t 15 /nobreak

:test
echo.
echo 🧪 Probando el servicio...
curl -s http://localhost:8082/api/v1/vehicles/health
if %errorlevel% equ 0 (
    echo.
    echo ✅ Vehicles Service funcionando correctamente
) else (
    echo.
    echo ❌ Vehicles Service aún no responde
    echo Esperando 10 segundos más...
    timeout /t 10 /nobreak
    curl -s http://localhost:8082/api/v1/vehicles/health
)

echo.
echo 🌐 Abriendo página de vehículos para prueba...
start http://localhost:8082/vehicles-simple.html

echo.
echo ========================================
echo   ✅ Fix Completado
echo ========================================
echo.
echo 📋 Si aún hay problemas:
echo    1. Verifica que el puerto 8082 esté libre
echo    2. Revisa los logs en la ventana del servicio
echo    3. Verifica que MongoDB esté corriendo
echo.
pause
