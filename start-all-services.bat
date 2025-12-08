@echo off
setlocal enabledelayedexpansion

title Sistema Combustible SKT - Inicio con MongoDB Atlas

REM Cambiar al directorio del script
cd /d "%~dp0"

REM Verificar que estamos en el directorio correcto
if not exist "src\auth-service\pom.xml" (
    echo ERROR: No se encontro el proyecto en el directorio actual.
    echo.
    echo Asegurate de ejecutar este script desde el directorio raiz del proyecto.
    echo Directorio actual: %CD%
    echo.
    pause
    exit /b 1
)

echo ============================================
echo Sistema Combustible SKT - Inicio
echo Con MongoDB Atlas
echo ============================================
echo.
echo Directorio: %CD%
echo.

REM Verificar conexion a MongoDB Atlas (opcional)
where mongosh >nul 2>&1
if not errorlevel 1 (
    echo Verificando conexion a MongoDB Atlas...
    set "ATLAS_URI=mongodb+srv://907johan_db_user:piIe4vWfuADsnRM6@combustibleskt.4n4nf9z.mongodb.net/?retryWrites=true&w=majority&readPreference=secondaryPreferred&maxPoolSize=50&minPoolSize=10&appName=combustibleskt"
    mongosh "!ATLAS_URI!" --quiet --eval "db.adminCommand('ping')" >nul 2>&1
    if errorlevel 1 (
        echo ADVERTENCIA: No se pudo conectar a MongoDB Atlas
        echo Los servicios intentaran conectarse de todas formas.
        echo.
    ) else (
        echo OK - Conexion a MongoDB Atlas verificada
        echo.
    )
)

echo Se iniciaran los siguientes servicios:
echo 1. Auth Service (puerto 8085) - MongoDB Atlas
echo 2. Drivers Service (puerto 8081) - MongoDB Atlas
echo 3. Vehicles Service (puerto 8082) - MongoDB Atlas
echo 4. Routes Service (puerto 8083) - MongoDB Atlas
echo 5. Fuel Service (puerto 8084) - MongoDB Atlas
echo 6. Gateway Service (puerto 8090)
echo.
echo IMPORTANTE: Las ventanas de los servicios permaneceran abiertas.
echo Si ves errores, revisa los logs en cada ventana.
echo.
pause

REM ==========================================
REM Paso 1: Compilar proyecto (opcional)
REM ==========================================
echo Deseas compilar el proyecto?
echo [S] Si   [N] No
choice /c SN /n /m "Selecciona una opcion: "

if errorlevel 2 goto skip_compile
if errorlevel 1 goto do_compile

:do_compile
echo.
echo Compilando proyecto con Maven (esto puede tomar 1-2 minutos)...
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Fallo la compilacion.
    echo Revisa los mensajes de error arriba.
    echo.
    pause
    exit /b 1
)
echo.
echo Compilacion exitosa.
goto after_compile

:skip_compile
echo Saltando compilacion...

:after_compile
echo.

