package com.skt.combustible.vehicles.infrastructure.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.skt.combustible.vehicles.domain.entity.Vehicle;
import com.skt.combustible.vehicles.infrastructure.metrics.VehicleMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manejador de eventos del servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Component
public class VehicleEventHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(VehicleEventHandler.class);
    
    @Autowired
    private VehicleMetrics vehicleMetrics;
    
    /**
     * Maneja eventos de creación de vehículos
     */
    @EventListener
    public void handleVehicleCreated(VehicleCreatedEvent event) {
        logger.info("Vehículo creado: {}", event.getVehicle().getPlaca());
        
        // Actualizar métricas
        updateMetrics();
    }
    
    /**
     * Maneja eventos de actualización de vehículos
     */
    @EventListener
    public void handleVehicleUpdated(VehicleUpdatedEvent event) {
        logger.info("Vehículo actualizado: {}", event.getVehicle().getPlaca());
        
        // Actualizar métricas
        updateMetrics();
    }
    
    /**
     * Maneja eventos de eliminación de vehículos
     */
    @EventListener
    public void handleVehicleDeleted(VehicleDeletedEvent event) {
        logger.info("Vehículo eliminado: {}", event.getVehicleId());
        
        // Actualizar métricas
        updateMetrics();
    }
    
    /**
     * Maneja eventos de cambio de estado de vehículos
     */
    @EventListener
    public void handleVehicleStateChanged(VehicleStateChangedEvent event) {
        logger.info("Estado del vehículo {} cambiado a: {}", 
                   event.getVehicle().getPlaca(), 
                   event.getNewState());
        
        // Actualizar métricas
        updateMetrics();
    }
    
    /**
     * Actualiza las métricas del servicio
     */
    private void updateMetrics() {
        try {
            // Actualizar métricas por tipo
            vehicleMetrics.getVehiclesByType();
            
            // Actualizar métricas por estado
            vehicleMetrics.getVehiclesByState();
            
            logger.debug("Métricas actualizadas correctamente");
        } catch (Exception e) {
            logger.error("Error al actualizar métricas: {}", e.getMessage());
        }
    }
    
    /**
     * Evento de creación de vehículo
     */
    public static class VehicleCreatedEvent {
        private final Vehicle vehicle;
        
        public VehicleCreatedEvent(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
        
        public Vehicle getVehicle() {
            return vehicle;
        }
    }
    
    /**
     * Evento de actualización de vehículo
     */
    public static class VehicleUpdatedEvent {
        private final Vehicle vehicle;
        
        public VehicleUpdatedEvent(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
        
        public Vehicle getVehicle() {
            return vehicle;
        }
    }
    
    /**
     * Evento de eliminación de vehículo
     */
    public static class VehicleDeletedEvent {
        private final Long vehicleId;
        
        public VehicleDeletedEvent(Long vehicleId) {
            this.vehicleId = vehicleId;
        }
        
        public Long getVehicleId() {
            return vehicleId;
        }
    }
    
    /**
     * Evento de cambio de estado de vehículo
     */
    public static class VehicleStateChangedEvent {
        private final Vehicle vehicle;
        private final String newState;
        
        public VehicleStateChangedEvent(Vehicle vehicle, String newState) {
            this.vehicle = vehicle;
            this.newState = newState;
        }
        
        public Vehicle getVehicle() {
            return vehicle;
        }
        
        public String getNewState() {
            return newState;
        }
    }
}
