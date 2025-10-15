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
     * Obtiene un vehículo por placa
     * GET /api/v1/vehicles/placa/{placa}
     */
    @GetMapping("/placa/{placa}")
    public ResponseEntity<Object> getVehicleByPlaca(@PathVariable String placa,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Obteniendo vehículo por placa: {}", placa);

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles/placa/" + placa;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error obteniendo vehículo por placa {}: {}", placa, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene vehículos por tipo de maquinaria
     * GET /api/v1/vehicles/tipo/{tipoMaquinaria}
     */
    @GetMapping("/tipo/{tipoMaquinaria}")
    public ResponseEntity<Object> getVehiclesByTipo(@PathVariable String tipoMaquinaria,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Obteniendo vehículos por tipo: {}", tipoMaquinaria);

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles/tipo/" + tipoMaquinaria;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error obteniendo vehículos por tipo {}: {}", tipoMaquinaria, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene vehículos por estado operativo
     * GET /api/v1/vehicles/estado/{estadoOperativo}
     */
    @GetMapping("/estado/{estadoOperativo}")
    public ResponseEntity<Object> getVehiclesByEstado(@PathVariable String estadoOperativo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Obteniendo vehículos por estado: {}", estadoOperativo);

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles/estado/" + estadoOperativo;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error obteniendo vehículos por estado {}: {}", estadoOperativo, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene vehículos disponibles
     * GET /api/v1/vehicles/disponibles
     */
    @GetMapping("/disponibles")
    public ResponseEntity<Object> getAvailableVehicles(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Obteniendo vehículos disponibles");

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles/disponibles";
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error obteniendo vehículos disponibles: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene vehículos disponibles por tipo de maquinaria
     * GET /api/v1/vehicles/disponibles/tipo/{tipoMaquinaria}
     */
    @GetMapping("/disponibles/tipo/{tipoMaquinaria}")
    public ResponseEntity<Object> getAvailableVehiclesByTipo(@PathVariable String tipoMaquinaria,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Obteniendo vehículos disponibles por tipo: {}", tipoMaquinaria);

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles/disponibles/tipo/" + tipoMaquinaria;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error obteniendo vehículos disponibles por tipo {}: {}", tipoMaquinaria, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cambia el estado operativo de un vehículo
     * PATCH /api/v1/vehicles/{id}/estado?nuevoEstado=DISPONIBLE
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Object> changeVehicleEstado(@PathVariable String id,
            @RequestParam String nuevoEstado,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Cambiando estado de vehículo {} a {}", id, nuevoEstado);

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles/" + id + "/estado?nuevoEstado=" + nuevoEstado;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error cambiando estado de vehículo {} a {}: {}", id, nuevoEstado, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualiza el kilometraje de un vehículo
     * PATCH /api/v1/vehicles/{id}/kilometraje?kilometraje=15000
     */
    @PatchMapping("/{id}/kilometraje")
    public ResponseEntity<Object> updateKilometraje(@PathVariable String id,
            @RequestParam Integer kilometraje,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Actualizando kilometraje de vehículo {} a {}", id, kilometraje);

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles/" + id + "/kilometraje?kilometraje=" + kilometraje;
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error actualizando kilometraje de vehículo {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene estadísticas de vehículos
     * GET /api/v1/vehicles/estadisticas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Object> getVehicleStatistics(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway REST: Obteniendo estadísticas de vehículos");

        try {
            String url = vehiclesServiceUrl + "/api/v1/vehicles/estadisticas";
            HttpHeaders headers = createHeadersWithAuth(authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de vehículos: {}", e.getMessage());
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