REM ==========================================
REM Paso 2: Iniciar Auth Service
REM ==========================================
echo [1/6] Iniciando Auth Service (puerto 8085)...
echo   - Directorio: %~dp0src\auth-service
echo   - Perfil: atlas
echo   - MongoDB Atlas: combustibleskt.4n4nf9z.mongodb.net
echo.
start "Auth Service - SKT (Atlas)" cmd /k "cd /d %~dp0src\auth-service && echo ============================================ && echo Auth Service - MongoDB Atlas && echo ============================================ && echo Directorio: %CD% && echo Perfil: atlas && echo Puerto: 8085 && echo. && echo Iniciando servicio... && echo. && mvn spring-boot:run -Dspring-boot.run.profiles=atlas"
if %errorlevel% neq 0 (
    echo ERROR: No se pudo iniciar la ventana del Auth Service
) else (
    echo OK - Ventana de Auth Service abierta
    echo   Espera a que el servicio inicie completamente...
    echo   El frontend estara disponible en: http://localhost:8085/
)
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM Paso 3: Iniciar Drivers Service
REM ==========================================
echo [2/6] Iniciando Drivers Service (puerto 8081)...
start "Drivers Service - SKT (Atlas)" cmd /k "cd /d %~dp0src\drivers-service && echo ============================================ && echo Drivers Service - MongoDB Atlas && echo ============================================ && echo. && echo Iniciando con perfil 'atlas'... && echo. && mvn spring-boot:run -Dspring-boot.run.profiles=atlas || (echo. && echo ERROR: Fallo al iniciar Drivers Service && echo Revisa los logs arriba para mas detalles && pause)"
echo OK - Drivers Service iniciado
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM Paso 4: Iniciar Vehicles Service
REM ==========================================
echo [3/6] Iniciando Vehicles Service (puerto 8082)...
start "Vehicles Service - SKT (Atlas)" cmd /k "cd /d %~dp0src\vehicles-service && echo ============================================ && echo Vehicles Service - MongoDB Atlas && echo ============================================ && echo. && echo Iniciando con perfil 'atlas'... && echo. && mvn spring-boot:run -Dspring-boot.run.profiles=atlas || (echo. && echo ERROR: Fallo al iniciar Vehicles Service && echo Revisa los logs arriba para mas detalles && pause)"
echo OK - Vehicles Service iniciado
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM Paso 5: Iniciar Routes Service
REM ==========================================
echo [4/6] Iniciando Routes Service (puerto 8083)...
start "Routes Service - SKT (Atlas)" cmd /k "cd /d %~dp0src\routes-service && echo ============================================ && echo Routes Service - MongoDB Atlas && echo ============================================ && echo. && echo Iniciando con perfil 'atlas'... && echo. && mvn spring-boot:run -Dspring-boot.run.profiles=atlas || (echo. && echo ERROR: Fallo al iniciar Routes Service && echo Revisa los logs arriba para mas detalles && pause)"
echo OK - Routes Service iniciado
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM Paso 6: Iniciar Fuel Service
REM ==========================================
echo [5/6] Iniciando Fuel Service (puerto 8084)...
start "Fuel Service - SKT (Atlas)" cmd /k "cd /d %~dp0src\fuel-service && echo ============================================ && echo Fuel Service - MongoDB Atlas && echo ============================================ && echo. && echo Iniciando con perfil 'atlas'... && echo. && mvn spring-boot:run -Dspring-boot.run.profiles=atlas || (echo. && echo ERROR: Fallo al iniciar Fuel Service && echo Revisa los logs arriba para mas detalles && pause)"
echo OK - Fuel Service iniciado
ping 127.0.0.1 -n 5 >nul
echo.

REM ==========================================
REM Paso 7: Iniciar Gateway Service
REM ==========================================
echo [6/6] Iniciando Gateway Service (puerto 8090)...
start "Gateway Service - SKT" cmd /k "cd /d %~dp0src\gateway-service && echo ============================================ && echo Gateway Service && echo ============================================ && echo. && echo Iniciando Gateway Service... && echo. && mvn spring-boot:run || (echo. && echo ERROR: Fallo al iniciar Gateway Service && echo Revisa los logs arriba para mas detalles && pause)"
echo OK - Gateway Service iniciado
ping 127.0.0.1 -n 5 >nul
echo.

