package com.skt.combustible.vehicles.infrastructure.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skt.combustible.vehicles.application.service.VehicleReportService;

/**
 * Controlador REST para reportes del servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/vehicles/reports")
public class VehicleReportController {
    
    @Autowired
    private VehicleReportService reportService;
    
    /**
     * Genera reporte de vehículos por estado
     */
    @GetMapping("/vehiculos-por-estado")
    public ResponseEntity<Map<String, Object>> reporteVehiculosPorEstado() {
        Map<String, Object> reporte = reportService.generarReporteVehiculosPorEstado();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Genera reporte de consumo por vehículo
     */
    @GetMapping("/consumo-por-vehiculo")
    public ResponseEntity<Map<String, Object>> reporteConsumoPorVehiculo() {
        Map<String, Object> reporte = reportService.generarReporteConsumoPorVehiculo();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Genera reporte de historial de mantenimientos
     */
    @GetMapping("/historial-mantenimientos")
    public ResponseEntity<Map<String, Object>> reporteHistorialMantenimientos() {
        Map<String, Object> reporte = reportService.generarReporteHistorialMantenimientos();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Genera reporte de asignaciones
     */
    @GetMapping("/asignaciones")
    public ResponseEntity<Map<String, Object>> reporteAsignaciones() {
        Map<String, Object> reporte = reportService.generarReporteAsignaciones();
        return ResponseEntity.ok(reporte);
    }
    
    /**
     * Genera reporte consolidado del sistema
     */
    @GetMapping("/consolidado")
    public ResponseEntity<Map<String, Object>> reporteConsolidado() {
        Map<String, Object> reporte = reportService.generarReporteConsolidado();
        return ResponseEntity.ok(reporte);
    }
}