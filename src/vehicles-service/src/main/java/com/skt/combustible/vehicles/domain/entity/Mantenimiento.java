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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Entidad Mantenimiento que representa el historial de mantenimientos de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Entity
@Table(name = "mantenimientos")
public class Mantenimiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    @NotNull(message = "El vehículo es obligatorio")
    private Vehicle vehicle;
    
    @NotBlank(message = "El tipo de mantenimiento es obligatorio")
    @Column(name = "tipo_mantenimiento", nullable = false, length = 50)
    private String tipoMantenimiento;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;
    
    @NotNull(message = "La fecha de mantenimiento es obligatoria")
    @Column(name = "fecha_mantenimiento", nullable = false)
    private LocalDateTime fechaMantenimiento;
    
    @Column(name = "fecha_proximo_mantenimiento")
    private LocalDateTime fechaProximoMantenimiento;
    
    @Positive(message = "El costo debe ser positivo")
    @Column(name = "costo")
    private Double costo;
    
    @Column(name = "kilometraje_mantenimiento")
    private Double kilometrajeMantenimiento;
    
    @NotBlank(message = "El proveedor es obligatorio")
    @Column(name = "proveedor", nullable = false, length = 100)
    private String proveedor;
    
    @Column(name = "observaciones", length = 1000)
    private String observaciones;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoMantenimiento estado = EstadoMantenimiento.PROGRAMADO;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "activo", nullable = false)
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
