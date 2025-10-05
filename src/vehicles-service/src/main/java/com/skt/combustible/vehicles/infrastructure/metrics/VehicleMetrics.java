package com.skt.combustible.vehicles.infrastructure.metrics;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.skt.combustible.vehicles.domain.repository.VehicleRepository;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.shared.domain.enums.EstadoOperativo;

import java.util.Map;
import java.util.HashMap;

/**
 * Métricas del servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Component
public class VehicleMetrics {
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Obtiene métricas de vehículos por tipo
     */
    public Map<TipoMaquinaria, Long> getVehiclesByType() {
        Map<TipoMaquinaria, Long> metrics = new HashMap<>();
        
        for (TipoMaquinaria tipo : TipoMaquinaria.values()) {
            long count = vehicleRepository.countByTipoMaquinaria(tipo);
            metrics.put(tipo, count);
        }
        
        return metrics;
    }
    
    /**
     * Obtiene métricas de vehículos por estado
     */
    public Map<EstadoOperativo, Long> getVehiclesByState() {
        Map<EstadoOperativo, Long> metrics = new HashMap<>();
        
        for (EstadoOperativo estado : EstadoOperativo.values()) {
            long count = vehicleRepository.countByEstadoOperativo(estado);
            metrics.put(estado, count);
        }
        
        return metrics;
    }
    
    /**
     * Obtiene métricas de vehículos disponibles
     */
    public long getAvailableVehicles() {
        return vehicleRepository.countByEstadoOperativo(EstadoOperativo.DISPONIBLE);
    }
    
    /**
     * Obtiene métricas de vehículos en uso
     */
    public long getVehiclesInUse() {
        return vehicleRepository.countByEstadoOperativo(EstadoOperativo.EN_USO);
    }
    
    /**
     * Obtiene métricas de vehículos en mantenimiento
     */
    public long getVehiclesInMaintenance() {
        return vehicleRepository.countByEstadoOperativo(EstadoOperativo.MANTENIMIENTO);
    }
    
    /**
     * Obtiene métricas de vehículos fuera de servicio
     */
    public long getVehiclesOutOfService() {
        return vehicleRepository.countByEstadoOperativo(EstadoOperativo.FUERA_SERVICIO);
    }
    
    /**
     * Obtiene métricas de vehículos activos
     */
    public long getActiveVehicles() {
        return vehicleRepository.findByActivoTrue().size();
    }
    
    /**
     * Obtiene métricas de vehículos inactivos
     */
    public long getInactiveVehicles() {
        return vehicleRepository.findByActivoFalse().size();
    }
    
    /**
     * Obtiene métricas de vehículos por capacidad de tanque
     */
    public Map<String, Long> getVehiclesByTankCapacity() {
        Map<String, Long> metrics = new HashMap<>();
        
        // Implementar lógica de métricas por capacidad de tanque
        metrics.put("0-50L", 0L);
        metrics.put("51-100L", 0L);
        metrics.put("101-200L", 0L);
        metrics.put("200L+", 0L);
        
        return metrics;
    }
    
    /**
     * Obtiene métricas de vehículos por kilometraje
     */
    public Map<String, Long> getVehiclesByMileage() {
        Map<String, Long> metrics = new HashMap<>();
        
        // Implementar lógica de métricas por kilometraje
        metrics.put("0-10K km", 0L);
        metrics.put("10K-50K km", 0L);
        metrics.put("50K-100K km", 0L);
        metrics.put("100K+ km", 0L);
        
        return metrics;
    }
}
