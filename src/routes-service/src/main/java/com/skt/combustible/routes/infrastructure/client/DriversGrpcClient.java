package com.skt.combustible.routes.infrastructure.client;

import com.skt.combustible.drivers.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;

/**
 * Cliente gRPC para comunicarse con el Drivers Service desde Routes Service
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class DriversGrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(DriversGrpcClient.class);

    @Value("${grpc.client.drivers-service.host:localhost}")
    private String driversServiceHost;

    @Value("${grpc.client.drivers-service.port:9091}")
    private int driversServicePort;

    @Autowired
    private JwtClientInterceptor jwtClientInterceptor;

    private ManagedChannel channel;
    private DriverServiceGrpc.DriverServiceBlockingStub blockingStub;

    /**
     * Inicializa la conexión gRPC con el Drivers Service
     */
    public void initialize() {
        if (channel == null || channel.isShutdown() || channel.isTerminated()) {
            // Cerrar el canal anterior si existe
            if (channel != null && !channel.isShutdown()) {
                try {
                    channel.shutdown();
                } catch (Exception e) {
                    logger.warn("Error cerrando canal anterior: {}", e.getMessage());
                }
            }
            
            channel = ManagedChannelBuilder.forAddress(driversServiceHost, driversServicePort)
                    .usePlaintext()
                    .intercept(jwtClientInterceptor) // Agregar interceptor JWT
                    .build();
            blockingStub = DriverServiceGrpc.newBlockingStub(channel);
            logger.info("Cliente gRPC inicializado CON JWT INTERCEPTOR para Drivers Service en {}:{}",
                    driversServiceHost, driversServicePort);
        }
    }

    /**
     * Obtiene un chofer por ID
     */
    public DriverResponse getDriverById(String driverId) {
        initialize();
        try {
            GetDriverByIdRequest request = GetDriverByIdRequest.newBuilder()
                    .setId(driverId)
                    .build();

            DriverResponse response = blockingStub.getDriverById(request);
            logger.debug("Chofer obtenido: {} {}", response.getNombre(), response.getApellido());
            return response;
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por ID {}: {}", driverId, e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si un chofer está disponible
     */
    public boolean isDriverAvailable(String driverId) {
        initialize();
        try {
            IsDriverAvailableRequest request = IsDriverAvailableRequest.newBuilder()
                    .setId(driverId)
                    .build();

            IsDriverAvailableResponse response = blockingStub.isDriverAvailable(request);
            logger.debug("Chofer {} disponible: {}", driverId, response.getAvailable());
            return response.getAvailable();
        } catch (Exception e) {
            logger.error("Error verificando disponibilidad del chofer {}: {}", driverId, e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los conductores disponibles
     */
    public List<DriverResponse> getAvailableDrivers() {
        initialize();
        try {
            GetAvailableDriversRequest request = GetAvailableDriversRequest.newBuilder().build();
            GetAvailableDriversResponse response = blockingStub.getAvailableDrivers(request);
            logger.debug("Conductores disponibles obtenidos: {}", response.getDriversCount());
            return response.getDriversList();
        } catch (Exception e) {
            logger.error("Error obteniendo conductores disponibles: {}", e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service: " + e.getMessage(), e);
        }
    }

    /**
     * Cierra la conexión gRPC
     */
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            logger.info("Conexión gRPC con Drivers Service cerrada");
        }
    }
}

