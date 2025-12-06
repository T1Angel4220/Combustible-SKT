package com.skt.combustible.vehicles.infrastructure.client;

import com.skt.combustible.drivers.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente gRPC para comunicarse con el Drivers Service desde Vehicles Service
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

    @Autowired
    private JwtClientInterceptor jwtClientInterceptor;

    private ManagedChannel channel;
    private DriverServiceGrpc.DriverServiceBlockingStub blockingStub;

    /**
     * Inicializa la conexión gRPC con el Drivers Service
     */
    public void initialize() {
        if (channel == null || channel.isShutdown()) {
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
     * Obtiene choferes disponibles por tipo de maquinaria
     */
    public List<DriverResponse> getAvailableDriversByMachineryType(com.skt.combustible.shared.domain.enums.TipoMaquinaria tipoMaquinaria) {
        initialize();
        try {
            com.skt.combustible.drivers.grpc.TipoMaquinaria grpcTipo = mapTipoMaquinariaToGrpc(tipoMaquinaria);
            
            GetAvailableDriversByMachineryTypeRequest request = GetAvailableDriversByMachineryTypeRequest.newBuilder()
                    .setTipoMaquinaria(grpcTipo)
                    .build();

            GetAvailableDriversByMachineryTypeResponse response = 
                    blockingStub.getAvailableDriversByMachineryType(request);
            
            logger.debug("Choferes disponibles para tipo {}: {}", tipoMaquinaria, response.getCount());
            return new ArrayList<>(response.getDriversList());
        } catch (Exception e) {
            logger.error("Error obteniendo choferes disponibles por tipo {}: {}", tipoMaquinaria, e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si un chofer puede manejar un tipo específico de maquinaria
     */
    public boolean canDriverHandleMachineryType(String driverId, com.skt.combustible.shared.domain.enums.TipoMaquinaria tipoMaquinaria) {
        initialize();
        try {
            DriverResponse driver = getDriverById(driverId);
            
            // Si el chofer no tiene tipo asignado, puede manejar cualquier tipo
            if (!driver.hasTipoMaquinariaAsignada()) {
                return true;
            }
            
            // Verificar si el tipo asignado coincide con el requerido
            com.skt.combustible.shared.domain.enums.TipoMaquinaria tipoAsignado = 
                mapGrpcTipoToDomain(driver.getTipoMaquinariaAsignada());
            return tipoAsignado == tipoMaquinaria;
        } catch (Exception e) {
            logger.error("Error verificando compatibilidad chofer-maquinaria: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Mapea TipoMaquinaria del dominio a gRPC
     */
    private com.skt.combustible.drivers.grpc.TipoMaquinaria mapTipoMaquinariaToGrpc(com.skt.combustible.shared.domain.enums.TipoMaquinaria tipo) {
        switch (tipo) {
            case CAMION:
                return com.skt.combustible.drivers.grpc.TipoMaquinaria.CAMION;
            case VOLQUETE:
                return com.skt.combustible.drivers.grpc.TipoMaquinaria.VOLQUETE;
            case EXCAVADORA:
                return com.skt.combustible.drivers.grpc.TipoMaquinaria.EXCAVADORA;
            case CARGADOR:
                return com.skt.combustible.drivers.grpc.TipoMaquinaria.CARGADOR;
            case GRUA:
                return com.skt.combustible.drivers.grpc.TipoMaquinaria.GRUA;
            case MOTONIVELADORA:
                return com.skt.combustible.drivers.grpc.TipoMaquinaria.MOTONIVELADORA;
            default:
                throw new IllegalArgumentException("Tipo de maquinaria no válido: " + tipo);
        }
    }

    /**
     * Mapea TipoMaquinaria de gRPC al dominio
     */
    private com.skt.combustible.shared.domain.enums.TipoMaquinaria mapGrpcTipoToDomain(com.skt.combustible.drivers.grpc.TipoMaquinaria tipo) {
        switch (tipo) {
            case CAMION:
                return com.skt.combustible.shared.domain.enums.TipoMaquinaria.CAMION;
            case VOLQUETE:
                return com.skt.combustible.shared.domain.enums.TipoMaquinaria.VOLQUETE;
            case EXCAVADORA:
                return com.skt.combustible.shared.domain.enums.TipoMaquinaria.EXCAVADORA;
            case CARGADOR:
                return com.skt.combustible.shared.domain.enums.TipoMaquinaria.CARGADOR;
            case GRUA:
                return com.skt.combustible.shared.domain.enums.TipoMaquinaria.GRUA;
            case MOTONIVELADORA:
                return com.skt.combustible.shared.domain.enums.TipoMaquinaria.MOTONIVELADORA;
            default:
                throw new IllegalArgumentException("Tipo de maquinaria gRPC no válido: " + tipo);
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

