package com.skt.combustible.fuel.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * DTO para crear un nuevo registro de consumo de combustible
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class FuelConsumptionCreateRequest {
    
    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime fechaHora;
    
    @NotNull(message = "La cantidad de litros es obligatoria")
    @Positive(message = "La cantidad de litros debe ser positiva")
    private Double cantidadLitros;
    
    @NotBlank(message = "El tipo de combustible es obligatorio")
    private String tipoCombustible; // DIESEL, GASOLINA, GAS, ELECTRICO
    
    private Double precioPorLitro;
    
    private Double costoTotal;
    
    @NotBlank(message = "El ID del vehículo es obligatorio")
    private String vehiculoId;
    
    @NotBlank(message = "El ID del chofer es obligatorio")
    private String choferId;
    
    private String rutaId; // Opcional
    
    private Double lecturaOdometroHoras;
    
    private String observaciones;
    
    // Getters y Setters
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public Double getCantidadLitros() {
        return cantidadLitros;
    }
    
    public void setCantidadLitros(Double cantidadLitros) {
        this.cantidadLitros = cantidadLitros;
    }
    
    public String getTipoCombustible() {
        return tipoCombustible;
    }
    
    public void setTipoCombustible(String tipoCombustible) {
        this.tipoCombustible = tipoCombustible;
    }
    
    public Double getPrecioPorLitro() {
        return precioPorLitro;
    }
    
    public void setPrecioPorLitro(Double precioPorLitro) {
        this.precioPorLitro = precioPorLitro;
    }
    
    public Double getCostoTotal() {
        return costoTotal;
    }
    
    public void setCostoTotal(Double costoTotal) {
        this.costoTotal = costoTotal;
    }
    
    public String getVehiculoId() {
        return vehiculoId;
    }
    
    public void setVehiculoId(String vehiculoId) {
        this.vehiculoId = vehiculoId;
    }
    
    public String getChoferId() {
        return choferId;
    }
    
    public void setChoferId(String choferId) {
        this.choferId = choferId;
    }
    
    public String getRutaId() {
        return rutaId;
    }
    
    public void setRutaId(String rutaId) {
        this.rutaId = rutaId;
    }
    
    public Double getLecturaOdometroHoras() {
        return lecturaOdometroHoras;
    }
    
    public void setLecturaOdometroHoras(Double lecturaOdometroHoras) {
        this.lecturaOdometroHoras = lecturaOdometroHoras;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}

