package com.skt.combustible.routes.infrastructure.rest;

import com.skt.combustible.routes.application.service.RouteService;
import com.skt.combustible.routes.domain.dto.RouteCreateRequest;
import com.skt.combustible.routes.domain.dto.RouteResponse;
import com.skt.combustible.routes.domain.dto.RouteUpdateRequest;
import com.skt.combustible.routes.domain.entity.Route;
import com.skt.combustible.routes.domain.exception.RouteNotFoundException;
import com.skt.combustible.shared.domain.enums.RolUsuario;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para el servicio de rutas
 * Expone endpoints HTTP para la gestión de rutas
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/routes")
@CrossOrigin(origins = "*")
public class RouteRestController {

    private static final Logger logger = LoggerFactory.getLogger(RouteRestController.class);

    @Autowired
    private RouteService routeService;

    @Autowired(required = false)
    private com.skt.combustible.routes.infrastructure.client.GoogleMapsClient googleMapsClient;

    @Autowired(required = false)
    private com.skt.combustible.routes.infrastructure.client.OpenStreetMapClient openStreetMapClient;

    @Autowired
    private com.skt.combustible.routes.infrastructure.client.AssignmentsRestClient assignmentsRestClient;

    @Autowired
    private com.skt.combustible.routes.infrastructure.client.DriversRestClient driversRestClient;

    @Autowired
    private com.skt.combustible.routes.infrastructure.client.AuthRestClient authRestClient;

    @Autowired
    private com.skt.combustible.routes.infrastructure.security.JwtService jwtService;

