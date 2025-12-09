# ⚡ Despliegue Rápido GRATIS en Render - Solo hasta Miércoles

## ✅ SÍ, puedes usar el Plan Gratuito

**Para una demostración temporal (hasta miércoles), el plan gratuito es perfecto.**

---

## 🎯 CONFIGURACIÓN MÍNIMA - Todo Gratis

### Servicios a Crear (Todos en Plan FREE):

1. ✅ **Gateway Service** → Web Service (PÚBLICO) - Free
2. ✅ **Auth Service** → Private Service - Free
3. ✅ **Drivers Service** → Private Service - Free
4. ✅ **Vehicles Service** → Private Service - Free
5. ✅ **Routes Service** → Private Service - Free
6. ✅ **Fuel Service** → Private Service - Free

**Total: $0.00/mes** 💰

---

## ⚠️ IMPORTANTE - Limitación del Plan Gratuito

### Los servicios se "duermen":
- ✅ Después de **15 minutos de inactividad**, los servicios se duermen
- ⏱️ El **primer request** después de dormir tarda **30-60 segundos**
- ✅ Una vez despiertos, funcionan normal

### Solución para tu Demo:
1. **Opción 1:** Mantén los servicios activos haciendo requests periódicos
2. **Opción 2:** Acepta que el primer request será lento
3. **Opción 3:** Antes de la demo, haz un request a cada servicio para despertarlos

---

## 🚀 PASOS RÁPIDOS (30-45 minutos)

