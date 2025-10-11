@echo off
REM ====================================================================
REM Script de inicio del Frontend de Gestión de Choferes - SKT Combustible
REM ====================================================================

title SKT Combustible - Frontend de Choferes

echo.
echo ====================================================================
echo    SKT COMBUSTIBLE - FRONTEND DE GESTION DE CHOFERES
echo ====================================================================
echo.
echo Este script iniciara el frontend del servicio de choferes.
echo.
echo Puerto: 8081
echo URL: http://localhost:8081/drivers.html
echo.
echo Prerequisitos:
echo   - Java 17 o superior instalado
echo   - MongoDB ejecutandose (puerto 27017)
echo   - Auth Service ejecutandose (puerto 8085) para autenticacion
echo.
echo ====================================================================
echo.

REM Verificar si Java está instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java no esta instalado o no esta en el PATH
    echo Por favor instala Java 17 o superior
    pause
    exit /b 1
)

echo [OK] Java detectado
echo.

REM Cambiar al directorio del servicio
cd /d "%~dp0src\drivers-service"

REM Verificar si existe el pom.xml
if not exist "pom.xml" (
    echo [ERROR] No se encuentra el archivo pom.xml
    echo Asegurate de estar en el directorio correcto
    pause
    exit /b 1
)

echo [INFO] Iniciando Drivers Service...
echo.
echo Por favor espera mientras se compila y se inicia el servicio...
echo Este proceso puede tardar unos minutos la primera vez.
echo.

REM Iniciar el servicio con Maven
start "Drivers Service - Backend" mvn spring-boot:run

REM Esperar a que el servicio esté listo
echo [INFO] Esperando a que el servicio este disponible...
timeout /t 15 /nobreak > nul

echo.
echo ====================================================================
echo   SERVICIO INICIADO CORRECTAMENTE
echo ====================================================================
echo.
echo El servicio de choferes se esta ejecutando en:
echo   - Backend API: http://localhost:8081/api/v1/drivers
echo   - Frontend: http://localhost:8081/drivers.html
echo.
echo IMPORTANTE:
echo   - Para acceder al frontend, primero debes iniciar sesion en:
echo     http://localhost:8085/
echo.
echo   - Desde el dashboard principal, usa el boton de "Gestion de Choferes"
echo     para acceder con autenticacion JWT.
echo.
echo Abriendo navegador en 5 segundos...
echo.
timeout /t 5 /nobreak > nul

REM Abrir el navegador
start http://localhost:8081/drivers.html

echo.
echo ====================================================================
echo   FRONTEND ABIERTO EN EL NAVEGADOR
echo ====================================================================
echo.
echo Si ves un error de autenticacion:
echo   1. Ve a http://localhost:8085/
echo   2. Inicia sesion
echo   3. Desde el dashboard, accede a "Gestion de Choferes"
echo.
echo Para detener el servicio:
echo   - Cierra la ventana del backend
echo   - O presiona Ctrl+C en la ventana del backend
echo.
echo Presiona cualquier tecla para cerrar esta ventana...
pause > nul

