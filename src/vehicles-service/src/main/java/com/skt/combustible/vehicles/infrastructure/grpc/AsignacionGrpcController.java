package com.skt.combustible.vehicles.infrastructure.grpc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.skt.combustible.vehicles.application.service.AsignacionService;
import com.skt.combustible.vehicles.domain.dto.AsignacionCreateRequest;
import com.skt.combustible.vehicles.domain.dto.AsignacionResponse;
import com.skt.combustible.vehicles.domain.entity.AsignacionVehiculo;
import com.skt.combustible.vehicles.domain.entity.Vehicle;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Controlador gRPC para el servicio de asignaciones de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@GrpcService
@Component
public class AsignacionGrpcController {
    
    @Autowired
    private AsignacionService asignacionService;
    
    /**
     * Asigna un vehículo a un chofer
     */
    public void asignarVehiculoAChofer(AsignacionCreateRequest request, StreamObserver<AsignacionResponse> responseObserver) {
        try {
            AsignacionResponse response = asignacionService.asignarVehiculoAChofer(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al asignar vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Desasigna un vehículo de un chofer
     */
    public void desasignarVehiculo(Long vehicleId, StreamObserver<AsignacionResponse> responseObserver) {
        try {
            AsignacionResponse response = asignacionService.desasignarVehiculo(vehicleId);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al desasignar vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene una asignación por ID
     */
    public void obtenerAsignacionPorId(Long id, StreamObserver<AsignacionResponse> responseObserver) {
        try {
            var response = asignacionService.obtenerAsignacionPorId(id.toString());
            if (response.isPresent()) {
                responseObserver.onNext(response.get());
            } else {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Asignación no encontrada con ID: " + id)
                    .asRuntimeException());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener asignación: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene asignaciones por vehículo
     */
    public void obtenerAsignacionesPorVehiculo(Long vehicleId, StreamObserver<AsignacionResponse> responseObserver) {
        try {
            List<AsignacionResponse> responses = asignacionService.obtenerAsignacionesPorVehiculo(vehicleId);
            for (AsignacionResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener asignaciones por vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene asignaciones activas por chofer
     */
    public void obtenerAsignacionesActivasPorChofer(Long choferId, StreamObserver<AsignacionResponse> responseObserver) {
        try {
            List<AsignacionResponse> responses = asignacionService.obtenerAsignacionesActivasPorChofer(choferId);
            for (AsignacionResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener asignaciones activas por chofer: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene todas las asignaciones
     */
    public void obtenerTodasLasAsignaciones(StreamObserver<AsignacionResponse> responseObserver) {
        try {
            List<AsignacionResponse> responses = asignacionService.obtenerTodasLasAsignaciones();
            for (AsignacionResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener asignaciones: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene asignaciones por estado
     */
    public void obtenerAsignacionesPorEstado(AsignacionVehiculo.EstadoAsignacion estado, StreamObserver<AsignacionResponse> responseObserver) {
        try {
            List<AsignacionResponse> responses = asignacionService.obtenerAsignacionesPorEstado(estado);
            for (AsignacionResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener asignaciones por estado: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene vehículos disponibles para asignación
     */
    public void obtenerVehiculosDisponiblesParaAsignacion(StreamObserver<Vehicle> responseObserver) {
        try {
            List<Vehicle> vehicles = asignacionService.obtenerVehiculosDisponiblesParaAsignacion();
            for (Vehicle vehicle : vehicles) {
                responseObserver.onNext(vehicle);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículos disponibles: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Verifica si un chofer puede recibir más asignaciones
     */
    public void puedeAsignarMasVehiculos(Long choferId, StreamObserver<com.google.protobuf.BoolValue> responseObserver) {
        try {
            boolean puedeAsignar = asignacionService.puedeAsignarMasVehiculos(choferId);
            responseObserver.onNext(com.google.protobuf.BoolValue.of(puedeAsignar));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al verificar capacidad de asignación: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene estadísticas de asignaciones
     */
    public void obtenerEstadisticasAsignacion(StreamObserver<AsignacionService.AsignacionStatsDTO> responseObserver) {
        try {
            AsignacionService.AsignacionStatsDTO stats = asignacionService.obtenerEstadisticasAsignacion();
            responseObserver.onNext(stats);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener estadísticas de asignación: " + e.getMessage())
                .asRuntimeException());
        }
    }
}
