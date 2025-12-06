package com.skt.combustible.drivers.infrastructure.rest;

import com.skt.combustible.drivers.domain.dto.CreateDriverRequest;
import com.skt.combustible.drivers.domain.dto.DriverResponse;
import com.skt.combustible.drivers.domain.dto.UpdateDriverRequest;
import com.skt.combustible.drivers.domain.exception.DriverNotFoundException;
import com.skt.combustible.drivers.domain.service.DriverService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * Obtiene todos los choferes
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
     * Crea un nuevo chofer
     * 
     * @param request Datos del chofer a crear
     * @return Chofer creado
     */
    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody CreateDriverRequest request) {
        try {
            logger.info("REST: Creando nuevo chofer: {} {}", request.getNombre(), request.getApellido());
            DriverResponse driver = driverService.createDriver(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(driver);
        } catch (Exception e) {
            logger.error("Error creando chofer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza un chofer existente
     * 
     * @param id ID del chofer a actualizar
     * @param request Datos de actualización
     * @return Chofer actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(@PathVariable("id") String id, 
                                                       @Valid @RequestBody UpdateDriverRequest request) {
        try {
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
     * Elimina un chofer (desactivación lógica)
     * 
     * @param id ID del chofer a eliminar
     * @return Respuesta vacía
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable("id") String id) {
        try {
            logger.info("REST: Eliminando chofer con ID: {}", id);
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

