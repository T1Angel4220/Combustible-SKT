package com.skt.combustible.vehicles.domain.entity;

import java.time.LocalDateTime;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Entidad Vehicle que representa un vehículo en el sistema SKT
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Entity
@Table(name = "vehicles")
public class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "La placa es obligatoria")
    @Column(name = "placa", unique = true, nullable = false, length = 10)
    private String placa;
    
    @NotBlank(message = "La marca es obligatoria")
    @Column(name = "marca", nullable = false, length = 50)
    private String marca;
    
    @NotBlank(message = "El modelo es obligatorio")
    @Column(name = "modelo", nullable = false, length = 50)
    private String modelo;
    
    @NotNull(message = "El año es obligatorio")
    @Positive(message = "El año debe ser positivo")
    @Column(name = "anio", nullable = false)
    private Integer anio;
    
    @NotNull(message = "El tipo de maquinaria es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_maquinaria", nullable = false)
    private TipoMaquinaria tipoMaquinaria;
    
    @NotNull(message = "El estado operativo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operativo", nullable = false)
    private EstadoOperativo estadoOperativo;
    
    @Column(name = "capacidad_tanque")
    private Double capacidadTanque;
    
    @Column(name = "consumo_promedio")
    private Double consumoPromedio;
    
    @Column(name = "kilometraje_actual")
    private Double kilometrajeActual;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    
    // Constructores
    public Vehicle() {
        this.fechaCreacion = LocalDateTime.now();
        this.activo = true;
    }
    
    public Vehicle(String placa, String marca, String modelo, Integer anio, 
                   TipoMaquinaria tipoMaquinaria, EstadoOperativo estadoOperativo) {
        this();
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.tipoMaquinaria = tipoMaquinaria;
        this.estadoOperativo = estadoOperativo;
    }
    
    // Métodos de negocio
    public void actualizarKilometraje(Double nuevoKilometraje) {
        if (nuevoKilometraje != null && nuevoKilometraje > this.kilometrajeActual) {
            this.kilometrajeActual = nuevoKilometraje;
            this.fechaActualizacion = LocalDateTime.now();
        }
    }
    
    public void cambiarEstadoOperativo(EstadoOperativo nuevoEstado) {
        this.estadoOperativo = nuevoEstado;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public void desactivar() {
        this.activo = false;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public boolean esLiviano() {
        return this.tipoMaquinaria == TipoMaquinaria.CAMION || 
               this.tipoMaquinaria == TipoMaquinaria.VOLQUETE;
    }
    
    public boolean esPesado() {
        return this.tipoMaquinaria == TipoMaquinaria.EXCAVADORA || 
               this.tipoMaquinaria == TipoMaquinaria.CARGADOR ||
               this.tipoMaquinaria == TipoMaquinaria.GRUA ||
               this.tipoMaquinaria == TipoMaquinaria.MOTONIVELADORA;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPlaca() {
        return placa;
    }
    
    public void setPlaca(String placa) {
        this.placa = placa;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public Integer getAnio() {
        return anio;
    }
    
    public void setAnio(Integer anio) {
        this.anio = anio;
    }
    
    public TipoMaquinaria getTipoMaquinaria() {
        return tipoMaquinaria;
    }
    
    public void setTipoMaquinaria(TipoMaquinaria tipoMaquinaria) {
        this.tipoMaquinaria = tipoMaquinaria;
    }
    
    public EstadoOperativo getEstadoOperativo() {
        return estadoOperativo;
    }
    
    public void setEstadoOperativo(EstadoOperativo estadoOperativo) {
        this.estadoOperativo = estadoOperativo;
    }
    
    public Double getCapacidadTanque() {
        return capacidadTanque;
    }
    
    public void setCapacidadTanque(Double capacidadTanque) {
        this.capacidadTanque = capacidadTanque;
    }
    
    public Double getConsumoPromedio() {
        return consumoPromedio;
    }
    
    public void setConsumoPromedio(Double consumoPromedio) {
        this.consumoPromedio = consumoPromedio;
    }
    
    public Double getKilometrajeActual() {
        return kilometrajeActual;
    }
    
    public void setKilometrajeActual(Double kilometrajeActual) {
        this.kilometrajeActual = kilometrajeActual;
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
    
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
