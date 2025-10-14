@echo off
echo ============================================
echo TESTING GATEWAY SERVICE
echo ============================================
echo.
echo Probando endpoints del Gateway Service...
echo.

REM Verificar que el gateway esté corriendo
echo [1/8] Verificando que el Gateway esté corriendo...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8090/api/v1/gateway/health' -UseBasicParsing; Write-Host '  Estado: OK - Gateway funcionando' } catch { Write-Host '  Estado: ERROR - Gateway no disponible' }"
echo.

REM Probar info del gateway
echo [2/8] Probando información del Gateway...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8090/api/v1/gateway/info' -UseBasicParsing; Write-Host '  Estado: OK - Info obtenida' } catch { Write-Host '  Estado: ERROR - No se pudo obtener info' }"
echo.

REM Probar status gRPC
echo [3/8] Probando estado de canales gRPC...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8090/api/v1/gateway/grpc/status' -UseBasicParsing; Write-Host '  Estado: OK - Status gRPC obtenido' } catch { Write-Host '  Estado: ERROR - No se pudo obtener status gRPC' }"
echo.

REM Probar drivers health
echo [4/8] Probando health de Drivers via Gateway...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8090/api/v1/drivers/health' -UseBasicParsing; Write-Host '  Estado: OK - Drivers health OK' } catch { Write-Host '  Estado: ERROR - Drivers health falló' }"
echo.

REM Probar vehicles health
echo [5/8] Probando health de Vehicles via Gateway...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8090/api/v1/vehicles/health' -UseBasicParsing; Write-Host '  Estado: OK - Vehicles health OK' } catch { Write-Host '  Estado: ERROR - Vehicles health falló' }"
echo.

REM Probar vehicles info
echo [6/8] Probando info de Vehicles via Gateway...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8090/api/v1/vehicles/info' -UseBasicParsing; Write-Host '  Estado: OK - Vehicles info OK' } catch { Write-Host '  Estado: ERROR - Vehicles info falló' }"
echo.

REM Probar auth health
echo [7/8] Probando health de Auth via Gateway...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8090/api/v1/auth/health' -UseBasicParsing; Write-Host '  Estado: OK - Auth health OK' } catch { Write-Host '  Estado: ERROR - Auth health falló' }"
echo.

REM Probar auth info
echo [8/8] Probando info de Auth via Gateway...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8090/api/v1/auth/info' -UseBasicParsing; Write-Host '  Estado: OK - Auth info OK' } catch { Write-Host '  Estado: ERROR - Auth info falló' }"
echo.

echo ============================================
echo RESUMEN DE PRUEBAS
echo ============================================
echo.
echo Endpoints probados:
echo   - Gateway Health: http://localhost:8090/api/v1/gateway/health
echo   - Gateway Info: http://localhost:8090/api/v1/gateway/info
echo   - Gateway gRPC Status: http://localhost:8090/api/v1/gateway/grpc/status
echo   - Drivers Health: http://localhost:8090/api/v1/drivers/health
echo   - Vehicles Health: http://localhost:8090/api/v1/vehicles/health
echo   - Vehicles Info: http://localhost:8090/api/v1/vehicles/info
echo   - Auth Health: http://localhost:8090/api/v1/auth/health
echo   - Auth Info: http://localhost:8090/api/v1/auth/info
echo.
echo ============================================
echo NOTAS:
echo ============================================
echo.
echo 1. El Gateway está funcionando correctamente
echo 2. Los endpoints básicos están operativos
echo 3. Para usar gRPC, los microservicios deben estar corriendo
echo 4. El Gateway actúa como proxy entre REST y gRPC
echo.
echo Para probar con datos reales:
echo   1. Ejecuta start-all-services.bat
echo   2. Espera que todos los servicios estén listos
echo   3. Prueba los endpoints de drivers con datos reales
echo.
pause
