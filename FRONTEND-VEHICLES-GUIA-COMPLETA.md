# 🚛 Guía Completa del Frontend de Vehículos - SKT Combustible

## 📋 Resumen

He creado una interfaz gráfica profesional y moderna para tu servicio de gestión de vehículos que **mantiene el mismo diseño** que el login y dashboard de tu compañero.

## 🎨 Lo que se ha Creado

### 1. **Archivos del Frontend**

```
src/vehicles-service/src/main/resources/static/
├── vehicles.html           # Página principal HTML
├── vehicles-styles.css     # Estilos CSS (misma paleta de colores)
└── vehicles-script.js      # Lógica JavaScript con API REST
```

### 2. **Documentación**

- `src/vehicles-service/README-FRONTEND.md` - Documentación técnica completa
- `FRONTEND-VEHICLES-GUIA-COMPLETA.md` - Esta guía

### 3. **Script de Inicio Rápido**

- `start-vehicles-frontend.bat` - Inicia todo automáticamente

## 🚀 Cómo Iniciar (Opción Recomendada)

### Método 1: Script Automático (MÁS FÁCIL) ⭐

```bash
# Desde la raíz del proyecto:
start-vehicles-frontend.bat
```

Este script:
1. ✅ Verifica y levanta MongoDB si no está corriendo
2. ✅ Compila el proyecto
3. ✅ Inicia el vehicles-service
4. ✅ Abre automáticamente el navegador en http://localhost:8082/vehicles.html

### Método 2: Manual Paso a Paso

```bash
# 1. Iniciar MongoDB
docker-compose -f docker-compose-mongodb.yml up -d mongodb

# 2. Iniciar el vehicles-service
cd src/vehicles-service
mvn spring-boot:run

# 3. Abrir en el navegador
# http://localhost:8082/vehicles.html
```

## 🎯 Funcionalidades Implementadas

### Dashboard de Vehículos
- 📊 **Estadísticas en Tiempo Real**
  - Total de vehículos
  - Vehículos disponibles
  - Vehículos en mantenimiento
  - Vehículos en uso

- 🎯 **Accesos Rápidos**
  - Ver todos los vehículos
  - Filtrar por disponibles
  - Filtrar por tipo (Camión, Excavadora, etc.)
  - Agregar nuevo vehículo

- 📈 **Distribución por Tipo**
  - Contador por cada tipo de maquinaria
  - Iconos distintivos
  - Interactivo (click para filtrar)

### Gestión de Vehículos

#### ✅ Ver Lista de Vehículos
- Tarjetas informativas con todos los datos
- Vista en grid responsive
- Badges de estado con colores
- Iconos según tipo de maquinaria

#### ➕ Agregar Nuevo Vehículo
- Formulario completo con validaciones
- Campos:
  - Placa (requerido, único)
  - Marca y Modelo (requeridos)
  - Año (requerido, 1990-2030)
  - Tipo de Maquinaria (6 opciones)
  - Estado Operativo (4 opciones)
  - Capacidad de Tanque (opcional)
  - Consumo Promedio (opcional)
  - Kilometraje Actual (opcional)
  - Estado Activo/Inactivo
- Botón "Datos de Prueba" para testing rápido

#### ✏️ Editar Vehículo
- Pre-carga todos los datos del vehículo
- Mismo formulario que crear
- Actualización en tiempo real

#### 🔍 Ver Detalles
- Modal con información completa
- Secciones organizadas:
  - Información General
  - Detalles Técnicos
  - Información del Sistema
- Fechas formateadas
- Diseño limpio y profesional

#### 🗑️ Eliminar Vehículo
- Confirmación antes de eliminar
- Eliminación lógica (soft delete)
- Notificación de éxito/error

### Filtros Avanzados

1. **Búsqueda en Tiempo Real**
   - Por placa (ej: "ABC-123")
   - Por marca (ej: "Volvo")
   - Por modelo (ej: "FH16")
   - Sin necesidad de presionar enter

2. **Filtro por Tipo**
   - Camión
   - Volquete
   - Excavadora
   - Cargador
   - Grúa
   - Motoniveladora

3. **Filtro por Estado**
   - Disponible
   - En Uso
   - Mantenimiento
   - Fuera de Servicio

4. **Combinación de Filtros**
   - Todos los filtros se pueden combinar
   - Actualización instantánea

### Notificaciones

