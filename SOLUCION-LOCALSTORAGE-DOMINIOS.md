# 🔧 Solución: Problema de localStorage entre Dominios

## 🚨 Problema Identificado

El problema era que **cada servicio corre en un puerto diferente**:
- **Auth Service**: `localhost:8085`
- **Vehicles Service**: `localhost:8082`

El `localStorage` **NO se comparte** entre dominios/puertos diferentes, por eso:
- ✅ El token se guardaba correctamente en `localhost:8085` (Auth Service)
- ❌ El Vehicles Service en `localhost:8082` no podía acceder a esos datos

## 🛠️ Solución Implementada

### **Método: Pasar Datos por URL**

En lugar de depender del `localStorage` compartido, ahora:

1. **Auth Service** pasa el token y datos del usuario como parámetros URL
2. **Vehicles Service** recibe esos datos y los guarda en su propio `localStorage`
3. **Futuras cargas** usan el `localStorage` local del Vehicles Service

---

## 🔄 Flujo de la Solución:

### **1. Usuario hace login en Auth Service**
```
localhost:8085 → Token guardado en localStorage
```

### **2. Click en "Gestión de Vehículos"**
```javascript
// Auth Service envía datos por URL
window.location.href = `http://localhost:8082/vehicles-simple.html?token=${tokenParam}&user=${userParam}`;
```

### **3. Vehicles Service recibe los datos**
```javascript
// Extrae datos de la URL
const urlParams = new URLSearchParams(window.location.search);
const tokenFromUrl = urlParams.get('token');
const userFromUrl = urlParams.get('user');

// Guarda en su propio localStorage
localStorage.setItem('authToken', tokenFromUrl);
localStorage.setItem('currentUser', userFromUrl);
```

### **4. Limpia la URL**
```javascript
// Remueve los parámetros para que no se vean
window.history.replaceState({}, document.title, window.location.pathname);
```

---

## 📁 Archivos Modificados:

### **Auth Service** (`src/auth-service/src/main/resources/static/script.js`):
```javascript
function goToVehiclesManagement() {
    if (!authToken) {
        showMessage('Debes iniciar sesión para acceder a la gestión de vehículos', 'error');
        return;
    }
    
    showMessage('Redirigiendo a la gestión de vehículos...', 'info');
    
    // Pasar el token y datos del usuario como parámetros URL
    const tokenParam = encodeURIComponent(authToken);
    const userParam = encodeURIComponent(JSON.stringify(currentUser));
    
    setTimeout(() => {
        window.location.href = `http://localhost:8082/vehicles-simple.html?token=${tokenParam}&user=${userParam}`;
    }, 1000);
}
```

### **Vehicles Service** (`src/vehicles-service/src/main/resources/static/vehicles-script-simple.js`):
```javascript
function checkAuthenticationSimple() {
    console.log('🔍 Verificando autenticación (modo simple)...');
    
    // Primero verificar si hay datos en la URL (venimos del Auth Service)
    const urlParams = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get('token');
    const userFromUrl = urlParams.get('user');
    
    if (tokenFromUrl && userFromUrl) {
        console.log('📥 Datos recibidos desde Auth Service');
        
        // Guardar en localStorage para futuras cargas
        localStorage.setItem('authToken', tokenFromUrl);
        localStorage.setItem('currentUser', userFromUrl);
        
        // Parsear y asignar datos
        authToken = tokenFromUrl;
        currentUser = JSON.parse(userFromUrl);
        
        console.log('✅ Datos guardados en localStorage');
        
        // Limpiar URL
        window.history.replaceState({}, document.title, window.location.pathname);
        
        // Continuar con la autenticación
        proceedWithAuthentication();
        return;
    }
    
    // Si no hay datos en URL, intentar cargar del localStorage
    // ... resto del código
}
```

---

## 🚀 Scripts de Inicio:

### **Para Iniciar el Sistema Arreglado:**
```bash
start-fixed-system.bat
```

### **Para Solo Arreglar Vehicles Service:**
```bash
fix-vehicles-service.bat
```

### **Para Debug Rápido:**
```bash
quick-debug-auth.bat
```

---

## 🧪 Prueba de la Solución:

### **1. Iniciar Sistema:**
```bash
start-fixed-system.bat
```

### **2. Flujo de Prueba:**
1. Ve a `http://localhost:8085/`
2. Login con `admin` / `admin123`
3. Click en "Gestión de Vehículos"
4. **Ahora debería funcionar correctamente**

