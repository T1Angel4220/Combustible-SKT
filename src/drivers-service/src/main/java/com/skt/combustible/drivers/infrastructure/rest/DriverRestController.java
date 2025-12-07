package com.skt.combustible.drivers.infrastructure.rest;

import com.skt.combustible.drivers.domain.dto.CreateDriverRequest;
import com.skt.combustible.drivers.domain.dto.DriverResponse;
import com.skt.combustible.drivers.domain.dto.UpdateDriverRequest;
import com.skt.combustible.drivers.domain.exception.DriverNotFoundException;
import com.skt.combustible.drivers.domain.service.DriverService;
import com.skt.combustible.shared.domain.enums.RolUsuario;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el servicio de choferes
 * Expone endpoints HTTP para la gestión de choferes
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/drivers")
@CrossOrigin(origins = "*")
public class DriverRestController {

    private static final Logger logger = LoggerFactory.getLogger(DriverRestController.class);

    @Autowired
    private DriverService driverService;

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
     * Obtiene todos los choferes
     * Todos los roles pueden ver choferes
     * 
     * @return Lista de choferes
     */
    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        try {
            logger.info("REST: Obteniendo todos los choferes");
            Pageable pageable = PageRequest.of(0, 1000); // Obtener hasta 1000 choferes
            Page<DriverResponse> driversPage = driverService.getAllDrivers(pageable);
            List<DriverResponse> drivers = driversPage.getContent();
            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            logger.error("Error obteniendo todos los choferes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un chofer por ID
     * 
     * @param id ID del chofer
     * @return Chofer encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable("id") String id) {
        try {
            logger.info("REST: Obteniendo chofer por ID: {}", id);
            DriverResponse driver = driverService.getDriverById(id);
            return ResponseEntity.ok(driver);
        } catch (DriverNotFoundException e) {
            logger.warn("Chofer no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un chofer por usuarioId
     * 
     * @param usuarioId ID del usuario en auth-service
     * @return Chofer encontrado
     */
    @GetMapping("/by-usuario/{usuarioId}")
    public ResponseEntity<DriverResponse> getDriverByUsuarioId(@PathVariable("usuarioId") String usuarioId) {
        try {
            logger.info("REST: Obteniendo chofer por usuarioId: {}", usuarioId);
            DriverResponse driver = driverService.getDriverByUsuarioId(usuarioId);
            return ResponseEntity.ok(driver);
        } catch (DriverNotFoundException e) {
            logger.warn("Chofer no encontrado con usuarioId: {}", usuarioId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por usuarioId: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un chofer por email (fallback cuando no se encuentra por usuarioId)
     * 
     * @param email Email del usuario/chofer
     * @return Chofer encontrado
     */
    @GetMapping("/by-email/{email}")
    public ResponseEntity<DriverResponse> getDriverByEmail(@PathVariable("email") String email) {
        try {
            logger.info("REST: Obteniendo chofer por email: {}", email);
            DriverResponse driver = driverService.getDriverByEmail(email);
            return ResponseEntity.ok(driver);
        } catch (DriverNotFoundException e) {
            logger.warn("Chofer no encontrado con email: {}", email);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea un nuevo chofer
     * Solo ADMIN y SUPERVISOR pueden crear choferes
     * 
     * @param request Datos del chofer a crear
     * @param authorization Header de autorización con el token JWT
     * @return Chofer creado
     */
    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(
            @Valid @RequestBody CreateDriverRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // Validar rol: solo ADMIN y SUPERVISOR pueden crear choferes
            if (!isAdminOrSupervisor()) {
                logger.warn("Intento de crear chofer por usuario sin permisos");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            logger.info("REST: Creando nuevo chofer: {} {}", request.getNombre(), request.getApellido());
            
            // Extraer token del header Authorization (Bearer token)
            String token = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }
            
            DriverResponse driver = driverService.createDriver(request, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(driver);
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación creando chofer: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creando chofer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza un chofer existente
     * Solo ADMIN y SUPERVISOR pueden actualizar choferes
     * 
     * @param id ID del chofer a actualizar
     * @param request Datos de actualización
     * @return Chofer actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(@PathVariable("id") String id, 
                                                       @Valid @RequestBody UpdateDriverRequest request) {
        try {
            // Validar rol: solo ADMIN y SUPERVISOR pueden actualizar choferes
            if (!isAdminOrSupervisor()) {
                logger.warn("Intento de actualizar chofer por usuario sin permisos");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            logger.info("REST: Actualizando chofer con ID: {}", id);
            DriverResponse driver = driverService.updateDriver(id, request);
            return ResponseEntity.ok(driver);
        } catch (DriverNotFoundException e) {
            logger.warn("Chofer no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error actualizando chofer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Desactiva un chofer (soft delete)
     * Solo ADMIN puede desactivar choferes
     * 
     * @param id ID del chofer a desactivar
     * @return Respuesta vacía
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateDriver(@PathVariable("id") String id) {
        try {
            // Validar rol: solo ADMIN puede desactivar choferes
            if (!hasRole(RolUsuario.ADMIN)) {
                logger.warn("Intento de desactivar chofer por usuario sin permisos");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            logger.info("REST: Desactivando chofer con ID: {}", id);
            driverService.deactivateDriver(id);
            return ResponseEntity.noContent().build();
        } catch (DriverNotFoundException e) {
            logger.warn("Chofer no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error desactivando chofer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reactiva un chofer
     * Solo ADMIN puede reactivar choferes
     * 
     * @param id ID del chofer a reactivar
     * @return Chofer reactivado
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<DriverResponse> activateDriver(@PathVariable("id") String id) {
        try {
            // Validar rol: solo ADMIN puede reactivar choferes
            if (!hasRole(RolUsuario.ADMIN)) {
                logger.warn("Intento de reactivar chofer por usuario sin permisos");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            logger.info("REST: Reactivando chofer con ID: {}", id);
            DriverResponse driver = driverService.activateDriver(id);
            return ResponseEntity.ok(driver);
        } catch (DriverNotFoundException e) {
            logger.warn("Chofer no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error reactivando chofer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina permanentemente un chofer (hard delete)
     * Solo ADMIN puede eliminar choferes permanentemente
     * 
     * @param id ID del chofer a eliminar
     * @return Respuesta vacía
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable("id") String id) {
        try {
            // Validar rol: solo ADMIN puede eliminar choferes permanentemente
            if (!hasRole(RolUsuario.ADMIN)) {
                logger.warn("Intento de eliminar chofer por usuario sin permisos");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            logger.info("REST: Eliminando permanentemente chofer con ID: {}", id);
            driverService.deleteDriver(id);
            return ResponseEntity.noContent().build();
        } catch (DriverNotFoundException e) {
            logger.warn("Chofer no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error eliminando chofer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene choferes disponibles
     * 
     * @return Lista de choferes disponibles
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers() {
        try {
            logger.info("REST: Obteniendo choferes disponibles");
            List<DriverResponse> drivers = driverService.getAvailableDrivers();
            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            logger.error("Error obteniendo choferes disponibles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     * 
     * @return Mensaje de estado
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Drivers Service is running");
    }
}

