package com.skt.combustible.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Controlador REST para el Gateway Service
 * Expone endpoints REST que internamente se comunican con microservicios via
 * HTTP REST reenviando tokens JWT
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/drivers")
@CrossOrigin(origins = "*")
public class DriversGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(DriversGatewayController.class);

    @Value("${service.urls.drivers-service}")
    private String driversServiceUrl;

    private final RestTemplate restTemplate;

    public DriversGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Obtiene un chofer por ID
     * GET /api/v1/drivers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getDriverById(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Obteniendo chofer por ID: {}", id);

        try {
            String url = driversServiceUrl + "/api/v1/drivers/" + id;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por ID: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene todos los choferes con paginación
     * GET /api/v1/drivers?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Object> getAllDrivers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        logger.info("Gateway REST: Obteniendo todos los choferes - página: {}, tamaño: {}", page, size);

        try {
            String url = driversServiceUrl + "/api/v1/drivers/all?page=" + page + "&size=" + size
                    + "&sortBy=nombre&sortDir=asc";
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error obteniendo todos los choferes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crea un nuevo chofer
     * POST /api/v1/drivers
     */
    @PostMapping
    public ResponseEntity<Object> createDriver(@RequestBody Map<String, Object> driverData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Creando nuevo chofer");

        try {
            String url = driversServiceUrl + "/api/v1/drivers";
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(driverData, headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.POST, entity, Object.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(response.getBody());
        } catch (Exception e) {
            logger.error("Error creando chofer: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualiza un chofer
     * PUT /api/v1/drivers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateDriver(@PathVariable String id,
            @RequestBody Map<String, Object> driverData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Actualizando chofer ID: {}", id);

        try {
            String url = driversServiceUrl + "/api/v1/drivers/" + id;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(driverData, headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error actualizando chofer {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Desactiva un chofer (soft delete)
     * DELETE /api/v1/drivers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deactivateDriver(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Desactivando chofer ID: {}", id);

        try {
            String url = driversServiceUrl + "/api/v1/drivers/" + id;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error desactivando chofer {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Elimina permanentemente un chofer (hard delete)
     * DELETE /api/v1/drivers/{id}/permanent
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Object> deleteDriverPermanently(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Eliminando permanentemente chofer ID: {}", id);

        try {
            String url = driversServiceUrl + "/api/v1/drivers/" + id + "/permanent";
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error eliminando permanentemente chofer {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reactiva un chofer
     * PATCH /api/v1/drivers/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Object> activateDriver(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Reactivando chofer ID: {}", id);

        try {
            String url = driversServiceUrl + "/api/v1/drivers/" + id + "/activate";
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error reactivando chofer {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check del servicio
     * GET /api/v1/drivers/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Drivers Gateway Service is running");
    }

    /**
     * Crea headers HTTP con autorización JWT
     */
    private HttpHeaders createHeadersWithAuth(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isEmpty()) {
            headers.set("Authorization", authHeader);
            logger.debug("Reenviando token JWT al servicio backend");
        } else {
            logger.warn("No se encontró token JWT en la request del cliente");
        }
        return headers;
    }
}