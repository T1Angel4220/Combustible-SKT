#!/bin/bash

# Script de prueba para el servicio de autenticación SKT
# Este script prueba los endpoints principales del servicio

echo "=== Pruebas del Servicio de Autenticación SKT ==="
echo ""

# Configuración
BASE_URL="http://localhost:8085"
ADMIN_USER="admin"
ADMIN_PASS="admin123"
SUPERVISOR_USER="supervisor"
SUPERVISOR_PASS="supervisor123"
OPERADOR_USER="operador"
OPERADOR_PASS="operador123"

# Función para hacer requests HTTP
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local headers=$4
    
    if [ -n "$data" ]; then
        curl -s -X $method "$url" \
             -H "Content-Type: application/json" \
             $headers \
             -d "$data"
    else
        curl -s -X $method "$url" $headers
    fi
}

# Función para extraer token de respuesta
extract_token() {
    echo "$1" | grep -o '"token":"[^"]*"' | cut -d'"' -f4
}

echo "1. Probando login de administrador..."
ADMIN_RESPONSE=$(make_request "POST" "$BASE_URL/api/auth/login" "{\"usernameOrEmail\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASS\"}")
ADMIN_TOKEN=$(extract_token "$ADMIN_RESPONSE")

if [ -n "$ADMIN_TOKEN" ]; then
    echo "✅ Login de administrador exitoso"
    echo "Token: ${ADMIN_TOKEN:0:50}..."
else
    echo "❌ Error en login de administrador"
    echo "Respuesta: $ADMIN_RESPONSE"
fi

echo ""
echo "2. Probando login de supervisor..."
SUPERVISOR_RESPONSE=$(make_request "POST" "$BASE_URL/api/auth/login" "{\"usernameOrEmail\":\"$SUPERVISOR_USER\",\"password\":\"$SUPERVISOR_PASS\"}")
SUPERVISOR_TOKEN=$(extract_token "$SUPERVISOR_RESPONSE")

if [ -n "$SUPERVISOR_TOKEN" ]; then
    echo "✅ Login de supervisor exitoso"
    echo "Token: ${SUPERVISOR_TOKEN:0:50}..."
else
    echo "❌ Error en login de supervisor"
    echo "Respuesta: $SUPERVISOR_RESPONSE"
fi

echo ""
echo "3. Probando login de operador..."
OPERADOR_RESPONSE=$(make_request "POST" "$BASE_URL/api/auth/login" "{\"usernameOrEmail\":\"$OPERADOR_USER\",\"password\":\"$OPERADOR_PASS\"}")
OPERADOR_TOKEN=$(extract_token "$OPERADOR_RESPONSE")

if [ -n "$OPERADOR_TOKEN" ]; then
    echo "✅ Login de operador exitoso"
    echo "Token: ${OPERADOR_TOKEN:0:50}..."
else
    echo "❌ Error en login de operador"
    echo "Respuesta: $OPERADOR_RESPONSE"
fi

echo ""
echo "4. Probando validación de token..."
if [ -n "$ADMIN_TOKEN" ]; then
    VALIDATION_RESPONSE=$(make_request "POST" "$BASE_URL/api/auth/validate?token=$ADMIN_TOKEN")
    echo "Respuesta de validación: $VALIDATION_RESPONSE"
fi

echo ""
echo "5. Probando endpoint protegido (información del usuario)..."
if [ -n "$ADMIN_TOKEN" ]; then
    USER_INFO_RESPONSE=$(make_request "GET" "$BASE_URL/api/auth/me" "" "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
    echo "Información del usuario: $USER_INFO_RESPONSE"
fi

echo ""
echo "6. Probando verificación de rol..."
if [ -n "$ADMIN_TOKEN" ]; then
    ROLE_CHECK_RESPONSE=$(make_request "GET" "$BASE_URL/api/auth/has-role/ADMIN" "" "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
    echo "Verificación de rol ADMIN: $ROLE_CHECK_RESPONSE"
fi

echo ""
echo "7. Probando verificación de permiso..."
if [ -n "$ADMIN_TOKEN" ]; then
    PERMISSION_CHECK_RESPONSE=$(make_request "GET" "$BASE_URL/api/auth/has-permission/USUARIOS_CREAR" "" "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
    echo "Verificación de permiso USUARIOS_CREAR: $PERMISSION_CHECK_RESPONSE"
fi

echo ""
echo "8. Probando gestión de usuarios (solo administradores)..."
if [ -n "$ADMIN_TOKEN" ]; then
    USERS_RESPONSE=$(make_request "GET" "$BASE_URL/api/users" "" "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
    echo "Lista de usuarios: $USERS_RESPONSE"
fi

echo ""
echo "9. Probando estadísticas de usuarios..."
if [ -n "$ADMIN_TOKEN" ]; then
    STATS_RESPONSE=$(make_request "GET" "$BASE_URL/api/users/stats" "" "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
    echo "Estadísticas: $STATS_RESPONSE"
fi

echo ""
echo "10. Probando registro de nuevo usuario..."
NEW_USER_DATA='{
    "username": "usuario_prueba",
    "email": "prueba@skt.com",
    "password": "prueba123",
    "nombre": "Usuario",
    "apellido": "Prueba",
    "rol": "OPERADOR",
    "permisos": ["VEHICULOS_LEER", "RUTAS_LEER"]
}'

if [ -n "$ADMIN_TOKEN" ]; then
    REGISTER_RESPONSE=$(make_request "POST" "$BASE_URL/api/users" "$NEW_USER_DATA" "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
    echo "Registro de nuevo usuario: $REGISTER_RESPONSE"
fi

echo ""
echo "11. Probando acceso denegado (operador intentando acceder a gestión de usuarios)..."
if [ -n "$OPERADOR_TOKEN" ]; then
    DENIED_RESPONSE=$(make_request "GET" "$BASE_URL/api/users" "" "-H \"Authorization: Bearer $OPERADOR_TOKEN\"")
    echo "Respuesta de acceso denegado: $DENIED_RESPONSE"
fi

echo ""
echo "12. Probando health check..."
HEALTH_RESPONSE=$(make_request "GET" "$BASE_URL/actuator/health")
echo "Estado del servicio: $HEALTH_RESPONSE"

echo ""
echo "=== Pruebas completadas ==="
echo ""
echo "Para probar manualmente:"
echo "1. Login: curl -X POST http://localhost:8085/api/auth/login -H 'Content-Type: application/json' -d '{\"usernameOrEmail\":\"admin\",\"password\":\"admin123\"}'"
echo "2. Usar el token en: curl -H 'Authorization: Bearer TOKEN' http://localhost:8085/api/auth/me"
echo ""
echo "Usuarios disponibles:"
echo "- admin / admin123 (ADMIN)"
echo "- supervisor / supervisor123 (SUPERVISOR)"
echo "- operador / operador123 (OPERADOR)"
echo "- test / test123 (OPERADOR)"
