package com.skt.combustible.vehicles.domain.dto;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para la creación de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
public class VehicleCreateRequest {
    
    @NotBlank(message = "La placa es obligatoria")
    private String placa;
    
    @NotBlank(message = "La marca es obligatoria")
    private String marca;
    
    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;
    
    @NotNull(message = "El año es obligatorio")
    @Positive(message = "El año debe ser positivo")
    private Integer anio;
    
    @NotNull(message = "El tipo de maquinaria es obligatorio")
    private TipoMaquinaria tipoMaquinaria;
    
    @NotNull(message = "El estado operativo es obligatorio")
    private EstadoOperativo estadoOperativo;
    
    private Double capacidadTanque;
    
    private Double consumoPromedio;
    
    private Double kilometrajeActual;
    
    // Constructores
    public VehicleCreateRequest() {}
    
    public VehicleCreateRequest(String placa, String marca, String modelo, Integer anio, 
                               TipoMaquinaria tipoMaquinaria, EstadoOperativo estadoOperativo) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.tipoMaquinaria = tipoMaquinaria;
        this.estadoOperativo = estadoOperativo;
    }
    
    // Getters y Setters
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
}
