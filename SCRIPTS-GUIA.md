# 🚀 Guía de Scripts - Sistema Combustible SKT

## 📋 Scripts Disponibles

### 🟢 **Scripts de Inicio**

#### 1. `start-all-services.bat` (Completo y Detallado)
**Uso:** Para desarrollo y testing completo
```bash
.\start-all-services.bat
```

**Características:**
- ✅ Pregunta si quieres compilar
- ✅ Muestra progreso detallado de cada paso
- ✅ Verifica el estado de cada servicio
- ✅ Abre cada servicio en ventana separada
- ✅ Opción para abrir navegador
- ✅ Información completa de URLs y puertos

**Servicios que inicia:**
1. MongoDB (puerto 27017)
2. Auth Service (puerto 8085)
3. Drivers Service (puerto 8081, gRPC 9091)
4. Vehicles Service (puerto 8082, gRPC 9092)

**Tiempo estimado:** 60-90 segundos

---

#### 2. `quick-start.bat` (Rápido y Simple)
**Uso:** Para desarrollo diario
```bash
.\quick-start.bat
```

**Características:**
- ⚡ No hace preguntas
- ⚡ Inicia todo automáticamente
- ⚡ Abre el navegador al finalizar
- ⚡ Perfecto para uso diario

**Tiempo estimado:** 30-45 segundos

---

#### 3. `restart-mongodb-no-auth.bat` (Solo MongoDB)
**Uso:** Para reiniciar solo MongoDB
```bash
.\restart-mongodb-no-auth.bat
```

**Características:**
- 🗄️ Solo reinicia MongoDB
- 🗄️ Sin autenticación (desarrollo)
- 🗄️ Verificación de conexión
- 🗄️ Mensajes de estado detallados

---

### 🔴 **Scripts de Detención**

#### `stop-all-services.bat`
**Uso:** Para detener todos los servicios
```bash
.\stop-all-services.bat
```

**Características:**
- ⛔ Detiene MongoDB
- ⛔ Opción para cerrar procesos Java
- ⛔ Limpieza completa

---

### 📊 **Scripts de Datos**

#### 1. `add-test-data.bat`
**Uso:** Agregar vehículos de prueba
```bash
.\add-test-data.bat
```

**Inserta:**
- Toyota Hilux (ABC-123) - DISPONIBLE
- Ford Ranger (DEF-456) - MANTENIMIENTO

---

#### 2. `add-drivers-data.bat`
**Uso:** Agregar choferes de prueba
```bash
.\add-drivers-data.bat
```

**Inserta:**
- Varios choferes con diferentes estados

---

#### 3. `add-auth-data.bat`
**Uso:** Agregar usuarios de prueba
```bash
.\add-auth-data.bat
```

**Inserta:**
- admin / admin123 (ADMIN)
- operador / operador123 (OPERADOR)

---

## 🎯 Flujos de Trabajo Recomendados

### Flujo 1: Desarrollo Diario
```bash
1. .\quick-start.bat
2. [Desarrollar y probar]
3. [Cerrar ventanas de servicios con Ctrl+C]
4. .\stop-all-services.bat
```

### Flujo 2: Primera Vez / Después de Cambios
```bash
1. .\start-all-services.bat
   - Seleccionar [S] para compilar
2. .\add-test-data.bat
3. .\add-drivers-data.bat
4. .\add-auth-data.bat
5. [Probar en navegador]
```

### Flujo 3: Solo Backend (sin frontend)
```bash
1. .\restart-mongodb-no-auth.bat
2. En terminal 1: cd src\auth-service && mvn spring-boot:run
3. En terminal 2: cd src\drivers-service && mvn spring-boot:run
4. En terminal 3: cd src\vehicles-service && mvn spring-boot:run
```

### Flujo 4: Reinicio Rápido
```bash
1. [Ctrl+C en cada ventana de servicio]
2. .\quick-start.bat
```

---

## 🌐 URLs del Sistema

Una vez iniciado, puedes acceder a:

