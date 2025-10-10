# 🔐 Autenticación JWT - Integración Auth Service ↔ Vehicles Service

## 📋 Resumen

He implementado un sistema completo de autenticación JWT que permite que el **Vehicles Service** valide tokens generados por el **Auth Service**, manteniendo la sesión del usuario entre servicios.

---

## 🔄 Flujo de Autenticación Completo:

### **1. Inicio de Sesión (Auth Service)**
```
Usuario → Login Form → Auth Service → JWT Token → localStorage
```

### **2. Navegación a Vehicles Service**
```
Click "Gestión de Vehículos" → Verificar Token → Redirigir si válido
```

### **3. Validación en Vehicles Service**
```
Vehicles Service → Validar Token con Auth Service → Permitir acceso
```

### **4. Operaciones CRUD**
```
Cada petición → Header Authorization → Validar Token → Ejecutar operación
```

---

## 🛠️ Implementación Técnica:

### **Variables Globales (vehicles-script.js)**
```javascript
let authToken = null;        // Token JWT del usuario
let currentUser = null;      // Información del usuario autenticado
```

### **Verificación de Autenticación al Cargar**
```javascript
async function checkAuthentication() {
    // 1. Cargar token y usuario del localStorage
    const savedToken = localStorage.getItem('authToken');
    const savedUser = localStorage.getItem('currentUser');
    
    // 2. Si no hay datos, redirigir al login
    if (!savedToken || !savedUser) {
        redirectToLogin();
        return;
    }
    
    // 3. Parsear datos del usuario
    authToken = savedToken;
    currentUser = JSON.parse(savedUser);
    
    // 4. Validar token con Auth Service
    const isValid = await validateTokenWithAuthService();
    
    // 5. Si válido, mostrar interfaz; si no, redirigir
    if (isValid) {
        updateUserInfo();
        showDashboard();
    } else {
        redirectToLogin();
    }
}
```

