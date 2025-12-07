package com.skt.combustible.fuel.infrastructure.grpc;

import com.skt.combustible.fuel.application.service.FuelConsumptionService;
import com.skt.combustible.fuel.domain.dto.FuelConsumptionCreateRequest;
import com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse;
import com.skt.combustible.fuel.domain.dto.FuelConsumptionUpdateRequest;
import com.skt.combustible.fuel.domain.exception.FuelConsumptionNotFoundException;
import com.skt.combustible.fuel.grpc.*;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador gRPC para el servicio de combustible
 * Implementa los métodos gRPC necesarios para la comunicación entre microservicios
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@GrpcService
public class FuelGrpcController extends FuelServiceGrpc.FuelServiceImplBase {
    
    private static final Logger logger = LoggerFactory.getLogger(FuelGrpcController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    @Autowired
    private FuelConsumptionService fuelConsumptionService;
    
    @Override
    public void createFuelConsumption(CreateFuelConsumptionRequest request,
            StreamObserver<com.skt.combustible.fuel.grpc.FuelConsumptionResponse> responseObserver) {
        try {
            logger.info("gRPC: Creando nuevo registro de combustible: {} litros para vehículo {}",
                    request.getCantidadLitros(), request.getVehiculoId());
            
            FuelConsumptionCreateRequest domainRequest = new FuelConsumptionCreateRequest();
            domainRequest.setFechaHora(parseDateTime(request.getFechaHora()));
            domainRequest.setCantidadLitros(request.getCantidadLitros());
            domainRequest.setTipoCombustible(mapTipoCombustibleFromGrpc(request.getTipoCombustible()));
            if (request.hasPrecioPorLitro()) {
                domainRequest.setPrecioPorLitro(request.getPrecioPorLitro());
            }
            if (request.hasCostoTotal()) {
                domainRequest.setCostoTotal(request.getCostoTotal());
            }
            domainRequest.setVehiculoId(request.getVehiculoId());
            domainRequest.setChoferId(request.getChoferId());
            if (request.hasRutaId() && !request.getRutaId().isEmpty()) {
                domainRequest.setRutaId(request.getRutaId());
            }
            if (request.hasLecturaOdometroHoras()) {
                domainRequest.setLecturaOdometroHoras(request.getLecturaOdometroHoras());
            }
            if (request.hasObservaciones() && !request.getObservaciones().isEmpty()) {
                domainRequest.setObservaciones(request.getObservaciones());
            }
            
            com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse domainResponse = fuelConsumptionService.crearRegistro(domainRequest);
            com.skt.combustible.fuel.grpc.FuelConsumptionResponse grpcResponse = mapToGrpcResponse(domainResponse);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            logger.info("gRPC: Registro de combustible creado exitosamente con ID: {}", domainResponse.getId());
        } catch (Exception e) {
            logger.error("gRPC: Error al crear registro de combustible: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Error al crear registro de combustible: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void getFuelConsumptionById(GetFuelConsumptionByIdRequest request,
            StreamObserver<com.skt.combustible.fuel.grpc.FuelConsumptionResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo registro de combustible por ID: {}", request.getId());
            
            java.util.Optional<com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse> domainResponse = 
                    fuelConsumptionService.obtenerRegistroPorId(request.getId());
            
            if (domainResponse.isPresent()) {
                com.skt.combustible.fuel.grpc.FuelConsumptionResponse grpcResponse = mapToGrpcResponse(domainResponse.get());
                responseObserver.onNext(grpcResponse);
                responseObserver.onCompleted();
                logger.info("gRPC: Registro de combustible obtenido exitosamente: {}", request.getId());
            } else {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Registro de combustible no encontrado con ID: " + request.getId())
                        .asRuntimeException());
            }
        } catch (Exception e) {
            logger.error("gRPC: Error obteniendo registro de combustible: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void getAllFuelConsumptions(GetAllFuelConsumptionsRequest request,
            StreamObserver<GetAllFuelConsumptionsResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo todos los registros de combustible");
            
            List<com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse> consumptions = fuelConsumptionService.obtenerTodosLosRegistros();
            
            GetAllFuelConsumptionsResponse.Builder responseBuilder = GetAllFuelConsumptionsResponse.newBuilder();
            for (com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse consumption : consumptions) {
                responseBuilder.addConsumptions(mapToGrpcResponse(consumption));
            }
            responseBuilder.setTotalCount(consumptions.size());
            responseBuilder.setPage(request.getPage());
            responseBuilder.setSize(request.getSize());
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error obteniendo todos los registros: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void getFuelConsumptionsByVehicle(GetFuelConsumptionsByVehicleRequest request,
            StreamObserver<GetAllFuelConsumptionsResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo registros por vehículo: {}", request.getVehiculoId());
            
            List<com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse> consumptions;
            if (request.hasFechaInicio() && request.hasFechaFin()) {
                LocalDateTime fechaInicio = parseDateTime(request.getFechaInicio());
                LocalDateTime fechaFin = parseDateTime(request.getFechaFin());
                consumptions = fuelConsumptionService.obtenerRegistrosPorRangoFechas(fechaInicio, fechaFin)
                        .stream()
                        .filter(c -> c.getVehiculoId().equals(request.getVehiculoId()))
                        .toList();
            } else {
                consumptions = fuelConsumptionService.obtenerRegistrosPorVehiculo(request.getVehiculoId());
            }
            
            GetAllFuelConsumptionsResponse.Builder responseBuilder = GetAllFuelConsumptionsResponse.newBuilder();
            for (com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse consumption : consumptions) {
                responseBuilder.addConsumptions(mapToGrpcResponse(consumption));
            }
            responseBuilder.setTotalCount(consumptions.size());
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error obteniendo registros por vehículo: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void getFuelConsumptionsByDriver(GetFuelConsumptionsByDriverRequest request,
            StreamObserver<GetAllFuelConsumptionsResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo registros por chofer: {}", request.getChoferId());
            
            List<com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse> consumptions;
            if (request.hasFechaInicio() && request.hasFechaFin()) {
                LocalDateTime fechaInicio = parseDateTime(request.getFechaInicio());
                LocalDateTime fechaFin = parseDateTime(request.getFechaFin());
                consumptions = fuelConsumptionService.obtenerRegistrosPorRangoFechas(fechaInicio, fechaFin)
                        .stream()
                        .filter(c -> c.getChoferId().equals(request.getChoferId()))
                        .toList();
            } else {
                consumptions = fuelConsumptionService.obtenerRegistrosPorChofer(request.getChoferId());
            }
            
            GetAllFuelConsumptionsResponse.Builder responseBuilder = GetAllFuelConsumptionsResponse.newBuilder();
            for (com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse consumption : consumptions) {
                responseBuilder.addConsumptions(mapToGrpcResponse(consumption));
            }
            responseBuilder.setTotalCount(consumptions.size());
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error obteniendo registros por chofer: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void getFuelConsumptionsByRoute(GetFuelConsumptionsByRouteRequest request,
            StreamObserver<GetAllFuelConsumptionsResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo registros por ruta: {}", request.getRutaId());
            
            List<com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse> consumptions = 
                    fuelConsumptionService.obtenerRegistrosPorRuta(request.getRutaId());
            
            GetAllFuelConsumptionsResponse.Builder responseBuilder = GetAllFuelConsumptionsResponse.newBuilder();
            for (com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse consumption : consumptions) {
                responseBuilder.addConsumptions(mapToGrpcResponse(consumption));
            }
            responseBuilder.setTotalCount(consumptions.size());
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error obteniendo registros por ruta: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void getFuelConsumptionsByMachineryType(GetFuelConsumptionsByMachineryTypeRequest request,
            StreamObserver<GetAllFuelConsumptionsResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo registros por tipo de maquinaria: {}", request.getTipoMaquinaria());
            
            TipoMaquinaria tipoMaquinaria = mapTipoMaquinariaFromGrpc(request.getTipoMaquinaria());
            List<com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse> consumptions;
            
            if (request.hasFechaInicio() && request.hasFechaFin()) {
                LocalDateTime fechaInicio = parseDateTime(request.getFechaInicio());
                LocalDateTime fechaFin = parseDateTime(request.getFechaFin());
                consumptions = fuelConsumptionService.obtenerRegistrosPorRangoFechas(fechaInicio, fechaFin)
                        .stream()
                        .filter(c -> c.getTipoMaquinaria() == tipoMaquinaria)
                        .toList();
            } else {
                consumptions = fuelConsumptionService.obtenerRegistrosPorTipoMaquinaria(tipoMaquinaria);
            }
            
            GetAllFuelConsumptionsResponse.Builder responseBuilder = GetAllFuelConsumptionsResponse.newBuilder();
            for (com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse consumption : consumptions) {
                responseBuilder.addConsumptions(mapToGrpcResponse(consumption));
            }
            responseBuilder.setTotalCount(consumptions.size());
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error obteniendo registros por tipo de maquinaria: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void getFuelConsumptionsByDateRange(GetFuelConsumptionsByDateRangeRequest request,
            StreamObserver<GetAllFuelConsumptionsResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo registros por rango de fechas: {} - {}", 
                    request.getFechaInicio(), request.getFechaFin());
            
            LocalDateTime fechaInicio = parseDateTime(request.getFechaInicio());
            LocalDateTime fechaFin = parseDateTime(request.getFechaFin());
            List<com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse> consumptions = 
                    fuelConsumptionService.obtenerRegistrosPorRangoFechas(fechaInicio, fechaFin);
            
            GetAllFuelConsumptionsResponse.Builder responseBuilder = GetAllFuelConsumptionsResponse.newBuilder();
            for (com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse consumption : consumptions) {
                responseBuilder.addConsumptions(mapToGrpcResponse(consumption));
            }
            responseBuilder.setTotalCount(consumptions.size());
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error obteniendo registros por rango de fechas: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void updateFuelConsumption(UpdateFuelConsumptionRequest request,
            StreamObserver<com.skt.combustible.fuel.grpc.FuelConsumptionResponse> responseObserver) {
        try {
            logger.info("gRPC: Actualizando registro de combustible con ID: {}", request.getId());
            
            FuelConsumptionUpdateRequest domainRequest = new FuelConsumptionUpdateRequest();
            if (request.hasFechaHora() && !request.getFechaHora().isEmpty()) {
                domainRequest.setFechaHora(parseDateTime(request.getFechaHora()));
            }
            if (request.hasCantidadLitros()) {
                domainRequest.setCantidadLitros(request.getCantidadLitros());
            }
            if (request.hasTipoCombustible()) {
                domainRequest.setTipoCombustible(mapTipoCombustibleFromGrpc(request.getTipoCombustible()));
            }
            if (request.hasPrecioPorLitro()) {
                domainRequest.setPrecioPorLitro(request.getPrecioPorLitro());
            }
            if (request.hasCostoTotal()) {
                domainRequest.setCostoTotal(request.getCostoTotal());
            }
            if (request.hasVehiculoId() && !request.getVehiculoId().isEmpty()) {
                domainRequest.setVehiculoId(request.getVehiculoId());
            }
            if (request.hasChoferId() && !request.getChoferId().isEmpty()) {
                domainRequest.setChoferId(request.getChoferId());
            }
            if (request.hasRutaId()) {
                domainRequest.setRutaId(request.getRutaId().isEmpty() ? null : request.getRutaId());
            }
            if (request.hasLecturaOdometroHoras()) {
                domainRequest.setLecturaOdometroHoras(request.getLecturaOdometroHoras());
            }
            if (request.hasObservaciones()) {
                domainRequest.setObservaciones(request.getObservaciones().isEmpty() ? null : request.getObservaciones());
            }
            
            com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse domainResponse = fuelConsumptionService.actualizarRegistro(
                    request.getId(), domainRequest);
            com.skt.combustible.fuel.grpc.FuelConsumptionResponse grpcResponse = mapToGrpcResponse(domainResponse);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            logger.info("gRPC: Registro de combustible actualizado exitosamente: {}", request.getId());
        } catch (FuelConsumptionNotFoundException e) {
            logger.error("gRPC: Registro no encontrado: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("gRPC: Error actualizando registro de combustible: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void deleteFuelConsumption(DeleteFuelConsumptionRequest request,
            StreamObserver<Empty> responseObserver) {
        try {
            logger.info("gRPC: Eliminando registro de combustible con ID: {}", request.getId());
            
            fuelConsumptionService.eliminarRegistro(request.getId());
            
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
            logger.info("gRPC: Registro de combustible eliminado exitosamente: {}", request.getId());
        } catch (FuelConsumptionNotFoundException e) {
            logger.error("gRPC: Registro no encontrado: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("gRPC: Error eliminando registro de combustible: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void getFuelConsumptionStats(GetFuelConsumptionStatsRequest request,
            StreamObserver<FuelConsumptionStatsResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo estadísticas de consumo");
            
            TipoMaquinaria tipoMaquinaria = null;
            if (request.hasTipoMaquinaria()) {
                tipoMaquinaria = mapTipoMaquinariaFromGrpc(request.getTipoMaquinaria());
            }
            
            LocalDateTime fechaInicio = null;
            LocalDateTime fechaFin = null;
            if (request.hasFechaInicio() && request.hasFechaFin()) {
                fechaInicio = parseDateTime(request.getFechaInicio());
                fechaFin = parseDateTime(request.getFechaFin());
            }
            
            FuelConsumptionService.FuelConsumptionStatsDTO stats = 
                    fuelConsumptionService.obtenerEstadisticas(tipoMaquinaria, fechaInicio, fechaFin);
            
            FuelConsumptionStatsResponse.Builder responseBuilder = FuelConsumptionStatsResponse.newBuilder()
                    .setTotalLitros(stats.getTotalLitros())
                    .setTotalCosto(stats.getTotalCosto())
                    .setPromedioLitrosPorRegistro(stats.getPromedioLitrosPorRegistro())
                    .setPromedioCostoPorLitro(stats.getPromedioCostoPorLitro())
                    .setTotalRegistros(stats.getTotalRegistros());
            
            if (stats.getTipoMaquinaria() != null) {
                responseBuilder.setConsumoPorTipoMaquinaria(stats.getTotalLitros());
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error obteniendo estadísticas: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void compareEstimatedVsReal(CompareEstimatedVsRealRequest request,
            StreamObserver<CompareEstimatedVsRealResponse> responseObserver) {
        try {
            logger.info("gRPC: Comparando consumo estimado vs real para ruta: {}", request.getRutaId());
            
            FuelConsumptionService.CompareEstimatedVsRealDTO comparison = 
                    fuelConsumptionService.compararConsumoEstimadoVsReal(request.getRutaId());
            
            CompareEstimatedVsRealResponse response = CompareEstimatedVsRealResponse.newBuilder()
                    .setRutaId(comparison.getRutaId() != null ? comparison.getRutaId() : "")
                    .setRutaNombre(comparison.getRutaNombre() != null ? comparison.getRutaNombre() : "")
                    .setConsumoEstimadoLitros(comparison.getConsumoEstimadoLitros() != null ? 
                            comparison.getConsumoEstimadoLitros() : 0.0)
                    .setConsumoRealLitros(comparison.getConsumoRealLitros() != null ? 
                            comparison.getConsumoRealLitros() : 0.0)
                    .setDiferenciaLitros(comparison.getDiferenciaLitros() != null ? 
                            comparison.getDiferenciaLitros() : 0.0)
                    .setDiferenciaPorcentaje(comparison.getDiferenciaPorcentaje() != null ? 
                            comparison.getDiferenciaPorcentaje() : 0.0)
                    .setConsumoRealMayor(comparison.getConsumoRealMayor() != null ? 
                            comparison.getConsumoRealMayor() : false)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error comparando consumo estimado vs real: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void getReportByMachineryType(GetReportByMachineryTypeRequest request,
            StreamObserver<MachineryTypeReportResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo reporte por tipo de maquinaria: {}", request.getTipoMaquinaria());
            
            TipoMaquinaria tipoMaquinaria = mapTipoMaquinariaFromGrpc(request.getTipoMaquinaria());
            
            LocalDateTime fechaInicio = null;
            LocalDateTime fechaFin = null;
            if (request.hasFechaInicio() && request.hasFechaFin()) {
                fechaInicio = parseDateTime(request.getFechaInicio());
                fechaFin = parseDateTime(request.getFechaFin());
            }
            
            FuelConsumptionService.MachineryTypeReportDTO report = 
                    fuelConsumptionService.obtenerReportePorTipoMaquinaria(tipoMaquinaria, fechaInicio, fechaFin);
            
            MachineryTypeReportResponse.Builder responseBuilder = MachineryTypeReportResponse.newBuilder()
                    .setTipoMaquinaria(mapTipoMaquinariaToGrpc(report.getTipoMaquinaria()))
                    .setTotalLitros(report.getTotalLitros())
                    .setTotalCosto(report.getTotalCosto())
                    .setTotalRegistros(report.getTotalRegistros())
                    .setPromedioLitrosPorRegistro(report.getPromedioLitrosPorRegistro());
            
            for (com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse consumption : report.getConsumptions()) {
                responseBuilder.addConsumptions(mapToGrpcResponse(consumption));
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC: Error obteniendo reporte por tipo de maquinaria: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    // Métodos auxiliares de mapeo
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            logger.warn("Error parseando fecha: {}, usando fecha actual", dateTimeStr);
            return LocalDateTime.now();
        }
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    
    private com.skt.combustible.fuel.grpc.FuelConsumptionResponse mapToGrpcResponse(com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse domain) {
        com.skt.combustible.fuel.grpc.FuelConsumptionResponse.Builder builder = com.skt.combustible.fuel.grpc.FuelConsumptionResponse.newBuilder()
                .setId(domain.getId())
                .setFechaHora(formatDateTime(domain.getFechaHora()))
                .setCantidadLitros(domain.getCantidadLitros() != null ? domain.getCantidadLitros() : 0.0)
                .setTipoCombustible(mapTipoCombustibleToGrpc(domain.getTipoCombustible()));
        
        if (domain.getPrecioPorLitro() != null) {
            builder.setPrecioPorLitro(domain.getPrecioPorLitro());
        }
        if (domain.getCostoTotal() != null) {
            builder.setCostoTotal(domain.getCostoTotal());
        }
        
        builder.setVehiculoId(domain.getVehiculoId())
                .setChoferId(domain.getChoferId());
        
        if (domain.getRutaId() != null && !domain.getRutaId().isEmpty()) {
            builder.setRutaId(domain.getRutaId());
        }
        if (domain.getLecturaOdometroHoras() != null) {
            builder.setLecturaOdometroHoras(domain.getLecturaOdometroHoras());
        }
        if (domain.getObservaciones() != null && !domain.getObservaciones().isEmpty()) {
            builder.setObservaciones(domain.getObservaciones());
        }
        
        builder.setCreatedAt(formatDateTime(domain.getCreatedAt()))
                .setUpdatedAt(formatDateTime(domain.getUpdatedAt()));
        
        if (domain.getTipoMaquinaria() != null) {
            builder.setTipoMaquinaria(mapTipoMaquinariaToGrpc(domain.getTipoMaquinaria()));
        }
        
        return builder.build();
    }
    
    private TipoCombustibleProto mapTipoCombustibleToGrpc(String tipo) {
        if (tipo == null) {
            return TipoCombustibleProto.DIESEL;
        }
        return switch (tipo.toUpperCase()) {
            case "GASOLINA" -> TipoCombustibleProto.GASOLINA;
            case "GAS" -> TipoCombustibleProto.GAS;
            case "ELECTRICO" -> TipoCombustibleProto.ELECTRICO;
            default -> TipoCombustibleProto.DIESEL;
        };
    }
    
    private String mapTipoCombustibleFromGrpc(TipoCombustibleProto tipo) {
        return switch (tipo) {
            case GASOLINA -> "GASOLINA";
            case GAS -> "GAS";
            case ELECTRICO -> "ELECTRICO";
            default -> "DIESEL";
        };
    }
    
    private TipoMaquinariaFuelProto mapTipoMaquinariaToGrpc(TipoMaquinaria tipo) {
        if (tipo == null) {
            return TipoMaquinariaFuelProto.CAMION;
        }
        return switch (tipo) {
            case CAMION -> TipoMaquinariaFuelProto.CAMION;
            case VOLQUETE -> TipoMaquinariaFuelProto.VOLQUETE;
            case EXCAVADORA -> TipoMaquinariaFuelProto.EXCAVADORA;
            case CARGADOR -> TipoMaquinariaFuelProto.CARGADOR;
            case GRUA -> TipoMaquinariaFuelProto.GRUA;
            case MOTONIVELADORA -> TipoMaquinariaFuelProto.MOTONIVELADORA;
        };
    }
    
    private TipoMaquinaria mapTipoMaquinariaFromGrpc(TipoMaquinariaFuelProto tipo) {
        return switch (tipo) {
            case CAMION -> TipoMaquinaria.CAMION;
            case VOLQUETE -> TipoMaquinaria.VOLQUETE;
            case EXCAVADORA -> TipoMaquinaria.EXCAVADORA;
            case CARGADOR -> TipoMaquinaria.CARGADOR;
            case GRUA -> TipoMaquinaria.GRUA;
            case MOTONIVELADORA -> TipoMaquinaria.MOTONIVELADORA;
            case UNRECOGNIZED -> null;
        };
    }
}

