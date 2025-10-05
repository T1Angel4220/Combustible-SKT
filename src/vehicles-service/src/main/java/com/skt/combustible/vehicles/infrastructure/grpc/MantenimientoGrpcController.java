package com.skt.combustible.vehicles.infrastructure.grpc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.skt.combustible.vehicles.application.service.MantenimientoService;
import com.skt.combustible.vehicles.domain.dto.MantenimientoCreateRequest;
import com.skt.combustible.vehicles.domain.dto.MantenimientoResponse;
import com.skt.combustible.vehicles.domain.entity.Mantenimiento;

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
public class MantenimientoGrpcController {
    
    @Autowired
    private MantenimientoService mantenimientoService;
    
    /**
     * Crea un nuevo mantenimiento
     */
    public void crearMantenimiento(MantenimientoCreateRequest request, StreamObserver<MantenimientoResponse> responseObserver) {
        try {
            MantenimientoResponse response = mantenimientoService.crearMantenimiento(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al crear mantenimiento: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene un mantenimiento por ID
     */
    public void obtenerMantenimientoPorId(Long id, StreamObserver<MantenimientoResponse> responseObserver) {
        try {
            var response = mantenimientoService.obtenerMantenimientoPorId(id.toString());
            if (response.isPresent()) {
                responseObserver.onNext(response.get());
            } else {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Mantenimiento no encontrado con ID: " + id)
                    .asRuntimeException());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener mantenimiento: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene mantenimientos por vehículo
     */
    public void obtenerMantenimientosPorVehiculo(Long vehicleId, StreamObserver<MantenimientoResponse> responseObserver) {
        try {
            List<MantenimientoResponse> responses = mantenimientoService.obtenerMantenimientosPorVehiculo(vehicleId.toString());
            for (MantenimientoResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener mantenimientos por vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene todos los mantenimientos
     */
    public void obtenerTodosLosMantenimientos(StreamObserver<MantenimientoResponse> responseObserver) {
        try {
            List<MantenimientoResponse> responses = mantenimientoService.obtenerTodosLosMantenimientos();
            for (MantenimientoResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener mantenimientos: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene mantenimientos por tipo
     */
    public void obtenerMantenimientosPorTipo(String tipoMantenimiento, StreamObserver<MantenimientoResponse> responseObserver) {
        try {
            List<MantenimientoResponse> responses = mantenimientoService.obtenerMantenimientosPorTipo(tipoMantenimiento);
            for (MantenimientoResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener mantenimientos por tipo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene mantenimientos por estado
     */
    public void obtenerMantenimientosPorEstado(Mantenimiento.EstadoMantenimiento estado, StreamObserver<MantenimientoResponse> responseObserver) {
        try {
            List<MantenimientoResponse> responses = mantenimientoService.obtenerMantenimientosPorEstado(estado);
            for (MantenimientoResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener mantenimientos por estado: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene mantenimientos próximos a vencer
     */
    public void obtenerMantenimientosProximosAVencer(Integer diasAdelante, StreamObserver<MantenimientoResponse> responseObserver) {
        try {
            List<MantenimientoResponse> responses = mantenimientoService.obtenerMantenimientosProximosAVencer(diasAdelante);
            for (MantenimientoResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener mantenimientos próximos a vencer: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Actualiza el estado de un mantenimiento
     */
    public void actualizarEstadoMantenimiento(Long id, Mantenimiento.EstadoMantenimiento nuevoEstado, StreamObserver<MantenimientoResponse> responseObserver) {
        try {
            MantenimientoResponse response = mantenimientoService.actualizarEstadoMantenimiento(id.toString(), nuevoEstado);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al actualizar estado del mantenimiento: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Elimina un mantenimiento
     */
    public void eliminarMantenimiento(Long id, StreamObserver<com.google.protobuf.Empty> responseObserver) {
        try {
            mantenimientoService.eliminarMantenimiento(id.toString());
            responseObserver.onNext(com.google.protobuf.Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al eliminar mantenimiento: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene estadísticas de mantenimiento por vehículo
     */
    public void obtenerEstadisticasMantenimiento(Long vehicleId, StreamObserver<MantenimientoService.MantenimientoStatsDTO> responseObserver) {
        try {
            MantenimientoService.MantenimientoStatsDTO stats = mantenimientoService.obtenerEstadisticasMantenimiento(vehicleId);
            responseObserver.onNext(stats);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener estadísticas de mantenimiento: " + e.getMessage())
                .asRuntimeException());
        }
    }
}
