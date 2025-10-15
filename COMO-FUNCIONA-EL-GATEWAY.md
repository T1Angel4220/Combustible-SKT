# 🌉 ¿CÓMO FUNCIONA EL GATEWAY SERVICE?

## 📋 Tabla de Contenidos
1. [¿Qué es un API Gateway?](#qué-es-un-api-gateway)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Flujo de Comunicación](#flujo-de-comunicación)
4. [Ejemplo Práctico Paso a Paso](#ejemplo-práctico-paso-a-paso)
5. [Ventajas del Gateway](#ventajas-del-gateway)
6. [Código Explicado](#código-explicado)

---

## 🤔 ¿Qué es un API Gateway?

Un **API Gateway** es como un **portero o recepcionista** de un edificio con muchas oficinas (microservicios).

### Analogía del Mundo Real:

Imagina un edificio con diferentes oficinas:
- 🏢 **Piso 1**: Oficina de Autenticación (Auth Service)
- 🏢 **Piso 2**: Oficina de Choferes (Drivers Service)
- 🏢 **Piso 3**: Oficina de Vehículos (Vehicles Service)

**SIN Gateway (Antes):**
```
Cliente → Tiene que saber dónde está cada oficina
         → Va directamente a cada piso
         → Debe recordar cada dirección y puerto
```

**CON Gateway (Ahora):**
```
Cliente → Habla con el Portero (Gateway)
         → El Portero sabe dónde está cada oficina
         → El Portero te lleva a la oficina correcta
         → Tú solo hablas con el Portero
```

---

## 🏗️ Arquitectura del Sistema

### Antes (Sin Gateway):
```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (Navegador)                      │
│                                                              │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Auth Panel  │  │ Drivers Panel│  │ Vehicles Panel│       │
│  └─────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
         │                  │                  │
         ↓                  ↓                  ↓
   localhost:8085     localhost:8081     localhost:8082
         │                  │                  │
         ↓                  ↓                  ↓
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Auth Service │  │Drivers Service│  │Vehicles Service│
│   :8085      │  │    :8081      │  │    :8082      │
└──────────────┘  └──────────────┘  └──────────────┘
```

**❌ Problemas:**
- El frontend debe conocer **3 URLs diferentes**
- Si cambia un puerto, hay que modificar el frontend
- Difícil de mantener y escalar
- No hay un punto centralizado de control

---

### Ahora (Con Gateway):
```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (Navegador)                      │
│                                                              │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Auth Panel  │  │ Drivers Panel│  │ Vehicles Panel│       │
│  └─────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
         │                  │                  │
         └──────────────────┼──────────────────┘
                            ↓
                   localhost:8090 (UNA SOLA URL!)
                            ↓
         ┌──────────────────────────────────────┐
         │      🌉 GATEWAY SERVICE :8090         │
         │                                       │
         │  ┌─────────────────────────────────┐ │
         │  │  Enrutador Inteligente          │ │
         │  │  - Reenvía requests             │ │
         │  │  - Maneja JWT tokens            │ │
         │  │  - Hace proxy HTTP              │ │
         │  └─────────────────────────────────┘ │
         └──────────────────────────────────────┘
                  │           │           │
        ┌─────────┘           │           └─────────┐
        ↓                     ↓                     ↓
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Auth Service │    │Drivers Service│    │Vehicles Service│
│   :8085      │    │    :8081      │    │    :8082      │
└──────────────┘    └──────────────┘    └──────────────┘
```

**✅ Ventajas:**
- El frontend **solo conoce 1 URL**: `localhost:8090`
- Gateway maneja el enrutamiento interno
- Fácil de escalar y mantener
- Control centralizado

---

## 🔄 Flujo de Comunicación

### Ejemplo 1: Login de Usuario

```
PASO 1: Cliente hace login
┌──────────────┐
│   Frontend   │ POST http://localhost:8090/api/v1/auth/login
│ (navegador)  │ Body: {"usernameOrEmail":"admin","password":"admin123"}
└──────────────┘
       │
       ↓
┌──────────────────────────────────────────────────────────┐
│ GATEWAY :8090                                            │
│                                                          │
│ AuthGatewayController.java                              │
│                                                          │
│ @PostMapping("/login")                                  │
│ public ResponseEntity<Object> login(...) {              │
│                                                          │
│   // 1. Recibe la petición del cliente                 │
│   String url = authServiceUrl + "/api/auth/login";     │
│   //    = "http://localhost:8085/api/auth/login"       │
│                                                          │
│   // 2. Crea headers                                    │
│   HttpHeaders headers = getHeaders(request);           │
│                                                          │
│   // 3. Hace proxy al Auth Service                     │
│   ResponseEntity<Object> response =                     │
│       restTemplate.postForEntity(url, entity, ...);     │
│                                                          │
│   // 4. Devuelve la respuesta al cliente               │
│   return response;                                      │
│ }                                                        │
└──────────────────────────────────────────────────────────┘
       │
       ↓ HTTP POST a localhost:8085
┌──────────────────────────────────────────────────────────┐
│ AUTH SERVICE :8085                                       │
│                                                          │
│ AuthController.java                                     │
│                                                          │
│ @PostMapping("/login")                                  │
│ public ResponseEntity<AuthResponse> login(...) {        │
│   // Valida credenciales                               │
│   // Genera JWT token                                   │
│   return ResponseEntity.ok(authResponse);              │
│ }                                                        │
└──────────────────────────────────────────────────────────┘
       │
       ↓ Respuesta con token JWT
┌──────────────────────────────────────────────────────────┐
│ GATEWAY :8090                                            │
│ Recibe: {                                               │
│   "token": "eyJhbGci...",                               │
│   "user": { ... }                                        │
│ }                                                        │
│                                                          │
│ Reenvía la respuesta al cliente                         │
└──────────────────────────────────────────────────────────┘
       │
       ↓
┌──────────────┐
│   Frontend   │ Recibe el token y lo guarda
│              │ localStorage.setItem('authToken', token)
└──────────────┘
```

---

### Ejemplo 2: Obtener Lista de Choferes (CON JWT)

```
PASO 1: Cliente solicita choferes
┌──────────────┐
│   Frontend   │ GET http://localhost:8090/api/v1/drivers?page=0&size=10
│              │ Headers: { Authorization: "Bearer eyJhbGci..." }
└──────────────┘
       │
       ↓
┌──────────────────────────────────────────────────────────┐
│ GATEWAY :8090                                            │
│                                                          │
│ DriversGatewayController.java                           │
│                                                          │
│ @GetMapping                                             │
│ public ResponseEntity<Object> getAllDrivers(            │
│     @RequestParam int page,                             │
│     @RequestParam int size,                             │
│     @RequestHeader("Authorization") String authHeader   │
│ ) {                                                      │
│                                                          │
│   // 1. Construye la URL del servicio backend          │
│   String url = driversServiceUrl +                     │
│       "/api/v1/drivers/all?page=0&size=10";            │
│   //    = "http://localhost:8081/api/v1/drivers/all?...│
│                                                          │
│   // 2. ⭐ IMPORTANTE: Reenvía el token JWT            │
│   HttpHeaders headers = createHeadersWithAuth(authHeader);│
│   // headers contiene: Authorization: Bearer eyJhbGci...│
│                                                          │
│   // 3. Hace proxy al Drivers Service                  │
│   HttpEntity<String> entity = new HttpEntity<>(headers);│
│   ResponseEntity<Object> response =                     │
│       restTemplate.exchange(url, GET, entity, ...);     │
│                                                          │
│   // 4. Devuelve la respuesta al cliente               │
│   return ResponseEntity.ok(response.getBody());        │
│ }                                                        │
└──────────────────────────────────────────────────────────┘
       │
       ↓ HTTP GET con JWT a localhost:8081
┌──────────────────────────────────────────────────────────┐
│ DRIVERS SERVICE :8081                                    │
│                                                          │
│ JwtAuthenticationFilter.java                            │
│ - Intercepta la request                                 │
│ - Extrae el token JWT del header                        │
│ - Valida el token con la clave secreta                  │
│ - Si es válido, permite acceso                          │
│                                                          │
│ DriverRestController.java                               │
│ @GetMapping("/all")                                     │
│ public ResponseEntity<Page<DriverResponse>> getAll(...) {│
│   // Obtiene choferes de MongoDB                        │
│   Page<DriverResponse> drivers = service.getAllDrivers();│
│   return ResponseEntity.ok(drivers);                    │
│ }                                                        │
└──────────────────────────────────────────────────────────┘
       │
       ↓ Respuesta con datos paginados
┌──────────────────────────────────────────────────────────┐
│ GATEWAY :8090                                            │
│ Recibe: {                                               │
│   "content": [ { chofer1 }, { chofer2 }, ... ],         │
│   "totalPages": 5,                                       │
│   "totalElements": 45                                    │
│ }                                                        │
│                                                          │
│ Reenvía la respuesta al cliente                         │
└──────────────────────────────────────────────────────────┘
       │
       ↓
┌──────────────┐
│   Frontend   │ Renderiza los choferes en la interfaz
└──────────────┘
```

---

## 💻 Código Explicado

### 1. Configuración del Gateway (`application.yml`)

```yaml
server:
  port: 8090  # Puerto del Gateway (punto de entrada único)

# URLs de los servicios backend
service:
  urls:
    drivers-service: http://localhost:8081
    vehicles-service: http://localhost:8082
    auth-service: http://localhost:8085
```

**¿Qué hace esto?**
- Define el puerto del Gateway: `8090`
- Configura las URLs de todos los servicios backend
- Si cambias un puerto del backend, solo modificas aquí

---

### 2. DriversGatewayController (Proxy HTTP)

```java
@RestController
@RequestMapping("/api/v1/drivers")  // Ruta del Gateway
public class DriversGatewayController {

    @Value("${service.urls.drivers-service}")
    private String driversServiceUrl;  // = "http://localhost:8081"

    private final RestTemplate restTemplate;  // Cliente HTTP

    // Método de ejemplo: Obtener todos los choferes
    @GetMapping
    public ResponseEntity<Object> getAllDrivers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // 1️⃣ Construye la URL completa del servicio backend
        String url = driversServiceUrl + "/api/v1/drivers/all?page=" + page + "&size=" + size;
        //    = "http://localhost:8081/api/v1/drivers/all?page=0&size=10"

        // 2️⃣ Crea headers HTTP y REENVÍA el token JWT
        HttpHeaders headers = createHeadersWithAuth(authHeader);
        //    headers.set("Authorization", "Bearer eyJhbGci...")

        // 3️⃣ Crea la entidad HTTP con los headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 4️⃣ Hace la petición HTTP al servicio backend usando RestTemplate
        ResponseEntity<Object> response = restTemplate.exchange(
            url,           // URL del backend
            HttpMethod.GET, // Método HTTP
            entity,        // Headers (con JWT)
            Object.class   // Tipo de respuesta
        );

        // 5️⃣ Devuelve la respuesta al cliente
        return ResponseEntity.ok(response.getBody());
    }

    // Método helper para crear headers con autenticación
    private HttpHeaders createHeadersWithAuth(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        
        // Si hay token JWT, lo agrega a los headers
        if (authHeader != null && !authHeader.isEmpty()) {
            headers.set("Authorization", authHeader);
            logger.debug("Reenviando token JWT al servicio backend");
        }
        
        return headers;
    }
}
```

**¿Qué hace este código?**

1. **Recibe la petición del cliente** en `/api/v1/drivers`
2. **Extrae el token JWT** del header `Authorization`
3. **Reenvía la petición** a `http://localhost:8081/api/v1/drivers/all`
4. **Incluye el token JWT** en la petición al backend
5. **Devuelve la respuesta** del backend al cliente

---

### 3. Configuración de RestTemplate

```java
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**¿Qué es RestTemplate?**
- Es un cliente HTTP de Spring
- Permite hacer peticiones HTTP a otros servicios
- Similar a `fetch()` en JavaScript o `axios`

---

## 🎯 Ventajas del Gateway

### 1. **Punto de Entrada Único**
```
Antes:
- Auth:     http://localhost:8085/api/auth/login
- Drivers:  http://localhost:8081/api/v1/drivers
- Vehicles: http://localhost:8082/api/v1/vehicles

Ahora:
- Auth:     http://localhost:8090/api/v1/auth/login
- Drivers:  http://localhost:8090/api/v1/drivers
- Vehicles: http://localhost:8090/api/v1/vehicles

👉 Una sola URL base, diferentes rutas
```

### 2. **Seguridad Centralizada**
- El Gateway puede validar tokens antes de reenviar
- Puede agregar rate limiting (límite de peticiones)
- Puede registrar todas las peticiones (logging)

### 3. **Fácil Mantenimiento**
```javascript
// Frontend (drivers-script.js)
const API_BASE_URL = 'http://localhost:8090/api/v1/drivers';

// Si cambias el puerto del Drivers Service de 8081 a 9001:
// ✅ Solo modificas la configuración del Gateway
// ❌ NO necesitas modificar el frontend
```

### 4. **Load Balancing (Futuro)**
```
Gateway puede distribuir peticiones entre múltiples instancias:

         Gateway
            │
    ┌───────┼───────┐
    ↓       ↓       ↓
Drivers1 Drivers2 Drivers3
 :8081    :8082    :8083
```

### 5. **Transformación de Respuestas**
- El Gateway puede modificar las respuestas antes de enviarlas al cliente
- Puede agregar información adicional
- Puede cambiar el formato de datos

---

## 📊 Comparación de Flujos

### Flujo SIN Gateway:
```
┌──────────┐                           ┌────────────────┐
│ Frontend │ ──── JWT Token ────────→  │ Drivers Service│
│          │                           │    :8081       │
│          │ ←──── Datos ─────────────  │                │
└──────────┘                           └────────────────┘

Frontend debe:
✗ Conocer el puerto del Drivers Service
✗ Manejar la URL directa
✗ Si cambia el puerto, modificar el código
```

### Flujo CON Gateway:
```
┌──────────┐         ┌─────────┐         ┌────────────────┐
│ Frontend │ ─ JWT → │ Gateway │ ─ JWT → │ Drivers Service│
│          │         │  :8090  │         │    :8081       │
│          │ ← Datos │         │ ← Datos │                │
└──────────┘         └─────────┘         └────────────────┘

Frontend debe:
✓ Solo conocer el Gateway
✓ No importa dónde esté el Drivers Service
✓ Si cambia el puerto, el Gateway lo maneja
```

---

## 🚀 URLs del Sistema

### Frontend solo usa el Gateway:

```bash
# Auth
http://localhost:8090/api/v1/auth/login
http://localhost:8090/api/v1/auth/register

# Drivers
http://localhost:8090/api/v1/drivers
http://localhost:8090/api/v1/drivers/{id}
http://localhost:8090/api/v1/drivers/{id}/activate

# Vehicles
http://localhost:8090/api/v1/vehicles
http://localhost:8090/api/v1/vehicles/{id}
```

### El Gateway internamente usa:

```bash
# Auth Service
http://localhost:8085/api/auth/login
http://localhost:8085/api/auth/register

# Drivers Service
http://localhost:8081/api/v1/drivers/all
http://localhost:8081/api/v1/drivers/{id}
http://localhost:8081/api/v1/drivers/{id}/activate

# Vehicles Service
http://localhost:8082/api/v1/vehicles
http://localhost:8082/api/v1/vehicles/{id}
```

---

## 🔑 Resumen

El Gateway es como un **intermediario inteligente** que:

1. **Recibe** peticiones del cliente en un solo puerto (8090)
2. **Identifica** a qué servicio debe ir la petición según la ruta
3. **Reenvía** la petición al servicio correcto (Auth, Drivers, Vehicles)
4. **Incluye** el token JWT para autenticación
5. **Devuelve** la respuesta del servicio al cliente

**Analogía final:**
```
El Gateway es como un "traductor-mensajero":
- El cliente habla con el Gateway (un solo idioma, una sola dirección)
- El Gateway traduce y lleva el mensaje al servicio correcto
- El servicio responde al Gateway
- El Gateway devuelve la respuesta al cliente

Sin Gateway: El cliente debe hablar 3 idiomas y conocer 3 direcciones
Con Gateway: El cliente solo habla 1 idioma y conoce 1 dirección
```

---

## 📝 Notas Importantes

1. **El Gateway NO almacena datos**, solo reenvía peticiones
2. **El Gateway NO valida JWT** (por ahora), solo lo reenvía a los servicios
3. **Cada servicio (Auth, Drivers, Vehicles) valida su propio JWT**
4. **El Gateway usa HTTP REST** para comunicarse con los servicios
5. **El frontend solo conoce el Gateway**, no los servicios internos

---

**¿Preguntas?** 🤔

Si tienes dudas sobre algún paso específico, revisa los ejemplos de código o los diagramas de flujo.

