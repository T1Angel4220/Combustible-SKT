package com.skt.combustible.routes.infrastructure.client;

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
 * Cliente gRPC para comunicarse con el Vehicles Service desde Routes Service
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
            // Cerrar el canal anterior si existe
            if (channel != null && !channel.isShutdown()) {
                try {
                    channel.shutdown();
                } catch (Exception e) {
                    logger.warn("Error cerrando canal anterior: {}", e.getMessage());
                }
            }
            
            channel = ManagedChannelBuilder.forAddress(vehiclesServiceHost, vehiclesServicePort)
                    .usePlaintext()
                    .intercept(jwtClientInterceptor) // Agregar interceptor JWT
                    .build();
            blockingStub = VehicleServiceProtoGrpc.newBlockingStub(channel);
            logger.info("Cliente gRPC inicializado CON JWT INTERCEPTOR para Vehicles Service en {}:{}",
                    vehiclesServiceHost, vehiclesServicePort);
        }
    }
    
    /**
     * Fuerza la recreación del canal (útil cuando hay errores de conexión)
     */
    private void recreateChannel() {
        logger.warn("Recreando canal gRPC para Vehicles Service debido a error de conexión");
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown();
            } catch (Exception e) {
                logger.warn("Error cerrando canal: {}", e.getMessage());
            }
        }
        channel = null;
        blockingStub = null;
        initialize();
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
        } catch (io.grpc.StatusRuntimeException e) {
            // Si es un error de conexión, intentar recrear el canal una vez
            if (e.getStatus().getCode() == io.grpc.Status.Code.UNAVAILABLE) {
                logger.warn("Error de conexión UNAVAILABLE, intentando recrear canal...");
                recreateChannel();
                try {
                    VehicleIdRequestProto request = VehicleIdRequestProto.newBuilder()
                            .setId(vehicleId)
                            .build();
                    VehicleResponseProto response = blockingStub.obtenerVehiculoPorId(request);
                    logger.debug("Vehículo obtenido después de recrear canal: {} {}", response.getMarca(), response.getModelo());
                    return response;
                } catch (Exception retryException) {
                    logger.error("Error obteniendo vehículo por ID {} después de recrear canal: {}", vehicleId, retryException.getMessage());
                    throw new RuntimeException("Error comunicándose con Vehicles Service después de reintento: " + retryException.getMessage(), retryException);
                }
            }
            logger.error("Error obteniendo vehículo por ID {}: {}", vehicleId, e.getMessage());
            throw new RuntimeException("Error comunicándose con Vehicles Service: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error obteniendo vehículo por ID {}: {}", vehicleId, e.getMessage());
            throw new RuntimeException("Error comunicándose con Vehicles Service: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si un vehículo está disponible
     */
    public boolean isVehicleAvailable(String vehicleId) {
        initialize();
        try {
            VehicleResponseProto vehicle = getVehicleById(vehicleId);
            // Un vehículo está disponible si está activo y su estado es DISPONIBLE
            return vehicle.getActivo() && 
                   vehicle.getEstadoOperativo() == EstadoOperativoProto.DISPONIBLE;
        } catch (Exception e) {
            logger.error("Error verificando disponibilidad del vehículo {}: {}", vehicleId, e.getMessage());
            return false;
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

