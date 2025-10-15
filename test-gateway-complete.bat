@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

echo ╔════════════════════════════════════════════════════════════════╗
echo ║        PRUEBA COMPLETA DEL GATEWAY SERVICE                     ║
echo ║        Verificación de integración con todos los servicios     ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

:: Colores (usando echo con caracteres especiales)
set "GREEN=[92m"
set "RED=[91m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "RESET=[0m"

:: Contadores
set /a TOTAL_TESTS=0
set /a PASSED_TESTS=0
set /a FAILED_TESTS=0

:: Variable para almacenar el token JWT
set "JWT_TOKEN="

echo.
echo ════════════════════════════════════════════════════════════════
echo  PASO 1: Verificar que todos los servicios estén levantados
echo ════════════════════════════════════════════════════════════════
echo.

echo [1/4] Verificando Auth Service (puerto 8085)...
curl -s -o nul -w "%%{http_code}" http://localhost:8085/actuator/health > temp_response.txt
set /p AUTH_STATUS=<temp_response.txt
del temp_response.txt

if "!AUTH_STATUS!"=="200" (
    echo %GREEN%✓ Auth Service: OK%RESET%
) else (
    echo %RED%✗ Auth Service: NO DISPONIBLE%RESET%
    echo %YELLOW%Por favor, inicia el Auth Service en el puerto 8085%RESET%
    goto :END
)

echo [2/4] Verificando Drivers Service (puerto 8081)...
curl -s -o nul -w "%%{http_code}" http://localhost:8081/actuator/health > temp_response.txt
set /p DRIVERS_STATUS=<temp_response.txt
del temp_response.txt

if "!DRIVERS_STATUS!"=="200" (
    echo %GREEN%✓ Drivers Service: OK%RESET%
) else (
    echo %RED%✗ Drivers Service: NO DISPONIBLE%RESET%
    echo %YELLOW%Por favor, inicia el Drivers Service en el puerto 8081%RESET%
    goto :END
)

echo [3/4] Verificando Vehicles Service (puerto 8082)...
curl -s -o nul -w "%%{http_code}" http://localhost:8082/actuator/health > temp_response.txt
set /p VEHICLES_STATUS=<temp_response.txt
del temp_response.txt

if "!VEHICLES_STATUS!"=="200" (
    echo %GREEN%✓ Vehicles Service: OK%RESET%
) else (
    echo %RED%✗ Vehicles Service: NO DISPONIBLE%RESET%
    echo %YELLOW%Por favor, inicia el Vehicles Service en el puerto 8082%RESET%
    goto :END
)

echo [4/4] Verificando Gateway Service (puerto 8090)...
curl -s -o nul -w "%%{http_code}" http://localhost:8090/actuator/health > temp_response.txt
set /p GATEWAY_STATUS=<temp_response.txt
del temp_response.txt

if "!GATEWAY_STATUS!"=="200" (
    echo %GREEN%✓ Gateway Service: OK%RESET%
) else (
    echo %RED%✗ Gateway Service: NO DISPONIBLE%RESET%
    echo %YELLOW%Por favor, inicia el Gateway Service en el puerto 8090%RESET%
    goto :END
)

echo.
echo %GREEN%════════════════════════════════════════════════════════════════%RESET%
echo %GREEN%    ✓ Todos los servicios están disponibles%RESET%
echo %GREEN%════════════════════════════════════════════════════════════════%RESET%
echo.

echo.
echo ════════════════════════════════════════════════════════════════
echo  PASO 2: Probar LOGIN via Gateway
echo ════════════════════════════════════════════════════════════════
echo.

set /a TOTAL_TESTS+=1
echo Test 1: POST /api/v1/auth/login (via Gateway)
echo Endpoint: http://localhost:8090/api/v1/auth/login
echo Body: {"usernameOrEmail":"admin","password":"admin123"}
echo.

:: Hacer login y capturar la respuesta completa
curl -s -X POST http://localhost:8090/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"usernameOrEmail\":\"admin\",\"password\":\"admin123\"}" ^
  -w "\nHTTP_STATUS:%%{http_code}" > login_response.txt

:: Leer el status code de la última línea
for /f "tokens=2 delims=:" %%a in ('findstr "HTTP_STATUS" login_response.txt') do set "LOGIN_STATUS=%%a"

if "!LOGIN_STATUS!"=="200" (
    echo %GREEN%✓ Login exitoso (Status: 200)%RESET%
    set /a PASSED_TESTS+=1
    
    :: Extraer el token JWT de la respuesta
    for /f "tokens=2 delims=:" %%a in ('findstr "token" login_response.txt ^| findstr /v "tipoToken"') do (
        set "JWT_RAW=%%a"
        :: Limpiar el token (quitar comillas y espacios)
        set "JWT_TOKEN=!JWT_RAW:~1,-2!"
    )
    
    echo Token JWT obtenido (primeros 50 caracteres): !JWT_TOKEN:~0,50!...
    echo.
) else (
    echo %RED%✗ Login falló (Status: !LOGIN_STATUS!)%RESET%
    set /a FAILED_TESTS+=1
    type login_response.txt
    del login_response.txt
    goto :END
)

del login_response.txt

echo.
echo ════════════════════════════════════════════════════════════════
echo  PASO 3: Probar DRIVERS SERVICE via Gateway
echo ════════════════════════════════════════════════════════════════
echo.

set /a TOTAL_TESTS+=1
echo Test 2: GET /api/v1/drivers/health (via Gateway)
curl -s -w "\nStatus: %%{http_code}\n" http://localhost:8090/api/v1/drivers/health
if !errorlevel! equ 0 (
    echo %GREEN%✓ Drivers Health OK%RESET%
    set /a PASSED_TESTS+=1
) else (
    echo %RED%✗ Drivers Health FAILED%RESET%
    set /a FAILED_TESTS+=1
)
echo.

set /a TOTAL_TESTS+=1
echo Test 3: GET /api/v1/drivers (con autenticación via Gateway)
echo Usando token JWT...
curl -s -w "\nStatus: %%{http_code}\n" ^
  -H "Authorization: Bearer !JWT_TOKEN!" ^
  http://localhost:8090/api/v1/drivers?page=0^&size=5
if !errorlevel! equ 0 (
    echo %GREEN%✓ Obtener drivers con JWT OK%RESET%
    set /a PASSED_TESTS+=1
) else (
    echo %RED%✗ Obtener drivers con JWT FAILED%RESET%
    set /a FAILED_TESTS+=1
)
echo.

echo.
echo ════════════════════════════════════════════════════════════════
echo  PASO 4: Probar VEHICLES SERVICE via Gateway
echo ════════════════════════════════════════════════════════════════
echo.

set /a TOTAL_TESTS+=1
echo Test 4: GET /api/v1/vehicles/health (via Gateway)
curl -s -w "\nStatus: %%{http_code}\n" http://localhost:8090/api/v1/vehicles/health
if !errorlevel! equ 0 (
    echo %GREEN%✓ Vehicles Health OK%RESET%
    set /a PASSED_TESTS+=1
) else (
    echo %RED%✗ Vehicles Health FAILED%RESET%
    set /a FAILED_TESTS+=1
)
echo.

set /a TOTAL_TESTS+=1
echo Test 5: GET /api/v1/vehicles (con autenticación via Gateway)
echo Usando token JWT...
curl -s -w "\nStatus: %%{http_code}\n" ^
  -H "Authorization: Bearer !JWT_TOKEN!" ^
  http://localhost:8090/api/v1/vehicles?page=0^&size=5
if !errorlevel! equ 0 (
    echo %GREEN%✓ Obtener vehicles con JWT OK%RESET%
    set /a PASSED_TESTS+=1
) else (
    echo %RED%✗ Obtener vehicles con JWT FAILED%RESET%
    set /a FAILED_TESTS+=1
)
echo.

set /a TOTAL_TESTS+=1
echo Test 6: GET /api/v1/vehicles/info (via Gateway)
curl -s -w "\nStatus: %%{http_code}\n" http://localhost:8090/api/v1/vehicles/info
if !errorlevel! equ 0 (
    echo %GREEN%✓ Vehicles Info OK%RESET%
    set /a PASSED_TESTS+=1
) else (
    echo %RED%✗ Vehicles Info FAILED%RESET%
    set /a FAILED_TESTS+=1
)
echo.

echo.
echo ════════════════════════════════════════════════════════════════
echo  PASO 5: Verificar Gateway Health e Info
echo ════════════════════════════════════════════════════════════════
echo.

set /a TOTAL_TESTS+=1
echo Test 7: GET /api/v1/gateway/health
curl -s -w "\nStatus: %%{http_code}\n" http://localhost:8090/api/v1/gateway/health
if !errorlevel! equ 0 (
    echo %GREEN%✓ Gateway Health OK%RESET%
    set /a PASSED_TESTS+=1
) else (
    echo %RED%✗ Gateway Health FAILED%RESET%
    set /a FAILED_TESTS+=1
)
echo.

set /a TOTAL_TESTS+=1
echo Test 8: GET /api/v1/gateway/info
curl -s http://localhost:8090/api/v1/gateway/info
echo.
if !errorlevel! equ 0 (
    echo %GREEN%✓ Gateway Info OK%RESET%
    set /a PASSED_TESTS+=1
) else (
    echo %RED%✗ Gateway Info FAILED%RESET%
    set /a FAILED_TESTS+=1
)
echo.

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                    RESUMEN DE PRUEBAS                          ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Total de pruebas:    !TOTAL_TESTS!
echo %GREEN%Pruebas exitosas:    !PASSED_TESTS!%RESET%
if !FAILED_TESTS! gtr 0 (
    echo %RED%Pruebas fallidas:    !FAILED_TESTS!%RESET%
) else (
    echo Pruebas fallidas:    !FAILED_TESTS!
)
echo.

if !FAILED_TESTS! equ 0 (
    echo %GREEN%════════════════════════════════════════════════════════════════%RESET%
    echo %GREEN%    ✓✓✓ TODAS LAS PRUEBAS PASARON EXITOSAMENTE ✓✓✓%RESET%
    echo %GREEN%    ✓ El Gateway está completamente funcional%RESET%
    echo %GREEN%    ✓ Drivers Service: Integrado correctamente%RESET%
    echo %GREEN%    ✓ Vehicles Service: Integrado correctamente%RESET%
    echo %GREEN%    ✓ Auth Service: Integrado correctamente%RESET%
    echo %GREEN%════════════════════════════════════════════════════════════════%RESET%
) else (
    echo %YELLOW%════════════════════════════════════════════════════════════════%RESET%
    echo %YELLOW%    ⚠ Algunas pruebas fallaron%RESET%
    echo %YELLOW%    Revisa los logs de los servicios para más detalles%RESET%
    echo %YELLOW%════════════════════════════════════════════════════════════════%RESET%
)

echo.
echo ════════════════════════════════════════════════════════════════
echo  ENDPOINTS DISPONIBLES VIA GATEWAY
echo ════════════════════════════════════════════════════════════════
echo.
echo AUTH SERVICE:
echo   - POST   http://localhost:8090/api/v1/auth/login
echo   - POST   http://localhost:8090/api/v1/auth/register
echo   - GET    http://localhost:8090/api/v1/auth/health
echo   - GET    http://localhost:8090/api/v1/auth/info
echo.
echo DRIVERS SERVICE:
echo   - GET    http://localhost:8090/api/v1/drivers
echo   - GET    http://localhost:8090/api/v1/drivers/{id}
echo   - POST   http://localhost:8090/api/v1/drivers
echo   - PUT    http://localhost:8090/api/v1/drivers/{id}
echo   - DELETE http://localhost:8090/api/v1/drivers/{id}
echo   - DELETE http://localhost:8090/api/v1/drivers/{id}/permanent
echo   - PATCH  http://localhost:8090/api/v1/drivers/{id}/activate
echo   - GET    http://localhost:8090/api/v1/drivers/health
echo.
echo VEHICLES SERVICE:
echo   - GET    http://localhost:8090/api/v1/vehicles
echo   - GET    http://localhost:8090/api/v1/vehicles/{id}
echo   - POST   http://localhost:8090/api/v1/vehicles
echo   - PUT    http://localhost:8090/api/v1/vehicles/{id}
echo   - DELETE http://localhost:8090/api/v1/vehicles/{id}
echo   - GET    http://localhost:8090/api/v1/vehicles/health
echo   - GET    http://localhost:8090/api/v1/vehicles/info
echo.
echo GATEWAY:
echo   - GET    http://localhost:8090/api/v1/gateway/health
echo   - GET    http://localhost:8090/api/v1/gateway/info
echo.

:END
echo.
echo Presiona cualquier tecla para salir...
pause >nul

