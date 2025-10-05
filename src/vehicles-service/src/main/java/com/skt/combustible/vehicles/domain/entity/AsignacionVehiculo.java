package com.skt.combustible.vehicles.domain.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.DBRef;
import jakarta.validation.constraints.NotNull;

/**
 * Documento AsignacionVehiculo que representa la asignación de vehículos a choferes
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Document(collection = "asignaciones_vehiculos")
public class AsignacionVehiculo {
    
    @Id
    private String id;
    
    @DBRef
    @NotNull(message = "El vehículo es obligatorio")
    private Vehicle vehicle;
    
    @NotNull(message = "El ID del chofer es obligatorio")
    @Field("chofer_id")
    private Long choferId;
    
    @NotNull(message = "La fecha de asignación es obligatoria")
    @Field("fecha_asignacion")
    private LocalDateTime fechaAsignacion;
    
    @Field("fecha_desasignacion")
    private LocalDateTime fechaDesasignacion;
    
    @Field("estado")
    private EstadoAsignacion estado = EstadoAsignacion.ACTIVA;
    
    @Field("observaciones")
    private String observaciones;
    
    @Field("fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Field("fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Field("activo")
    private Boolean activo = true;
    
    // Enum para estados de asignación
    public enum EstadoAsignacion {
        ACTIVA,
        FINALIZADA,
        SUSPENDIDA
    }
    
    // Constructores
    public AsignacionVehiculo() {
        this.fechaCreacion = LocalDateTime.now();
        this.activo = true;
    }
    
    public AsignacionVehiculo(Vehicle vehicle, Long choferId, LocalDateTime fechaAsignacion) {
        this();
        this.vehicle = vehicle;
        this.choferId = choferId;
        this.fechaAsignacion = fechaAsignacion;
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
    
    public LocalDateTime getFechaDesasignacion() {
        return fechaDesasignacion;
    }
    
    public void setFechaDesasignacion(LocalDateTime fechaDesasignacion) {
        this.fechaDesasignacion = fechaDesasignacion;
    }
    
    public EstadoAsignacion getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoAsignacion estado) {
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
