package com.skt.combustible.vehicles.infrastructure.grpc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.skt.combustible.vehicles.application.service.AsignacionService;
import com.skt.combustible.vehicles.domain.dto.AsignacionCreateRequest;
import com.skt.combustible.vehicles.domain.dto.AsignacionResponse;
import com.skt.combustible.vehicles.grpc.AsignacionServiceProtoGrpc;

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
public class AsignacionGrpcController extends AsignacionServiceProtoGrpc.AsignacionServiceProtoImplBase {

    @Autowired
    private AsignacionService asignacionService;

    @Override
    public void asignarVehiculoAChofer(com.skt.combustible.vehicles.grpc.AsignacionCreateRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.AsignacionResponseProto> responseObserver) {
        try {
            // Convertir de gRPC a dominio
            AsignacionCreateRequest domainRequest = new AsignacionCreateRequest();
            domainRequest.setVehicleId(request.getVehicleId()); // Ya es String en el proto
            // Convertir Long choferId del proto a String
            domainRequest.setChoferId(String.valueOf(request.getChoferId()));
            domainRequest.setFechaAsignacion(java.time.LocalDateTime.now());
            domainRequest.setObservaciones(request.getObservaciones());

            AsignacionResponse domainResponse = asignacionService.asignarVehiculoAChofer(domainRequest);

            // Convertir de dominio a gRPC
            com.skt.combustible.vehicles.grpc.AsignacionResponseProto grpcResponse = com.skt.combustible.vehicles.grpc.AsignacionResponseProto
                    .newBuilder()
                    .setId(domainResponse.getId())
                    .setVehicleId(domainResponse.getVehicleId())
                    .setChoferId(Long.parseLong(domainResponse.getChoferId()))
                    .setFechaInicio(
                            domainResponse.getFechaAsignacion() != null ? domainResponse.getFechaAsignacion().toString()
                                    : "")
                    .setFechaFin(domainResponse.getFechaDesasignacion() != null
                            ? domainResponse.getFechaDesasignacion().toString()
                            : "")
                    .setFechaFinReal("")
                    .setMotivo("")
                    .setObservaciones(
                            domainResponse.getObservaciones() != null ? domainResponse.getObservaciones() : "")
                    .setEstado(mapEstadoToGrpc(domainResponse.getEstado()))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Error al asignar vehículo: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void obtenerTodasLasAsignaciones(com.google.protobuf.Empty request,
            StreamObserver<com.skt.combustible.vehicles.grpc.AsignacionResponseProto> responseObserver) {
        try {
            List<AsignacionResponse> responses = asignacionService.obtenerTodasLasAsignaciones();
            for (AsignacionResponse response : responses) {
                // Convertir de dominio a gRPC
                com.skt.combustible.vehicles.grpc.AsignacionResponseProto grpcResponse = com.skt.combustible.vehicles.grpc.AsignacionResponseProto
                        .newBuilder()
                        .setId(response.getId())
                        .setVehicleId(response.getVehicleId())
                        .setChoferId(Long.parseLong(response.getChoferId()))
                        .setFechaInicio(
                                response.getFechaAsignacion() != null ? response.getFechaAsignacion().toString() : "")
                        .setFechaFin(
                                response.getFechaDesasignacion() != null ? response.getFechaDesasignacion().toString()
                                        : "")
                        .setFechaFinReal("")
                        .setMotivo("")
                        .setObservaciones(response.getObservaciones() != null ? response.getObservaciones() : "")
                        .setEstado(mapEstadoToGrpc(response.getEstado()))
                        .build();

                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error al obtener asignaciones: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void obtenerAsignacionPorId(com.skt.combustible.vehicles.grpc.AsignacionIdRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.AsignacionResponseProto> responseObserver) {
        try {
            var response = asignacionService.obtenerAsignacionPorId(request.getId());
            if (response.isPresent()) {
                AsignacionResponse domainResponse = response.get();
                // Convertir de dominio a gRPC
                com.skt.combustible.vehicles.grpc.AsignacionResponseProto grpcResponse = com.skt.combustible.vehicles.grpc.AsignacionResponseProto
                        .newBuilder()
                        .setId(domainResponse.getId())
                        .setVehicleId(domainResponse.getVehicleId())
                        .setChoferId(Long.parseLong(domainResponse.getChoferId()))
                        .setFechaInicio(domainResponse.getFechaAsignacion() != null
                                ? domainResponse.getFechaAsignacion().toString()
                                : "")
                        .setFechaFin(domainResponse.getFechaDesasignacion() != null
                                ? domainResponse.getFechaDesasignacion().toString()
                                : "")
                        .setFechaFinReal("")
                        .setMotivo("")
                        .setObservaciones(
                                domainResponse.getObservaciones() != null ? domainResponse.getObservaciones() : "")
                        .setEstado(mapEstadoToGrpc(domainResponse.getEstado()))
                        .build();

                responseObserver.onNext(grpcResponse);
            } else {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Asignación no encontrada con ID: " + request.getId())
                        .asRuntimeException());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error al obtener asignación: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void obtenerAsignacionesPorVehiculo(
            com.skt.combustible.vehicles.grpc.AsignacionVehicleIdRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.AsignacionResponseProto> responseObserver) {
        try {
            List<AsignacionResponse> responses = asignacionService
                    .obtenerAsignacionesPorVehiculo(request.getVehicleId());
            for (AsignacionResponse response : responses) {
                // Convertir de dominio a gRPC
                com.skt.combustible.vehicles.grpc.AsignacionResponseProto grpcResponse = com.skt.combustible.vehicles.grpc.AsignacionResponseProto
                        .newBuilder()
                        .setId(response.getId())
                        .setVehicleId(response.getVehicleId())
                        .setChoferId(Long.parseLong(response.getChoferId()))
                        .setFechaInicio(
                                response.getFechaAsignacion() != null ? response.getFechaAsignacion().toString() : "")
                        .setFechaFin(
                                response.getFechaDesasignacion() != null ? response.getFechaDesasignacion().toString()
                                        : "")
                        .setFechaFinReal("")
                        .setMotivo("")
                        .setObservaciones(response.getObservaciones() != null ? response.getObservaciones() : "")
                        .setEstado(mapEstadoToGrpc(response.getEstado()))
                        .build();

                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error al obtener asignaciones por vehículo: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void obtenerAsignacionesPorChofer(com.skt.combustible.vehicles.grpc.AsignacionChoferIdRequestProto request,
            StreamObserver<com.skt.combustible.vehicles.grpc.AsignacionResponseProto> responseObserver) {
        try {
            // Convertir Long choferId del proto a String
            List<AsignacionResponse> responses = asignacionService
                    .obtenerAsignacionesActivasPorChofer(String.valueOf(request.getChoferId()));
            for (AsignacionResponse response : responses) {
                // Convertir de dominio a gRPC
                com.skt.combustible.vehicles.grpc.AsignacionResponseProto grpcResponse = com.skt.combustible.vehicles.grpc.AsignacionResponseProto
                        .newBuilder()
                        .setId(response.getId())
                        .setVehicleId(response.getVehicleId())
                        .setChoferId(Long.parseLong(response.getChoferId()))
                        .setFechaInicio(
                                response.getFechaAsignacion() != null ? response.getFechaAsignacion().toString() : "")
                        .setFechaFin(
                                response.getFechaDesasignacion() != null ? response.getFechaDesasignacion().toString()
                                        : "")
                        .setFechaFinReal("")
                        .setMotivo("")
                        .setObservaciones(response.getObservaciones() != null ? response.getObservaciones() : "")
                        .setEstado(mapEstadoToGrpc(response.getEstado()))
                        .build();

                responseObserver.onNext(grpcResponse);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error al obtener asignaciones por chofer: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void obtenerEstadisticasAsignacion(com.google.protobuf.Empty request,
            StreamObserver<com.skt.combustible.vehicles.grpc.AsignacionStatsResponseProto> responseObserver) {
        try {
            AsignacionService.AsignacionStatsDTO stats = asignacionService.obtenerEstadisticasAsignacion();

            // Convertir de dominio a gRPC
            com.skt.combustible.vehicles.grpc.AsignacionStatsResponseProto grpcResponse = com.skt.combustible.vehicles.grpc.AsignacionStatsResponseProto
                    .newBuilder()
                    .setTotalAsignaciones(stats.getTotalAsignaciones())
                    .setAsignacionesActivas(stats.getAsignacionesActivas())
                    .setAsignacionesCompletadas(0L)
                    .setAsignacionesCanceladas(0L)
                    .setChoferesConAsignaciones(0L)
                    .setVehiculosAsignados(0L)
                    .setDuracionPromedio(0.0)
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error al obtener estadísticas de asignación: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Mapea el estado del dominio al estado gRPC
     */
    private com.skt.combustible.vehicles.grpc.EstadoAsignacionProto mapEstadoToGrpc(String estado) {
        if (estado == null) {
            return com.skt.combustible.vehicles.grpc.EstadoAsignacionProto.ACTIVA;
        }
        return switch (estado.toUpperCase()) {
            case "ACTIVA" -> com.skt.combustible.vehicles.grpc.EstadoAsignacionProto.ACTIVA;
            case "COMPLETADA" -> com.skt.combustible.vehicles.grpc.EstadoAsignacionProto.COMPLETADA;
            case "CANCELADA" -> com.skt.combustible.vehicles.grpc.EstadoAsignacionProto.CANCELADA;
            case "VENCIDA" -> com.skt.combustible.vehicles.grpc.EstadoAsignacionProto.VENCIDA;
            default -> com.skt.combustible.vehicles.grpc.EstadoAsignacionProto.ACTIVA;
        };
    }
}