package com.skt.combustible.fuel.infrastructure.client;

import com.skt.combustible.fuel.infrastructure.security.JwtTokenHolder;
import com.skt.combustible.vehicles.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * Cliente gRPC para comunicarse con el Vehicles Service desde Fuel Service
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class VehiclesGrpcClient {
    
    private static final Logger logger = LoggerFactory.getLogger(VehiclesGrpcClient.class);
    
    @Value("${grpc.client.vehicles-service.host:localhost}")
    private String vehiclesServiceHost;
    
    @Value("${grpc.client.vehicles-service.port:9092}")
    private int vehiclesServicePort;
    
    @Autowired
    private JwtClientInterceptor jwtClientInterceptor;
    
    private ManagedChannel channel;
    private VehicleServiceProtoGrpc.VehicleServiceProtoBlockingStub blockingStub;
    
    /**
     * Inicializa la conexión gRPC con el Vehicles Service
     */
    public void initialize() {
        if (channel == null || channel.isShutdown() || channel.isTerminated()) {
            if (channel != null && !channel.isShutdown()) {
                try {
                    channel.shutdown();
                } catch (Exception e) {
                    logger.warn("Error cerrando canal anterior: {}", e.getMessage());
                }
            }
            
            channel = ManagedChannelBuilder.forAddress(vehiclesServiceHost, vehiclesServicePort)
                    .usePlaintext()
                    .intercept(jwtClientInterceptor)
                    .build();
            blockingStub = VehicleServiceProtoGrpc.newBlockingStub(channel);
            logger.info("Cliente gRPC inicializado CON JWT INTERCEPTOR para Vehicles Service en {}:{}",
                    vehiclesServiceHost, vehiclesServicePort);
        }
    }
    
    /**
     * Obtiene un vehículo por ID
     */
    public VehicleResponseProto getVehicleById(String vehicleId) {
        initialize();
        try {
            VehicleIdRequestProto request = VehicleIdRequestProto.newBuilder()
                    .setId(vehicleId)
                    .build();
            
            VehicleResponseProto response = blockingStub.obtenerVehiculoPorId(request);
            logger.debug("Vehículo obtenido: {} {}", response.getMarca(), response.getModelo());
            return response;
        } catch (Exception e) {
            logger.error("Error obteniendo vehículo por ID {}: {}", vehicleId, e.getMessage());
            throw new RuntimeException("Error comunicándose con Vehicles Service: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cierra la conexión gRPC
     */
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            logger.info("Conexión gRPC con Vehicles Service cerrada");
        }
    }
}

