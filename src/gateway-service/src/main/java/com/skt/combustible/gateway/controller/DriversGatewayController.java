package com.skt.combustible.gateway.controller;

import com.skt.combustible.drivers.grpc.*;
import com.skt.combustible.gateway.domain.dto.DriverRestResponse;
import com.skt.combustible.gateway.domain.dto.UpdateDriverRequest;
import com.skt.combustible.gateway.infrastructure.client.DriversGrpcClient;
import com.skt.combustible.gateway.infrastructure.interceptor.JwtClientInterceptor;
import com.skt.combustible.gateway.infrastructure.mapper.DriverMapper;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el Gateway Service
 * Expone endpoints REST que internamente se comunican con microservicios via
 * gRPC
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/drivers")
public class DriversGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(DriversGatewayController.class);

    @Autowired
    private DriversGrpcClient driversGrpcClient;

    @Autowired
    private DriverMapper driverMapper;

    /**
     * Maneja las peticiones OPTIONS para CORS preflight
     */
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "*")
                .header("Access-Control-Max-Age", "3600")
                .build();
    }

    /**
     * Crea un nuevo chofer
     * POST /api/v1/drivers
     */
    @PostMapping
    public ResponseEntity<?> createDriver(
            @RequestBody com.skt.combustible.gateway.domain.dto.CreateDriverRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway gRPC: Creando nuevo chofer: {} {}", request.getNombre(), request.getApellido());

        try {
            // Establecer el token JWT para la llamada gRPC
            JwtClientInterceptor.setJwtToken(authHeader);

            // Llamar al servicio gRPC
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = driversGrpcClient.createDriver(request);

            // Convertir a respuesta REST
            DriverRestResponse restResponse = driverMapper.toRestResponse(grpcResponse);

            return ResponseEntity.status(201).body(restResponse);
        } catch (Exception e) {
            logger.error("Error creando chofer: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Error creando chofer: " + e.getMessage()));
        } finally {
            JwtClientInterceptor.clearJwtToken();
        }
    }

    /**
     * Obtiene un chofer por ID
     * GET /api/v1/drivers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDriverById(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway gRPC: Obteniendo chofer por ID: {}", id);

        try {
            // Establecer el token JWT para la llamada gRPC
            JwtClientInterceptor.setJwtToken(authHeader);

            // Si el ID es "all", obtener todos los choferes
            if ("all".equalsIgnoreCase(id)) {
                logger.info("Gateway gRPC: ID es 'all', obteniendo todos los choferes");
                List<DriverResponse> grpcResponses = driversGrpcClient.getAllDrivers();
                List<DriverRestResponse> restResponses = grpcResponses.stream()
                        .map(driverMapper::toRestResponse)
                        .toList();
                return ResponseEntity.ok(restResponses);
            } else {
                // Obtener chofer específico por ID
                DriverResponse grpcResponse = driversGrpcClient.getDriverById(id);
                DriverRestResponse restResponse = driverMapper.toRestResponse(grpcResponse);
                return ResponseEntity.ok(restResponse);
            }
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por ID: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } finally {
            // Limpiar el token después de la llamada
            JwtClientInterceptor.clearJwtToken();
        }
    }

    /**
     * Obtiene choferes disponibles
     * GET /api/v1/drivers/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<DriverRestResponse>> getAvailableDrivers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway gRPC: Obteniendo choferes disponibles");

        try {
            JwtClientInterceptor.setJwtToken(authHeader);
            List<DriverResponse> grpcResponses = driversGrpcClient.getAvailableDrivers();
            List<DriverRestResponse> restResponses = driverMapper.toRestResponseList(grpcResponses);
            return ResponseEntity.ok(restResponses);
        } catch (Exception e) {
            logger.error("Error obteniendo choferes disponibles: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } finally {
            JwtClientInterceptor.clearJwtToken();
        }
    }

    /**
     * Obtiene choferes disponibles por tipo de maquinaria
     * GET /api/v1/drivers/available-by-type?machineryType=CAMION
     */
    @GetMapping("/available-by-type")
    public ResponseEntity<List<DriverRestResponse>> getAvailableDriversByMachineryType(
            @RequestParam String machineryType,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway gRPC: Obteniendo choferes disponibles por tipo: {}", machineryType);

        try {
            JwtClientInterceptor.setJwtToken(authHeader);
            TipoMaquinaria tipo = TipoMaquinaria.valueOf(machineryType.toUpperCase());
            List<DriverResponse> grpcResponses = driversGrpcClient.getAvailableDriversByMachineryType(tipo);
            List<DriverRestResponse> restResponses = driverMapper.toRestResponseList(grpcResponses);
            return ResponseEntity.ok(restResponses);
        } catch (Exception e) {
            logger.error("Error obteniendo choferes disponibles por tipo {}: {}", machineryType, e.getMessage());
            return ResponseEntity.internalServerError().build();
        } finally {
            JwtClientInterceptor.clearJwtToken();
        }
    }

    /**
     * Cuenta choferes disponibles
     * GET /api/v1/drivers/count/available
     */
    @GetMapping("/count/available")
    public ResponseEntity<Long> countAvailableDrivers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway gRPC: Contando choferes disponibles");

        try {
            JwtClientInterceptor.setJwtToken(authHeader);
            Long count = driversGrpcClient.countAvailableDrivers();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error contando choferes disponibles: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } finally {
            JwtClientInterceptor.clearJwtToken();
        }
    }

    /**
     * Verifica si un chofer está disponible
     * GET /api/v1/drivers/{id}/available
     */
    @GetMapping("/{id}/available")
    public ResponseEntity<Boolean> isDriverAvailable(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway gRPC: Verificando disponibilidad de chofer: {}", id);

        try {
            JwtClientInterceptor.setJwtToken(authHeader);
            Boolean available = driversGrpcClient.isDriverAvailable(id);
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            logger.error("Error verificando disponibilidad de chofer {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        } finally {
            JwtClientInterceptor.clearJwtToken();
        }
    }

    /**
     * Actualiza un chofer
     * PUT /api/v1/drivers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DriverRestResponse> updateDriver(@PathVariable String id,
            @RequestBody UpdateDriverRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway gRPC: Actualizando chofer con ID: {}", id);

        try {
            // Establecer el token JWT para la llamada gRPC
            JwtClientInterceptor.setJwtToken(authHeader);

            // Llamar al servicio gRPC
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = driversGrpcClient.updateDriver(id, request);

            // Convertir respuesta gRPC a REST
            DriverRestResponse restResponse = driverMapper.toRestResponse(grpcResponse);

            return ResponseEntity.ok(restResponse);
        } catch (Exception e) {
            logger.error("Error actualizando chofer: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } finally {
            // Limpiar el token después de la llamada
            JwtClientInterceptor.clearJwtToken();
        }
    }

    /**
     * Desactiva un chofer
     * DELETE /api/v1/drivers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivateDriver(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway gRPC: Desactivando chofer con ID: {}", id);

        try {
            // Establecer el token JWT para la llamada gRPC
            JwtClientInterceptor.setJwtToken(authHeader);

            // Llamar al servicio gRPC
            driversGrpcClient.deactivateDriver(id);

            return ResponseEntity.ok("Chofer desactivado exitosamente");
        } catch (Exception e) {
            logger.error("Error desactivando chofer: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } finally {
            // Limpiar el token después de la llamada
            JwtClientInterceptor.clearJwtToken();
        }
    }

    /**
     * Elimina permanentemente un chofer
     * DELETE /api/v1/drivers/{id}/permanent
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<String> deleteDriverPermanently(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway gRPC: Eliminando permanentemente chofer con ID: {}", id);

        try {
            // Establecer el token JWT para la llamada gRPC
            JwtClientInterceptor.setJwtToken(authHeader);

            // Llamar al servicio gRPC
            driversGrpcClient.deleteDriverPermanently(id);

            return ResponseEntity.ok("Chofer eliminado permanentemente");
        } catch (Exception e) {
            logger.error("Error eliminando chofer permanentemente: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } finally {
            // Limpiar el token después de la llamada
            JwtClientInterceptor.clearJwtToken();
        }
    }

    /**
     * Reactiva un chofer (cambia activo = true)
     * PATCH /api/v1/drivers/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<DriverRestResponse> activateDriver(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway gRPC: Reactivando chofer con ID: {}", id);

        try {
            // Establecer el token JWT para la llamada gRPC
            JwtClientInterceptor.setJwtToken(authHeader);

            // Llamar al servicio gRPC
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = driversGrpcClient.activateDriver(id);

            // Convertir respuesta gRPC a REST
            DriverRestResponse restResponse = driverMapper.toRestResponse(grpcResponse);

            return ResponseEntity.ok(restResponse);
        } catch (Exception e) {
            logger.error("Error reactivando chofer: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } finally {
            // Limpiar el token después de la llamada
            JwtClientInterceptor.clearJwtToken();
        }
    }

    /**
     * Obtiene choferes en servicio (Asignado y En Ruta)
     * GET /api/v1/drivers/in-service
     */
    @GetMapping("/in-service")
    public ResponseEntity<List<DriverRestResponse>> getDriversInService(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Gateway gRPC: Obteniendo choferes en servicio");

        try {
            JwtClientInterceptor.setJwtToken(authHeader);
            com.skt.combustible.drivers.grpc.GetDriversInServiceResponse grpcResponse = driversGrpcClient
                    .getDriversInService();

            List<DriverRestResponse> restResponse = grpcResponse.getDriversList().stream()
                    .map(driverMapper::toRestResponse)
                    .toList();

            return ResponseEntity.ok(restResponse);
        } catch (Exception e) {
            logger.error("Error obteniendo choferes en servicio: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } finally {
            JwtClientInterceptor.clearJwtToken();
        }
    }

    /**
     * Health check del servicio
     * GET /api/v1/drivers/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Drivers Gateway Service is running (gRPC)");
    }
}