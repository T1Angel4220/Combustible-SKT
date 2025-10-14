package com.skt.combustible.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Controlador REST para Vehicles en el Gateway Service
 * Expone endpoints REST que internamente se comunican con vehicles-service via
 * HTTP REST reenviando tokens JWT
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = "*")
public class VehiclesGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(VehiclesGatewayController.class);

    @Value("${service.urls.vehicles-service}")
    private String vehiclesServiceUrl;

    private final RestTemplate restTemplate;

    public VehiclesGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Obtiene un vehículo por ID
     * GET /api/v1/vehicles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getVehicleById(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Obteniendo vehículo por ID: {}", id);

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles/" + id;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error obteniendo vehículo por ID: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene todos los vehículos
     * GET /api/v1/vehicles
     */
    @GetMapping
    public ResponseEntity<Object> getAllVehicles(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        logger.info("Gateway REST: Obteniendo todos los vehículos - página: {}, tamaño: {}", page, size);

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles?page=" + page + "&size=" + size;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error obteniendo todos los vehículos: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crea un nuevo vehículo
     * POST /api/v1/vehicles
     */
    @PostMapping
    public ResponseEntity<Object> createVehicle(@RequestBody Map<String, Object> vehicleData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Creando nuevo vehículo");

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles";
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(vehicleData, headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.POST, entity, Object.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(response.getBody());
        } catch (Exception e) {
            logger.error("Error creando vehículo: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualiza un vehículo
     * PUT /api/v1/vehicles/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateVehicle(@PathVariable String id,
            @RequestBody Map<String, Object> vehicleData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Actualizando vehículo ID: {}", id);

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles/" + id;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(vehicleData, headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error actualizando vehículo {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Elimina un vehículo
     * DELETE /api/v1/vehicles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteVehicle(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Eliminando vehículo ID: {}", id);

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles/" + id;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error eliminando vehículo {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check del servicio
     * GET /api/v1/vehicles/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("Gateway REST: Health check vehicles service");
        return ResponseEntity.ok("Vehicles Gateway Service is running");
    }

    /**
     * Información del servicio
     * GET /api/v1/vehicles/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getVehiclesInfo() {
        logger.info("Gateway REST: Obteniendo información del vehicles service");

        Map<String, Object> info = Map.of(
                "service", "Vehicles Gateway Service",
                "version", "1.0.0",
                "description", "Gateway para comunicación con Vehicles Service",
                "status", "READY");

        return ResponseEntity.ok(info);
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