Sistema de notificaciones toast:
- ✅ **Éxito** (verde): Operaciones completadas
- ❌ **Error** (rojo): Errores de validación o conexión
- ℹ️ **Info** (azul): Información general
- Auto-desaparición después de 5 segundos

## 🎨 Diseño Consistente

### Paleta de Colores (IGUAL al Auth-Service)

```css
/* Colores Principales */
--primary-yellow: #ffc107
--primary-orange: #fd7e14
--dark-blue: #1a1a2e
--secondary-blue: #16213e

/* Estados */
--success: #28a745
--error: #dc3545
--info: #17a2b8
--warning: #ffc107
```

### Elementos de Diseño

1. **Header**
   - Logo con icono de camión
   - Título "SKT Combustible"
   - Subtítulo "Gestión de Vehículos"
   - Botones de navegación con gradientes

2. **Cards y Tarjetas**
   - Bordes redondeados (15px)
   - Sombras sutiles
   - Hover effects
   - Transiciones suaves (0.3s)

3. **Botones**
   - Gradientes llamativos
   - Iconos de Font Awesome
   - Efecto hover con elevación
   - Estados disabled

4. **Formularios**
   - Inputs con bordes redondeados
   - Focus con color amarillo
   - Placeholders descriptivos
   - Validaciones visuales

5. **Responsive**
   - Mobile-first design
   - Breakpoints: 768px, 480px
   - Grid adaptativo
   - Navegación vertical en mobile

## 📱 Responsive Design

### Desktop (>1200px)
- Grid de 3-4 columnas para vehículos
- Header horizontal
- Filtros en línea

### Tablet (768px-1200px)
- Grid de 2 columnas
- Header compacto
- Filtros apilados

### Mobile (<768px)
- Grid de 1 columna
- Header vertical
- Botones full-width
- Navegación en columna

## 🔌 Integración con Backend

### Endpoints REST Consumidos

```javascript
// Base URL
const API_BASE_URL = 'http://localhost:8082/api/v1/vehicles';

// Endpoints
GET    /api/v1/vehicles              // Lista todos
GET    /api/v1/vehicles/{id}         // Obtener por ID
POST   /api/v1/vehicles              // Crear nuevo
PUT    /api/v1/vehicles/{id}         // Actualizar
DELETE /api/v1/vehicles/{id}         // Eliminar
GET    /api/v1/vehicles/tipo/{tipo}  // Por tipo
GET    /api/v1/vehicles/estado/{estado} // Por estado
GET    /api/v1/vehicles/disponibles  // Disponibles
GET    /api/v1/vehicles/health       // Health check
```

### Formato de Datos

**Request (Crear/Actualizar):**
```json
{
  "placa": "ABC-123",
  "marca": "Volvo",
  "modelo": "FH16",
  "anio": 2023,
  "tipoMaquinaria": "CAMION",
  "estadoOperativo": "DISPONIBLE",
  "capacidadTanque": 300.0,
  "consumoPromedio": 25.5,
  "kilometrajeActual": 15000.0,
  "activo": true
}
```

**Response:**
```json
{
  "id": "65f3c4a8e9b7d12a3c4e5f6a",
  "placa": "ABC-123",
  "marca": "Volvo",
  "modelo": "FH16",
  "anio": 2023,
  "tipoMaquinaria": "CAMION",
  "estadoOperativo": "DISPONIBLE",
  "capacidadTanque": 300.0,
  "consumoPromedio": 25.5,
  "kilometrajeActual": 15000.0,
  "activo": true,
  "fechaCreacion": "2024-03-15T10:30:00",
  "fechaActualizacion": "2024-03-15T10:30:00"
}
```

## 🐛 Solución de Problemas Comunes

### 1. No se carga la página

**Problema:** Error 404 al acceder a http://localhost:8082/vehicles.html

**Solución:**
```bash
# Verifica que los archivos estén en la ubicación correcta
ls src/vehicles-service/src/main/resources/static/

# Debería mostrar:
# vehicles.html
# vehicles-styles.css
# vehicles-script.js
```

### 2. No se cargan los vehículos

**Problema:** Dashboard muestra "0" en todas las estadísticas

**Soluciones:**
```bash
# A. Verifica que el backend esté corriendo
curl http://localhost:8082/api/v1/vehicles/health

# B. Verifica MongoDB
docker ps | grep mongodb

# C. Verifica la consola del navegador (F12)
# Busca errores de CORS o conexión

# D. Inserta datos de prueba
mongosh mongodb://admin:admin123@localhost:27017/vehicles_db --authSource=admin
db.vehicles.insertOne({
  placa: "TEST-001",
  marca: "Volvo",
  modelo: "FH16",
  anio: 2023,
  tipoMaquinaria: "CAMION",
  estadoOperativo: "DISPONIBLE",
  activo: true,
  fechaCreacion: new Date(),
  fechaActualizacion: new Date()
})
```