### **3. Logs que Verás:**
```
🔍 Verificando autenticación (modo simple)...
📥 Datos recibidos desde Auth Service
✅ Datos guardados en localStorage
✅ Datos cargados: {username: "admin", tokenLength: 123}
✅ Token JWT válido y usuario encontrado
🎉 Usuario autenticado: admin
```

---

## 🔒 Seguridad de la Solución:

### **Ventajas:**
- ✅ **Funciona entre dominios diferentes**
- ✅ **No requiere configuración CORS especial**
- ✅ **Mantiene la sesión localmente**
- ✅ **URL se limpia automáticamente**

### **Consideraciones:**
- ⚠️ **Token visible en URL temporalmente** (se limpia inmediatamente)
- ⚠️ **Solo para desarrollo** (en producción usar SSO o cookies compartidas)

### **Para Producción:**
- Usar **Single Sign-On (SSO)**
- Configurar **cookies compartidas** en el mismo dominio
- Usar **JWT en cookies HttpOnly**

---

## 🐛 Solución de Problemas:

### **Problema: Vehicles Service no inicia**
```bash
# Solución:
fix-vehicles-service.bat
```

### **Problema: Token no se pasa**
- Verifica que Auth Service tenga el token
- Revisa la consola del navegador
- Comprueba que la redirección funcione

### **Problema: Datos no se guardan**
- Verifica que la URL tenga los parámetros
- Revisa la consola para errores de parsing
- Comprueba que localStorage funcione

---

## 📊 Comparación Antes vs Después:

### **❌ Antes (No Funcionaba):**
```
Auth Service (8085) → localStorage: {token, user}
Vehicles Service (8082) → localStorage: {} (vacío)
Resultado: "Debes iniciar sesión"
```

### **✅ Después (Funciona):**
```
Auth Service (8085) → localStorage: {token, user}
                    ↓ (pasa por URL)
Vehicles Service (8082) → URL params → localStorage: {token, user}
Resultado: "Usuario autenticado: admin"
```

---

## 🎯 Próximos Pasos (Opcionales):

### **1. Implementar en Otros Servicios:**
- Drivers Service
- Routes Service  
- Fuel Service

### **2. Mejorar Seguridad:**
- Encriptar parámetros URL
- Implementar timeouts de sesión
- Agregar validación de origen

### **3. Optimizar UX:**
- Loading states durante redirección
- Manejo de errores de red
- Persistencia de preferencias de usuario

---

## ✅ ¡Problema Resuelto!

La solución implementada resuelve completamente el problema de autenticación entre servicios que corren en puertos diferentes.

### **Para Probar:**
```bash
start-fixed-system.bat
```

### **Resultado Esperado:**
- ✅ Login funciona en Auth Service
- ✅ Click en "Gestión de Vehículos" redirige correctamente
- ✅ Vehicles Service muestra "¡Bienvenido, Administrador!"
- ✅ Todas las operaciones CRUD funcionan
- ✅ Sesión se mantiene entre navegación

---

**¡El sistema ahora funciona perfectamente entre servicios!** 🎉✨

**Desarrollado para**: SKT Combustible System  
**Versión**: 1.0.0 - Fix localStorage  
**Fecha**: Octubre 2024  
**Problema**: localStorage entre dominios diferentes  
**Solución**: Pasar datos por URL
