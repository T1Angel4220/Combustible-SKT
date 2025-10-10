# 🚛 Frontend de Gestión de Vehículos - SKT Combustible

Interfaz gráfica moderna y profesional para la gestión de vehículos del sistema SKT Combustible.

## 🎨 Características

- ✅ **Dashboard Interactivo**: Panel de control con estadísticas en tiempo real
- ✅ **CRUD Completo**: Crear, leer, actualizar y eliminar vehículos
- ✅ **Filtros Avanzados**: Búsqueda por placa, marca, modelo, tipo y estado
- ✅ **Diseño Responsive**: Adaptable a cualquier dispositivo
- ✅ **Notificaciones**: Sistema de alertas tipo toast
- ✅ **Modal de Detalles**: Vista detallada de cada vehículo
- ✅ **Diseño Consistente**: Mantiene el mismo estilo del login/dashboard

## 🚀 Cómo Usar

### Opción 1: Ejecutar con Spring Boot

1. **Iniciar el servicio:**
   ```bash
   cd src/vehicles-service
   mvn spring-boot:run
   ```

2. **Abrir el navegador:**
   ```
   http://localhost:8082/vehicles.html
   ```

### Opción 2: Ejecutar con script (Windows)

1. **Ejecutar el script:**
   ```bash
   start-vehicles-frontend.bat
   ```

2. El navegador se abrirá automáticamente

## 📁 Archivos del Frontend

```
src/vehicles-service/src/main/resources/static/
├── vehicles.html         # Página principal
├── vehicles-styles.css   # Estilos (mantiene diseño del auth-service)
└── vehicles-script.js    # Lógica y conexión con API REST
```

## 🎯 Funcionalidades

### Dashboard
- Estadísticas de vehículos (Total, Disponibles, En Mantenimiento, En Uso)
- Accesos rápidos a funcionalidades principales
- Distribución por tipo de maquinaria

### Gestión de Vehículos
- **Ver Lista**: Visualiza todos los vehículos con tarjetas informativas
- **Agregar Nuevo**: Formulario completo con validaciones
- **Editar**: Modificar información de vehículos existentes
- **Eliminar**: Desactivar vehículos con confirmación
- **Ver Detalles**: Modal con información completa del vehículo

### Filtros
- **Búsqueda en tiempo real**: Por placa, marca o modelo
- **Filtro por Tipo**: Camión, Volquete, Excavadora, etc.
- **Filtro por Estado**: Disponible, En Uso, Mantenimiento, Fuera de Servicio

## 🎨 Paleta de Colores

El frontend mantiene la misma paleta de colores del auth-service:

- **Primario**: `#ffc107` (Amarillo) y `#fd7e14` (Naranja)
- **Secundario**: `#1a1a2e` (Azul oscuro)
- **Acentos**: 
  - Éxito: `#28a745` (Verde)
  - Error: `#dc3545` (Rojo)
  - Info: `#17a2b8` (Cyan)
  - Advertencia: `#ffc107` (Amarillo)

## 🔌 Endpoints REST Utilizados

El frontend consume los siguientes endpoints del backend:

```
GET    /api/v1/vehicles              # Obtener todos los vehículos
GET    /api/v1/vehicles/{id}         # Obtener por ID
POST   /api/v1/vehicles              # Crear nuevo vehículo
PUT    /api/v1/vehicles/{id}         # Actualizar vehículo
DELETE /api/v1/vehicles/{id}         # Eliminar vehículo
GET    /api/v1/vehicles/tipo/{tipo}  # Filtrar por tipo
GET    /api/v1/vehicles/estado/{estado} # Filtrar por estado
GET    /api/v1/vehicles/disponibles  # Obtener disponibles
```

## 🛠️ Configuración

### Cambiar Puerto del Backend

Si tu backend usa un puerto diferente a 8082, edita `vehicles-script.js`:

```javascript
const API_BASE_URL = 'http://localhost:PUERTO/api/v1/vehicles';
```

### CORS

El backend ya tiene CORS habilitado en el controlador REST:

```java
@CrossOrigin(origins = "*")
```

Para producción, se recomienda restringir los orígenes permitidos.

## 📱 Responsive Design

La interfaz es completamente responsive:

- **Desktop (>1200px)**: Grid de 3-4 columnas
- **Tablet (768px-1200px)**: Grid de 2 columnas
- **Mobile (<768px)**: Grid de 1 columna

## 🎭 Tipos de Maquinaria Soportados

- 🚚 **Camión** (CAMION)
- 🚛 **Volquete** (VOLQUETE)
- 🏗️ **Excavadora** (EXCAVADORA)
- 📦 **Cargador** (CARGADOR)
- 🏗️ **Grúa** (GRUA)
- 🛣️ **Motoniveladora** (MOTONIVELADORA)

## 🚦 Estados Operativos

- ✅ **Disponible** (DISPONIBLE)
- 🚗 **En Uso** (EN_USO)
- 🔧 **Mantenimiento** (MANTENIMIENTO)
- ❌ **Fuera de Servicio** (FUERA_SERVICIO)

## 🐛 Solución de Problemas

### Error: No se cargan los vehículos

1. Verifica que el backend esté ejecutándose:
   ```bash
   curl http://localhost:8082/api/v1/vehicles/health
   ```

2. Verifica la consola del navegador (F12) para errores de CORS

3. Asegúrate de que MongoDB esté ejecutándose:
   ```bash
   docker ps | grep mongodb
   ```

### Error: CORS

Si ves errores de CORS, verifica que el controlador tenga:
```java
@CrossOrigin(origins = "*")
```

## 📚 Recursos Adicionales

- **Font Awesome**: Iconos (CDN)
- **Google Fonts**: Segoe UI (sistema)
- **Gradientes CSS**: Linear gradients para efectos

## 🔐 Integración con Auth Service

Para integrar autenticación completa:

1. Agregar verificación de token JWT
2. Agregar header `Authorization: Bearer {token}` en las peticiones
3. Implementar redirección a login si no hay token válido

## 📝 Notas

- Los datos se cargan dinámicamente desde el backend
- Las operaciones de eliminación son lógicas (soft delete)
- El formulario incluye validaciones en frontend y backend
- Las notificaciones se auto-ocultan después de 5 segundos

## 👨‍💻 Desarrollo

Para modificar el frontend:

1. Edita los archivos en `src/main/resources/static/`
2. Reinicia Spring Boot
3. Refresca el navegador (Ctrl + F5 para limpiar caché)

## 🎉 ¡Listo!

Tu frontend de gestión de vehículos está completo y listo para usar. ¡Disfruta gestionando tu flota de vehículos!

---

**Desarrollado por**: Sistema SKT  
**Versión**: 1.0.0  
**Fecha**: 2024

