@echo off
echo ========================================
echo    PRUEBA GATEWAY CON gRPC
echo ========================================
echo.

echo Esperando que el Gateway se inicie...
timeout /t 10 /nobreak >nul

echo.
echo 1. Probando health check del Gateway...
curl -s http://localhost:8090/api/v1/drivers/health
echo.
echo.

echo 2. Probando health check del Drivers Service directo...
curl -s http://localhost:8081/api/v1/drivers/health
echo.
echo.

echo 3. Probando obtener choferes disponibles via Gateway (gRPC)...
curl -s http://localhost:8090/api/v1/drivers/available
echo.
echo.

echo 4. Probando obtener choferes disponibles directo (REST)...
curl -s http://localhost:8081/api/v1/drivers/available
echo.
echo.

echo 5. Probando contar choferes disponibles via Gateway (gRPC)...
curl -s http://localhost:8090/api/v1/drivers/count/available
echo.
echo.

echo 6. Probando contar choferes disponibles directo (REST)...
curl -s http://localhost:8081/api/v1/drivers/count/available
echo.
echo.

echo ========================================
echo    PRUEBAS COMPLETADAS
echo ========================================
echo.
echo Si ves respuestas similares entre Gateway y directo,
echo significa que el Gateway gRPC funciona correctamente!
echo.
pause