echo.
echo ============================================
echo Ventanas de Servicios Abiertas
echo ============================================
echo.
echo IMPORTANTE: Revisa las ventanas de cada servicio.
echo.
echo Si ves errores de conexion a MongoDB:
echo   1. Verifica que tu IP este permitida en Network Access de MongoDB Atlas
echo   2. Verifica que tengas conexion a Internet
echo   3. Revisa las credenciales en application-atlas.yml
echo.
echo Esperando que los servicios inicien (45 segundos)...
echo Esto puede tardar mas si es la primera vez...
echo.
for /l %%i in (45,-1,1) do (
    set /a minutos=%%i/60
    set /a segundos=%%i%%60
    echo Tiempo restante: !minutos!m !segundos!s...
    ping 127.0.0.1 -n 2 >nul
)
echo.
echo Verificando estado de los servicios...
echo.
REM Verificar estado de Auth Service
echo Verificando Auth Service...
curl -s http://localhost:8085/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] Auth Service esta respondiendo
    set AUTH_OK=1
) else (
    echo   [ESPERANDO] Auth Service aun no responde
    echo   Esto es normal si acaba de iniciar. Espera unos segundos mas.
    set AUTH_OK=0
)
echo.

echo ============================================
echo Servicios disponibles:
echo ============================================
echo.
echo  MongoDB Atlas:
echo    - Cluster: combustibleskt.4n4nf9z.mongodb.net
echo    - Usuario: 907johan_db_user
echo.
echo  Auth Service (Puerto 8085):
if defined AUTH_OK if !AUTH_OK! equ 1 (
    echo    - Estado: [OK] Funcionando
) else (
    echo    - Estado: [INICIANDO] Espera unos segundos...
)
echo    - Frontend: http://localhost:8085/
echo    - Login: http://localhost:8085/index.html
echo    - Dashboard: http://localhost:8085/dashboard.html
echo    - API: http://localhost:8085/api/auth
echo    - Health: http://localhost:8085/actuator/health
echo.
echo  Drivers Service (Puerto 8081):
echo    - Frontend: http://localhost:8081/drivers.html
echo    - API: http://localhost:8081/api/v1/drivers
echo.
echo  Vehicles Service (Puerto 8082):
echo    - Frontend: http://localhost:8082/vehicles.html
echo    - API: http://localhost:8082/api/v1/vehicles
echo.
echo  Routes Service (Puerto 8083):
echo    - Frontend: http://localhost:8083/routes.html
echo    - API: http://localhost:8083/api/v1/routes
echo.
echo  Fuel Service (Puerto 8084):
echo    - Frontend: http://localhost:8084/fuel.html
echo    - API: http://localhost:8084/api/v1/fuel
echo.
echo  Gateway Service (Puerto 8090):
echo    - API: http://localhost:8090/api/v1/
echo.
echo ============================================
echo.
echo TROUBLESHOOTING:
echo.
echo Si el frontend no carga:
echo   1. Espera 30-60 segundos mas (Spring Boot tarda en iniciar)
echo   2. Revisa la ventana "Auth Service - SKT (Atlas)" para ver errores
echo   3. Verifica que no haya errores de conexion a MongoDB Atlas
echo   4. Ejecuta: verificar-servicios.bat para diagnosticar
echo.
echo Si ves errores de MongoDB:
echo   1. Verifica tu IP en Network Access de MongoDB Atlas
echo   2. Asegurate de tener conexion a Internet
echo   3. Revisa las credenciales en application-atlas.yml
echo.
echo Deseas abrir el navegador en el Auth Service?
choice /c SN /n /m "[S] Si   [N] No: "
if errorlevel 2 goto skip_browser
if errorlevel 1 (
    echo.
    echo Abriendo navegador en http://localhost:8085/...
    echo Si no carga, espera unos segundos y recarga la pagina.
    start http://localhost:8085/
)

:skip_browser
echo.
echo ============================================
echo   SERVICIOS EN EJECUCION
echo ============================================
echo.
echo Los servicios estan ejecutandose en ventanas separadas.
echo Revisa cada ventana para ver los logs y posibles errores.
echo.
echo Para verificar el estado, ejecuta: verificar-servicios.bat
echo.
echo Presiona cualquier tecla para cerrar esta ventana...
echo (Los servicios seguiran ejecutandose en sus propias ventanas)
pause >nul
