package com.skt.combustible.vehicles.domain.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para la creación de asignaciones de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
public class AsignacionCreateRequest {
    
    @NotNull(message = "El ID del vehículo es obligatorio")
    private Long vehicleId;
    
    @NotNull(message = "El ID del chofer es obligatorio")
    private Long choferId;
    
    @NotNull(message = "La fecha de asignación es obligatoria")
    private LocalDateTime fechaAsignacion;
    
    private String observaciones;
    
    // Constructores
    public AsignacionCreateRequest() {}
    
    public AsignacionCreateRequest(Long vehicleId, Long choferId, LocalDateTime fechaAsignacion) {
        this.vehicleId = vehicleId;
        this.choferId = choferId;
        this.fechaAsignacion = fechaAsignacion;
    }
    
    // Getters y Setters
    public Long getVehicleId() {
        return vehicleId;
    }
    
    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
    
    public Long getChoferId() {
        return choferId;
    }
    
    public void setChoferId(Long choferId) {
        this.choferId = choferId;
    }
    
    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }
    
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
