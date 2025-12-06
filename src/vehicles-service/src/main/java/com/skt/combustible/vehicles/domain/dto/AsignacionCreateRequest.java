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
    private String vehicleId;
    
    @NotNull(message = "El ID del chofer es obligatorio")
    private String choferId;
    
    // La fecha es opcional, si no se proporciona se usa la fecha actual
    private LocalDateTime fechaAsignacion;
    
    private String observaciones;
    
    // Constructores
    public AsignacionCreateRequest() {}
    
    public AsignacionCreateRequest(String vehicleId, String choferId, LocalDateTime fechaAsignacion) {
        this.vehicleId = vehicleId;
        this.choferId = choferId;
        this.fechaAsignacion = fechaAsignacion;
    }
    
    // Getters y Setters
    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
    
    public String getChoferId() {
        return choferId;
    }

    public void setChoferId(String choferId) {
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
