package com.skt.combustible.drivers.controller;

import com.skt.combustible.drivers.domain.dto.CreateDriverRequest;
import com.skt.combustible.drivers.domain.dto.UpdateDriverRequest;
import com.skt.combustible.drivers.domain.service.DriverService;
import com.skt.combustible.drivers.grpc.*;
import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador gRPC para el servicio de choferes
 * Implementa la interfaz DriverServiceGrpc.DriverServiceImplBase
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@GrpcService
public class DriverGrpcController extends DriverServiceGrpc.DriverServiceImplBase {
    
    private static final Logger logger = LoggerFactory.getLogger(DriverGrpcController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final DriverService driverService;
    
    public DriverGrpcController(DriverService driverService) {
        this.driverService = driverService;
    }
    
    @Override
    public void createDriver(com.skt.combustible.drivers.grpc.CreateDriverRequest request, StreamObserver<com.skt.combustible.drivers.grpc.DriverResponse> responseObserver) {
        logger.info("gRPC: Creando nuevo chofer: {}", request.getNombre() + " " + request.getApellido());
        
        try {
            CreateDriverRequest createRequest = new CreateDriverRequest(
                request.getNombre(),
                request.getApellido(),
                request.getDni(),
                request.getLicencia()
            );
            
            if (!request.getTelefono().isEmpty()) {
                createRequest.setTelefono(request.getTelefono());
            }
            if (!request.getEmail().isEmpty()) {
                createRequest.setEmail(request.getEmail());
            }
            
            if (!request.getFechaContratacion().isEmpty()) {
                createRequest.setFechaContratacion(LocalDate.parse(request.getFechaContratacion(), DATE_FORMATTER));
            }
            
            if (request.hasTipoMaquinariaAsignada()) {
                createRequest.setTipoMaquinariaAsignada(mapTipoMaquinaria(request.getTipoMaquinariaAsignada()));
            }
            
            com.skt.combustible.drivers.domain.dto.DriverResponse response = driverService.createDriver(createRequest);
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = mapToGrpcResponse(response);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error creando chofer: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void getDriverById(com.skt.combustible.drivers.grpc.GetDriverByIdRequest request, StreamObserver<com.skt.combustible.drivers.grpc.DriverResponse> responseObserver) {
        logger.info("gRPC: Obteniendo chofer por ID: {}", request.getId());
        
        try {
            com.skt.combustible.drivers.domain.dto.DriverResponse response = driverService.getDriverById(String.valueOf(request.getId()));
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = mapToGrpcResponse(response);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por ID: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void getDriverByDni(com.skt.combustible.drivers.grpc.GetDriverByDniRequest request, StreamObserver<com.skt.combustible.drivers.grpc.DriverResponse> responseObserver) {
        logger.info("gRPC: Obteniendo chofer por DNI: {}", request.getDni());
        
        try {
            com.skt.combustible.drivers.domain.dto.DriverResponse response = driverService.getDriverByDni(request.getDni());
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = mapToGrpcResponse(response);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por DNI: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void getDriverByLicense(com.skt.combustible.drivers.grpc.GetDriverByLicenseRequest request, StreamObserver<com.skt.combustible.drivers.grpc.DriverResponse> responseObserver) {
        logger.info("gRPC: Obteniendo chofer por licencia: {}", request.getLicencia());
        
        try {
            com.skt.combustible.drivers.domain.dto.DriverResponse response = driverService.getDriverByLicencia(request.getLicencia());
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = mapToGrpcResponse(response);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por licencia: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void getAvailableDrivers(com.skt.combustible.drivers.grpc.GetAvailableDriversRequest request, StreamObserver<com.skt.combustible.drivers.grpc.GetAvailableDriversResponse> responseObserver) {
        logger.info("gRPC: Obteniendo choferes disponibles");
        
        try {
            List<com.skt.combustible.drivers.domain.dto.DriverResponse> drivers = driverService.getAvailableDrivers();
            
            com.skt.combustible.drivers.grpc.GetAvailableDriversResponse.Builder responseBuilder = 
                com.skt.combustible.drivers.grpc.GetAvailableDriversResponse.newBuilder();
            
            for (com.skt.combustible.drivers.domain.dto.DriverResponse driver : drivers) {
                responseBuilder.addDrivers(mapToGrpcResponse(driver));
            }
            
            com.skt.combustible.drivers.grpc.GetAvailableDriversResponse response = responseBuilder.setCount(drivers.size()).build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error obteniendo choferes disponibles: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void getAvailableDriversByMachineryType(com.skt.combustible.drivers.grpc.GetAvailableDriversByMachineryTypeRequest request, StreamObserver<com.skt.combustible.drivers.grpc.GetAvailableDriversByMachineryTypeResponse> responseObserver) {
        logger.info("gRPC: Obteniendo choferes disponibles para tipo de maquinaria: {}", request.getTipoMaquinaria());
        
        try {
            TipoMaquinaria tipoMaquinaria = mapTipoMaquinaria(request.getTipoMaquinaria());
            List<com.skt.combustible.drivers.domain.dto.DriverResponse> drivers = driverService.getAvailableDriversByMachineryType(tipoMaquinaria);
            
            com.skt.combustible.drivers.grpc.GetAvailableDriversByMachineryTypeResponse.Builder responseBuilder = 
                com.skt.combustible.drivers.grpc.GetAvailableDriversByMachineryTypeResponse.newBuilder();
            
            for (com.skt.combustible.drivers.domain.dto.DriverResponse driver : drivers) {
                responseBuilder.addDrivers(mapToGrpcResponse(driver));
            }
            
            com.skt.combustible.drivers.grpc.GetAvailableDriversByMachineryTypeResponse response = responseBuilder
                .setCount(drivers.size())
                .setTipoMaquinariaFiltrada(request.getTipoMaquinaria())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error obteniendo choferes disponibles por tipo de maquinaria: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void updateDriver(com.skt.combustible.drivers.grpc.UpdateDriverRequest request, StreamObserver<com.skt.combustible.drivers.grpc.DriverResponse> responseObserver) {
        logger.info("gRPC: Actualizando chofer con ID: {}", request.getId());
        
        try {
            UpdateDriverRequest updateRequest = new UpdateDriverRequest();
            
            if (!request.getNombre().isEmpty()) {
                updateRequest.setNombre(request.getNombre());
            }
            if (!request.getApellido().isEmpty()) {
                updateRequest.setApellido(request.getApellido());
            }
            if (!request.getTelefono().isEmpty()) {
                updateRequest.setTelefono(request.getTelefono());
            }
            if (!request.getEmail().isEmpty()) {
                updateRequest.setEmail(request.getEmail());
            }
            if (!request.getFechaContratacion().isEmpty()) {
                updateRequest.setFechaContratacion(LocalDate.parse(request.getFechaContratacion(), DATE_FORMATTER));
            }
            if (request.hasEstado()) {
                updateRequest.setEstado(mapEstadoOperativo(request.getEstado()));
            }
            if (request.hasTipoMaquinariaAsignada()) {
                updateRequest.setTipoMaquinariaAsignada(mapTipoMaquinaria(request.getTipoMaquinariaAsignada()));
            }
            if (request.hasActivo()) {
                updateRequest.setActivo(request.getActivo());
            }
            
            com.skt.combustible.drivers.domain.dto.DriverResponse response = driverService.updateDriver(String.valueOf(request.getId()), updateRequest);
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = mapToGrpcResponse(response);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error actualizando chofer: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void changeDriverStatus(com.skt.combustible.drivers.grpc.ChangeDriverStatusRequest request, StreamObserver<com.skt.combustible.drivers.grpc.DriverResponse> responseObserver) {
        logger.info("gRPC: Cambiando estado del chofer ID: {} a {}", request.getId(), request.getNuevoEstado());
        
        try {
            EstadoOperativo nuevoEstado = mapEstadoOperativo(request.getNuevoEstado());
            com.skt.combustible.drivers.domain.dto.DriverResponse response = driverService.changeDriverStatus(String.valueOf(request.getId()), nuevoEstado);
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = mapToGrpcResponse(response);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error cambiando estado del chofer: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void assignMachineryType(com.skt.combustible.drivers.grpc.AssignMachineryTypeRequest request, StreamObserver<com.skt.combustible.drivers.grpc.DriverResponse> responseObserver) {
        logger.info("gRPC: Asignando tipo de maquinaria {} al chofer ID: {}", request.getTipoMaquinaria(), request.getId());
        
        try {
            TipoMaquinaria tipoMaquinaria = mapTipoMaquinaria(request.getTipoMaquinaria());
            com.skt.combustible.drivers.domain.dto.DriverResponse response = driverService.assignMachineryType(String.valueOf(request.getId()), tipoMaquinaria);
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = mapToGrpcResponse(response);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error asignando tipo de maquinaria: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void deactivateDriver(com.skt.combustible.drivers.grpc.DeactivateDriverRequest request, StreamObserver<com.skt.combustible.drivers.grpc.Empty> responseObserver) {
        logger.info("gRPC: Desactivando chofer con ID: {}", request.getId());
        
        try {
            driverService.deactivateDriver(String.valueOf(request.getId()));
            
            responseObserver.onNext(com.skt.combustible.drivers.grpc.Empty.newBuilder().build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error desactivando chofer: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void countAvailableDrivers(com.skt.combustible.drivers.grpc.CountAvailableDriversRequest request, StreamObserver<com.skt.combustible.drivers.grpc.CountAvailableDriversResponse> responseObserver) {
        logger.info("gRPC: Contando choferes disponibles");
        
        try {
            long count = driverService.countAvailableDrivers();
            
            com.skt.combustible.drivers.grpc.CountAvailableDriversResponse response = 
                com.skt.combustible.drivers.grpc.CountAvailableDriversResponse.newBuilder()
                .setCount(count)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error contando choferes disponibles: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void isDriverAvailable(com.skt.combustible.drivers.grpc.IsDriverAvailableRequest request, StreamObserver<com.skt.combustible.drivers.grpc.IsDriverAvailableResponse> responseObserver) {
        logger.info("gRPC: Verificando disponibilidad del chofer ID: {}", request.getId());
        
        try {
            boolean available = driverService.isDriverAvailable(String.valueOf(request.getId()));
            
            com.skt.combustible.drivers.grpc.IsDriverAvailableResponse response = 
                com.skt.combustible.drivers.grpc.IsDriverAvailableResponse.newBuilder()
                .setAvailable(available)
                .setDriverId(request.getId())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error verificando disponibilidad del chofer: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    // Métodos de mapeo
    private com.skt.combustible.drivers.grpc.DriverResponse mapToGrpcResponse(com.skt.combustible.drivers.domain.dto.DriverResponse response) {
        com.skt.combustible.drivers.grpc.DriverResponse.Builder builder = 
            com.skt.combustible.drivers.grpc.DriverResponse.newBuilder()
            .setId(Long.parseLong(response.getId()))
            .setNombre(response.getNombre())
            .setApellido(response.getApellido())
            .setDni(response.getDni())
            .setLicencia(response.getLicencia())
            .setEstado(mapEstadoOperativo(response.getEstado()))
            .setActivo(response.getActivo())
            .setCreatedAt(response.getCreatedAt().toString())
            .setUpdatedAt(response.getUpdatedAt().toString());
        
        if (response.getTelefono() != null) {
            builder.setTelefono(response.getTelefono());
        }
        if (response.getEmail() != null) {
            builder.setEmail(response.getEmail());
        }
        if (response.getFechaContratacion() != null) {
            builder.setFechaContratacion(response.getFechaContratacion().format(DATE_FORMATTER));
        }
        if (response.getTipoMaquinariaAsignada() != null) {
            builder.setTipoMaquinariaAsignada(mapTipoMaquinaria(response.getTipoMaquinariaAsignada()));
        }
        
        return builder.build();
    }
    
    private com.skt.combustible.drivers.grpc.EstadoOperativo mapEstadoOperativo(EstadoOperativo estado) {
        return switch (estado) {
            case ACTIVO -> com.skt.combustible.drivers.grpc.EstadoOperativo.ACTIVO;
            case MANTENIMIENTO -> com.skt.combustible.drivers.grpc.EstadoOperativo.MANTENIMIENTO;
            case FUERA_SERVICIO -> com.skt.combustible.drivers.grpc.EstadoOperativo.FUERA_SERVICIO;
            case DISPONIBLE -> com.skt.combustible.drivers.grpc.EstadoOperativo.DISPONIBLE;
            case EN_USO -> com.skt.combustible.drivers.grpc.EstadoOperativo.EN_USO;
            case RESERVADO -> com.skt.combustible.drivers.grpc.EstadoOperativo.RESERVADO;
        };
    }
    
    private EstadoOperativo mapEstadoOperativo(com.skt.combustible.drivers.grpc.EstadoOperativo estado) {
        return switch (estado) {
            case ACTIVO -> EstadoOperativo.ACTIVO;
            case MANTENIMIENTO -> EstadoOperativo.MANTENIMIENTO;
            case FUERA_SERVICIO -> EstadoOperativo.FUERA_SERVICIO;
            case DISPONIBLE -> EstadoOperativo.DISPONIBLE;
            case EN_USO -> EstadoOperativo.EN_USO;
            case RESERVADO -> EstadoOperativo.RESERVADO;
            case UNRECOGNIZED -> EstadoOperativo.ACTIVO; // Default fallback
        };
    }
    
    private com.skt.combustible.drivers.grpc.TipoMaquinaria mapTipoMaquinaria(TipoMaquinaria tipo) {
        return switch (tipo) {
            case CAMION -> com.skt.combustible.drivers.grpc.TipoMaquinaria.CAMION;
            case VOLQUETE -> com.skt.combustible.drivers.grpc.TipoMaquinaria.VOLQUETE;
            case EXCAVADORA -> com.skt.combustible.drivers.grpc.TipoMaquinaria.EXCAVADORA;
            case CARGADOR -> com.skt.combustible.drivers.grpc.TipoMaquinaria.CARGADOR;
            case GRUA -> com.skt.combustible.drivers.grpc.TipoMaquinaria.GRUA;
            case MOTONIVELADORA -> com.skt.combustible.drivers.grpc.TipoMaquinaria.MOTONIVELADORA;
        };
    }
    
    private TipoMaquinaria mapTipoMaquinaria(com.skt.combustible.drivers.grpc.TipoMaquinaria tipo) {
        return switch (tipo) {
            case CAMION -> TipoMaquinaria.CAMION;
            case VOLQUETE -> TipoMaquinaria.VOLQUETE;
            case EXCAVADORA -> TipoMaquinaria.EXCAVADORA;
            case CARGADOR -> TipoMaquinaria.CARGADOR;
            case GRUA -> TipoMaquinaria.GRUA;
            case MOTONIVELADORA -> TipoMaquinaria.MOTONIVELADORA;
            case UNRECOGNIZED -> TipoMaquinaria.CAMION; // Default fallback
        };
    }
}