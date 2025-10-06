package com.skt.combustible.gateway.infrastructure.client;

import com.skt.combustible.drivers.grpc.*;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cliente gRPC para comunicarse con el Drivers Service
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class DriversGrpcClient {
    
    private static final Logger logger = LoggerFactory.getLogger(DriversGrpcClient.class);
    
    @Value("${grpc.drivers-service.host:localhost}")
    private String driversServiceHost;
    
    @Value("${grpc.drivers-service.port:9091}")
    private int driversServicePort;
    
    private ManagedChannel channel;
    private DriverServiceGrpc.DriverServiceBlockingStub blockingStub;
    
    /**
     * Inicializa la conexión gRPC con el Drivers Service
     */
    public void initialize() {
        if (channel == null || channel.isShutdown()) {
            channel = ManagedChannelBuilder.forAddress(driversServiceHost, driversServicePort)
                    .usePlaintext()
                    .build();
            blockingStub = DriverServiceGrpc.newBlockingStub(channel);
            logger.info("Cliente gRPC inicializado para Drivers Service en {}:{}", driversServiceHost, driversServicePort);
        }
    }
    
    /**
     * Obtiene un chofer por ID
     */
    public DriverResponse getDriverById(String id) {
        initialize();
        try {
            GetDriverByIdRequest request = GetDriverByIdRequest.newBuilder()
                    .setId(id)
                    .build();
            
            return blockingStub.getDriverById(request);
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
        }
    }
    
    /**
     * Obtiene todos los choferes disponibles
     */
    public List<DriverResponse> getAvailableDrivers() {
        initialize();
        try {
            GetAvailableDriversRequest request = GetAvailableDriversRequest.newBuilder().build();
            GetAvailableDriversResponse response = blockingStub.getAvailableDrivers(request);
            
            return response.getDriversList();
        } catch (Exception e) {
            logger.error("Error obteniendo choferes disponibles: {}", e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
        }
    }
    
    /**
     * Obtiene choferes disponibles por tipo de maquinaria
     */
    public List<DriverResponse> getAvailableDriversByMachineryType(TipoMaquinaria tipoMaquinaria) {
        initialize();
        try {
            GetAvailableDriversByMachineryTypeRequest request = GetAvailableDriversByMachineryTypeRequest.newBuilder()
                    .setTipoMaquinaria(mapTipoMaquinariaToGrpc(tipoMaquinaria))
                    .build();
            
            GetAvailableDriversByMachineryTypeResponse response = blockingStub.getAvailableDriversByMachineryType(request);
            
            return response.getDriversList();
        } catch (Exception e) {
            logger.error("Error obteniendo choferes disponibles por tipo de maquinaria: {}", e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
        }
    }
    
    /**
     * Cuenta choferes disponibles
     */
    public Long countAvailableDrivers() {
        initialize();
        try {
            CountAvailableDriversRequest request = CountAvailableDriversRequest.newBuilder().build();
            CountAvailableDriversResponse response = blockingStub.countAvailableDrivers(request);
            
            return response.getCount();
        } catch (Exception e) {
            logger.error("Error contando choferes disponibles: {}", e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
        }
    }
    
    /**
     * Verifica si un chofer está disponible
     */
    public Boolean isDriverAvailable(String id) {
        initialize();
        try {
            IsDriverAvailableRequest request = IsDriverAvailableRequest.newBuilder()
                    .setId(id)
                    .build();
            
            IsDriverAvailableResponse response = blockingStub.isDriverAvailable(request);
            
            return response.getAvailable();
        } catch (Exception e) {
            logger.error("Error verificando disponibilidad del chofer {}: {}", id, e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
        }
    }
    
    /**
     * Mapea TipoMaquinaria del dominio a gRPC
     */
    private com.skt.combustible.drivers.grpc.TipoMaquinaria mapTipoMaquinariaToGrpc(TipoMaquinaria tipo) {
        return switch (tipo) {
            case CAMION -> com.skt.combustible.drivers.grpc.TipoMaquinaria.CAMION;
            case VOLQUETE -> com.skt.combustible.drivers.grpc.TipoMaquinaria.VOLQUETE;
            case EXCAVADORA -> com.skt.combustible.drivers.grpc.TipoMaquinaria.EXCAVADORA;
            case CARGADOR -> com.skt.combustible.drivers.grpc.TipoMaquinaria.CARGADOR;
            case GRUA -> com.skt.combustible.drivers.grpc.TipoMaquinaria.GRUA;
            case MOTONIVELADORA -> com.skt.combustible.drivers.grpc.TipoMaquinaria.MOTONIVELADORA;
        };
    }
    
    /**
     * Cierra la conexión gRPC
     */
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            logger.info("Cliente gRPC cerrado para Drivers Service");
        }
    }
}
