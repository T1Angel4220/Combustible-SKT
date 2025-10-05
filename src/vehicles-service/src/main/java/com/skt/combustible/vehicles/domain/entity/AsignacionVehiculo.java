package com.skt.combustible.vehicles.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad AsignacionVehiculo que representa la asignación de vehículos a choferes
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Entity
@Table(name = "asignaciones_vehiculos")
public class AsignacionVehiculo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    @NotNull(message = "El vehículo es obligatorio")
    private Vehicle vehicle;
    
    @NotNull(message = "El ID del chofer es obligatorio")
    @Column(name = "chofer_id", nullable = false)
    private Long choferId;
    
    @NotNull(message = "La fecha de asignación es obligatoria")
    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;
    
    @Column(name = "fecha_desasignacion")
    private LocalDateTime fechaDesasignacion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoAsignacion estado = EstadoAsignacion.ACTIVA;
    
    @Column(name = "observaciones", length = 500)
    private String observaciones;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "activo", nullable = false)
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
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
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
