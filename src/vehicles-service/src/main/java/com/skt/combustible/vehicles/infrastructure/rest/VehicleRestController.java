package com.skt.combustible.vehicles.infrastructure.rest;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.RolUsuario;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.application.service.VehicleService;
import com.skt.combustible.vehicles.domain.dto.VehicleCreateRequest;
import com.skt.combustible.vehicles.domain.dto.VehicleResponse;
import com.skt.combustible.vehicles.domain.dto.VehicleUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = "*")
public class VehicleRestController {
    
    private static final Logger logger = LoggerFactory.getLogger(VehicleRestController.class);
    
    @Autowired
    private VehicleService vehicleService;
    
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
     * Crea un nuevo vehículo
     * Solo ADMIN puede crear vehículos
     */
    @PostMapping
    public ResponseEntity<?> crearVehiculo(@Valid @RequestBody VehicleCreateRequest request) {
        try {
            if (!hasRole(RolUsuario.ADMIN)) {
                logger.warn("Intento de crear vehículo por usuario sin permisos");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            logger.debug("Creando vehículo con placa: {}", request.getPlaca());
            VehicleResponse response = vehicleService.crearVehiculo(request);
            logger.info("Vehículo creado exitosamente con placa: {}", response.getPlaca());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al crear vehículo: {}", e.getMessage());
            throw e; // Dejar que el ExceptionHandler lo maneje
        } catch (Exception e) {
            logger.error("Error inesperado al crear vehículo: {}", e.getMessage(), e);
            throw e; // Dejar que el ExceptionHandler lo maneje
        }
    }
    
    /**
     * Obtiene un vehículo por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> obtenerVehiculoPorId(@PathVariable("id") String id) {
        Optional<VehicleResponse> response = vehicleService.obtenerVehiculoPorId(id);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Obtiene un vehículo por placa
     */
    @GetMapping("/placa/{placa}")
    public ResponseEntity<VehicleResponse> obtenerVehiculoPorPlaca(@PathVariable("placa") String placa) {
        Optional<VehicleResponse> response = vehicleService.obtenerVehiculoPorPlaca(placa);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Obtiene todos los vehículos
     */
    @GetMapping
    public ResponseEntity<?> obtenerTodosLosVehiculos() {
        try {
            logger.debug("Obteniendo todos los vehículos");
            List<VehicleResponse> responses = vehicleService.obtenerTodosLosVehiculos();
            logger.debug("Vehículos obtenidos: {}", responses.size());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error al obtener todos los vehículos: {}", e.getMessage(), e);
            throw e; // Dejar que el ExceptionHandler lo maneje
        }
    }
    
    /**
     * Obtiene vehículos por tipo de maquinaria
     */
    @GetMapping("/tipo/{tipoMaquinaria}")
    public ResponseEntity<List<VehicleResponse>> obtenerVehiculosPorTipo(@PathVariable("tipoMaquinaria") TipoMaquinaria tipoMaquinaria) {
        List<VehicleResponse> responses = vehicleService.obtenerVehiculosPorTipo(tipoMaquinaria);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Obtiene vehículos por estado operativo
     */
    @GetMapping("/estado/{estadoOperativo}")
    public ResponseEntity<List<VehicleResponse>> obtenerVehiculosPorEstado(@PathVariable("estadoOperativo") EstadoOperativo estadoOperativo) {
        List<VehicleResponse> responses = vehicleService.obtenerVehiculosPorEstado(estadoOperativo);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Obtiene vehículos disponibles
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<VehicleResponse>> obtenerVehiculosDisponibles() {
        List<VehicleResponse> responses = vehicleService.obtenerVehiculosDisponibles();
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Obtiene vehículos disponibles por tipo
     */
    @GetMapping("/disponibles/tipo/{tipoMaquinaria}")
    public ResponseEntity<List<VehicleResponse>> obtenerVehiculosDisponiblesPorTipo(@PathVariable("tipoMaquinaria") TipoMaquinaria tipoMaquinaria) {
        List<VehicleResponse> responses = vehicleService.obtenerVehiculosDisponiblesPorTipo(tipoMaquinaria);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Actualiza un vehículo
     * Solo ADMIN y SUPERVISOR pueden actualizar vehículos
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> actualizarVehiculo(@PathVariable("id") String id, 
                                                               @Valid @RequestBody VehicleUpdateRequest request) {
        if (!isAdminOrSupervisor()) {
            logger.warn("Intento de actualizar vehículo por usuario sin permisos");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        VehicleResponse response = vehicleService.actualizarVehiculo(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cambia el estado de un vehículo
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<VehicleResponse> cambiarEstadoVehiculo(@PathVariable("id") String id, 
                                                               @RequestParam EstadoOperativo estado) {
        VehicleResponse response = vehicleService.cambiarEstadoVehiculo(id, estado);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Actualiza el kilometraje de un vehículo
     */
    @PatchMapping("/{id}/kilometraje")
    public ResponseEntity<VehicleResponse> actualizarKilometraje(@PathVariable("id") String id, 
                                                                @RequestParam Double kilometraje) {
        VehicleResponse response = vehicleService.actualizarKilometraje(id, kilometraje);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Desactiva un vehículo
     * Solo ADMIN puede eliminar vehículos
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarVehiculo(@PathVariable("id") String id) {
        if (!hasRole(RolUsuario.ADMIN)) {
            logger.warn("Intento de eliminar vehículo por usuario sin permisos");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        vehicleService.desactivarVehiculo(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Obtiene estadísticas de vehículos
     * Solo ADMIN y SUPERVISOR pueden ver estadísticas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<VehicleService.VehicleStatsDTO> obtenerEstadisticas() {
        if (!isAdminOrSupervisor()) {
            logger.warn("Intento de ver estadísticas por usuario sin permisos");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        VehicleService.VehicleStatsDTO stats = vehicleService.obtenerEstadisticas();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Vehicles Service is running");
    }
}
