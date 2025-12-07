package com.skt.combustible.fuel.infrastructure.rest;

import com.skt.combustible.fuel.application.service.FuelConsumptionService;
import com.skt.combustible.fuel.domain.dto.FuelConsumptionCreateRequest;
import com.skt.combustible.fuel.domain.dto.FuelConsumptionResponse;
import com.skt.combustible.fuel.domain.dto.FuelConsumptionUpdateRequest;
import com.skt.combustible.fuel.domain.exception.FuelConsumptionNotFoundException;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para el servicio de combustible
 * Expone endpoints HTTP para la gestión de consumo de combustible
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/fuel")
@CrossOrigin(origins = "*")
public class FuelRestController {
    
    private static final Logger logger = LoggerFactory.getLogger(FuelRestController.class);
    
    @Autowired
    private FuelConsumptionService fuelConsumptionService;
    
    /**
     * Obtiene todos los registros de combustible
     */
    @GetMapping
    public ResponseEntity<List<FuelConsumptionResponse>> getAllFuelConsumptions() {
        try {
            logger.info("REST: Obteniendo todos los registros de combustible");
            List<FuelConsumptionResponse> consumptions = fuelConsumptionService.obtenerTodosLosRegistros();
            return ResponseEntity.ok(consumptions);
        } catch (Exception e) {
            logger.error("Error obteniendo todos los registros: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene un registro por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FuelConsumptionResponse> getFuelConsumptionById(@PathVariable("id") String id) {
        try {
            logger.info("REST: Obteniendo registro de combustible por ID: {}", id);
            Optional<FuelConsumptionResponse> consumption = fuelConsumptionService.obtenerRegistroPorId(id);
            if (consumption.isPresent()) {
                return ResponseEntity.ok(consumption.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (FuelConsumptionNotFoundException e) {
            logger.warn("Registro de combustible no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error obteniendo registro por ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Crea un nuevo registro de combustible
     */
    @PostMapping
    public ResponseEntity<?> createFuelConsumption(@Valid @RequestBody FuelConsumptionCreateRequest request) {
        try {
            logger.info("REST: Creando nuevo registro de combustible: {} litros para vehículo {}",
                    request.getCantidadLitros(), request.getVehiculoId());
            FuelConsumptionResponse consumption = fuelConsumptionService.crearRegistro(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(consumption);
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al crear registro: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage(), "status", 400));
        } catch (Exception e) {
            logger.error("Error creando registro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al crear registro: " + e.getMessage(), "status", 500));
        }
    }
    
    /**
     * Actualiza un registro de combustible
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFuelConsumption(@PathVariable("id") String id,
            @Valid @RequestBody FuelConsumptionUpdateRequest request) {
        try {
            logger.info("REST: Actualizando registro de combustible con ID: {}", id);
            FuelConsumptionResponse consumption = fuelConsumptionService.actualizarRegistro(id, request);
            return ResponseEntity.ok(consumption);
        } catch (FuelConsumptionNotFoundException e) {
            logger.warn("Registro de combustible no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al actualizar registro: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage(), "status", 400));
        } catch (Exception e) {
            logger.error("Error actualizando registro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al actualizar registro: " + e.getMessage(), "status", 500));
        }
    }
    
    /**
     * Elimina un registro de combustible (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFuelConsumption(@PathVariable("id") String id) {
        try {
            logger.info("REST: Eliminando registro de combustible con ID: {}", id);
            fuelConsumptionService.eliminarRegistro(id);
            return ResponseEntity.noContent().build();
        } catch (FuelConsumptionNotFoundException e) {
            logger.warn("Registro de combustible no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error eliminando registro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al eliminar registro: " + e.getMessage(), "status", 500));
        }
    }
    
    /**
     * Obtiene registros por vehículo
     */
    @GetMapping("/vehicle/{vehiculoId}")
    public ResponseEntity<List<FuelConsumptionResponse>> getFuelConsumptionsByVehicle(
            @PathVariable("vehiculoId") String vehiculoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            logger.info("REST: Obteniendo registros por vehículo: {}", vehiculoId);
            List<FuelConsumptionResponse> consumptions;
            if (fechaInicio != null && fechaFin != null) {
                consumptions = fuelConsumptionService.obtenerRegistrosPorRangoFechas(fechaInicio, fechaFin)
                        .stream()
                        .filter(c -> c.getVehiculoId().equals(vehiculoId))
                        .toList();
            } else {
                consumptions = fuelConsumptionService.obtenerRegistrosPorVehiculo(vehiculoId);
            }
            return ResponseEntity.ok(consumptions);
        } catch (Exception e) {
            logger.error("Error obteniendo registros por vehículo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene registros por chofer
     */
    @GetMapping("/driver/{choferId}")
    public ResponseEntity<List<FuelConsumptionResponse>> getFuelConsumptionsByDriver(
            @PathVariable("choferId") String choferId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            logger.info("REST: Obteniendo registros por chofer: {}", choferId);
            List<FuelConsumptionResponse> consumptions;
            if (fechaInicio != null && fechaFin != null) {
                consumptions = fuelConsumptionService.obtenerRegistrosPorRangoFechas(fechaInicio, fechaFin)
                        .stream()
                        .filter(c -> c.getChoferId().equals(choferId))
                        .toList();
            } else {
                consumptions = fuelConsumptionService.obtenerRegistrosPorChofer(choferId);
            }
            return ResponseEntity.ok(consumptions);
        } catch (Exception e) {
            logger.error("Error obteniendo registros por chofer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene registros por ruta
     */
    @GetMapping("/route/{rutaId}")
    public ResponseEntity<List<FuelConsumptionResponse>> getFuelConsumptionsByRoute(
            @PathVariable("rutaId") String rutaId) {
        try {
            logger.info("REST: Obteniendo registros por ruta: {}", rutaId);
            List<FuelConsumptionResponse> consumptions = fuelConsumptionService.obtenerRegistrosPorRuta(rutaId);
            return ResponseEntity.ok(consumptions);
        } catch (Exception e) {
            logger.error("Error obteniendo registros por ruta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene registros por tipo de maquinaria
     */
    @GetMapping("/machinery-type/{tipoMaquinaria}")
    public ResponseEntity<List<FuelConsumptionResponse>> getFuelConsumptionsByMachineryType(
            @PathVariable("tipoMaquinaria") TipoMaquinaria tipoMaquinaria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            logger.info("REST: Obteniendo registros por tipo de maquinaria: {}", tipoMaquinaria);
            List<FuelConsumptionResponse> consumptions;
            if (fechaInicio != null && fechaFin != null) {
                consumptions = fuelConsumptionService.obtenerRegistrosPorRangoFechas(fechaInicio, fechaFin)
                        .stream()
                        .filter(c -> c.getTipoMaquinaria() == tipoMaquinaria)
                        .toList();
            } else {
                consumptions = fuelConsumptionService.obtenerRegistrosPorTipoMaquinaria(tipoMaquinaria);
            }
            return ResponseEntity.ok(consumptions);
        } catch (Exception e) {
            logger.error("Error obteniendo registros por tipo de maquinaria: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene registros por rango de fechas
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<FuelConsumptionResponse>> getFuelConsumptionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            logger.info("REST: Obteniendo registros por rango de fechas: {} - {}", fechaInicio, fechaFin);
            List<FuelConsumptionResponse> consumptions = 
                    fuelConsumptionService.obtenerRegistrosPorRangoFechas(fechaInicio, fechaFin);
            return ResponseEntity.ok(consumptions);
        } catch (Exception e) {
            logger.error("Error obteniendo registros por rango de fechas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene estadísticas de consumo
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getFuelConsumptionStats(
            @RequestParam(required = false) TipoMaquinaria tipoMaquinaria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            logger.info("REST: Obteniendo estadísticas de consumo");
            FuelConsumptionService.FuelConsumptionStatsDTO stats = 
                    fuelConsumptionService.obtenerEstadisticas(tipoMaquinaria, fechaInicio, fechaFin);
            
            Map<String, Object> response = Map.of(
                    "totalLitros", stats.getTotalLitros(),
                    "totalCosto", stats.getTotalCosto(),
                    "promedioLitrosPorRegistro", stats.getPromedioLitrosPorRegistro(),
                    "promedioCostoPorLitro", stats.getPromedioCostoPorLitro(),
                    "totalRegistros", stats.getTotalRegistros(),
                    "tipoMaquinaria", stats.getTipoMaquinaria() != null ? stats.getTipoMaquinaria().name() : "TODOS"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener estadísticas: " + e.getMessage()));
        }
    }
    
    /**
     * Compara consumo estimado vs real para una ruta
     */
    @GetMapping("/compare/{rutaId}")
    public ResponseEntity<Map<String, Object>> compareEstimatedVsReal(@PathVariable("rutaId") String rutaId) {
        try {
            logger.info("REST: Comparando consumo estimado vs real para ruta: {}", rutaId);
            FuelConsumptionService.CompareEstimatedVsRealDTO comparison = 
                    fuelConsumptionService.compararConsumoEstimadoVsReal(rutaId);
            
            Map<String, Object> response = Map.of(
                    "rutaId", comparison.getRutaId() != null ? comparison.getRutaId() : "",
                    "rutaNombre", comparison.getRutaNombre() != null ? comparison.getRutaNombre() : "",
                    "consumoEstimadoLitros", comparison.getConsumoEstimadoLitros() != null ? 
                            comparison.getConsumoEstimadoLitros() : 0.0,
                    "consumoRealLitros", comparison.getConsumoRealLitros() != null ? 
                            comparison.getConsumoRealLitros() : 0.0,
                    "diferenciaLitros", comparison.getDiferenciaLitros() != null ? 
                            comparison.getDiferenciaLitros() : 0.0,
                    "diferenciaPorcentaje", comparison.getDiferenciaPorcentaje() != null ? 
                            comparison.getDiferenciaPorcentaje() : 0.0,
                    "consumoRealMayor", comparison.getConsumoRealMayor() != null ? 
                            comparison.getConsumoRealMayor() : false
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error comparando consumo estimado vs real: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al comparar consumo: " + e.getMessage()));
        }
    }
    
    /**
     * Obtiene reporte por tipo de maquinaria
     */
    @GetMapping("/report/machinery-type/{tipoMaquinaria}")
    public ResponseEntity<Map<String, Object>> getReportByMachineryType(
            @PathVariable("tipoMaquinaria") TipoMaquinaria tipoMaquinaria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            logger.info("REST: Obteniendo reporte por tipo de maquinaria: {}", tipoMaquinaria);
            FuelConsumptionService.MachineryTypeReportDTO report = 
                    fuelConsumptionService.obtenerReportePorTipoMaquinaria(tipoMaquinaria, fechaInicio, fechaFin);
            
            Map<String, Object> response = Map.of(
                    "tipoMaquinaria", report.getTipoMaquinaria().name(),
                    "totalLitros", report.getTotalLitros(),
                    "totalCosto", report.getTotalCosto(),
                    "totalRegistros", report.getTotalRegistros(),
                    "promedioLitrosPorRegistro", report.getPromedioLitrosPorRegistro(),
                    "consumptions", report.getConsumptions()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error obteniendo reporte por tipo de maquinaria: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener reporte: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "fuel-service"));
    }
    
    /**
     * Info endpoint
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
                "name", "Fuel Service",
                "version", "1.0.0",
                "description", "Servicio de gestión de consumo de combustible"
        ));
    }
}

