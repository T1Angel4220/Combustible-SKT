package com.skt.combustible.vehicles.domain.dto;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de mantenimientos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
public class MantenimientoResponse {
    
    private String id;
    private String vehicleId;
    private String placaVehiculo;
    private String marcaVehiculo;
    private String modeloVehiculo;
    private String tipoMantenimiento;
    private String descripcion;
    private LocalDateTime fechaMantenimiento;
    private LocalDateTime fechaProximoMantenimiento;
    private Double costo;
    private Double kilometrajeMantenimiento;
    private String proveedor;
    private String observaciones;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;
    
    // Constructores
    public MantenimientoResponse() {}
    
    public MantenimientoResponse(String id, String vehicleId, String tipoMantenimiento, 
                                String descripcion, LocalDateTime fechaMantenimiento, 
                                String proveedor, String estado) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.tipoMantenimiento = tipoMantenimiento;
        this.descripcion = descripcion;
        this.fechaMantenimiento = fechaMantenimiento;
        this.proveedor = proveedor;
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
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
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
