package com.skt.combustible.vehicles.domain.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.DBRef;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Documento Mantenimiento que representa el historial de mantenimientos de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Document(collection = "mantenimientos")
public class Mantenimiento {
    
    @Id
    private String id;
    
    @DBRef
    @NotNull(message = "El vehículo es obligatorio")
    private Vehicle vehicle;
    
    @NotBlank(message = "El tipo de mantenimiento es obligatorio")
    @Field("tipo_mantenimiento")
    private String tipoMantenimiento;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Field("descripcion")
    private String descripcion;
    
    @NotNull(message = "La fecha de mantenimiento es obligatoria")
    @Field("fecha_mantenimiento")
    private LocalDateTime fechaMantenimiento;
    
    @Field("fecha_proximo_mantenimiento")
    private LocalDateTime fechaProximoMantenimiento;
    
    @Positive(message = "El costo debe ser positivo")
    @Field("costo")
    private Double costo;
    
    @Field("kilometraje_mantenimiento")
    private Double kilometrajeMantenimiento;
    
    @NotBlank(message = "El proveedor es obligatorio")
    @Field("proveedor")
    private String proveedor;
    
    @Field("observaciones")
    private String observaciones;
    
    @Field("estado")
    private EstadoMantenimiento estado = EstadoMantenimiento.PROGRAMADO;
    
    @Field("fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Field("fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Field("activo")
    private Boolean activo = true;
    
    // Enum para estados de mantenimiento
    public enum EstadoMantenimiento {
        PROGRAMADO,
        EN_PROGRESO,
        COMPLETADO,
        CANCELADO
    }
    
    // Constructores
    public Mantenimiento() {
        this.fechaCreacion = LocalDateTime.now();
        this.activo = true;
    }
    
    public Mantenimiento(Vehicle vehicle, String tipoMantenimiento, String descripcion, 
                        LocalDateTime fechaMantenimiento, String proveedor) {
        this();
        this.vehicle = vehicle;
        this.tipoMantenimiento = tipoMantenimiento;
        this.descripcion = descripcion;
        this.fechaMantenimiento = fechaMantenimiento;
        this.proveedor = proveedor;
    }
    
    public void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
    
    public void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
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
    
    public EstadoMantenimiento getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoMantenimiento estado) {
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
