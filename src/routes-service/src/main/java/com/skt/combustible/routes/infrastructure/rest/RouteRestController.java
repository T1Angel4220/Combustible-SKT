package com.skt.combustible.routes.infrastructure.rest;

import com.skt.combustible.routes.application.service.RouteService;
import com.skt.combustible.routes.domain.dto.RouteCreateRequest;
import com.skt.combustible.routes.domain.dto.RouteResponse;
import com.skt.combustible.routes.domain.dto.RouteUpdateRequest;
import com.skt.combustible.routes.domain.entity.Route;
import com.skt.combustible.routes.domain.exception.RouteNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private com.skt.combustible.routes.infrastructure.client.AssignmentsRestClient assignmentsRestClient;

    /**
     * Obtiene todas las rutas activas
     */
    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        try {
            logger.info("REST: Obteniendo todas las rutas");
            List<RouteResponse> routes = routeService.obtenerTodasLasRutas();
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
     */
    @PostMapping
    public ResponseEntity<?> createRoute(@Valid @RequestBody RouteCreateRequest request) {
        try {
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
     */
    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable("id") String id,
                                                     @Valid @RequestBody RouteUpdateRequest request) {
        try {
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
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable("id") String id) {
        try {
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
     */
    @GetMapping("/stats")
    public ResponseEntity<RouteService.RouteStatsDTO> getRouteStats() {
        try {
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

