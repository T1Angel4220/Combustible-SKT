package com.skt.combustible.gateway.infrastructure.client;

import com.skt.combustible.drivers.grpc.*;
import com.skt.combustible.gateway.infrastructure.interceptor.JwtClientInterceptor;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;

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
            // Crear una instancia del interceptor JWT
            JwtClientInterceptor jwtInterceptor = new JwtClientInterceptor();

            channel = ManagedChannelBuilder.forAddress(driversServiceHost, driversServicePort)
                    .usePlaintext()
                    .intercept(jwtInterceptor) // Agregar interceptor JWT
                    .build();
            blockingStub = DriverServiceGrpc.newBlockingStub(channel);
            logger.info("Cliente gRPC inicializado CON JWT INTERCEPTOR para Drivers Service en {}:{}",
                    driversServiceHost, driversServicePort);
        }
    }

    /**
     * Crea un nuevo chofer
     */
    public com.skt.combustible.drivers.grpc.DriverResponse createDriver(
            com.skt.combustible.gateway.domain.dto.CreateDriverRequest request) {
        initialize();
        try {
            com.skt.combustible.drivers.grpc.CreateDriverRequest grpcRequest = com.skt.combustible.drivers.grpc.CreateDriverRequest
                    .newBuilder()
                    .setNombre(request.getNombre())
                    .setApellido(request.getApellido())
                    .setDni(request.getDni())
                    .setLicencia(request.getLicencia())
                    .setTelefono(request.getTelefono() != null ? request.getTelefono() : "")
                    .setEmail(request.getEmail() != null ? request.getEmail() : "")
                    .setFechaContratacion(request.getFechaContratacion() != null ? request.getFechaContratacion() : "")
                    .setEstado(mapEstadoToGrpc(request.getEstado()))
                    .setTipoMaquinariaAsignada(mapTipoMaquinariaToGrpc(request.getTipoMaquinariaAsignada()))
                    .build();

            com.skt.combustible.drivers.grpc.DriverResponse response = blockingStub.createDriver(grpcRequest);
            return response;
        } catch (Exception e) {
            logger.error("Error creando chofer: {}", e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
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
     * Obtiene todos los choferes
     */
    public List<DriverResponse> getAllDrivers() {
        initialize();
        try {
            GetAllDriversRequest request = GetAllDriversRequest.newBuilder().build();
            GetAllDriversResponse response = blockingStub.getAllDrivers(request);

            return response.getDriversList();
        } catch (Exception e) {
            logger.error("Error obteniendo todos los choferes: {}", e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
        }
    }

    /**
     * Actualiza un chofer via gRPC
     */
    public DriverResponse updateDriver(String id, com.skt.combustible.gateway.domain.dto.UpdateDriverRequest request) {
        initialize();
        try {
            com.skt.combustible.drivers.grpc.UpdateDriverRequest grpcRequest = com.skt.combustible.drivers.grpc.UpdateDriverRequest
                    .newBuilder()
                    .setId(id)
                    .setNombre(request.getNombre() != null ? request.getNombre() : "")
                    .setApellido(request.getApellido() != null ? request.getApellido() : "")
                    .setTelefono(request.getTelefono() != null ? request.getTelefono() : "")
                    .setEmail(request.getEmail() != null ? request.getEmail() : "")
                    .setFechaContratacion(request.getFechaContratacion() != null ? request.getFechaContratacion() : "")
                    .setEstado(mapEstadoToGrpc(request.getEstado()))
                    .setTipoMaquinariaAsignada(mapTipoMaquinariaToGrpc(request.getTipoMaquinariaAsignada()))
                    .setActivo(request.getActivo() != null ? request.getActivo() : true)
                    .build();

            DriverResponse response = blockingStub.updateDriver(grpcRequest);
            return response;
        } catch (Exception e) {
            logger.error("Error actualizando chofer: {}", e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
        }
    }

    /**
     * Desactiva un chofer via gRPC
     */
    public com.skt.combustible.drivers.grpc.Empty deactivateDriver(String id) {
        initialize();
        try {
            DeactivateDriverRequest request = DeactivateDriverRequest.newBuilder()
                    .setId(id)
                    .build();

            com.skt.combustible.drivers.grpc.Empty response = blockingStub.deactivateDriver(request);
            return response;
        } catch (Exception e) {
            logger.error("Error desactivando chofer: {}", e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
        }
    }

    /**
     * Elimina permanentemente un chofer via gRPC
     */
    public com.skt.combustible.drivers.grpc.Empty deleteDriverPermanently(String id) {
        initialize();
        try {
            DeleteDriverRequest request = DeleteDriverRequest.newBuilder()
                    .setId(id)
                    .build();

            com.skt.combustible.drivers.grpc.Empty response = blockingStub.deleteDriver(request);
            return response;
        } catch (Exception e) {
            logger.error("Error eliminando chofer permanentemente: {}", e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
        }
    }

    // Métodos helper para mapeo de enums
    private com.skt.combustible.drivers.grpc.EstadoOperativo mapEstadoToGrpc(
            com.skt.combustible.shared.domain.enums.EstadoOperativo estado) {
        if (estado == null) {
            return com.skt.combustible.drivers.grpc.EstadoOperativo.DISPONIBLE;
        }

        switch (estado) {
            case DISPONIBLE:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.DISPONIBLE;
            case ASIGNADO:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.ASIGNADO;
            case EN_RUTA:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.EN_RUTA;
            case DESCANSANDO:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.DESCANSANDO;
            case VACACIONES:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.VACACIONES;
            case ENFERMO:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.ENFERMO;
            case LICENCIA:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.LICENCIA;
            case EN_USO:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.EN_USO;
            case MANTENIMIENTO:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.MANTENIMIENTO;
            case FUERA_SERVICIO:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.FUERA_SERVICIO;
            case RESERVADO:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.RESERVADO;
            default:
                return com.skt.combustible.drivers.grpc.EstadoOperativo.DISPONIBLE;
        }
    }

    private com.skt.combustible.drivers.grpc.TipoMaquinaria mapTipoMaquinariaToGrpc(
            com.skt.combustible.shared.domain.enums.TipoMaquinaria tipo) {
        if (tipo == null) {
            return com.skt.combustible.drivers.grpc.TipoMaquinaria.CAMION;
        }

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
                return com.skt.combustible.drivers.grpc.TipoMaquinaria.CAMION;
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

            GetAvailableDriversByMachineryTypeResponse response = blockingStub
                    .getAvailableDriversByMachineryType(request);

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
     * Reactiva un chofer
     */
    public DriverResponse activateDriver(String id) {
        initialize();
        try {
            ActivateDriverRequest request = ActivateDriverRequest.newBuilder()
                    .setId(id)
                    .build();

            DriverResponse response = blockingStub.activateDriver(request);
            return response;
        } catch (Exception e) {
            logger.error("Error reactivando chofer: {}", e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
        }
    }

    /**
     * Obtiene choferes en servicio (Asignado y En Ruta)
     */
    public com.skt.combustible.drivers.grpc.GetDriversInServiceResponse getDriversInService() {
        initialize();
        try {
            com.skt.combustible.drivers.grpc.GetDriversInServiceRequest request = com.skt.combustible.drivers.grpc.GetDriversInServiceRequest
                    .newBuilder().build();

            com.skt.combustible.drivers.grpc.GetDriversInServiceResponse response = blockingStub
                    .getDriversInService(request);
            return response;
        } catch (Exception e) {
            logger.error("Error obteniendo choferes en servicio: {}", e.getMessage());
            throw new RuntimeException("Error comunicándose con Drivers Service", e);
        }
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
