package com.skt.combustible.vehicles.infrastructure.grpc;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.application.service.VehicleService;
import com.skt.combustible.vehicles.domain.dto.VehicleCreateRequest;
import com.skt.combustible.vehicles.domain.dto.VehicleResponse;
import com.skt.combustible.vehicles.domain.dto.VehicleUpdateRequest;
import com.skt.combustible.vehicles.grpc.VehicleServiceProtoGrpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Controlador gRPC para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@GrpcService
@Component
public class VehicleGrpcController extends VehicleServiceProtoGrpc.VehicleServiceProtoImplBase {
    
    private static final Logger logger = LoggerFactory.getLogger(VehicleGrpcController.class);
    
    @Autowired
    private VehicleService vehicleService;
    
    @Override
    public void crearVehiculo(com.skt.combustible.vehicles.grpc.VehicleCreateRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Creando vehículo con placa: {}", request.getPlaca());
            
            // Convertir de gRPC a dominio
            VehicleCreateRequest domainRequest = new VehicleCreateRequest();
            domainRequest.setPlaca(request.getPlaca());
            domainRequest.setMarca(request.getMarca());
            domainRequest.setModelo(request.getModelo());
            domainRequest.setAnio(request.getAnio());
            domainRequest.setTipoMaquinaria(mapTipoMaquinariaFromGrpc(request.getTipoMaquinaria()));
            domainRequest.setEstadoOperativo(mapEstadoFromGrpc(request.getEstadoOperativo()));
            domainRequest.setCapacidadTanque(request.getCapacidadTanque());
            domainRequest.setConsumoPromedio(request.getConsumoPromedio());
            domainRequest.setKilometrajeActual(request.getKilometrajeActual());
            // El campo 'activo' se establece automáticamente en el servicio
            
            VehicleResponse domainResponse = vehicleService.crearVehiculo(domainRequest);
            
            // Convertir de dominio a gRPC
            com.skt.combustible.vehicles.grpc.VehicleResponseProto grpcResponse = mapToGrpcResponse(domainResponse);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            logger.info("gRPC: Vehículo creado exitosamente: {}", domainResponse.getPlaca());
        } catch (Exception e) {
            logger.error("gRPC: Error al crear vehículo: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al crear vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerVehiculoPorId(com.skt.combustible.vehicles.grpc.VehicleIdRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo vehículo por ID: {}", request.getId());
            
            Optional<VehicleResponse> domainResponse = vehicleService.obtenerVehiculoPorId(request.getId());
            if (domainResponse.isPresent()) {
                com.skt.combustible.vehicles.grpc.VehicleResponseProto grpcResponse = mapToGrpcResponse(domainResponse.get());
                responseObserver.onNext(grpcResponse);
                responseObserver.onCompleted();
                logger.info("gRPC: Vehículo obtenido: {}", domainResponse.get().getPlaca());
            } else {
                logger.warn("gRPC: Vehículo no encontrado con ID: {}", request.getId());
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Vehículo no encontrado con ID: " + request.getId())
                    .asRuntimeException());
            }
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener vehículo por ID: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerVehiculoPorPlaca(com.skt.combustible.vehicles.grpc.VehiclePlacaRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo vehículo por placa: {}", request.getPlaca());
            
            Optional<VehicleResponse> domainResponse = vehicleService.obtenerVehiculoPorPlaca(request.getPlaca());
            if (domainResponse.isPresent()) {
                com.skt.combustible.vehicles.grpc.VehicleResponseProto grpcResponse = mapToGrpcResponse(domainResponse.get());
                responseObserver.onNext(grpcResponse);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Vehículo no encontrado con placa: " + request.getPlaca())
                    .asRuntimeException());
            }
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener vehículo por placa: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerTodosLosVehiculos(com.google.protobuf.Empty request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo todos los vehículos");
            
            List<VehicleResponse> responses = vehicleService.obtenerTodosLosVehiculos();
            for (VehicleResponse response : responses) {
                com.skt.combustible.vehicles.grpc.VehicleResponseProto grpcResponse = mapToGrpcResponse(response);
                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
            logger.info("gRPC: {} vehículos obtenidos", responses.size());
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener vehículos: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículos: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void actualizarVehiculo(com.skt.combustible.vehicles.grpc.VehicleUpdateRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Actualizando vehículo");
            
            // Convertir de gRPC a dominio
            VehicleUpdateRequest domainRequest = new VehicleUpdateRequest();
            domainRequest.setMarca(request.getMarca());
            domainRequest.setModelo(request.getModelo());
            domainRequest.setAnio(request.getAnio());
            domainRequest.setTipoMaquinaria(mapTipoMaquinariaFromGrpc(request.getTipoMaquinaria()));
            domainRequest.setEstadoOperativo(mapEstadoFromGrpc(request.getEstadoOperativo()));
            domainRequest.setCapacidadTanque(request.getCapacidadTanque());
            domainRequest.setConsumoPromedio(request.getConsumoPromedio());
            domainRequest.setKilometrajeActual(request.getKilometrajeActual());
            domainRequest.setActivo(request.getActivo());
            
            // Nota: El proto no incluye el ID en VehicleUpdateRequestProto, necesitaríamos agregarlo
            // Por ahora, asumimos que se actualiza por placa o necesitamos modificar el proto
            responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Método no implementado completamente")
                .asRuntimeException());
        } catch (Exception e) {
            logger.error("gRPC: Error al actualizar vehículo: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al actualizar vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void desactivarVehiculo(com.skt.combustible.vehicles.grpc.VehicleIdRequestProto request,
            StreamObserver<com.google.protobuf.Empty> responseObserver) {
        try {
            logger.info("gRPC: Desactivando vehículo con ID: {}", request.getId());
            vehicleService.desactivarVehiculo(request.getId());
            responseObserver.onNext(com.google.protobuf.Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al desactivar vehículo: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al desactivar vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerVehiculosPorTipo(com.skt.combustible.vehicles.grpc.VehicleTipoRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo vehículos por tipo: {}", request.getTipoMaquinaria());
            
            TipoMaquinaria tipo = mapTipoMaquinariaFromGrpc(request.getTipoMaquinaria());
            List<VehicleResponse> responses = vehicleService.obtenerVehiculosPorTipo(tipo);
            for (VehicleResponse response : responses) {
                com.skt.combustible.vehicles.grpc.VehicleResponseProto grpcResponse = mapToGrpcResponse(response);
                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener vehículos por tipo: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículos por tipo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerVehiculosPorEstado(com.skt.combustible.vehicles.grpc.VehicleEstadoRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo vehículos por estado: {}", request.getEstadoOperativo());
            
            EstadoOperativo estado = mapEstadoFromGrpc(request.getEstadoOperativo());
            List<VehicleResponse> responses = vehicleService.obtenerVehiculosPorEstado(estado);
            for (VehicleResponse response : responses) {
                com.skt.combustible.vehicles.grpc.VehicleResponseProto grpcResponse = mapToGrpcResponse(response);
                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener vehículos por estado: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículos por estado: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerVehiculosDisponibles(com.google.protobuf.Empty request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo vehículos disponibles");
            
            List<VehicleResponse> responses = vehicleService.obtenerVehiculosDisponibles();
            for (VehicleResponse response : responses) {
                com.skt.combustible.vehicles.grpc.VehicleResponseProto grpcResponse = mapToGrpcResponse(response);
                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener vehículos disponibles: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículos disponibles: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerVehiculosDisponiblesPorTipo(com.skt.combustible.vehicles.grpc.VehicleTipoRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo vehículos disponibles por tipo: {}", request.getTipoMaquinaria());
            
            TipoMaquinaria tipo = mapTipoMaquinariaFromGrpc(request.getTipoMaquinaria());
            List<VehicleResponse> responses = vehicleService.obtenerVehiculosDisponiblesPorTipo(tipo);
            for (VehicleResponse response : responses) {
                com.skt.combustible.vehicles.grpc.VehicleResponseProto grpcResponse = mapToGrpcResponse(response);
                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener vehículos disponibles por tipo: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículos disponibles por tipo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void cambiarEstadoVehiculo(com.skt.combustible.vehicles.grpc.VehicleEstadoRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleResponseProto> responseObserver) {
        // Este método requiere el ID del vehículo, pero el proto solo tiene el estado
        // Necesitaríamos modificar el proto o usar otro método
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
            .withDescription("Método requiere ID del vehículo")
            .asRuntimeException());
    }
    
    @Override
    public void actualizarKilometraje(com.skt.combustible.vehicles.grpc.VehicleKilometrajeRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Actualizando kilometraje del vehículo: {}", request.getId());
            
            VehicleResponse domainResponse = vehicleService.actualizarKilometraje(request.getId(), request.getKilometraje());
            com.skt.combustible.vehicles.grpc.VehicleResponseProto grpcResponse = mapToGrpcResponse(domainResponse);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al actualizar kilometraje: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al actualizar kilometraje: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerEstadisticas(com.google.protobuf.Empty request,
            StreamObserver<com.skt.combustible.vehicles.grpc.VehicleStatsResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo estadísticas de vehículos");
            
            VehicleService.VehicleStatsDTO stats = vehicleService.obtenerEstadisticas();
            com.skt.combustible.vehicles.grpc.VehicleStatsResponseProto grpcResponse = 
                com.skt.combustible.vehicles.grpc.VehicleStatsResponseProto.newBuilder()
                    .setTotalVehiculos(stats.getTotalVehiculos())
                    .setVehiculosActivos(stats.getTotalVehiculos()) // Usar total como activos por ahora
                    .setVehiculosDisponibles(stats.getVehiculosDisponibles())
                    .setVehiculosEnUso(stats.getVehiculosEnUso())
                    .setVehiculosEnMantenimiento(stats.getVehiculosEnMantenimiento())
                    .setKilometrajePromedio(0.0) // No disponible en el DTO actual
                    .setConsumoPromedioTotal(0.0) // No disponible en el DTO actual
                    .build();
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener estadísticas: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener estadísticas: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    // Métodos helper para mapeo
    private com.skt.combustible.vehicles.grpc.VehicleResponseProto mapToGrpcResponse(VehicleResponse domain) {
        com.skt.combustible.vehicles.grpc.VehicleResponseProto.Builder builder = 
            com.skt.combustible.vehicles.grpc.VehicleResponseProto.newBuilder()
                .setId(domain.getId() != null ? domain.getId() : "")
                .setPlaca(domain.getPlaca() != null ? domain.getPlaca() : "")
                .setMarca(domain.getMarca() != null ? domain.getMarca() : "")
                .setModelo(domain.getModelo() != null ? domain.getModelo() : "")
                .setActivo(domain.getActivo() != null ? domain.getActivo() : true);
        
        if (domain.getAnio() != null) {
            builder.setAnio(domain.getAnio());
        }
        
        if (domain.getTipoMaquinaria() != null) {
            builder.setTipoMaquinaria(mapTipoMaquinariaToGrpc(domain.getTipoMaquinaria()));
        }
        
        if (domain.getEstadoOperativo() != null) {
            builder.setEstadoOperativo(mapEstadoToGrpc(domain.getEstadoOperativo()));
        }
        
        if (domain.getCapacidadTanque() != null) {
            builder.setCapacidadTanque(domain.getCapacidadTanque());
        }
        
        if (domain.getConsumoPromedio() != null) {
            builder.setConsumoPromedio(domain.getConsumoPromedio());
        }
        
        if (domain.getKilometrajeActual() != null) {
            builder.setKilometrajeActual(domain.getKilometrajeActual());
        }
        
        if (domain.getFechaCreacion() != null) {
            builder.setFechaCreacion(domain.getFechaCreacion().toString());
        }
        
        if (domain.getFechaActualizacion() != null) {
            builder.setFechaActualizacion(domain.getFechaActualizacion().toString());
        }
        
        return builder.build();
    }
    
    private TipoMaquinaria mapTipoMaquinariaFromGrpc(com.skt.combustible.vehicles.grpc.TipoMaquinariaProto tipo) {
        return switch (tipo) {
            case CAMION -> TipoMaquinaria.CAMION;
            case VOLQUETE -> TipoMaquinaria.VOLQUETE;
            case EXCAVADORA -> TipoMaquinaria.EXCAVADORA;
            case CARGADOR -> TipoMaquinaria.CARGADOR;
            case GRUA -> TipoMaquinaria.GRUA;
            case MOTONIVELADORA -> TipoMaquinaria.MOTONIVELADORA;
            default -> throw new IllegalArgumentException("Tipo de maquinaria no válido: " + tipo);
        };
    }
    
    private com.skt.combustible.vehicles.grpc.TipoMaquinariaProto mapTipoMaquinariaToGrpc(TipoMaquinaria tipo) {
        return switch (tipo) {
            case CAMION -> com.skt.combustible.vehicles.grpc.TipoMaquinariaProto.CAMION;
            case VOLQUETE -> com.skt.combustible.vehicles.grpc.TipoMaquinariaProto.VOLQUETE;
            case EXCAVADORA -> com.skt.combustible.vehicles.grpc.TipoMaquinariaProto.EXCAVADORA;
            case CARGADOR -> com.skt.combustible.vehicles.grpc.TipoMaquinariaProto.CARGADOR;
            case GRUA -> com.skt.combustible.vehicles.grpc.TipoMaquinariaProto.GRUA;
            case MOTONIVELADORA -> com.skt.combustible.vehicles.grpc.TipoMaquinariaProto.MOTONIVELADORA;
        };
    }
    
    private EstadoOperativo mapEstadoFromGrpc(com.skt.combustible.vehicles.grpc.EstadoOperativoProto estado) {
        return switch (estado) {
            case DISPONIBLE -> EstadoOperativo.DISPONIBLE;
            case EN_USO -> EstadoOperativo.EN_USO;
            case MANTENIMIENTO -> EstadoOperativo.MANTENIMIENTO;
            case FUERA_DE_SERVICIO -> EstadoOperativo.FUERA_SERVICIO;
            case ASIGNADO -> EstadoOperativo.ASIGNADO;
            default -> throw new IllegalArgumentException("Estado operativo no válido: " + estado);
        };
    }
    
    private com.skt.combustible.vehicles.grpc.EstadoOperativoProto mapEstadoToGrpc(EstadoOperativo estado) {
        return switch (estado) {
            case DISPONIBLE -> com.skt.combustible.vehicles.grpc.EstadoOperativoProto.DISPONIBLE;
            case EN_USO -> com.skt.combustible.vehicles.grpc.EstadoOperativoProto.EN_USO;
            case MANTENIMIENTO -> com.skt.combustible.vehicles.grpc.EstadoOperativoProto.MANTENIMIENTO;
            case FUERA_SERVICIO -> com.skt.combustible.vehicles.grpc.EstadoOperativoProto.FUERA_DE_SERVICIO;
            case ASIGNADO -> com.skt.combustible.vehicles.grpc.EstadoOperativoProto.ASIGNADO;
            default -> com.skt.combustible.vehicles.grpc.EstadoOperativoProto.DISPONIBLE;
        };
    }
}
