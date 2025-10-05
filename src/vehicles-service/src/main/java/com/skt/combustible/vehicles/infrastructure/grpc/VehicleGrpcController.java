package com.skt.combustible.vehicles.infrastructure.grpc;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.application.service.VehicleService;
import com.skt.combustible.vehicles.domain.dto.VehicleCreateRequest;
import com.skt.combustible.vehicles.domain.dto.VehicleResponse;
import com.skt.combustible.vehicles.domain.dto.VehicleUpdateRequest;

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
public class VehicleGrpcController {
    
    @Autowired
    private VehicleService vehicleService;
    
    /**
     * Crea un nuevo vehículo
     */
    public void crearVehiculo(VehicleCreateRequest request, StreamObserver<VehicleResponse> responseObserver) {
        try {
            VehicleResponse response = vehicleService.crearVehiculo(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al crear vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene un vehículo por ID
     */
    public void obtenerVehiculoPorId(Long id, StreamObserver<VehicleResponse> responseObserver) {
        try {
            Optional<VehicleResponse> response = vehicleService.obtenerVehiculoPorId(id.toString());
            if (response.isPresent()) {
                responseObserver.onNext(response.get());
            } else {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Vehículo no encontrado con ID: " + id)
                    .asRuntimeException());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene un vehículo por placa
     */
    public void obtenerVehiculoPorPlaca(String placa, StreamObserver<VehicleResponse> responseObserver) {
        try {
            Optional<VehicleResponse> response = vehicleService.obtenerVehiculoPorPlaca(placa);
            if (response.isPresent()) {
                responseObserver.onNext(response.get());
            } else {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Vehículo no encontrado con placa: " + placa)
                    .asRuntimeException());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene todos los vehículos
     */
    public void obtenerTodosLosVehiculos(StreamObserver<VehicleResponse> responseObserver) {
        try {
            List<VehicleResponse> responses = vehicleService.obtenerTodosLosVehiculos();
            for (VehicleResponse response : responses) {
            responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículos: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene vehículos por tipo de maquinaria
     */
    public void obtenerVehiculosPorTipo(TipoMaquinaria tipoMaquinaria, StreamObserver<VehicleResponse> responseObserver) {
        try {
            List<VehicleResponse> responses = vehicleService.obtenerVehiculosPorTipo(tipoMaquinaria);
            for (VehicleResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículos por tipo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene vehículos por estado operativo
     */
    public void obtenerVehiculosPorEstado(EstadoOperativo estadoOperativo, StreamObserver<VehicleResponse> responseObserver) {
        try {
            List<VehicleResponse> responses = vehicleService.obtenerVehiculosPorEstado(estadoOperativo);
            for (VehicleResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículos por estado: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene vehículos disponibles
     */
    public void obtenerVehiculosDisponibles(StreamObserver<VehicleResponse> responseObserver) {
        try {
            List<VehicleResponse> responses = vehicleService.obtenerVehiculosDisponibles();
            for (VehicleResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículos disponibles: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene vehículos disponibles por tipo
     */
    public void obtenerVehiculosDisponiblesPorTipo(TipoMaquinaria tipoMaquinaria, StreamObserver<VehicleResponse> responseObserver) {
        try {
            List<VehicleResponse> responses = vehicleService.obtenerVehiculosDisponiblesPorTipo(tipoMaquinaria);
            for (VehicleResponse response : responses) {
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener vehículos disponibles por tipo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Actualiza un vehículo
     */
    public void actualizarVehiculo(Long id, VehicleUpdateRequest request, StreamObserver<VehicleResponse> responseObserver) {
        try {
            VehicleResponse response = vehicleService.actualizarVehiculo(id.toString(), request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al actualizar vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Cambia el estado de un vehículo
     */
    public void cambiarEstadoVehiculo(Long id, EstadoOperativo nuevoEstado, StreamObserver<VehicleResponse> responseObserver) {
        try {
            VehicleResponse response = vehicleService.cambiarEstadoVehiculo(id.toString(), nuevoEstado);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al cambiar estado del vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Actualiza el kilometraje de un vehículo
     */
    public void actualizarKilometraje(Long id, Double nuevoKilometraje, StreamObserver<VehicleResponse> responseObserver) {
        try {
            VehicleResponse response = vehicleService.actualizarKilometraje(id.toString(), nuevoKilometraje);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al actualizar kilometraje: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Desactiva un vehículo
     */
    public void desactivarVehiculo(Long id, StreamObserver<com.google.protobuf.Empty> responseObserver) {
        try {
            vehicleService.desactivarVehiculo(id.toString());
            responseObserver.onNext(com.google.protobuf.Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al desactivar vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Elimina un vehículo
     */
    public void eliminarVehiculo(Long id, StreamObserver<com.google.protobuf.Empty> responseObserver) {
        try {
            vehicleService.eliminarVehiculo(id.toString());
            responseObserver.onNext(com.google.protobuf.Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Error al eliminar vehículo: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    /**
     * Obtiene estadísticas de vehículos
     */
    public void obtenerEstadisticas(StreamObserver<VehicleService.VehicleStatsDTO> responseObserver) {
        try {
            VehicleService.VehicleStatsDTO stats = vehicleService.obtenerEstadisticas();
            responseObserver.onNext(stats);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error al obtener estadísticas: " + e.getMessage())
                .asRuntimeException());
        }
    }
}