### Frontend
| Servicio | URL | Descripción |
|----------|-----|-------------|
| Auth Service | http://localhost:8085/ | Login y Dashboard Principal |
| Vehicles Service | http://localhost:8082/vehicles.html | Gestión de Vehículos |

### APIs REST
| Servicio | URL Base | Health Check |
|----------|----------|--------------|
| Auth | http://localhost:8085/api/auth | http://localhost:8085/actuator/health |
| Drivers | http://localhost:8081/api/v1/drivers | http://localhost:8081/actuator/health |
| Vehicles | http://localhost:8082/api/v1/vehicles | http://localhost:8082/actuator/health |

### gRPC
| Servicio | Puerto |
|----------|--------|
| Auth | 9095 |
| Drivers | 9091 |
| Vehicles | 9092 |

### Base de Datos
```
MongoDB: mongodb://localhost:27017
Bases de datos:
- auth_db
- drivers_db
- vehicles_db
```

---

## 🔐 Credenciales de Prueba

### Usuarios (después de ejecutar `add-auth-data.bat`)
```
Admin:
  Usuario: admin
  Password: admin123
  Rol: ADMIN

Operador:
  Usuario: operador
  Password: operador123
  Rol: OPERADOR
```

---

## 🐛 Solución de Problemas

### Problema: "El puerto ya está en uso"
**Solución:**
```bash
.\stop-all-services.bat
# Seleccionar [S] para cerrar procesos Java
```

### Problema: "MongoDB no responde"
**Solución:**
```bash
docker stop mongodb-local
docker rm mongodb-local
.\restart-mongodb-no-auth.bat
```

### Problema: "Error de compilación"
**Solución:**
```bash
# En la raíz del proyecto
mvn clean install -DskipTests
```

### Problema: "Servicio no inicia"
**Solución:**
1. Verificar que MongoDB esté corriendo:
   ```bash
   docker ps | findstr mongodb
   ```
2. Revisar los logs en la ventana del servicio
3. Verificar que el puerto no esté ocupado:
   ```bash
   netstat -ano | findstr :8085
   ```

---

## ⚙️ Requisitos del Sistema

- ✅ Windows 10/11
- ✅ Java 17+ (JDK)
- ✅ Maven 3.8+
- ✅ Docker Desktop
- ✅ PowerShell o CMD
- ✅ 8GB RAM mínimo (recomendado 16GB)

---

## 📝 Notas Importantes

### Ventanas de Servicios
- Cada servicio se abre en su propia ventana
- **NO cierres las ventanas** mientras uses los servicios
- Los logs aparecen en tiempo real en cada ventana
- Para detener: `Ctrl+C` en la ventana o ciérrala

### Compilación
- Primera vez: Siempre compilar ([S])
- Desarrollo diario: Solo si hay cambios
- Compilación completa: ~60-90 segundos
- Sin compilar: Los servicios usan el JAR previo

### MongoDB
- Siempre debe iniciar PRIMERO
- Sin autenticación para desarrollo
- Con volumen persistente (mongodb_data)
- Los datos se mantienen entre reinicios

### Orden de Inicio
1. MongoDB (obligatorio primero)
2. Auth Service (independiente)
3. Drivers Service (independiente)
4. Vehicles Service (independiente)

Los servicios pueden iniciarse en cualquier orden después de MongoDB.

---

## 🚀 Inicio Rápido (TLDR)

**Primera vez:**
```bash
.\start-all-services.bat
# Seleccionar [S] para compilar
# Esperar 60-90 segundos
# Ir a http://localhost:8085/
```

**Uso diario:**
```bash
.\quick-start.bat
# Esperar 30 segundos
# Navegador se abre automáticamente
```

**Detener:**
```bash
[Ctrl+C en cada ventana]
.\stop-all-services.bat
```

---

## 📞 Soporte

Si tienes problemas:
1. Revisa esta guía
2. Verifica los logs en las ventanas de servicios
3. Ejecuta `docker logs mongodb-local`
4. Verifica que los puertos estén libres

---

**Desarrollado para**: Sistema Combustible SKT  
**Versión**: 1.0.0  
**Fecha**: Octubre 2024

