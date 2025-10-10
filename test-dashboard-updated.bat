@echo off
echo ========================================
echo   🚛 Dashboard Actualizado - Prueba
echo ========================================
echo.

echo ✅ Cambios realizados:
echo    📊 Nueva tarjeta: "Fuera de Servicio"
echo    🔧 Mantenimiento: Cambió de "Fuera de servicio" a "En reparación"
echo    🎯 Nuevas acciones: Mantenimiento y Fuera de Servicio
echo.

echo 🚀 Iniciando sistema para prueba...
echo.

echo 📊 1. Verificando MongoDB...
docker ps | findstr mongodb >nul
if %errorlevel% neq 0 (
    echo ❌ MongoDB no está ejecutándose
    echo Iniciando MongoDB...
    docker-compose -f docker-compose-mongodb.yml up -d mongodb
    timeout /t 10 /nobreak
    echo ✅ MongoDB iniciado
) else (
    echo ✅ MongoDB ya está ejecutándose
)

echo.
echo 🔐 2. Iniciando Auth Service...
cd src\auth-service
start "Auth Service" cmd /k "mvn spring-boot:run"
cd ..\..

echo.
echo ⏳ Esperando Auth Service (10 segundos)...
timeout /t 10 /nobreak

echo.
echo 🚛 3. Iniciando Vehicles Service...
cd src\vehicles-service
start "Vehicles Service" cmd /k "mvn spring-boot:run"
cd ..\..

echo.
echo ⏳ Esperando Vehicles Service (15 segundos)...
timeout /t 15 /nobreak

echo.
echo 🌐 4. Abriendo sistema actualizado...
start http://localhost:8085/

echo.
echo ========================================
echo   ✅ Dashboard Actualizado - Listo
echo ========================================
echo.
echo 📋 Nuevas características del Dashboard:
echo.
echo 📊 Estadísticas (5 tarjetas):
echo    ✅ Total de Vehículos
echo    ✅ Vehículos Disponibles
echo    ✅ En Mantenimiento (ahora dice "En reparación")
echo    ✅ En Uso
echo    🆕 Fuera de Servicio (NUEVA)
echo.
echo 🎯 Acciones Rápidas (8 tarjetas):
echo    ✅ Ver Todos
echo    ✅ Disponibles
echo    ✅ Camiones
echo    ✅ Excavadoras
echo    ✅ Volquetes
echo    🆕 Mantenimiento (NUEVA)
echo    🆕 Fuera de Servicio (NUEVA)
echo    ✅ Agregar Nuevo
echo.
echo 🔧 Flujo de Prueba:
echo    1. Ve a http://localhost:8085/
echo    2. Login con: admin / admin123
echo    3. Click en "Gestión de Vehículos"
echo    4. Verifica las nuevas tarjetas en el dashboard
echo    5. Prueba los filtros de "Mantenimiento" y "Fuera de Servicio"
echo.
echo 📝 Estados Operativos:
echo    ✅ DISPONIBLE - Listos para usar
echo    ✅ MANTENIMIENTO - En reparación
echo    ✅ EN_USO - En operación
echo    ✅ FUERA_SERVICIO - No operativo
echo.
pause
