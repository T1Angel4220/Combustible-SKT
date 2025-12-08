@echo off
setlocal enabledelayedexpansion

title Verificando Estado de Servicios

echo ============================================
echo Verificando Estado de los Servicios
echo ============================================
echo.

echo Verificando si los servicios estan respondiendo...
echo.

REM Verificar Auth Service
echo [1/6] Auth Service (puerto 8085)...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:8085/actuator/health 2>nul
if %errorlevel% equ 0 (
    curl -s http://localhost:8085/actuator/health | findstr /i "UP" >nul
    if %errorlevel% equ 0 (
        echo   Estado: OK - Servicio funcionando
        echo   Frontend: http://localhost:8085/
        curl -s -o nul -w "   Frontend Status: %%{http_code}\n" http://localhost:8085/ 2>nul
    ) else (
        echo   Estado: ERROR - Servicio no responde correctamente
    )
) else (
    echo   Estado: NO INICIADO - El servicio no esta corriendo
)
echo.

REM Verificar Drivers Service
echo [2/6] Drivers Service (puerto 8081)...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:8081/actuator/health 2>nul
if %errorlevel% equ 0 (
    echo   Estado: OK - Servicio funcionando
) else (
    echo   Estado: NO INICIADO - El servicio no esta corriendo
)
echo.

REM Verificar Vehicles Service
echo [3/6] Vehicles Service (puerto 8082)...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:8082/actuator/health 2>nul
if %errorlevel% equ 0 (
    echo   Estado: OK - Servicio funcionando
) else (
    echo   Estado: NO INICIADO - El servicio no esta corriendo
)
echo.

REM Verificar Routes Service
echo [4/6] Routes Service (puerto 8083)...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:8083/actuator/health 2>nul
if %errorlevel% equ 0 (
    echo   Estado: OK - Servicio funcionando
) else (
    echo   Estado: NO INICIADO - El servicio no esta corriendo
)
echo.

REM Verificar Fuel Service
echo [5/6] Fuel Service (puerto 8084)...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:8084/actuator/health 2>nul
if %errorlevel% equ 0 (
    echo   Estado: OK - Servicio funcionando
) else (
    echo   Estado: NO INICIADO - El servicio no esta corriendo
)
echo.

REM Verificar Gateway Service
echo [6/6] Gateway Service (puerto 8090)...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:8090/actuator/health 2>nul
if %errorlevel% equ 0 (
    echo   Estado: OK - Servicio funcionando
) else (
    echo   Estado: NO INICIADO - El servicio no esta corriendo
)
echo.

echo ============================================
echo Resumen
echo ============================================
echo.
echo Si los servicios muestran "NO INICIADO":
echo   1. Verifica las ventanas de los servicios
echo   2. Revisa los logs de error en cada ventana
echo   3. Asegurate de que MongoDB Atlas este accesible
echo   4. Verifica que tu IP este permitida en Network Access
echo.
echo URLs importantes:
echo   - Auth Frontend: http://localhost:8085/
echo   - Auth Health: http://localhost:8085/actuator/health
echo.
pause

