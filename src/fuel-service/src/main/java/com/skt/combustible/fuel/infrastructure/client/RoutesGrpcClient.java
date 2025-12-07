package com.skt.combustible.fuel.infrastructure.client;

import com.skt.combustible.fuel.infrastructure.security.JwtTokenHolder;
import com.skt.combustible.routes.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * Cliente gRPC para comunicarse con el Routes Service desde Fuel Service
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class RoutesGrpcClient {
    
    private static final Logger logger = LoggerFactory.getLogger(RoutesGrpcClient.class);
    
    @Value("${grpc.client.routes-service.host:localhost}")
    private String routesServiceHost;
    
    @Value("${grpc.client.routes-service.port:9093}")
    private int routesServicePort;
    
    @Autowired
    private JwtClientInterceptor jwtClientInterceptor;
    
    private ManagedChannel channel;
    private RouteServiceGrpc.RouteServiceBlockingStub blockingStub;
    
    /**
     * Inicializa la conexión gRPC con el Routes Service
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
            
            channel = ManagedChannelBuilder.forAddress(routesServiceHost, routesServicePort)
                    .usePlaintext()
                    .intercept(jwtClientInterceptor)
                    .build();
            blockingStub = RouteServiceGrpc.newBlockingStub(channel);
            logger.info("Cliente gRPC inicializado CON JWT INTERCEPTOR para Routes Service en {}:{}",
                    routesServiceHost, routesServicePort);
        }
    }
    
    /**
     * Obtiene una ruta por ID
     */
    public RouteResponse getRouteById(String routeId) {
        initialize();
        try {
            GetRouteByIdRequest request = GetRouteByIdRequest.newBuilder()
                    .setId(routeId)
                    .build();
            
            RouteResponse response = blockingStub.getRouteById(request);
            logger.debug("Ruta obtenida: {}", response.getNombreRuta());
            return response;
        } catch (Exception e) {
            logger.error("Error obteniendo ruta por ID {}: {}", routeId, e.getMessage());
            throw new RuntimeException("Error comunicándose con Routes Service: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cierra la conexión gRPC
     */
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            logger.info("Conexión gRPC con Routes Service cerrada");
        }
    }
}

