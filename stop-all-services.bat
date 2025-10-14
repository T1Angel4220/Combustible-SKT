@echo off
echo ============================================
echo Sistema Combustible SKT - Detener Servicios
echo ============================================
echo.

REM ==========================================
REM Detener MongoDB
REM ==========================================
echo [1/2] Deteniendo MongoDB...
docker stop mongodb-local 2>nul
docker rm mongodb-local 2>nul
if %errorlevel% equ 0 (
    echo OK - MongoDB detenido
) else (
    echo INFO - MongoDB no estaba corriendo
)
echo.

REM ==========================================
REM Cerrar ventanas de servicios
REM ==========================================
echo [2/2] Cerrando ventanas de servicios Spring Boot...
echo.
echo NOTA: Debes cerrar manualmente las ventanas de:
echo - Auth Service
echo - Drivers Service  
echo - Vehicles Service
echo.
echo O usa Ctrl+C en cada ventana.
echo.

REM Buscar y matar procesos Java de Spring Boot (opcional)
echo Deseas forzar el cierre de todos los procesos Java?
echo (Esto cerrara TODAS las aplicaciones Java en ejecucion)
choice /c SN /n /m "[S] Si   [N] No: "
if errorlevel 2 goto skip_kill
if errorlevel 1 goto do_kill

:do_kill
echo.
echo Cerrando procesos Java...
taskkill /F /IM java.exe 2>nul
if %errorlevel% equ 0 (
    echo OK - Procesos Java cerrados
) else (
    echo INFO - No habia procesos Java corriendo
)
goto after_kill

:skip_kill
echo Procesos Java NO cerrados

:after_kill
echo.
echo ============================================
echo Servicios Detenidos
echo ============================================
echo.
pause

