@echo off
chcp 65001 >nul
echo ================================================
echo   PRUEBA DE RÉPLICA DE NODOS EN MONGODB ATLAS
echo ================================================
echo.

REM Configuración de la cadena de conexión a MongoDB Atlas
REM Dentro de comillas dobles, el & no necesita escape
set "MONGODB_ATLAS_URI=mongodb+srv://907johan_db_user:piIe4vWfuADsnRM6@combustibleskt.4n4nf9z.mongodb.net/drivers_db?retryWrites=true&w=majority&readPreference=secondaryPreferred&maxPoolSize=50&minPoolSize=10&appName=combustibleskt"

REM URI para conexión primaria (solo lectura desde primary)
set "MONGODB_ATLAS_PRIMARY=mongodb+srv://907johan_db_user:piIe4vWfuADsnRM6@combustibleskt.4n4nf9z.mongodb.net/drivers_db?retryWrites=true&w=majority&readPreference=primary&maxPoolSize=50&minPoolSize=10&appName=combustibleskt"

REM URI para conexión secundaria (solo lectura desde secondary)
set "MONGODB_ATLAS_SECONDARY=mongodb+srv://907johan_db_user:piIe4vWfuADsnRM6@combustibleskt.4n4nf9z.mongodb.net/drivers_db?retryWrites=true&w=majority&readPreference=secondary&maxPoolSize=50&minPoolSize=10&appName=combustibleskt"

REM Generar un ID único para el documento de prueba usando timestamp
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value 2^>nul') do set datetime=%%I
if not defined datetime (
    REM Fallback si wmic no está disponible
    set datetime=%date:~-4%%date:~3,2%%date:~0,2%%time:~0,2%%time:~3,2%%time:~6,2%
    set datetime=%datetime: =0%
)
set TEST_ID=TEST_REPLICA_%datetime:~0,14%
set TEST_COLLECTION=replica_test

echo [1/5] Verificando conexión a MongoDB Atlas...
echo.
mongosh "%MONGODB_ATLAS_URI%" --eval "db.runCommand({ping: 1})" --quiet
if %errorlevel% neq 0 (
    echo ERROR: No se pudo conectar a MongoDB Atlas
    echo Verifica que mongosh esté instalado y la cadena de conexión sea correcta
    pause
    exit /b 1
)
echo ✓ Conexión exitosa
echo.

echo [2/5] Verificando estado del replica set...
echo.
mongosh "%MONGODB_ATLAS_URI%" --eval "try { var status = rs.status(); print('Replica Set: ' + status.set); print('Miembros: ' + status.members.length); status.members.forEach(function(m) { print('  - ' + m.name + ' (' + m.stateStr + ')'); }); } catch(e) { print('No es un replica set o no se puede acceder al estado'); }" --quiet
echo.

echo [3/5] Insertando documento de prueba en el nodo PRIMARIO...
echo.
mongosh "%MONGODB_ATLAS_PRIMARY%" --eval "var doc = {_id: '%TEST_ID%', testType: 'REPLICA_TEST', timestamp: new Date(), message: 'Documento de prueba para verificar réplica', node: 'PRIMARY_INSERT', createdAt: new Date()}; var result = db.replica_test.insertOne(doc); print('Documento insertado con ID: ' + result.insertedId); print('Total documentos en colección: ' + db.replica_test.countDocuments());" --quiet
if %errorlevel% neq 0 (
    echo ERROR: No se pudo insertar el documento
    pause
    exit /b 1
)
echo ✓ Documento insertado exitosamente
echo.

echo [4/5] Esperando 3 segundos para que se replique a los nodos secundarios...
timeout /t 3 /nobreak >nul
echo.

echo [5/5] Verificando réplica en nodo PRIMARIO...
echo.
mongosh "%MONGODB_ATLAS_PRIMARY%" --eval "var doc = db.replica_test.findOne({_id: '%TEST_ID%'}); if (doc) { print('✓ Documento encontrado en PRIMARY'); print('  ID: ' + doc._id); print('  Timestamp: ' + doc.timestamp); print('  Node: ' + doc.node); } else { print('✗ Documento NO encontrado en PRIMARY'); }" --quiet
echo.

echo Verificando réplica en nodo SECUNDARIO...
echo.
mongosh "%MONGODB_ATLAS_SECONDARY%" --eval "var doc = db.replica_test.findOne({_id: '%TEST_ID%'}); if (doc) { print('✓ Documento encontrado en SECONDARY'); print('  ID: ' + doc._id); print('  Timestamp: ' + doc.timestamp); print('  Node: ' + doc.node); } else { print('✗ Documento NO encontrado en SECONDARY - La réplica puede estar en proceso'); }" --quiet
echo.

echo ================================================
echo   RESUMEN DE LA PRUEBA
echo ================================================
echo.
echo Verificando todos los documentos de prueba...
mongosh "%MONGODB_ATLAS_URI%" --eval "print('Total documentos de prueba: ' + db.replica_test.countDocuments()); print('\\nÚltimos 5 documentos:'); db.replica_test.find().sort({timestamp: -1}).limit(5).forEach(function(doc) { print('  - ID: ' + doc._id + ' | Timestamp: ' + doc.timestamp + ' | Node: ' + (doc.node || 'N/A')); });" --quiet
echo.

echo ================================================
echo   PRUEBA COMPLETADA
echo ================================================
echo.
echo Para limpiar los documentos de prueba, ejecuta:
echo mongosh "%MONGODB_ATLAS_URI%" --eval "db.replica_test.deleteMany({testType: 'REPLICA_TEST'})"
echo.
pause

