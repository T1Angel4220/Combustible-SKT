package com.skt.combustible.drivers.controller;

import com.skt.combustible.drivers.domain.dto.CreateDriverRequest;
import com.skt.combustible.drivers.domain.dto.DriverResponse;
import com.skt.combustible.drivers.domain.dto.UpdateDriverRequest;
import com.skt.combustible.drivers.domain.exception.DriverNotFoundException;
import com.skt.combustible.drivers.domain.service.DriverService;
import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el servicio de choferes
 * Expone endpoints para comunicación con el API Gateway
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/drivers")
@CrossOrigin(origins = "*")
public class DriverRestController {

    private static final Logger logger = LoggerFactory.getLogger(DriverRestController.class);

    private final DriverService driverService;

    public DriverRestController(DriverService driverService) {
        this.driverService = driverService;
    }

    /**
     * Crea un nuevo chofer
     * POST /api/v1/drivers
     */
    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody CreateDriverRequest request) {
        logger.info("POST /api/v1/drivers - Creando nuevo chofer");

        DriverResponse response = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene un chofer por ID
     * GET /api/v1/drivers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable("id") String id) {
        logger.info("GET /api/v1/drivers/{} - Obteniendo chofer por ID", id);

        DriverResponse response = driverService.getDriverById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un chofer por DNI
     * GET /api/v1/drivers/dni/{dni}
     */
    @GetMapping("/dni/{dni}")
    public ResponseEntity<DriverResponse> getDriverByDni(@PathVariable("dni") String dni) {
        logger.info("GET /api/v1/drivers/dni/{} - Obteniendo chofer por DNI", dni);

        DriverResponse response = driverService.getDriverByDni(dni);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un chofer por número de licencia
     * GET /api/v1/drivers/license/{licencia}
     */
    @GetMapping("/license/{licencia}")
    public ResponseEntity<DriverResponse> getDriverByLicense(@PathVariable("licencia") String licencia) {
        logger.info("GET /api/v1/drivers/license/{} - Obteniendo chofer por licencia", licencia);

        DriverResponse response = driverService.getDriverByLicencia(licencia);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todos los choferes con paginación
     * GET /api/v1/drivers?page=0&size=10&sort=nombre,asc
     */
    @GetMapping
    public ResponseEntity<Page<DriverResponse>> getAllDrivers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "nombre") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        logger.info("GET /api/v1/drivers - Obteniendo todos los choferes (page={}, size={})", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DriverResponse> response = driverService.getAllDrivers(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todos los choferes activos
     * GET /api/v1/drivers/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<DriverResponse>> getActiveDrivers() {
        logger.info("GET /api/v1/drivers/active - Obteniendo choferes activos");

        List<DriverResponse> response = driverService.getAllActiveDrivers();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene choferes disponibles
     * GET /api/v1/drivers/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers() {
        logger.info("GET /api/v1/drivers/available - Obteniendo choferes disponibles");

        List<DriverResponse> response = driverService.getAvailableDrivers();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene choferes disponibles por tipo de maquinaria
     * GET /api/v1/drivers/available-by-type?machineryType=CAMION
     */
    @GetMapping("/available-by-type")
    public ResponseEntity<List<DriverResponse>> getAvailableDriversByMachineryType(
            @RequestParam TipoMaquinaria machineryType) {

        logger.info("GET /api/v1/drivers/available-by-type - Obteniendo choferes disponibles para {}", machineryType);

        List<DriverResponse> response = driverService.getAvailableDriversByMachineryType(machineryType);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca choferes por nombre o apellido
     * GET /api/v1/drivers/search?name=Juan
     */
    @GetMapping("/search")
    public ResponseEntity<List<DriverResponse>> searchDriversByName(@RequestParam String name) {
        logger.info("GET /api/v1/drivers/search - Buscando choferes por nombre: {}", name);

        List<DriverResponse> response = driverService.searchDriversByName(name);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene choferes por estado operativo
     * GET /api/v1/drivers/status/{estado}
     */
    @GetMapping("/status/{estado}")
    public ResponseEntity<List<DriverResponse>> getDriversByStatus(@PathVariable("estado") EstadoOperativo estado) {
        logger.info("GET /api/v1/drivers/status/{} - Obteniendo choferes por estado", estado);

        List<DriverResponse> response = driverService.getDriversByStatus(estado);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todos los choferes (incluyendo inactivos) con paginación
     * GET /api/v1/drivers/all?page=0&size=10&sort=nombre,asc
     */
    @GetMapping("/all")
    public ResponseEntity<Page<DriverResponse>> getAllDriversIncludingInactive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "nombre") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        logger.info("GET /api/v1/drivers/all - Obteniendo todos los choferes incluyendo inactivos (page={}, size={})",
                page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DriverResponse> response = driverService.getAllDriversIncludingInactive(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza un chofer
     * PUT /api/v1/drivers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateDriverRequest request) {

        logger.info("PUT /api/v1/drivers/{} - Actualizando chofer", id);

        DriverResponse response = driverService.updateDriver(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cambia el estado de un chofer
     * PATCH /api/v1/drivers/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<DriverResponse> changeDriverStatus(
            @PathVariable("id") String id,
            @RequestParam EstadoOperativo status) {

        logger.info("PATCH /api/v1/drivers/{}/status - Cambiando estado a {}", id, status);

        DriverResponse response = driverService.changeDriverStatus(id, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Asigna tipo de maquinaria a un chofer
     * PATCH /api/v1/drivers/{id}/machinery-type
     */
    @PatchMapping("/{id}/machinery-type")
    public ResponseEntity<DriverResponse> assignMachineryType(
            @PathVariable("id") String id,
            @RequestParam TipoMaquinaria machineryType) {

        logger.info("PATCH /api/v1/drivers/{}/machinery-type - Asignando tipo {}", id, machineryType);

        DriverResponse response = driverService.assignMachineryType(id, machineryType);
        return ResponseEntity.ok(response);
    }

    /**
     * Desactiva un chofer (soft delete)
     * DELETE /api/v1/drivers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateDriver(@PathVariable("id") String id) {
        logger.info("DELETE /api/v1/drivers/{} - Desactivando chofer", id);

        driverService.deactivateDriver(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina permanentemente un chofer
     * DELETE /api/v1/drivers/{id}/permanent
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> deleteDriverPermanently(@PathVariable("id") String id) {
        logger.info("DELETE /api/v1/drivers/{}/permanent - Eliminando chofer permanentemente", id);

        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cuenta choferes disponibles
     * GET /api/v1/drivers/count/available
     */
    @GetMapping("/count/available")
    public ResponseEntity<Long> countAvailableDrivers() {
        logger.info("GET /api/v1/drivers/count/available - Contando choferes disponibles");

        long count = driverService.countAvailableDrivers();
        return ResponseEntity.ok(count);
    }

    /**
     * Cuenta choferes disponibles por tipo de maquinaria
     * GET /api/v1/drivers/count/available-by-type?machineryType=CAMION
     */
    @GetMapping("/count/available-by-type")
    public ResponseEntity<Long> countAvailableDriversByMachineryType(
            @RequestParam TipoMaquinaria machineryType) {

        logger.info("GET /api/v1/drivers/count/available-by-type - Contando choferes disponibles para {}",
                machineryType);

        long count = driverService.countAvailableDriversByMachineryType(machineryType);
        return ResponseEntity.ok(count);
    }

    /**
     * Verifica si un chofer está disponible
     * GET /api/v1/drivers/{id}/available
     */
    @GetMapping("/{id}/available")
    public ResponseEntity<Boolean> isDriverAvailable(@PathVariable("id") String id) {
        logger.info("GET /api/v1/drivers/{}/available - Verificando disponibilidad", id);

        boolean available = driverService.isDriverAvailable(id);
        return ResponseEntity.ok(available);
    }

    /**
     * Manejo de excepciones para DriverNotFoundException
     */
    @ExceptionHandler(DriverNotFoundException.class)
    public ResponseEntity<String> handleDriverNotFoundException(DriverNotFoundException ex) {
        logger.warn("Chofer no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Manejo de excepciones para validaciones
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        logger.warn("Error de validación: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error de validación: " + ex.getMessage());
    }
}