    /**
     * Verifica si el usuario actual tiene un rol específico
     */
    private boolean hasRole(RolUsuario rol) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        String roleString = "ROLE_" + rol.name();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(roleString));
    }

    /**
     * Verifica si el usuario es ADMIN o SUPERVISOR
     */
    private boolean isAdminOrSupervisor() {
        return hasRole(RolUsuario.ADMIN) || hasRole(RolUsuario.SUPERVISOR);
    }

    /**
     * Obtiene el ID del conductor asociado al usuario actual si es CONDUCTOR
     */
    private String getCurrentDriverId(String token) {
        try {
            if (hasRole(RolUsuario.CONDUCTOR)) {
                // Intentar obtener userId del token directamente
                String userId = jwtService.getUserIdFromToken(token);
                logger.debug("UserId extraído del token (directo): {}", userId);
                
                // Si no se pudo obtener del token o parece ser un username, obtener desde auth-service
                if (userId == null || userId.isEmpty() || !userId.matches("^[0-9a-fA-F]{24}$")) {
                    logger.debug("UserId no válido o parece ser username, obteniendo desde auth-service");
                    userId = authRestClient.getUserIdFromToken(token);
                    logger.debug("UserId obtenido desde auth-service: {}", userId);
                }
                
                // Primero intentar buscar por usuarioId
                if (userId != null && !userId.isEmpty()) {
                    Map<String, Object> driver = driversRestClient.getDriverByUsuarioId(userId, token);
                    if (driver != null && driver.get("id") != null) {
                        String driverId = driver.get("id").toString();
                        logger.info("Conductor encontrado para userId {}: driverId {}", userId, driverId);
                        return driverId;
                    } else {
                        logger.warn("No se encontró conductor para userId: {}", userId);
                    }
                }
                
                // Si no se encontró por usuarioId, intentar buscar por email
                String email = authRestClient.getEmailFromToken(token);
                if (email != null && !email.isEmpty()) {
                    logger.debug("Intentando buscar conductor por email: {}", email);
                    Map<String, Object> driver = driversRestClient.getDriverByEmail(email, token);
                    if (driver != null && driver.get("id") != null) {
                        String driverId = driver.get("id").toString();
                        logger.info("Conductor encontrado para email {}: driverId {}", email, driverId);
                        return driverId;
                    } else {
                        logger.warn("No se encontró conductor para email: {}", email);
                    }
                }
                
                logger.warn("No se pudo obtener conductor ni por userId ni por email");
            }
        } catch (Exception e) {
            logger.error("Error obteniendo conductor actual: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Obtiene todas las rutas activas
     * Todos los roles pueden ver rutas, pero CONDUCTOR solo ve las suyas
     * 
     * @param authHeader Header de autorización
     * @param allRoutes Parámetro opcional para obtener todas las rutas sin filtrar por rol (para fuel-service)
     */
    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAllRoutes(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean allRoutes) {
        try {
            logger.info("REST: Obteniendo todas las rutas (all={})", allRoutes);
            List<RouteResponse> routes;
            
            // Si se solicita todas las rutas (para fuel-service), no filtrar por rol
            if (allRoutes) {
                logger.info("REST: Obteniendo todas las rutas sin filtrar por rol");
                routes = routeService.obtenerTodasLasRutas();
            } else if (hasRole(RolUsuario.CONDUCTOR) && authHeader != null && authHeader.startsWith("Bearer ")) {
                // Si es CONDUCTOR, filtrar solo sus rutas
                String token = authHeader.substring(7);
                String driverId = getCurrentDriverId(token);
                if (driverId != null) {
                    logger.info("REST: Filtrando rutas para conductor con ID: {}", driverId);
                    routes = routeService.obtenerRutasPorChofer(driverId);
                } else {
                    logger.warn("REST: No se pudo obtener el ID del conductor. El usuario puede no tener un conductor asociado. Retornando lista vacía.");
                    // Si no se encuentra el conductor, retornar lista vacía
                    // El usuario debe tener un conductor asociado para ver rutas
                    routes = java.util.Collections.emptyList();
                }
            } else {
                routes = routeService.obtenerTodasLasRutas();
            }
            
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            logger.error("Error obteniendo todas las rutas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene una ruta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable("id") String id) {
        try {
            logger.info("REST: Obteniendo ruta por ID: {}", id);
            Optional<RouteResponse> route = routeService.obtenerRutaPorId(id);
            if (route.isPresent()) {
                return ResponseEntity.ok(route.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RouteNotFoundException e) {
            logger.warn("Ruta no encontrada con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error obteniendo ruta por ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea una nueva ruta
     * ADMIN y SUPERVISOR pueden crear rutas para cualquier conductor
     * CONDUCTOR solo puede crear rutas para sí mismo
     */
    @PostMapping
    public ResponseEntity<?> createRoute(@Valid @RequestBody RouteCreateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Si es CONDUCTOR, forzar que la ruta sea para él mismo
            if (hasRole(RolUsuario.CONDUCTOR)) {
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(java.util.Map.of("error", "Token de autenticación requerido", "status", 401));
                }
                
                String token = authHeader.substring(7);
                String driverId = getCurrentDriverId(token);
                
                if (driverId == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(java.util.Map.of("error", "No se pudo identificar el conductor asociado", "status", 403));
                }
                
                // Forzar que el choferId sea el del conductor actual
                if (request.getChoferId() == null || !request.getChoferId().equals(driverId)) {
                    logger.info("REST: CONDUCTOR intentando crear ruta, forzando choferId a: {}", driverId);
                    request.setChoferId(driverId);
                }
            } else if (!isAdminOrSupervisor()) {
                logger.warn("Intento de crear ruta por usuario sin permisos");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of("error", "No tiene permisos para crear rutas", "status", 403));
            }
            
            logger.info("REST: Creando nueva ruta: {} -> {}", request.getOrigen(), request.getDestino());
            RouteResponse route = routeService.crearRuta(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(route);
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al crear ruta: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage(), "status", 400));
        } catch (Exception e) {
            logger.error("Error creando ruta: {}", e.getMessage(), e);
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("UNAVAILABLE")) {
                errorMessage = "El servicio de vehículos no está disponible. Por favor, verifica que el vehicles-service esté corriendo en el puerto 9092.";
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", errorMessage != null ? errorMessage : "Error interno del servidor", "status", 500));
        }
    }

    /**
     * Actualiza una ruta existente
     * ADMIN y SUPERVISOR pueden actualizar cualquier ruta
     * CONDUCTOR solo puede actualizar sus propias rutas
     */
    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable("id") String id,
                                                     @Valid @RequestBody RouteUpdateRequest request,
                                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Verificar permisos
            if (hasRole(RolUsuario.CONDUCTOR)) {
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
                
                String token = authHeader.substring(7);
                String driverId = getCurrentDriverId(token);
                
                // Obtener la ruta para verificar que pertenece al conductor
                Optional<RouteResponse> existingRoute = routeService.obtenerRutaPorId(id);
                if (existingRoute.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                if (driverId == null || !driverId.equals(existingRoute.get().getChoferId())) {
                    logger.warn("CONDUCTOR intentando actualizar ruta que no le pertenece. Ruta ID: {}, Conductor actual: {}", 
                            id, driverId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } else if (!isAdminOrSupervisor()) {
                logger.warn("Intento de actualizar ruta por usuario sin permisos");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            logger.info("REST: Actualizando ruta con ID: {}", id);
            RouteResponse route = routeService.actualizarRuta(id, request);
            return ResponseEntity.ok(route);
        } catch (RouteNotFoundException e) {
            logger.warn("Ruta no encontrada con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al actualizar ruta: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error actualizando ruta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina una ruta (soft delete)
     * ADMIN puede eliminar cualquier ruta
     * CONDUCTOR solo puede eliminar sus propias rutas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable("id") String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Verificar permisos
            if (hasRole(RolUsuario.CONDUCTOR)) {
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
                
                String token = authHeader.substring(7);
                String driverId = getCurrentDriverId(token);
                
                // Obtener la ruta para verificar que pertenece al conductor
                Optional<RouteResponse> existingRoute = routeService.obtenerRutaPorId(id);
                if (existingRoute.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                if (driverId == null || !driverId.equals(existingRoute.get().getChoferId())) {
                    logger.warn("CONDUCTOR intentando eliminar ruta que no le pertenece. Ruta ID: {}, Conductor actual: {}", 
                            id, driverId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } else if (!hasRole(RolUsuario.ADMIN)) {
                logger.warn("Intento de eliminar ruta por usuario sin permisos");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            logger.info("REST: Eliminando ruta con ID: {}", id);
            routeService.eliminarRuta(id);
            return ResponseEntity.noContent().build();
        } catch (RouteNotFoundException e) {
            logger.warn("Ruta no encontrada con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error eliminando ruta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Inicia una ruta
     */
    @PatchMapping("/{id}/start")
    public ResponseEntity<RouteResponse> startRoute(@PathVariable("id") String id) {
        try {
            logger.info("REST: Iniciando ruta con ID: {}", id);
            RouteResponse route = routeService.iniciarRuta(id);
            return ResponseEntity.ok(route);
        } catch (RouteNotFoundException e) {
            logger.warn("Ruta no encontrada con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Error al iniciar ruta: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error iniciando ruta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Completa una ruta
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<RouteResponse> completeRoute(@PathVariable("id") String id) {
        try {
            logger.info("REST: Completando ruta con ID: {}", id);
            RouteResponse route = routeService.completarRuta(id);
            return ResponseEntity.ok(route);
        } catch (RouteNotFoundException e) {
            logger.warn("Ruta no encontrada con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Error al completar ruta: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error completando ruta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cancela una ruta
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RouteResponse> cancelRoute(@PathVariable("id") String id) {
        try {
            logger.info("REST: Cancelando ruta con ID: {}", id);
            RouteResponse route = routeService.cancelarRuta(id);
            return ResponseEntity.ok(route);
        } catch (RouteNotFoundException e) {
            logger.warn("Ruta no encontrada con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Error al cancelar ruta: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error cancelando ruta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene rutas por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<RouteResponse>> getRoutesByEstado(@PathVariable("estado") Route.EstadoRuta estado) {
        try {
            logger.info("REST: Obteniendo rutas por estado: {}", estado);
            List<RouteResponse> routes = routeService.obtenerRutasPorEstado(estado);
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            logger.error("Error obteniendo rutas por estado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas de rutas
     * Solo ADMIN y SUPERVISOR pueden ver estadísticas
     */
    @GetMapping("/stats")
    public ResponseEntity<RouteService.RouteStatsDTO> getRouteStats() {
        try {
            if (!isAdminOrSupervisor()) {
                logger.warn("Intento de ver estadísticas por usuario sin permisos");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            logger.info("REST: Obteniendo estadísticas de rutas");
            RouteService.RouteStatsDTO stats = routeService.obtenerEstadisticas();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene asignaciones activas de un conductor (para auto-completar vehículo en rutas)
     */
    @GetMapping("/driver/{choferId}/assignments")
    public ResponseEntity<List<Map<String, Object>>> getDriverAssignments(@PathVariable("choferId") String choferId,
                                                   @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            logger.info("REST: Obteniendo asignaciones activas para chofer: {}", choferId);
            // Extraer token del header si existe
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : null;
            
            List<Map<String, Object>> asignaciones = assignmentsRestClient.getActiveAssignmentsByDriver(choferId, token);
            return ResponseEntity.ok(asignaciones);
        } catch (Exception e) {
            logger.error("Error obteniendo asignaciones del conductor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Calcula la distancia y duración entre origen y destino
     * Usa OpenStreetMap (gratuito) o Google Maps (si está configurado)
     */
    @GetMapping("/calculate-distance")
    public ResponseEntity<Map<String, Object>> calculateDistance(
            @RequestParam("origen") String origen,
            @RequestParam("destino") String destino,
            @RequestParam(value = "origenLat", required = false) Double origenLat,
            @RequestParam(value = "origenLng", required = false) Double origenLng,
            @RequestParam(value = "destinoLat", required = false) Double destinoLat,
            @RequestParam(value = "destinoLng", required = false) Double destinoLng,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            logger.info("REST: Calculando distancia entre {} y {}", origen, destino);
            
            Map<String, Double> resultado = null;
            
            // Si tenemos coordenadas, usarlas directamente (más preciso y rápido)
            if (origenLat != null && origenLng != null && destinoLat != null && destinoLng != null) {
                logger.info("Usando coordenadas proporcionadas: origen=({}, {}), destino=({}, {})", 
                    origenLat, origenLng, destinoLat, destinoLng);
                
                if (openStreetMapClient != null) {
                    Map<String, Double> coordsOrigen = new java.util.HashMap<>();
                    coordsOrigen.put("lat", origenLat);
                    coordsOrigen.put("lng", origenLng);
                    
                    Map<String, Double> coordsDestino = new java.util.HashMap<>();
                    coordsDestino.put("lat", destinoLat);
                    coordsDestino.put("lng", destinoLng);
                    
                    resultado = openStreetMapClient.calcularDistanciaConCoordenadas(coordsOrigen, coordsDestino);
                }
            }
            
            // Si no hay coordenadas o falló, intentar geocodificar las direcciones
            if (resultado == null) {
                // Intentar primero con Google Maps si está disponible
                if (googleMapsClient != null) {
                    resultado = googleMapsClient.calcularDistancia(origen, destino);
                }
                
                // Si Google Maps no está disponible o falló, usar OpenStreetMap (gratuito)
                if (resultado == null && openStreetMapClient != null) {
                    logger.info("Usando OpenStreetMap para calcular distancia (geocodificando direcciones)");
                    resultado = openStreetMapClient.calcularDistancia(origen, destino);
                }
            }
            
            if (resultado == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", "No se pudo calcular la distancia. Verifique las direcciones."));
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("distanciaKm", resultado.get("distanciaKm"));
            response.put("duracionHoras", resultado.get("duracionHoras"));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error calculando distancia: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Error al calcular distancia: " + e.getMessage()));
        }
    }

    /**
     * Obtiene conductores disponibles (sin rutas activas)
     */
    @GetMapping("/available-drivers")
    public ResponseEntity<List<Map<String, Object>>> getAvailableDrivers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            logger.info("REST: Obteniendo conductores disponibles");
            
            // Asegurar que el token esté disponible en JwtTokenHolder para la llamada gRPC
            String jwtToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
                com.skt.combustible.routes.infrastructure.security.JwtTokenHolder.setToken(jwtToken);
            } else {
                // Intentar obtener el token del ThreadLocal (ya establecido por el filtro)
                jwtToken = com.skt.combustible.routes.infrastructure.security.JwtTokenHolder.getToken();
                if (jwtToken == null || jwtToken.isEmpty()) {
                    logger.warn("No se encontró token JWT para obtener conductores disponibles");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            }
            
            try {
                // Obtener todos los conductores disponibles del drivers-service
                List<com.skt.combustible.drivers.grpc.DriverResponse> allDrivers = 
                    routeService.obtenerTodosLosConductoresDisponibles();
                
                // Filtrar solo los que no tienen rutas activas
                List<Map<String, Object>> availableDrivers = new java.util.ArrayList<>();
                for (com.skt.combustible.drivers.grpc.DriverResponse driver : allDrivers) {
                    long rutasActivas = routeService.countRutasActivasByChofer(driver.getId());
                    if (rutasActivas == 0) {
                        Map<String, Object> driverMap = new java.util.HashMap<>();
                        driverMap.put("id", driver.getId());
                        driverMap.put("nombre", driver.getNombre());
                        driverMap.put("apellido", driver.getApellido());
                        driverMap.put("dni", driver.getDni());
                        driverMap.put("licencia", driver.getLicencia());
                        driverMap.put("estado", driver.getEstado().name());
                        driverMap.put("tipoMaquinariaAsignada", driver.hasTipoMaquinariaAsignada() 
                            ? driver.getTipoMaquinariaAsignada().name() : null);
                        availableDrivers.add(driverMap);
                    }
                }
                
                return ResponseEntity.ok(availableDrivers);
            } finally {
                // No limpiar el token aquí, el filtro lo hará
            }
        } catch (Exception e) {
            logger.error("Error obteniendo conductores disponibles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Routes Service is running");
    }
}

