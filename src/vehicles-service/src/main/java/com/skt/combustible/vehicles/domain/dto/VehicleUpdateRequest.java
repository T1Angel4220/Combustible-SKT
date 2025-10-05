package com.skt.combustible.vehicles.domain.dto;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;

import jakarta.validation.constraints.Positive;

/**
 * DTO para la actualización de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
public class VehicleUpdateRequest {
    
    private String marca;
    
    private String modelo;
    
    @Positive(message = "El año debe ser positivo")
    private Integer anio;
    
    private TipoMaquinaria tipoMaquinaria;
    
    private EstadoOperativo estadoOperativo;
    
    private Double capacidadTanque;
    
    private Double consumoPromedio;
    
    private Double kilometrajeActual;
    
    private Boolean activo;
    
    // Constructores
    public VehicleUpdateRequest() {}
    
    // Getters y Setters
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
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