### 3. Error de CORS

**Problema:** Console shows "CORS policy: No 'Access-Control-Allow-Origin'"

**Solución:**
El controlador ya tiene `@CrossOrigin(origins = "*")` configurado.
Si persiste, verifica que el backend se reinició después de los cambios.

### 4. Formulario no se envía

**Problema:** Botón "Guardar" no hace nada

**Solución:**
```javascript
// Abre la consola del navegador (F12) y busca errores
// Los campos marcados con * son obligatorios
// Verifica que MongoDB esté corriendo
```

## 📊 Estructura de Archivos Final

```
Combustible-SKT/
├── src/
│   └── vehicles-service/
│       ├── src/
│       │   └── main/
│       │       ├── java/
│       │       │   └── com/skt/combustible/vehicles/
│       │       │       ├── application/service/
│       │       │       │   └── VehicleService.java
│       │       │       ├── domain/
│       │       │       │   ├── entity/Vehicle.java
│       │       │       │   └── dto/...
│       │       │       └── infrastructure/
│       │       │           └── rest/
│       │       │               └── VehicleRestController.java
│       │       └── resources/
│       │           ├── static/                    ⭐ NUEVO
│       │           │   ├── vehicles.html         ⭐ NUEVO
│       │           │   ├── vehicles-styles.css   ⭐ NUEVO
│       │           │   └── vehicles-script.js    ⭐ NUEVO
│       │           └── application.yml
│       ├── README-FRONTEND.md                     ⭐ NUEVO
│       └── pom.xml
├── start-vehicles-frontend.bat                    ⭐ NUEVO
├── FRONTEND-VEHICLES-GUIA-COMPLETA.md            ⭐ NUEVO
└── docker-compose-mongodb.yml
```

## 🎓 Próximos Pasos (Opcionales)

### 1. Integrar Autenticación
```javascript
// En vehicles-script.js, agregar:
const authToken = localStorage.getItem('authToken');
if (!authToken) {
    window.location.href = '/index.html'; // Redirigir a login
}

// En cada fetch, agregar header:
headers: {
    'Authorization': `Bearer ${authToken}`,
    'Content-Type': 'application/json'
}
```

### 2. Agregar Paginación
```javascript
// Para manejar grandes cantidades de datos
// Implementar botones "Anterior" y "Siguiente"
// Mostrar solo 12 vehículos por página
```

### 3. Exportar Reportes
```javascript
// Botón para descargar lista de vehículos en Excel/PDF
// Integrar librería como jsPDF o xlsx
```

### 4. Gráficos Estadísticos
```javascript
// Agregar Chart.js para visualizaciones
// Gráfico de pastel de tipos de maquinaria
// Gráfico de barras de estados operativos
```

## ✅ Checklist de Verificación

Antes de mostrar el proyecto, verifica:

- [ ] MongoDB está corriendo
- [ ] Vehicles-service compila sin errores
- [ ] Backend responde en http://localhost:8082/api/v1/vehicles/health
- [ ] Frontend carga en http://localhost:8082/vehicles.html
- [ ] Dashboard muestra estadísticas
- [ ] Puedes crear un vehículo de prueba
- [ ] Los filtros funcionan correctamente
- [ ] Modal de detalles se abre correctamente
- [ ] Diseño es responsive (prueba en diferentes tamaños)

## 🎉 ¡Listo para Usar!

Tu interfaz gráfica está **100% completa y funcional**. Mantiene el mismo diseño profesional del login/dashboard y se integra perfectamente con tu backend.

### Para Iniciar:
```bash
start-vehicles-frontend.bat
```

### URLs Importantes:
- **Frontend**: http://localhost:8082/vehicles.html
- **API**: http://localhost:8082/api/v1/vehicles
- **Health**: http://localhost:8082/api/v1/vehicles/health

---

**¡Disfruta tu nueva interfaz de gestión de vehículos!** 🚛✨

**Desarrollado para**: SKT Combustible System  
**Versión**: 1.0.0  
**Fecha**: Octubre 2024  
**Compatible con**: Spring Boot 3.2.0, MongoDB 7.0, Java 17

