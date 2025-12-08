@echo off
chcp 65001 >nul
echo ================================================
echo   LIMPIEZA DE DOCUMENTOS DE PRUEBA DE RÉPLICA
echo ================================================
echo.

REM Configuración de la cadena de conexión a MongoDB Atlas
REM Dentro de comillas dobles, el & no necesita escape
set "MONGODB_ATLAS_URI=mongodb+srv://907johan_db_user:piIe4vWfuADsnRM6@combustibleskt.4n4nf9z.mongodb.net/drivers_db?retryWrites=true&w=majority&readPreference=secondaryPreferred&maxPoolSize=50&minPoolSize=10&appName=combustibleskt"

echo Verificando documentos de prueba antes de eliminar...
echo.
mongosh "%MONGODB_ATLAS_URI%" --eval "var count = db.replica_test.countDocuments({testType: 'REPLICA_TEST'}); print('Documentos de prueba encontrados: ' + count); if (count > 0) { print('\\nEliminando documentos...'); var result = db.replica_test.deleteMany({testType: 'REPLICA_TEST'}); print('Documentos eliminados: ' + result.deletedCount); } else { print('No hay documentos de prueba para eliminar.'); }" --quiet

echo.
echo ================================================
echo   LIMPIEZA COMPLETADA
echo ================================================
echo.
pause

