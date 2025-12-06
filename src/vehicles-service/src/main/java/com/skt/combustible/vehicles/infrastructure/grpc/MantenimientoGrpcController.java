package com.skt.combustible.vehicles.infrastructure.grpc;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.skt.combustible.vehicles.application.service.MantenimientoService;
import com.skt.combustible.vehicles.domain.dto.MantenimientoCreateRequest;
import com.skt.combustible.vehicles.domain.dto.MantenimientoResponse;
import com.skt.combustible.vehicles.domain.entity.Mantenimiento;
import com.skt.combustible.vehicles.grpc.MantenimientoServiceProtoGrpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Controlador gRPC para el servicio de mantenimientos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@GrpcService
@Component
public class MantenimientoGrpcController extends MantenimientoServiceProtoGrpc.MantenimientoServiceProtoImplBase {
    
    private static final Logger logger = LoggerFactory.getLogger(MantenimientoGrpcController.class);
    
    @Autowired
    private MantenimientoService mantenimientoService;
    
    @Override
    public void crearMantenimiento(com.skt.combustible.vehicles.grpc.MantenimientoCreateRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.MantenimientoResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Creando mantenimiento para vehículo: {}", request.getVehicleId());
            
            // Convertir de gRPC a dominio
            MantenimientoCreateRequest domainRequest = new MantenimientoCreateRequest();
            // Convertir String vehicleId del proto a Long
            try {
                domainRequest.setVehicleId(Long.parseLong(request.getVehicleId()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID de vehículo inválido: " + request.getVehicleId());
            }
            domainRequest.setTipoMantenimiento(mapTipoToDomainString(request.getTipo()));
            domainRequest.setDescripcion(request.getDescripcion());
            domainRequest.setCosto(request.getCosto());
            // El proto tiene fechaProgramada, pero el DTO tiene fechaMantenimiento
            if (request.getFechaProgramada() != null && !request.getFechaProgramada().isEmpty()) {
                domainRequest.setFechaMantenimiento(java.time.LocalDateTime.parse(request.getFechaProgramada()));
            }
            domainRequest.setProveedor(request.getProveedor());
            domainRequest.setObservaciones(request.getObservaciones());
            
            MantenimientoResponse domainResponse = mantenimientoService.crearMantenimiento(domainRequest);
            
            // Convertir de dominio a gRPC
            com.skt.combustible.vehicles.grpc.MantenimientoResponseProto grpcResponse = mapToGrpcResponse(domainResponse);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            logger.info("gRPC: Mantenimiento creado exitosamente: {}", domainResponse.getId());
        } catch (Exception e) {
            logger.error("gRPC: Error al crear mantenimiento: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al crear mantenimiento: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerMantenimientoPorId(com.skt.combustible.vehicles.grpc.MantenimientoIdRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.MantenimientoResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo mantenimiento por ID: {}", request.getId());
            
            Optional<MantenimientoResponse> domainResponse = mantenimientoService.obtenerMantenimientoPorId(request.getId());
            if (domainResponse.isPresent()) {
                com.skt.combustible.vehicles.grpc.MantenimientoResponseProto grpcResponse = mapToGrpcResponse(domainResponse.get());
                responseObserver.onNext(grpcResponse);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Mantenimiento no encontrado con ID: " + request.getId())
                    .asRuntimeException());
            }
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener mantenimiento por ID: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener mantenimiento: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerMantenimientosPorVehiculo(com.skt.combustible.vehicles.grpc.MantenimientoVehicleIdRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.MantenimientoResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo mantenimientos por vehículo: {}", request.getVehicleId());
            
            List<MantenimientoResponse> responses = mantenimientoService.obtenerMantenimientosPorVehiculo(request.getVehicleId());
            for (MantenimientoResponse response : responses) {
                com.skt.combustible.vehicles.grpc.MantenimientoResponseProto grpcResponse = mapToGrpcResponse(response);
                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener mantenimientos por vehículo: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener mantenimientos por vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void actualizarMantenimiento(com.skt.combustible.vehicles.grpc.MantenimientoUpdateRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.MantenimientoResponseProto> responseObserver) {
        // Implementación pendiente - requiere MantenimientoUpdateRequest en el dominio
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
            .withDescription("Método no implementado completamente")
            .asRuntimeException());
    }
    
    @Override
    public void eliminarMantenimiento(com.skt.combustible.vehicles.grpc.MantenimientoIdRequestProto request,
            StreamObserver<com.google.protobuf.Empty> responseObserver) {
        try {
            logger.info("gRPC: Eliminando mantenimiento: {}", request.getId());
            mantenimientoService.eliminarMantenimiento(request.getId());
            responseObserver.onNext(com.google.protobuf.Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al eliminar mantenimiento: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al eliminar mantenimiento: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerMantenimientosPorTipo(com.skt.combustible.vehicles.grpc.MantenimientoTipoRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.MantenimientoResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo mantenimientos por tipo: {}", request.getTipo());
            
            String tipoString = mapTipoToDomainString(request.getTipo());
            List<MantenimientoResponse> responses = mantenimientoService.obtenerMantenimientosPorTipo(tipoString);
            for (MantenimientoResponse response : responses) {
                com.skt.combustible.vehicles.grpc.MantenimientoResponseProto grpcResponse = mapToGrpcResponse(response);
                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener mantenimientos por tipo: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener mantenimientos por tipo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerMantenimientosPorEstado(com.skt.combustible.vehicles.grpc.MantenimientoEstadoRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.MantenimientoResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo mantenimientos por estado: {}", request.getEstado());
            
            Mantenimiento.EstadoMantenimiento estado = mapEstadoFromGrpc(request.getEstado());
            List<MantenimientoResponse> responses = mantenimientoService.obtenerMantenimientosPorEstado(estado);
            for (MantenimientoResponse response : responses) {
                com.skt.combustible.vehicles.grpc.MantenimientoResponseProto grpcResponse = mapToGrpcResponse(response);
                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener mantenimientos por estado: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener mantenimientos por estado: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void obtenerTodosLosMantenimientos(com.google.protobuf.Empty request,
            StreamObserver<com.skt.combustible.vehicles.grpc.MantenimientoResponseProto> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo todos los mantenimientos");
            
            List<MantenimientoResponse> responses = mantenimientoService.obtenerTodosLosMantenimientos();
            for (MantenimientoResponse response : responses) {
                com.skt.combustible.vehicles.grpc.MantenimientoResponseProto grpcResponse = mapToGrpcResponse(response);
                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error al obtener todos los mantenimientos: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener mantenimientos: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void actualizarEstadoMantenimiento(com.skt.combustible.vehicles.grpc.MantenimientoUpdateRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.MantenimientoResponseProto> responseObserver) {
        // Implementación pendiente - requiere actualizar estado usando el ID del request
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
            .withDescription("Método requiere implementación completa")
            .asRuntimeException());
    }
    
    @Override
    public void calcularCostoTotalMantenimientos(com.skt.combustible.vehicles.grpc.MantenimientoCostoRequestProto request,
            StreamObserver<com.google.protobuf.DoubleValue> responseObserver) {
        // Implementación pendiente
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
            .withDescription("Método no implementado")
            .asRuntimeException());
    }
    
    @Override
    public void obtenerEstadisticasMantenimiento(com.google.protobuf.Empty request,
            StreamObserver<com.skt.combustible.vehicles.grpc.MantenimientoStatsResponseProto> responseObserver) {
        // Implementación pendiente
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
            .withDescription("Método no implementado")
            .asRuntimeException());
    }
    
    @Override
    public void obtenerEstadisticasMantenimientoPorVehiculo(com.skt.combustible.vehicles.grpc.MantenimientoVehicleIdRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.MantenimientoStatsResponseProto> responseObserver) {
        // Implementación pendiente
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
            .withDescription("Método no implementado")
            .asRuntimeException());
    }
    
    // Métodos helper para mapeo
    private com.skt.combustible.vehicles.grpc.MantenimientoResponseProto mapToGrpcResponse(MantenimientoResponse domain) {
        com.skt.combustible.vehicles.grpc.MantenimientoResponseProto.Builder builder = 
            com.skt.combustible.vehicles.grpc.MantenimientoResponseProto.newBuilder()
                .setId(domain.getId() != null ? domain.getId() : "")
                .setVehicleId(domain.getVehicleId() != null ? domain.getVehicleId() : "")
                .setDescripcion(domain.getDescripcion() != null ? domain.getDescripcion() : "")
                .setCosto(domain.getCosto() != null ? domain.getCosto() : 0.0)
                .setProveedor(domain.getProveedor() != null ? domain.getProveedor() : "")
                .setObservaciones(domain.getObservaciones() != null ? domain.getObservaciones() : "");
        
        if (domain.getTipoMantenimiento() != null) {
            builder.setTipo(mapTipoStringToGrpc(domain.getTipoMantenimiento()));
        }
        
        if (domain.getEstado() != null) {
            // Convertir String estado a enum
            try {
                Mantenimiento.EstadoMantenimiento estadoEnum = Mantenimiento.EstadoMantenimiento.valueOf(domain.getEstado());
                builder.setEstado(mapEstadoToGrpc(estadoEnum));
            } catch (IllegalArgumentException e) {
                logger.warn("Estado de mantenimiento no válido: {}, usando PROGRAMADO por defecto", domain.getEstado());
                builder.setEstado(com.skt.combustible.vehicles.grpc.EstadoMantenimientoProto.PROGRAMADO);
            }
        }
        
        // El DTO tiene fechaMantenimiento, el proto tiene fechaProgramada
        if (domain.getFechaMantenimiento() != null) {
            builder.setFechaProgramada(domain.getFechaMantenimiento().toString());
        }
        
        // El DTO no tiene fechaInicio ni fechaFin, usar null o valores por defecto
        // Si el proto requiere estos campos, se pueden agregar al DTO más adelante
        
        if (domain.getFechaCreacion() != null) {
            builder.setFechaCreacion(domain.getFechaCreacion().toString());
        }
        
        if (domain.getFechaActualizacion() != null) {
            builder.setFechaActualizacion(domain.getFechaActualizacion().toString());
        }
        
        return builder.build();
    }
    
    private com.skt.combustible.vehicles.grpc.TipoMantenimientoProto mapTipoStringToGrpc(String tipo) {
        if (tipo == null) {
            return com.skt.combustible.vehicles.grpc.TipoMantenimientoProto.PREVENTIVO;
        }
        return switch (tipo.toUpperCase()) {
            case "PREVENTIVO" -> com.skt.combustible.vehicles.grpc.TipoMantenimientoProto.PREVENTIVO;
            case "CORRECTIVO" -> com.skt.combustible.vehicles.grpc.TipoMantenimientoProto.CORRECTIVO;
            case "EMERGENCIA" -> com.skt.combustible.vehicles.grpc.TipoMantenimientoProto.EMERGENCIA;
            case "REVISION" -> com.skt.combustible.vehicles.grpc.TipoMantenimientoProto.REVISION;
            default -> com.skt.combustible.vehicles.grpc.TipoMantenimientoProto.PREVENTIVO;
        };
    }
    
    private String mapTipoToDomainString(com.skt.combustible.vehicles.grpc.TipoMantenimientoProto tipo) {
        return switch (tipo) {
            case PREVENTIVO -> "PREVENTIVO";
            case CORRECTIVO -> "CORRECTIVO";
            case EMERGENCIA -> "EMERGENCIA";
            case REVISION -> "REVISION";
            default -> "PREVENTIVO";
        };
    }
    
    private Mantenimiento.EstadoMantenimiento mapEstadoFromGrpc(com.skt.combustible.vehicles.grpc.EstadoMantenimientoProto estado) {
        return switch (estado) {
            case PROGRAMADO -> Mantenimiento.EstadoMantenimiento.PROGRAMADO;
            case EN_PROGRESO -> Mantenimiento.EstadoMantenimiento.EN_PROGRESO;
            case COMPLETADO -> Mantenimiento.EstadoMantenimiento.COMPLETADO;
            case CANCELADO -> Mantenimiento.EstadoMantenimiento.CANCELADO;
            case REPROGRAMADO -> Mantenimiento.EstadoMantenimiento.PROGRAMADO; // REPROGRAMADO se mapea a PROGRAMADO
            default -> throw new IllegalArgumentException("Estado de mantenimiento no válido: " + estado);
        };
    }
    
    private com.skt.combustible.vehicles.grpc.EstadoMantenimientoProto mapEstadoToGrpc(Mantenimiento.EstadoMantenimiento estado) {
        return switch (estado) {
            case PROGRAMADO -> com.skt.combustible.vehicles.grpc.EstadoMantenimientoProto.PROGRAMADO;
            case EN_PROGRESO -> com.skt.combustible.vehicles.grpc.EstadoMantenimientoProto.EN_PROGRESO;
            case COMPLETADO -> com.skt.combustible.vehicles.grpc.EstadoMantenimientoProto.COMPLETADO;
            case CANCELADO -> com.skt.combustible.vehicles.grpc.EstadoMantenimientoProto.CANCELADO;
            // No hay REPROGRAMADO en el enum del dominio, se mapea a PROGRAMADO
        };
    }
}
