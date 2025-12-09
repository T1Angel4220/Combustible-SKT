# 🚀 Guía Rápida - Despliegue en Render

## ✅ RESUMEN: SÍ puedes desplegar en Render

Tu proyecto **es compatible con Render** pero requiere configuración específica debido a:
- ✅ **gRPC** (comunicación interna entre servicios)
- ✅ **6 Microservicios** (cada uno debe ser un servicio separado)
- ✅ **MongoDB Atlas** (ya lo tienes configurado)

---

## 📋 PASOS PARA DESPLEGAR

### Paso 1: Preparar Repositorio

1. ✅ Los archivos `application-render.yml` ya están creados
2. ✅ El archivo `render.yaml` está listo (opcional, para Blueprint)
3. ✅ Verifica que todos los servicios compilen localmente

### Paso 2: Conectar Repositorio a Render

1. Ve a [render.com](https://render.com) y crea una cuenta
2. Conecta tu repositorio Git (GitHub/GitLab/Bitbucket)
3. Selecciona el repositorio `Combustible-SKT`

### Paso 3: Crear Servicios en Render

**IMPORTANTE:** Crea los servicios en este orden:

#### 3.1 Auth Service (PRIMERO - otros servicios lo necesitan)

1. **Nuevo** → **Private Service**
2. **Configuración:**
   - **Name:** `auth-service`
   - **Environment:** `Docker`
   - **Dockerfile Path:** `src/auth-service/Dockerfile`
   - **Docker Context:** `.` (punto, raíz del proyecto)
   - **Plan:** Free (o Starter si prefieres)

3. **Variables de Entorno:**
   ```
   SPRING_PROFILES_ACTIVE=render
   MONGODB_ATLAS_URI=tu-mongodb-atlas-uri-completa
   JWT_SECRET=tu-secreto-seguro-aqui-minimo-32-caracteres
   JWT_EXPIRATION=86400000
   ```

4. **⚠️ IMPORTANTE:** NO definas la variable `PORT` (permite múltiples puertos para gRPC)

5. **Health Check Path:** `/actuator/health`

6. **Click en "Create Private Service"**

#### 3.2 Drivers Service

1. **Nuevo** → **Private Service**
2. **Configuración:**
   - **Name:** `drivers-service`
   - **Environment:** `Docker`
   - **Dockerfile Path:** `src/drivers-service/Dockerfile`
   - **Docker Context:** `.`
   - **Plan:** Free

3. **Variables de Entorno:**
   ```
   SPRING_PROFILES_ACTIVE=render
   MONGODB_ATLAS_URI=tu-mongodb-atlas-uri-drivers-db
   JWT_SECRET=mismo-secreto-que-auth-service
   GRPC_AUTH_HOST=auth-service.onrender.com
   GRPC_AUTH_PORT=9095
   AUTH_SERVICE_URL=https://auth-service.onrender.com
   ```

4. **NO definir PORT**

5. **Health Check Path:** `/actuator/health`

#### 3.3 Vehicles Service

Similar a Drivers Service:
- **Name:** `vehicles-service`
- **MongoDB URI:** para `vehicles_db`
- **Variables gRPC:** Conectar con `drivers-service`

#### 3.4 Routes Service

Similar, conectado con `drivers-service` y `vehicles-service`

#### 3.5 Fuel Service

Similar, conectado con `routes-service` y `vehicles-service`

#### 3.6 Gateway Service (ÚLTIMO - el público)

1. **Nuevo** → **Web Service** (este es PÚBLICO)
2. **Configuración:**
   - **Name:** `combustible-gateway`
   - **Environment:** `Docker`
   - **Dockerfile Path:** `src/gateway-service/Dockerfile`
   - **Docker Context:** `.`
   - **Plan:** Starter (recomendado para servicio público)

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
   JWT_SECRET=mismo-secreto-que-todos
   ```

4. **SÍ definir PORT** (Render lo hará automáticamente para servicios web públicos)

5. **Health Check Path:** `/actuator/health`

---

## 🔧 CONFIGURACIÓN DE URLs EN RENDER

Después de crear cada servicio privado, Render te dará una URL del tipo:
- `https://auth-service-xxxx.onrender.com`

**IMPORTANTE:** Toma nota de estas URLs y actualiza las variables de entorno en:
1. Gateway Service
2. Otros servicios que se comuniquen entre sí

### Ejemplo de URLs reales:

Si Render te da:
- Auth: `https://auth-service-abc123.onrender.com`
- Drivers: `https://drivers-service-def456.onrender.com`

Entonces en Gateway Service, configura:
```
AUTH_SERVICE_URL=https://auth-service-abc123.onrender.com
GRPC_AUTH_HOST=auth-service-abc123.onrender.com
DRIVERS_SERVICE_URL=https://drivers-service-def456.onrender.com
GRPC_DRIVERS_HOST=drivers-service-def456.onrender.com
```

---

## ✅ VERIFICAR DESPLIEGUE

1. **Health Checks:**
   - Gateway: `https://tu-gateway.onrender.com/actuator/health`
   - Auth: `https://auth-service-xxx.onrender.com/actuator/health`
   - (Los servicios privados no son accesibles desde internet, solo desde Gateway)

2. **Probar Login:**
   ```
   POST https://tu-gateway.onrender.com/api/v1/auth/login
   {
     "usernameOrEmail": "admin",
     "password": "admin123"
   }
   ```

3. **Ver Logs:**
   - En cada servicio en Render, ve a la pestaña "Logs"
   - Busca errores de conexión gRPC o MongoDB

---

## 🐛 SOLUCIÓN DE PROBLEMAS COMUNES

### Error: "gRPC connection failed"

**Causa:** URLs incorrectas o servicios no iniciados

**Solución:**
1. Verifica que todos los servicios estén "Live" (verde) en Render
2. Revisa las URLs de gRPC en variables de entorno
3. Verifica que NO hayas definido `PORT` en servicios privados

### Error: "MongoDB connection failed"

**Causa:** URI incorrecta o IP no whitelisted en MongoDB Atlas

**Solución:**
1. En MongoDB Atlas → Network Access → Agrega `0.0.0.0/0` (permite desde cualquier IP)
2. Verifica que la URI incluye las credenciales correctas

### Error: "Service not found"

**Causa:** Servicios privados no accesibles desde Gateway

**Solución:**
- Los servicios privados solo son accesibles desde la red interna de Render
- Usa las URLs `.onrender.com` para comunicación interna
- Gateway debe usar HTTPS para servicios privados

### Servicios se "duermen" (Free Plan)

**Causa:** Plan gratuito duerme servicios después de 15 min inactivos

**Solución:**
- Upgrade a Starter ($7/mes) para servicios críticos
- O acepta que el primer request será lento (despertar servicio)

---

## 📊 COSTOS ESTIMADOS

### Opción 1: Todo en Free Plan
- ✅ 6 servicios gratuitos
- ⚠️ Servicios se duermen después de inactividad
- ⚠️ Primer request puede tardar 30-60 segundos

### Opción 2: Recomendado
- Gateway: Starter ($7/mes) - Siempre activo
- Servicios internos: Free (5 servicios) - Pueden dormir
- **Total:** ~$7/mes

### Opción 3: Todo Starter
- 6 servicios × $7 = $42/mes
- Todo siempre activo

---

## 📝 CHECKLIST FINAL

- [ ] Repositorio conectado a Render
- [ ] Auth Service creado y funcionando
- [ ] Drivers Service creado y conectado a Auth
- [ ] Vehicles Service creado
- [ ] Routes Service creado
- [ ] Fuel Service creado
- [ ] Gateway Service creado (público)
- [ ] Todas las variables de entorno configuradas
- [ ] MongoDB Atlas accesible desde Render
- [ ] Health checks pasando
- [ ] Login funciona desde Gateway público

---

## 🎉 ¡LISTO!

Una vez completado todo, tu aplicación estará disponible en:
- **Gateway Público:** `https://combustible-gateway.onrender.com`
- **API Base:** `https://combustible-gateway.onrender.com/api/v1`

---

**¿Problemas?** Revisa los logs en Render y el documento `DEPLOYMENT-RENDER.md` para más detalles.

