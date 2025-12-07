package com.skt.combustible.fuel.application.service;

import com.skt.combustible.fuel.domain.dto.FuelConsumptionCreateRequest;
import com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse;
import com.skt.combustible.fuel.domain.dto.FuelConsumptionUpdateRequest;
import com.skt.combustible.fuel.domain.entity.FuelConsumption;
import com.skt.combustible.fuel.domain.exception.FuelConsumptionNotFoundException;
import com.skt.combustible.fuel.domain.repository.FuelConsumptionRepository;
import com.skt.combustible.fuel.infrastructure.client.RoutesGrpcClient;
import com.skt.combustible.fuel.infrastructure.client.VehiclesGrpcClient;
import com.skt.combustible.routes.grpc.RouteResponse;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.grpc.VehicleResponseProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para la gestión de consumo de combustible
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Service
@Transactional
public class FuelConsumptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(FuelConsumptionService.class);
    
    @Autowired
    private FuelConsumptionRepository fuelConsumptionRepository;
    
    @Autowired(required = false)
    private RoutesGrpcClient routesGrpcClient;
    
    @Autowired(required = false)
    private VehiclesGrpcClient vehiclesGrpcClient;
    
    /**
     * Crea un nuevo registro de consumo de combustible
     */
    public FuelConsumptionResponse crearRegistro(FuelConsumptionCreateRequest request) {
        logger.info("Creando nuevo registro de combustible: {} litros para vehículo {}", 
                request.getCantidadLitros(), request.getVehiculoId());
        
        FuelConsumption consumo = new FuelConsumption();
        consumo.setFechaHora(request.getFechaHora() != null ? request.getFechaHora() : LocalDateTime.now());
        consumo.setCantidadLitros(request.getCantidadLitros());
        consumo.setTipoCombustible(request.getTipoCombustible());
        consumo.setPrecioPorLitro(request.getPrecioPorLitro());
        consumo.setVehiculoId(request.getVehiculoId());
        consumo.setChoferId(request.getChoferId());
        consumo.setRutaId(request.getRutaId());
        consumo.setLecturaOdometroHoras(request.getLecturaOdometroHoras());
        consumo.setObservaciones(request.getObservaciones());
        
        // Si se proporciona costo total directamente, usarlo; si no, calcularlo
        if (request.getCostoTotal() != null) {
            consumo.setCostoTotal(request.getCostoTotal());
        } else {
            consumo.calcularCostoTotal();
        }
        
        // Obtener tipo de maquinaria del vehículo si es posible
        if (vehiclesGrpcClient != null) {
            try {
                VehicleResponseProto vehicle = vehiclesGrpcClient.getVehicleById(request.getVehiculoId());
                if (vehicle != null) {
                    TipoMaquinaria tipo = mapGrpcTipoToDomain(vehicle.getTipoMaquinaria());
                    consumo.setTipoMaquinaria(tipo);
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener tipo de maquinaria del vehículo: {}", e.getMessage());
            }
        }
        
        FuelConsumption saved = fuelConsumptionRepository.save(consumo);
        logger.info("Registro de combustible creado exitosamente con ID: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    /**
     * Obtiene un registro por ID
     */
    @Transactional(readOnly = true)
    public Optional<FuelConsumptionResponse> obtenerRegistroPorId(String id) {
        return fuelConsumptionRepository.findById(id)
                .filter(FuelConsumption::getActivo)
                .map(this::mapToResponse);
    }
    
    /**
     * Obtiene todos los registros activos
     */
    @Transactional(readOnly = true)
    public List<FuelConsumptionResponse> obtenerTodosLosRegistros() {
        return fuelConsumptionRepository.findByActivoTrue(Pageable.unpaged())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene registros por vehículo
     */
    @Transactional(readOnly = true)
    public List<FuelConsumptionResponse> obtenerRegistrosPorVehiculo(String vehiculoId) {
        return fuelConsumptionRepository.findByVehiculoIdAndActivoTrue(vehiculoId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene registros por chofer
     */
    @Transactional(readOnly = true)
    public List<FuelConsumptionResponse> obtenerRegistrosPorChofer(String choferId) {
        return fuelConsumptionRepository.findByChoferIdAndActivoTrue(choferId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene registros por ruta
     */
    @Transactional(readOnly = true)
    public List<FuelConsumptionResponse> obtenerRegistrosPorRuta(String rutaId) {
        return fuelConsumptionRepository.findByRutaIdAndActivoTrue(rutaId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene registros por tipo de maquinaria
     */
    @Transactional(readOnly = true)
    public List<FuelConsumptionResponse> obtenerRegistrosPorTipoMaquinaria(TipoMaquinaria tipoMaquinaria) {
        return fuelConsumptionRepository.findByTipoMaquinariaAndActivoTrue(tipoMaquinaria)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene registros por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<FuelConsumptionResponse> obtenerRegistrosPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return fuelConsumptionRepository.findByFechaHoraBetween(fechaInicio, fechaFin)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Actualiza un registro de consumo
     */
    public FuelConsumptionResponse actualizarRegistro(String id, FuelConsumptionUpdateRequest request) {
        logger.info("Actualizando registro de combustible con ID: {}", id);
        
        FuelConsumption consumo = fuelConsumptionRepository.findById(id)
                .filter(FuelConsumption::getActivo)
                .orElseThrow(() -> FuelConsumptionNotFoundException.withId(id));
        
        if (request.getFechaHora() != null) {
            consumo.setFechaHora(request.getFechaHora());
        }
        if (request.getCantidadLitros() != null) {
            consumo.setCantidadLitros(request.getCantidadLitros());
        }
        if (request.getTipoCombustible() != null) {
            consumo.setTipoCombustible(request.getTipoCombustible());
        }
        if (request.getPrecioPorLitro() != null) {
            consumo.setPrecioPorLitro(request.getPrecioPorLitro());
        }
        if (request.getCostoTotal() != null) {
            consumo.setCostoTotal(request.getCostoTotal());
        } else {
            consumo.calcularCostoTotal();
        }
        if (request.getVehiculoId() != null) {
            consumo.setVehiculoId(request.getVehiculoId());
        }
        if (request.getChoferId() != null) {
            consumo.setChoferId(request.getChoferId());
        }
        if (request.getRutaId() != null) {
            consumo.setRutaId(request.getRutaId());
        }
        if (request.getLecturaOdometroHoras() != null) {
            consumo.setLecturaOdometroHoras(request.getLecturaOdometroHoras());
        }
        if (request.getObservaciones() != null) {
            consumo.setObservaciones(request.getObservaciones());
        }
        
        FuelConsumption updated = fuelConsumptionRepository.save(consumo);
        logger.info("Registro de combustible actualizado exitosamente: {}", updated.getId());
        
        return mapToResponse(updated);
    }
    
    /**
     * Elimina un registro (soft delete)
     */
    public void eliminarRegistro(String id) {
        logger.info("Eliminando registro de combustible con ID: {}", id);
        
        FuelConsumption consumo = fuelConsumptionRepository.findById(id)
                .filter(FuelConsumption::getActivo)
                .orElseThrow(() -> FuelConsumptionNotFoundException.withId(id));
        
        consumo.setActivo(false);
        fuelConsumptionRepository.save(consumo);
        logger.info("Registro de combustible eliminado exitosamente: {}", id);
    }
    
    /**
     * Compara consumo estimado vs real para una ruta
     */
    @Transactional(readOnly = true)
    public CompareEstimatedVsRealDTO compararConsumoEstimadoVsReal(String rutaId) {
        logger.info("Comparando consumo estimado vs real para ruta: {}", rutaId);
        
        // Obtener consumo estimado de la ruta
        Double consumoEstimado = null;
        String rutaNombre = null;
        
        if (routesGrpcClient != null) {
            try {
                RouteResponse route = routesGrpcClient.getRouteById(rutaId);
                if (route != null) {
                    consumoEstimado = route.getConsumoEstimadoLitros();
                    rutaNombre = route.getNombreRuta();
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener información de la ruta: {}", e.getMessage());
            }
        }
        
        // Obtener consumo real de los registros asociados a esta ruta
        List<FuelConsumption> registrosReales = fuelConsumptionRepository.findByRutaIdAndActivoTrue(rutaId);
        Double consumoReal = registrosReales.stream()
                .mapToDouble(c -> c.getCantidadLitros() != null ? c.getCantidadLitros() : 0.0)
                .sum();
        
        if (consumoEstimado == null || consumoEstimado == 0) {
            logger.warn("No se encontró consumo estimado para la ruta {}", rutaId);
            return new CompareEstimatedVsRealDTO(rutaId, rutaNombre, 0.0, consumoReal, consumoReal, 0.0, false);
        }
        
        double diferencia = consumoReal - consumoEstimado;
        double diferenciaPorcentaje = (diferencia / consumoEstimado) * 100.0;
        boolean consumoRealMayor = consumoReal > consumoEstimado;
        
        return new CompareEstimatedVsRealDTO(
                rutaId,
                rutaNombre,
                consumoEstimado,
                consumoReal,
                diferencia,
                diferenciaPorcentaje,
                consumoRealMayor
        );
    }
    
    /**
     * Obtiene estadísticas de consumo
     */
    @Transactional(readOnly = true)
    public FuelConsumptionStatsDTO obtenerEstadisticas(TipoMaquinaria tipoMaquinaria, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<FuelConsumption> registros;
        
        if (tipoMaquinaria != null && fechaInicio != null && fechaFin != null) {
            registros = fuelConsumptionRepository.findByTipoMaquinariaAndFechaHoraBetween(tipoMaquinaria, fechaInicio, fechaFin);
        } else if (tipoMaquinaria != null) {
            registros = fuelConsumptionRepository.findByTipoMaquinariaAndActivoTrue(tipoMaquinaria);
        } else if (fechaInicio != null && fechaFin != null) {
            registros = fuelConsumptionRepository.findByFechaHoraBetween(fechaInicio, fechaFin);
        } else {
            registros = fuelConsumptionRepository.findByActivoTrue(Pageable.unpaged()).getContent();
        }
        
        if (registros.isEmpty()) {
            return new FuelConsumptionStatsDTO(0.0, 0.0, 0.0, 0.0, 0L, null);
        }
        
        double totalLitros = registros.stream()
                .mapToDouble(c -> c.getCantidadLitros() != null ? c.getCantidadLitros() : 0.0)
                .sum();
        
        double totalCosto = registros.stream()
                .mapToDouble(c -> c.getCostoTotal() != null ? c.getCostoTotal() : 0.0)
                .sum();
        
        double promedioLitros = totalLitros / registros.size();
        double promedioCostoPorLitro = totalLitros > 0 ? totalCosto / totalLitros : 0.0;
        
        return new FuelConsumptionStatsDTO(
                totalLitros,
                totalCosto,
                promedioLitros,
                promedioCostoPorLitro,
                (long) registros.size(),
                tipoMaquinaria
        );
    }
    
    /**
     * Obtiene reporte por tipo de maquinaria
     */
    @Transactional(readOnly = true)
    public MachineryTypeReportDTO obtenerReportePorTipoMaquinaria(TipoMaquinaria tipoMaquinaria, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<FuelConsumption> registros;
        
        if (fechaInicio != null && fechaFin != null) {
            registros = fuelConsumptionRepository.findByTipoMaquinariaAndFechaHoraBetween(tipoMaquinaria, fechaInicio, fechaFin);
        } else {
            registros = fuelConsumptionRepository.findByTipoMaquinariaAndActivoTrue(tipoMaquinaria);
        }
        
        double totalLitros = registros.stream()
                .mapToDouble(c -> c.getCantidadLitros() != null ? c.getCantidadLitros() : 0.0)
                .sum();
        
        double totalCosto = registros.stream()
                .mapToDouble(c -> c.getCostoTotal() != null ? c.getCostoTotal() : 0.0)
                .sum();
        
        double promedioLitros = registros.isEmpty() ? 0.0 : totalLitros / registros.size();
        
        List<FuelConsumptionResponse> responses = registros.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new MachineryTypeReportDTO(
                tipoMaquinaria,
                totalLitros,
                totalCosto,
                (long) registros.size(),
                promedioLitros,
                responses
        );
    }
    
    /**
     * Mapea una entidad FuelConsumption a FuelConsumptionResponse
     */
    private FuelConsumptionResponse mapToResponse(FuelConsumption consumo) {
        FuelConsumptionResponse response = new FuelConsumptionResponse();
        response.setId(consumo.getId());
        response.setFechaHora(consumo.getFechaHora());
        response.setCantidadLitros(consumo.getCantidadLitros());
        response.setTipoCombustible(consumo.getTipoCombustible());
        response.setPrecioPorLitro(consumo.getPrecioPorLitro());
        response.setCostoTotal(consumo.getCostoTotal());
        response.setVehiculoId(consumo.getVehiculoId());
        response.setChoferId(consumo.getChoferId());
        response.setRutaId(consumo.getRutaId());
        response.setLecturaOdometroHoras(consumo.getLecturaOdometroHoras());
        response.setObservaciones(consumo.getObservaciones());
        response.setTipoMaquinaria(consumo.getTipoMaquinaria());
        response.setCreatedAt(consumo.getCreatedAt());
        response.setUpdatedAt(consumo.getUpdatedAt());
        return response;
    }
    
    /**
     * Mapea TipoMaquinariaProto a TipoMaquinaria del dominio
     */
    private TipoMaquinaria mapGrpcTipoToDomain(com.skt.combustible.vehicles.grpc.TipoMaquinariaProto tipo) {
        return switch (tipo) {
            case CAMION -> TipoMaquinaria.CAMION;
            case VOLQUETE -> TipoMaquinaria.VOLQUETE;
            case EXCAVADORA -> TipoMaquinaria.EXCAVADORA;
            case CARGADOR -> TipoMaquinaria.CARGADOR;
            case GRUA -> TipoMaquinaria.GRUA;
            case MOTONIVELADORA -> TipoMaquinaria.MOTONIVELADORA;
            default -> null;
        };
    }
    
    /**
     * DTO para comparación de consumo estimado vs real
     */
    public static class CompareEstimatedVsRealDTO {
        private final String rutaId;
        private final String rutaNombre;
        private final Double consumoEstimadoLitros;
        private final Double consumoRealLitros;
        private final Double diferenciaLitros;
        private final Double diferenciaPorcentaje;
        private final Boolean consumoRealMayor;
        
        public CompareEstimatedVsRealDTO(String rutaId, String rutaNombre, Double consumoEstimadoLitros,
                                         Double consumoRealLitros, Double diferenciaLitros,
                                         Double diferenciaPorcentaje, Boolean consumoRealMayor) {
            this.rutaId = rutaId;
            this.rutaNombre = rutaNombre;
            this.consumoEstimadoLitros = consumoEstimadoLitros;
            this.consumoRealLitros = consumoRealLitros;
            this.diferenciaLitros = diferenciaLitros;
            this.diferenciaPorcentaje = diferenciaPorcentaje;
            this.consumoRealMayor = consumoRealMayor;
        }
        
        // Getters
        public String getRutaId() { return rutaId; }
        public String getRutaNombre() { return rutaNombre; }
        public Double getConsumoEstimadoLitros() { return consumoEstimadoLitros; }
        public Double getConsumoRealLitros() { return consumoRealLitros; }
        public Double getDiferenciaLitros() { return diferenciaLitros; }
        public Double getDiferenciaPorcentaje() { return diferenciaPorcentaje; }
        public Boolean getConsumoRealMayor() { return consumoRealMayor; }
    }
    
    /**
     * DTO para estadísticas de consumo
     */
    public static class FuelConsumptionStatsDTO {
        private final Double totalLitros;
        private final Double totalCosto;
        private final Double promedioLitrosPorRegistro;
        private final Double promedioCostoPorLitro;
        private final Long totalRegistros;
        private final TipoMaquinaria tipoMaquinaria;
        
        public FuelConsumptionStatsDTO(Double totalLitros, Double totalCosto, Double promedioLitrosPorRegistro,
                                      Double promedioCostoPorLitro, Long totalRegistros, TipoMaquinaria tipoMaquinaria) {
            this.totalLitros = totalLitros;
            this.totalCosto = totalCosto;
            this.promedioLitrosPorRegistro = promedioLitrosPorRegistro;
            this.promedioCostoPorLitro = promedioCostoPorLitro;
            this.totalRegistros = totalRegistros;
            this.tipoMaquinaria = tipoMaquinaria;
        }
        
        // Getters
        public Double getTotalLitros() { return totalLitros; }
        public Double getTotalCosto() { return totalCosto; }
        public Double getPromedioLitrosPorRegistro() { return promedioLitrosPorRegistro; }
        public Double getPromedioCostoPorLitro() { return promedioCostoPorLitro; }
        public Long getTotalRegistros() { return totalRegistros; }
        public TipoMaquinaria getTipoMaquinaria() { return tipoMaquinaria; }
    }
    
    /**
     * DTO para reporte por tipo de maquinaria
     */
    public static class MachineryTypeReportDTO {
        private final TipoMaquinaria tipoMaquinaria;
        private final Double totalLitros;
        private final Double totalCosto;
        private final Long totalRegistros;
        private final Double promedioLitrosPorRegistro;
        private final List<FuelConsumptionResponse> consumptions;
        
        public MachineryTypeReportDTO(TipoMaquinaria tipoMaquinaria, Double totalLitros, Double totalCosto,
                                     Long totalRegistros, Double promedioLitrosPorRegistro,
                                     List<FuelConsumptionResponse> consumptions) {
            this.tipoMaquinaria = tipoMaquinaria;
            this.totalLitros = totalLitros;
            this.totalCosto = totalCosto;
            this.totalRegistros = totalRegistros;
            this.promedioLitrosPorRegistro = promedioLitrosPorRegistro;
            this.consumptions = consumptions;
        }
        
        // Getters
        public TipoMaquinaria getTipoMaquinaria() { return tipoMaquinaria; }
        public Double getTotalLitros() { return totalLitros; }
        public Double getTotalCosto() { return totalCosto; }
        public Long getTotalRegistros() { return totalRegistros; }
        public Double getPromedioLitrosPorRegistro() { return promedioLitrosPorRegistro; }
        public List<FuelConsumptionResponse> getConsumptions() { return consumptions; }
    }
}

