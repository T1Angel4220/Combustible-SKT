package com.skt.combustible.gateway.controller;

import com.skt.combustible.gateway.service.GrpcClientService;
import io.grpc.ManagedChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST del Gateway con soporte gRPC
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayController {

    @Autowired
    private GrpcClientService grpcClientService;

    /**
     * Endpoint para verificar el estado de los canales gRPC
     */
    @GetMapping("/grpc/status")
    public Map<String, Object> getGrpcStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // Verificar estado de los canales gRPC
        status.put("driversChannel", getChannelStatus(grpcClientService.getDriversChannel()));
        status.put("vehiclesChannel", getChannelStatus(grpcClientService.getVehiclesChannel()));
        status.put("routesChannel", getChannelStatus(grpcClientService.getRoutesChannel()));
        status.put("fuelChannel", getChannelStatus(grpcClientService.getFuelChannel()));
        status.put("authChannel", getChannelStatus(grpcClientService.getAuthChannel()));
        
        return status;
    }

    /**
     * Endpoint para información del gateway
     */
    @GetMapping("/info")
    public Map<String, Object> getGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Gateway Service");
        info.put("version", "1.0.0");
        info.put("description", "API Gateway con soporte HTTP y gRPC");
        info.put("grpcEnabled", true);
        info.put("httpEnabled", true);
        info.put("routes", new String[]{
            "/api/v1/drivers/** -> http://localhost:8081 (gRPC: 9091)",
            "/api/v1/vehicles/** -> http://localhost:8082 (gRPC: 9092)", 
            "/api/v1/routes/** -> http://localhost:8083 (gRPC: 9093)",
            "/api/v1/fuel/** -> http://localhost:8084 (gRPC: 9094)",
            "/api/v1/auth/** -> http://localhost:8085 (gRPC: 9095)"
        });
        
        return info;
    }

    /**
     * Endpoint para verificar el estado del gateway
     */
    @GetMapping("/health")
    public Map<String, Object> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("gateway", "ACTIVE");
        health.put("grpc", "CONFIGURED");
        health.put("http", "ENABLED");
        health.put("timestamp", System.currentTimeMillis());
        
        return health;
    }

    /**
     * Obtiene el estado de un canal gRPC
     */
    private String getChannelStatus(ManagedChannel channel) {
        if (channel == null) {
            return "NOT_CONFIGURED";
        }
        
        try {
            var state = channel.getState(true);
            return switch (state) {
                case IDLE -> "IDLE";
                case CONNECTING -> "CONNECTING";
                case READY -> "READY";
                case TRANSIENT_FAILURE -> "TRANSIENT_FAILURE";
                case SHUTDOWN -> "SHUTDOWN";
            };
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
