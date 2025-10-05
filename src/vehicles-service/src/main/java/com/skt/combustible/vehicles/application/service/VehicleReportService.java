package com.skt.combustible.vehicles.application.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.domain.entity.AsignacionVehiculo;
import com.skt.combustible.vehicles.domain.entity.Mantenimiento;
import com.skt.combustible.vehicles.domain.entity.Vehicle;
import com.skt.combustible.vehicles.domain.repository.AsignacionRepository;
import com.skt.combustible.vehicles.domain.repository.MantenimientoRepository;
import com.skt.combustible.vehicles.domain.repository.VehicleRepository;

/**
 * Servicio de aplicación para la generación de reportes
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class VehicleReportService {
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private MantenimientoRepository mantenimientoRepository;
    
    @Autowired
    private AsignacionRepository asignacionRepository;
    
    /**
     * Genera reporte de vehículos por estado
     */
    public Map<String, Object> generarReporteVehiculosPorEstado() {
        Map<String, Object> reporte = new HashMap<>();
        
        // Contar vehículos por estado
        Map<EstadoOperativo, Long> vehiculosPorEstado = new HashMap<>();
        for (EstadoOperativo estado : EstadoOperativo.values()) {
            Long count = vehicleRepository.countByEstadoOperativoAndActivoTrue(estado);
            vehiculosPorEstado.put(estado, count);
        }
        
        // Contar vehículos por tipo de maquinaria
        Map<TipoMaquinaria, Long> vehiculosPorTipo = new HashMap<>();
        for (TipoMaquinaria tipo : TipoMaquinaria.values()) {
            Long count = vehicleRepository.countByTipoMaquinariaAndActivoTrue(tipo);
            vehiculosPorTipo.put(tipo, count);
        }
        
        // Estadísticas generales
        Long totalVehiculos = vehicleRepository.countByActivoTrue();
        Long vehiculosActivos = vehicleRepository.countByActivoTrue();
        
        reporte.put("fechaGeneracion", LocalDateTime.now());
        reporte.put("totalVehiculos", totalVehiculos);
        reporte.put("vehiculosActivos", vehiculosActivos);
        reporte.put("vehiculosPorEstado", vehiculosPorEstado);
        reporte.put("vehiculosPorTipo", vehiculosPorTipo);
        
        return reporte;
    }
    
    /**
     * Genera reporte de consumo por vehículo
     */
    public Map<String, Object> generarReporteConsumoPorVehiculo() {
        Map<String, Object> reporte = new HashMap<>();
        
        List<Vehicle> vehiculos = vehicleRepository.findByActivoTrue();
        List<Map<String, Object>> consumoPorVehiculo = vehiculos.stream()
            .map(vehiculo -> {
                Map<String, Object> consumo = new HashMap<>();
                consumo.put("vehicleId", vehiculo.getId());
                consumo.put("placa", vehiculo.getPlaca());
                consumo.put("marca", vehiculo.getMarca());
                consumo.put("modelo", vehiculo.getModelo());
                consumo.put("tipoMaquinaria", vehiculo.getTipoMaquinaria().toString());
                consumo.put("consumoPromedio", vehiculo.getConsumoPromedio());
                consumo.put("kilometrajeActual", vehiculo.getKilometrajeActual());
                consumo.put("capacidadTanque", vehiculo.getCapacidadTanque());
                
                // Calcular eficiencia si hay datos suficientes
                if (vehiculo.getConsumoPromedio() != null && vehiculo.getKilometrajeActual() != null) {
                    Double eficiencia = vehiculo.getKilometrajeActual() / vehiculo.getConsumoPromedio();
                    consumo.put("eficiencia", eficiencia);
                }
                
                return consumo;
            })
            .collect(Collectors.toList());
        
        reporte.put("fechaGeneracion", LocalDateTime.now());
        reporte.put("totalVehiculos", vehiculos.size());
        reporte.put("consumoPorVehiculo", consumoPorVehiculo);
        
        return reporte;
    }
    
    /**
     * Genera reporte de historial de mantenimientos
     */
    public Map<String, Object> generarReporteHistorialMantenimientos() {
        Map<String, Object> reporte = new HashMap<>();
        
        List<Mantenimiento> mantenimientos = mantenimientoRepository.findAll()
            .stream()
            .filter(Mantenimiento::getActivo)
            .collect(Collectors.toList());
        
        // Agrupar por tipo de mantenimiento
        Map<String, Long> mantenimientosPorTipo = mantenimientos.stream()
            .collect(Collectors.groupingBy(
                Mantenimiento::getTipoMantenimiento,
                Collectors.counting()
            ));
        
        // Agrupar por estado
        Map<String, Long> mantenimientosPorEstado = mantenimientos.stream()
            .collect(Collectors.groupingBy(
                m -> m.getEstado().toString(),
                Collectors.counting()
            ));
        
        // Calcular costo total
        Double costoTotal = mantenimientos.stream()
            .filter(m -> m.getCosto() != null)
            .mapToDouble(Mantenimiento::getCosto)
            .sum();
        
        // Mantenimientos próximos a vencer (30 días)
        List<Mantenimiento> proximosAVencer = mantenimientoRepository
            .findMantenimientosProximosAVencer(LocalDateTime.now().plusDays(30));
        
        reporte.put("fechaGeneracion", LocalDateTime.now());
        reporte.put("totalMantenimientos", mantenimientos.size());
        reporte.put("mantenimientosPorTipo", mantenimientosPorTipo);
        reporte.put("mantenimientosPorEstado", mantenimientosPorEstado);
        reporte.put("costoTotal", costoTotal);
        reporte.put("proximosAVencer", proximosAVencer.size());
        reporte.put("mantenimientosProximosAVencer", proximosAVencer.stream()
            .map(this::mapMantenimientoToMap)
            .collect(Collectors.toList()));
        
        return reporte;
    }
    
    /**
     * Genera reporte de asignaciones
     */
    public Map<String, Object> generarReporteAsignaciones() {
        Map<String, Object> reporte = new HashMap<>();
        
        List<AsignacionVehiculo> asignaciones = asignacionRepository.findAll()
            .stream()
            .filter(AsignacionVehiculo::getActivo)
            .collect(Collectors.toList());
        
        // Agrupar por estado
        Map<String, Long> asignacionesPorEstado = asignaciones.stream()
            .collect(Collectors.groupingBy(
                a -> a.getEstado().toString(),
                Collectors.counting()
            ));
        
        // Asignaciones activas por chofer
        Map<Long, Long> asignacionesPorChofer = asignaciones.stream()
            .filter(a -> a.getEstado() == AsignacionVehiculo.EstadoAsignacion.ACTIVA)
            .collect(Collectors.groupingBy(
                AsignacionVehiculo::getChoferId,
                Collectors.counting()
            ));
        
        // Vehículos disponibles
        List<Vehicle> vehiculosDisponibles = asignacionRepository.findVehiculosDisponiblesParaAsignacion();
        
        reporte.put("fechaGeneracion", LocalDateTime.now());
        reporte.put("totalAsignaciones", asignaciones.size());
        reporte.put("asignacionesPorEstado", asignacionesPorEstado);
        reporte.put("asignacionesActivasPorChofer", asignacionesPorChofer);
        reporte.put("vehiculosDisponibles", vehiculosDisponibles.size());
        reporte.put("vehiculosDisponiblesDetalle", vehiculosDisponibles.stream()
            .map(this::mapVehicleToMap)
            .collect(Collectors.toList()));
        
        return reporte;
    }
    
    /**
     * Genera reporte consolidado del sistema
     */
    public Map<String, Object> generarReporteConsolidado() {
        Map<String, Object> reporte = new HashMap<>();
        
        reporte.put("fechaGeneracion", LocalDateTime.now());
        reporte.put("vehiculosPorEstado", generarReporteVehiculosPorEstado());
        reporte.put("consumoPorVehiculo", generarReporteConsumoPorVehiculo());
        reporte.put("historialMantenimientos", generarReporteHistorialMantenimientos());
        reporte.put("asignaciones", generarReporteAsignaciones());
        
        return reporte;
    }
    
    /**
     * Mapea mantenimiento a Map para reportes
     */
    private Map<String, Object> mapMantenimientoToMap(Mantenimiento mantenimiento) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", mantenimiento.getId());
        map.put("vehicleId", mantenimiento.getVehicle().getId());
        map.put("placaVehiculo", mantenimiento.getVehicle().getPlaca());
        map.put("tipoMantenimiento", mantenimiento.getTipoMantenimiento());
        map.put("fechaMantenimiento", mantenimiento.getFechaMantenimiento());
        map.put("fechaProximoMantenimiento", mantenimiento.getFechaProximoMantenimiento());
        map.put("costo", mantenimiento.getCosto());
        map.put("proveedor", mantenimiento.getProveedor());
        map.put("estado", mantenimiento.getEstado().toString());
        return map;
    }
    
    /**
     * Mapea vehículo a Map para reportes
     */
    private Map<String, Object> mapVehicleToMap(Vehicle vehicle) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", vehicle.getId());
        map.put("placa", vehicle.getPlaca());
        map.put("marca", vehicle.getMarca());
        map.put("modelo", vehicle.getModelo());
        map.put("tipoMaquinaria", vehicle.getTipoMaquinaria().toString());
        map.put("estadoOperativo", vehicle.getEstadoOperativo().toString());
        map.put("kilometrajeActual", vehicle.getKilometrajeActual());
        return map;
    }
}