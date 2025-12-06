package com.skt.combustible.vehicles.domain.dto;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de asignaciones de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
public class AsignacionResponse {
    
    private String id;
    private String vehicleId;
    private String placaVehiculo;
    private String marcaVehiculo;
    private String modeloVehiculo;
    private String choferId;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaDesasignacion;
    private String estado;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;
    
    // Constructores
    public AsignacionResponse() {}
    
    public AsignacionResponse(String id, String vehicleId, String choferId, 
                            LocalDateTime fechaAsignacion, String estado) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.choferId = choferId;
        this.fechaAsignacion = fechaAsignacion;
        this.estado = estado;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getVehicleId() {
        return vehicleId;
    }
    
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
    
    public String getPlacaVehiculo() {
        return placaVehiculo;
    }
    
    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }
    
    public String getMarcaVehiculo() {
        return marcaVehiculo;
    }
    
    public void setMarcaVehiculo(String marcaVehiculo) {
        this.marcaVehiculo = marcaVehiculo;
    }
    
    public String getModeloVehiculo() {
        return modeloVehiculo;
    }
    
    public void setModeloVehiculo(String modeloVehiculo) {
        this.modeloVehiculo = modeloVehiculo;
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
    
    public LocalDateTime getFechaDesasignacion() {
        return fechaDesasignacion;
    }
    
    public void setFechaDesasignacion(LocalDateTime fechaDesasignacion) {
        this.fechaDesasignacion = fechaDesasignacion;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