### Paso 1: Crear Cuenta en Render
1. Ve a [render.com](https://render.com)
2. Regístrate con GitHub/GitLab/Bitbucket
3. Conecta tu repositorio `Combustible-SKT`

### Paso 2: Crear Servicios (En este orden)

#### 2.1 Auth Service (PRIMERO)
1. **New** → **Private Service**
2. **Configuración:**
   ```
   Name: auth-service
   Environment: Docker
   Dockerfile Path: src/auth-service/Dockerfile
   Docker Context: .
   Plan: Free ✅
   ```
3. **Variables de Entorno:**
   ```
   SPRING_PROFILES_ACTIVE=render
   MONGODB_ATLAS_URI=tu-uri-completa-de-mongodb-atlas
   JWT_SECRET=tu-secreto-seguro-minimo-32-caracteres-1234567890123456
   JWT_EXPIRATION=86400000
   ```
4. **⚠️ NO agregues variable PORT** (déjala vacía)
5. **Health Check:** `/actuator/health`
6. **Click "Create Private Service"**

**Espera 5-10 minutos** mientras se construye el servicio.

#### 2.2 Drivers Service
1. **New** → **Private Service**
2. **Configuración:**
   ```
   Name: drivers-service
   Environment: Docker
   Dockerfile Path: src/drivers-service/Dockerfile
   Docker Context: .
   Plan: Free ✅
   ```
3. **Variables de Entorno:**
   ```
   SPRING_PROFILES_ACTIVE=render
   MONGODB_ATLAS_URI=tu-uri-mongodb-atlas-para-drivers-db
   JWT_SECRET=el-mismo-secreto-que-auth-service
   GRPC_AUTH_HOST=auth-service.onrender.com
   GRPC_AUTH_PORT=9095
   AUTH_SERVICE_URL=https://auth-service.onrender.com
   ```
4. **NO agregues PORT**
5. **Health Check:** `/actuator/health`
6. **Create**

#### 2.3 Vehicles Service
Igual que Drivers, pero:
- **Name:** `vehicles-service`
- **MongoDB URI:** para `vehicles_db`
- **Variables adicionales:**
   ```
   GRPC_DRIVERS_HOST=drivers-service.onrender.com
   GRPC_DRIVERS_PORT=9091
   DRIVERS_SERVICE_URL=https://drivers-service.onrender.com
   ```

#### 2.4 Routes Service
- **Name:** `routes-service`
- **MongoDB URI:** para `routes_db`
- **Variables:**
   ```
   GRPC_DRIVERS_HOST=drivers-service.onrender.com
   GRPC_DRIVERS_PORT=9091
   GRPC_VEHICLES_HOST=vehicles-service.onrender.com
   GRPC_VEHICLES_PORT=9092
   DRIVERS_SERVICE_URL=https://drivers-service.onrender.com
   VEHICLES_SERVICE_URL=https://vehicles-service.onrender.com
   ```

#### 2.5 Fuel Service
- **Name:** `fuel-service`
- **MongoDB URI:** para `fuel_db`
- **Variables:**
   ```
   GRPC_ROUTES_HOST=routes-service.onrender.com
   GRPC_ROUTES_PORT=9093
   GRPC_VEHICLES_HOST=vehicles-service.onrender.com
   GRPC_VEHICLES_PORT=9092
   ROUTES_SERVICE_URL=https://routes-service.onrender.com
   VEHICLES_SERVICE_URL=https://vehicles-service.onrender.com
   ```

#### 2.6 Gateway Service (ÚLTIMO - El Público)
1. **New** → **Web Service** (este es PÚBLICO)
2. **Configuración:**
   ```
   Name: combustible-gateway
   Environment: Docker
   Dockerfile Path: src/gateway-service/Dockerfile
   Docker Context: .
   Plan: Free ✅
   ```
3. **Variables de Entorno:**
   ```
   SPRING_PROFILES_ACTIVE=render
   DRIVERS_SERVICE_URL=https://drivers-service.onrender.com
   VEHICLES_SERVICE_URL=https://vehicles-service.onrender.com
   ROUTES_SERVICE_URL=https://routes-service.onrender.com
   FUEL_SERVICE_URL=https://fuel-service.onrender.com
   AUTH_SERVICE_URL=https://auth-service.onrender.com
   GRPC_DRIVERS_HOST=drivers-service.onrender.com
   GRPC_VEHICLES_HOST=vehicles-service.onrender.com
   GRPC_ROUTES_HOST=routes-service.onrender.com
   GRPC_FUEL_HOST=fuel-service.onrender.com
   GRPC_AUTH_HOST=auth-service.onrender.com
   JWT_SECRET=el-mismo-secreto-que-usaste-en-todos
   ```
4. **Health Check:** `/actuator/health`
5. **Create**

---

## 📝 IMPORTANTE: URLs Reales vs URLs en Variables

Render te dará URLs reales como:
- `https://auth-service-abc123.onrender.com`
- `https://drivers-service-def456.onrender.com`

**Pero en las variables de entorno, usa:**
- `auth-service.onrender.com` (sin el código único)
- `drivers-service.onrender.com`

**O mejor aún:** Copia la URL real que Render te da y úsala directamente.

### Ejemplo:
Si Render te da: `https://auth-service-xyz789.onrender.com`

Entonces en Gateway y otros servicios:
```
AUTH_SERVICE_URL=https://auth-service-xyz789.onrender.com
GRPC_AUTH_HOST=auth-service-xyz789.onrender.com
```

---

## ✅ VERIFICAR QUE FUNCIONA

### 1. Espera a que todos los servicios estén "Live" (verde)

### 2. Verifica Health Checks:
```
https://tu-gateway.onrender.com/actuator/health
```
Debería responder: `{"status":"UP"}`

### 3. Prueba Login:
```bash
curl -X POST https://tu-gateway.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'
```

O usa Postman/Insomnia.

---

## 🔄 MANTENER SERVICIOS ACTIVOS (Para la Demo)

Si quieres evitar que se duerman antes de tu demo:

### Opción 1: Script de "Keep Alive"
Crea un script que haga requests cada 10 minutos:

```bash
# keep-alive.sh
while true; do
  curl https://tu-gateway.onrender.com/actuator/health
  sleep 600  # 10 minutos
done
```

### Opción 2: Antes de la Demo
5 minutos antes de empezar, haz un request a:
```
https://tu-gateway.onrender.com/actuator/health
```

Esto despertará todos los servicios.

---

## 🐛 SOLUCIÓN RÁPIDA DE PROBLEMAS

### "Build failed"
- ✅ Verifica que los Dockerfiles estén correctos
- ✅ Revisa los logs en Render
- ✅ Asegúrate de que `Docker Context` sea `.` (punto)

### "Health check failed"
- ✅ Espera 2-3 minutos más (a veces tarda)
- ✅ Revisa los logs del servicio
- ✅ Verifica que MongoDB Atlas esté accesible

### "gRPC connection failed"
- ✅ Verifica que todos los servicios estén "Live"
- ✅ Revisa las URLs en variables de entorno
- ✅ Asegúrate de NO haber definido `PORT` en servicios privados

### "MongoDB connection failed"
- ✅ En MongoDB Atlas → Network Access → Agrega `0.0.0.0/0`
- ✅ Verifica que la URI tenga las credenciales correctas
- ✅ Prueba la conexión desde MongoDB Compass

---

## ⏰ TIEMPO ESTIMADO

- **Crear cuenta y conectar repo:** 5 min
- **Crear 6 servicios:** 30-40 min (5-7 min cada uno)
- **Configurar variables:** 10 min
- **Verificar funcionamiento:** 10 min

**Total: ~1 hora** ⏱️

---

## 📊 RESUMEN PARA TU DEMO

✅ **Plan:** Gratis (6 servicios free)  
✅ **Costo:** $0  
✅ **Tiempo setup:** ~1 hora  
⚠️ **Limitación:** Se duermen después de 15 min inactivos  
✅ **Solución:** Despertarlos antes de la demo  

---

## 🎯 CHECKLIST FINAL

- [ ] Cuenta de Render creada
- [ ] Repositorio conectado
- [ ] Auth Service creado y "Live"
- [ ] Drivers Service creado
- [ ] Vehicles Service creado
- [ ] Routes Service creado
- [ ] Fuel Service creado
- [ ] Gateway Service creado y "Live"
- [ ] Todas las URLs configuradas
- [ ] Health checks pasando
- [ ] Login funciona
- [ ] 5 min antes de la demo: despertar servicios

---

## 💡 TIP EXTRA

**Para la demo del miércoles:**

1. **Hoy/Martes:** Despliega todo y verifica que funciona
2. **Miércoles en la mañana:** Haz un request a cada servicio para despertarlos
3. **Durante la demo:** Si un servicio se duerme, simplemente haz otro request y espera 30 segundos

---

**¡Todo listo para tu demo del miércoles!** 🚀

¿Necesitas ayuda con algún paso específico?

