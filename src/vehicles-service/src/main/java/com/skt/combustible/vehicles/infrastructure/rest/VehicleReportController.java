package com.skt.combustible.vehicles.infrastructure.rest;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.application.service.VehicleReportService;
import com.skt.combustible.vehicles.domain.entity.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para reportes y consultas de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/vehicles/reports")
@CrossOrigin(origins = "*")
public class VehicleReportController {
    
    @Autowired
    private VehicleReportService vehicleReportService;
    
    /**
     * Reporte de vehículos por tipo de maquinaria
     */
    @GetMapping("/por-tipo")
    public ResponseEntity<Map<TipoMaquinaria, Long>> reporteVehiculosPorTipo() {
        Map<TipoMaquinaria, Long> reporte = vehicleReportService.reporteVehiculosPorTipo();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos por estado operativo
     */
    @GetMapping("/por-estado")
    public ResponseEntity<Map<EstadoOperativo, Long>> reporteVehiculosPorEstado() {
        Map<EstadoOperativo, Long> reporte = vehicleReportService.reporteVehiculosPorEstado();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos por marca
     */
    @GetMapping("/por-marca")
    public ResponseEntity<Map<String, Long>> reporteVehiculosPorMarca() {
        Map<String, Long> reporte = vehicleReportService.reporteVehiculosPorMarca();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos por año
     */
    @GetMapping("/por-anio")
    public ResponseEntity<Map<Integer, Long>> reporteVehiculosPorAnio() {
        Map<Integer, Long> reporte = vehicleReportService.reporteVehiculosPorAnio();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de consumo promedio por tipo de maquinaria
     */
    @GetMapping("/consumo-promedio-por-tipo")
    public ResponseEntity<Map<TipoMaquinaria, Double>> reporteConsumoPromedioPorTipo() {
        Map<TipoMaquinaria, Double> reporte = vehicleReportService.reporteConsumoPromedioPorTipo();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos con mayor consumo
     */
    @GetMapping("/mayor-consumo")
    public ResponseEntity<List<Vehicle>> reporteVehiculosConMayorConsumo(
            @RequestParam(defaultValue = "10") int limite) {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosConMayorConsumo(limite);
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos con menor consumo
     */
    @GetMapping("/menor-consumo")
    public ResponseEntity<List<Vehicle>> reporteVehiculosConMenorConsumo(
            @RequestParam(defaultValue = "10") int limite) {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosConMenorConsumo(limite);
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos por rango de años
     */
    @GetMapping("/por-rango-anios")
    public ResponseEntity<List<Vehicle>> reporteVehiculosPorRangoAnios(
            @RequestParam Integer anioInicio,
            @RequestParam Integer anioFin) {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosPorRangoAnios(anioInicio, anioFin);
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos por marca (búsqueda)
     */
    @GetMapping("/buscar-por-marca")
    public ResponseEntity<List<Vehicle>> reporteVehiculosPorMarca(@RequestParam String marca) {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosPorMarca(marca);
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos por modelo (búsqueda)
     */
    @GetMapping("/buscar-por-modelo")
    public ResponseEntity<List<Vehicle>> reporteVehiculosPorModelo(@RequestParam String modelo) {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosPorModelo(modelo);
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos con consumo mayor a un valor
     */
    @GetMapping("/consumo-mayor-a")
    public ResponseEntity<List<Vehicle>> reporteVehiculosConConsumoMayorA(@RequestParam Double consumo) {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosConConsumoMayorA(consumo);
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos en mantenimiento
     */
    @GetMapping("/en-mantenimiento")
    public ResponseEntity<List<Vehicle>> reporteVehiculosEnMantenimiento() {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosEnMantenimiento();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos en uso
     */
    @GetMapping("/en-uso")
    public ResponseEntity<List<Vehicle>> reporteVehiculosEnUso() {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosEnUso();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos disponibles
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<Vehicle>> reporteVehiculosDisponibles() {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosDisponibles();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos disponibles por tipo
     */
    @GetMapping("/disponibles-por-tipo")
    public ResponseEntity<List<Vehicle>> reporteVehiculosDisponiblesPorTipo(@RequestParam TipoMaquinaria tipo) {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosDisponiblesPorTipo(tipo);
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos por tipo y estado
     */
    @GetMapping("/por-tipo-y-estado")
    public ResponseEntity<List<Vehicle>> reporteVehiculosPorTipoYEstado(
            @RequestParam TipoMaquinaria tipo,
            @RequestParam EstadoOperativo estado) {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosPorTipoYEstado(tipo, estado);
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos creados en un rango de fechas
     */
    @GetMapping("/creados-en-rango")
    public ResponseEntity<List<Vehicle>> reporteVehiculosCreadosEnRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosCreadosEnRango(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos actualizados en un rango de fechas
     */
    @GetMapping("/actualizados-en-rango")
    public ResponseEntity<List<Vehicle>> reporteVehiculosActualizadosEnRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<Vehicle> reporte = vehicleReportService.reporteVehiculosActualizadosEnRango(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos por capacidad de tanque
     */
    @GetMapping("/por-capacidad-tanque")
    public ResponseEntity<Map<String, Long>> reporteVehiculosPorCapacidadTanque() {
        Map<String, Long> reporte = vehicleReportService.reporteVehiculosPorCapacidadTanque();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Reporte de vehículos por kilometraje
     */
    @GetMapping("/por-kilometraje")
    public ResponseEntity<Map<String, Long>> reporteVehiculosPorKilometraje() {
        Map<String, Long> reporte = vehicleReportService.reporteVehiculosPorKilometraje();
        return ResponseEntity.ok(reporte);
    }
}
