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
 * Controlador REST del Gateway para demostrar funcionalidad gRPC
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
        
        return info;
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
