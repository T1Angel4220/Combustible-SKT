package com.skt.combustible.vehicles.domain.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para la creación de mantenimientos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
public class MantenimientoCreateRequest {
    
    @NotNull(message = "El ID del vehículo es obligatorio")
    private Long vehicleId;
    
    @NotBlank(message = "El tipo de mantenimiento es obligatorio")
    private String tipoMantenimiento;
    
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
    
    @NotNull(message = "La fecha de mantenimiento es obligatoria")
    private LocalDateTime fechaMantenimiento;
    
    private LocalDateTime fechaProximoMantenimiento;
    
    @Positive(message = "El costo debe ser positivo")
    private Double costo;
    
    private Double kilometrajeMantenimiento;
    
    @NotBlank(message = "El proveedor es obligatorio")
    private String proveedor;
    
    private String observaciones;
    
    // Constructores
    public MantenimientoCreateRequest() {}
    
    public MantenimientoCreateRequest(Long vehicleId, String tipoMantenimiento, String descripcion, 
                                     LocalDateTime fechaMantenimiento, String proveedor) {
        this.vehicleId = vehicleId;
        this.tipoMantenimiento = tipoMantenimiento;
        this.descripcion = descripcion;
        this.fechaMantenimiento = fechaMantenimiento;
        this.proveedor = proveedor;
    }
    
    // Getters y Setters
    public Long getVehicleId() {
        return vehicleId;
    }
    
    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
    
    public String getTipoMantenimiento() {
        return tipoMantenimiento;
    }
    
    public void setTipoMantenimiento(String tipoMantenimiento) {
        this.tipoMantenimiento = tipoMantenimiento;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public LocalDateTime getFechaMantenimiento() {
        return fechaMantenimiento;
    }
    
    public void setFechaMantenimiento(LocalDateTime fechaMantenimiento) {
        this.fechaMantenimiento = fechaMantenimiento;
    }
    
    public LocalDateTime getFechaProximoMantenimiento() {
        return fechaProximoMantenimiento;
    }
    
    public void setFechaProximoMantenimiento(LocalDateTime fechaProximoMantenimiento) {
        this.fechaProximoMantenimiento = fechaProximoMantenimiento;
    }
    
    public Double getCosto() {
        return costo;
    }
    
    public void setCosto(Double costo) {
        this.costo = costo;
    }
    
    public Double getKilometrajeMantenimiento() {
        return kilometrajeMantenimiento;
    }
    
    public void setKilometrajeMantenimiento(Double kilometrajeMantenimiento) {
        this.kilometrajeMantenimiento = kilometrajeMantenimiento;
    }
    
    public String getProveedor() {
        return proveedor;
    }
    
    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
