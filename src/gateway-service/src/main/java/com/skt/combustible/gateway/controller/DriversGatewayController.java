package com.skt.combustible.gateway.controller;

import com.skt.combustible.gateway.domain.dto.DriverRestResponse;
import com.skt.combustible.gateway.infrastructure.client.DriversGrpcClient;
import com.skt.combustible.gateway.infrastructure.mapper.DriverMapper;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el Gateway Service
 * Expone endpoints REST que internamente se comunican con microservicios via gRPC
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/drivers")
@CrossOrigin(origins = "*")
public class DriversGatewayController {
    
    private static final Logger logger = LoggerFactory.getLogger(DriversGatewayController.class);
    
    private final DriversGrpcClient driversGrpcClient;
    private final DriverMapper driverMapper;
    
    public DriversGatewayController(DriversGrpcClient driversGrpcClient, DriverMapper driverMapper) {
        this.driversGrpcClient = driversGrpcClient;
        this.driverMapper = driverMapper;
    }
    
    /**
     * Obtiene un chofer por ID
     * GET /api/v1/drivers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DriverRestResponse> getDriverById(@PathVariable String id) {
        logger.info("Gateway REST: Obteniendo chofer por ID: {}", id);

        try {
            var grpcResponse = driversGrpcClient.getDriverById(id);
            var restResponse = driverMapper.toRestResponse(grpcResponse);

            return ResponseEntity.ok(restResponse);
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtiene choferes disponibles (con filtro opcional por tipo de maquinaria)
     * GET /api/v1/drivers/available
     * GET /api/v1/drivers/available?tipoMaquinaria={tipo}
     */
    @GetMapping("/available")
    public ResponseEntity<List<DriverRestResponse>> getAvailableDrivers(
            @RequestParam(value = "tipoMaquinaria", required = false) TipoMaquinaria tipoMaquinaria) {
        
        logger.info("Gateway REST: Obteniendo choferes disponibles por tipo de maquinaria: {}", tipoMaquinaria);
        
        try {
            List<DriverRestResponse> restResponses;
            
            if (tipoMaquinaria != null) {
                var grpcResponses = driversGrpcClient.getAvailableDriversByMachineryType(tipoMaquinaria);
                restResponses = driverMapper.toRestResponseList(grpcResponses);
            } else {
                var grpcResponses = driversGrpcClient.getAvailableDrivers();
                restResponses = driverMapper.toRestResponseList(grpcResponses);
            }
            
            return ResponseEntity.ok(restResponses);
        } catch (Exception e) {
            logger.error("Error obteniendo choferes disponibles: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Cuenta choferes disponibles
     * GET /api/v1/drivers/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAvailableDrivers() {
        logger.info("Gateway REST: Contando choferes disponibles");
        
        try {
            var count = driversGrpcClient.countAvailableDrivers();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error contando choferes disponibles: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Verifica si un chofer está disponible
     * GET /api/v1/drivers/{id}/available
     */
    @GetMapping("/{id}/available")
    public ResponseEntity<Boolean> isDriverAvailable(@PathVariable String id) {
        logger.info("Gateway REST: Verificando disponibilidad del chofer ID: {}", id);

        try {
            var available = driversGrpcClient.isDriverAvailable(id);
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            logger.error("Error verificando disponibilidad del chofer {}: {}", id, e.getMessage());
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
}