### **Validación de Token con Auth Service**
```javascript
async function validateTokenWithAuthService() {
    const response = await fetch('http://localhost:8085/api/auth/validate', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${authToken}`
        },
        body: JSON.stringify({ token: authToken })
    });
    
    if (response.ok) {
        const data = await response.json();
        return data.valid === true;  // Auth Service devuelve {"valid": true}
    }
    return false;
}
```

### **Headers de Autorización en API Calls**
```javascript
// Todas las peticiones al Vehicles Service incluyen el token
const response = await fetch(`${API_BASE_URL}`, {
    method: 'GET',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`  // ← Token JWT
    }
});

// Manejo de errores 401 (No autorizado)
if (response.status === 401) {
    showMessage('Sesión expirada. Redirigiendo al login...', 'error');
    setTimeout(() => redirectToLogin(), 2000);
}
```

---

## 🔐 Endpoints de Autenticación:

### **Auth Service (Puerto 8085)**

#### **POST /api/auth/login**
```json
// Request
{
  "usernameOrEmail": "admin",
  "password": "admin123"
}

// Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "65f3c4a8e9b7d12a3c4e5f6a",
    "username": "admin",
    "email": "admin@skt.com",
    "nombre": "Administrador",
    "apellido": "Sistema",
    "rol": "ADMIN"
  }
}
```

#### **POST /api/auth/validate**
```json
// Request
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

// Response (Token válido)
{
  "valid": true,
  "user": {
    "id": "65f3c4a8e9b7d12a3c4e5f6a",
    "username": "admin",
    "email": "admin@skt.com",
    "nombre": "Administrador",
    "apellido": "Sistema",
    "rol": "ADMIN"
  }
}

// Response (Token inválido)
{
  "valid": false,
  "error": "Token expirado"
}
```

### **Vehicles Service (Puerto 8082)**

Todas las peticiones requieren el header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📱 Experiencia de Usuario:

### **Escenario 1: Usuario Autenticado**
1. ✅ Usuario inicia sesión en Auth Service
2. ✅ Token se guarda en localStorage
3. ✅ Click en "Gestión de Vehículos"
4. ✅ Vehicles Service valida token automáticamente
5. ✅ Muestra página de vehículos con nombre del usuario
6. ✅ Todas las operaciones funcionan normalmente

### **Escenario 2: Token Expirado**
1. ❌ Usuario intenta acceder a Vehicles Service
2. ❌ Token expirado detectado
3. 🔄 Mensaje: "Sesión expirada. Redirigiendo al login..."
4. 🔄 Redirección automática al Auth Service
5. ✅ Usuario debe iniciar sesión nuevamente

### **Escenario 3: Sin Autenticación**
1. ❌ Usuario accede directamente a `/vehicles.html`
2. ❌ No hay token en localStorage
3. 🔄 Mensaje: "Debes iniciar sesión para acceder a este módulo"
4. 🔄 Redirección automática al Auth Service

---

## 🔧 Configuración del Sistema:

### **Auth Service (application.yml)**
```yaml
server:
  port: 8085

jwt:
  secret: mi-clave-secreta-muy-segura-para-jwt-tokens
  expiration: 3600000  # 1 hora en milisegundos
```

### **Vehicles Service (application.yml)**
```yaml
server:
  port: 8082

# No necesita configuración JWT propia
# Solo valida tokens del Auth Service
```

---

## 🧪 Pruebas de Validación:

### **Script de Prueba Automática**
```bash
test-auth-vehicles-integration.bat
```

### **Pruebas Manuales**

#### **1. Prueba de Login**
```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'
```

#### **2. Prueba de Validación de Token**
```bash
curl -X POST http://localhost:8085/api/auth/validate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -d '{"token":"TU_TOKEN_AQUI"}'
```

#### **3. Prueba de Acceso a Vehicles Service**
```bash
curl -X GET http://localhost:8082/api/v1/vehicles \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
```

---

## 🔒 Seguridad Implementada:

### **Validación de Token**
- ✅ Verificación en cada carga de página
- ✅ Validación con Auth Service en tiempo real
- ✅ Manejo de tokens expirados

### **Headers de Autorización**
- ✅ Token incluido en todas las peticiones API
- ✅ Manejo de errores 401 (No autorizado)
- ✅ Redirección automática en caso de error

### **Limpieza de Sesión**
- ✅ Logout limpia localStorage
- ✅ Variables globales se resetean
- ✅ Redirección al login

### **CORS Configurado**
```java
@CrossOrigin(origins = "*")  // En ambos controladores
```

---

## 🐛 Solución de Problemas:

### **Error: "Debes iniciar sesión para acceder a este módulo"**

**Causas:**
- No hay token en localStorage
- Token corrupto o inválido
- Auth Service no está corriendo

**Soluciones:**
```bash
# 1. Verificar Auth Service
curl http://localhost:8085/api/auth/health

# 2. Verificar localStorage en navegador (F12)
localStorage.getItem('authToken')
localStorage.getItem('currentUser')

# 3. Limpiar localStorage y volver a iniciar sesión
localStorage.clear()
```

### **Error: "Sesión expirada. Redirigiendo al login..."**

**Causas:**
- Token JWT expirado (por defecto 1 hora)
- Token inválido o corrupto

**Soluciones:**
```bash
# 1. Verificar expiración del token
# Decodificar JWT en jwt.io para ver expiración

# 2. Verificar configuración JWT en Auth Service
jwt:
  expiration: 3600000  # 1 hora en ms

# 3. Reiniciar sesión
# Ir a login y autenticarse nuevamente
```

### **Error: CORS Policy**

**Causas:**
- Auth Service no tiene CORS habilitado
- Origen no permitido

**Soluciones:**
```java
// En AuthController.java
@CrossOrigin(origins = "*")  // Debe estar presente

// Verificar que esté en todas las peticiones
```

---

## 📊 Flujo de Datos Completo:

```mermaid
graph TD
    A[Usuario] --> B[Auth Service Login]
    B --> C[Token JWT Generado]
    C --> D[Token Guardado en localStorage]
    D --> E[Click Gestión Vehículos]
    E --> F[Vehicles Service Carga]
    F --> G[Validar Token con Auth Service]
    G --> H{Token Válido?}
    H -->|Sí| I[Mostrar Dashboard Vehículos]
    H -->|No| J[Redirigir a Login]
    I --> K[Operaciones CRUD con Token]
    K --> L[Headers Authorization]
    L --> M[Validación en Backend]
    M --> N{Autorizado?}
    N -->|Sí| O[Ejecutar Operación]
    N -->|No| P[Error 401]
    P --> Q[Redirigir a Login]
```

---

## ✅ Checklist de Implementación:

- [x] Variables globales para token y usuario
- [x] Función `checkAuthentication()` al cargar página
- [x] Función `validateTokenWithAuthService()`
- [x] Headers de autorización en todas las API calls
- [x] Manejo de errores 401 (No autorizado)
- [x] Función `redirectToLogin()` para casos de error
- [x] Función `logout()` que limpia sesión
- [x] Actualización de información de usuario en UI
- [x] Endpoint `/validate` en Auth Service
- [x] CORS configurado en ambos servicios
- [x] Scripts de prueba automatizados

---

## 🎯 Próximos Pasos (Opcionales):

### **1. Roles y Permisos**
```javascript
// Verificar rol antes de mostrar funcionalidades
if (currentUser.rol === 'ADMIN') {
    // Mostrar botón de eliminar
} else {
    // Ocultar botón de eliminar
}
```

### **2. Refresh Token**
```javascript
// Implementar renovación automática de token
async function refreshTokenIfNeeded() {
    // Lógica para renovar token antes de que expire
}
```

### **3. Interceptor HTTP**
```javascript
// Interceptor global para manejar errores 401
fetch.interceptors.response.use(
    response => response,
    error => {
        if (error.response.status === 401) {
            redirectToLogin();
        }
        return Promise.reject(error);
    }
);
```

---

## 🎉 ¡Sistema de Autenticación Completo!

Tu sistema ahora tiene:

1. ✅ **Autenticación JWT** entre servicios
2. ✅ **Validación de tokens** en tiempo real
3. ✅ **Manejo de sesiones** persistente
4. ✅ **Redirección automática** en casos de error
5. ✅ **Seguridad completa** en todas las operaciones

### **Para Probar:**
```bash
start-complete-system.bat
```

### **Flujo de Prueba:**
1. Login: admin/admin123
2. Click "Gestión de Vehículos"
3. ¡Disfruta la gestión con autenticación completa!

---

**¡Tu sistema de autenticación JWT está 100% funcional!** 🔐✨

**Desarrollado para**: SKT Combustible System  
**Versión**: 1.0.0  
**Fecha**: Octubre 2024  
**Tecnología**: JWT, Spring Boot, MongoDB
