package com.skt.combustible.fuel.domain.dto;

import com.skt.combustible.shared.domain.enums.TipoMaquinaria;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para registros de consumo de combustible
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class FuelConsumptionResponse {
    
    private String id;
    private LocalDateTime fechaHora;
    private Double cantidadLitros;
    private String tipoCombustible;
    private Double precioPorLitro;
    private Double costoTotal;
    private String vehiculoId;
    private String choferId;
    private String rutaId;
    private Double lecturaOdometroHoras;
    private String observaciones;
    private TipoMaquinaria tipoMaquinaria;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Información adicional (opcional, para enriquecer la respuesta)
    private String vehiculoPlaca;
    private String choferNombre;
    private String rutaNombre;
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
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
    
    public TipoMaquinaria getTipoMaquinaria() {
        return tipoMaquinaria;
    }
    
    public void setTipoMaquinaria(TipoMaquinaria tipoMaquinaria) {
        this.tipoMaquinaria = tipoMaquinaria;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getVehiculoPlaca() {
        return vehiculoPlaca;
    }
    
    public void setVehiculoPlaca(String vehiculoPlaca) {
        this.vehiculoPlaca = vehiculoPlaca;
    }
    
    public String getChoferNombre() {
        return choferNombre;
    }
    
    public void setChoferNombre(String choferNombre) {
        this.choferNombre = choferNombre;
    }
    
    public String getRutaNombre() {
        return rutaNombre;
    }
    
    public void setRutaNombre(String rutaNombre) {
        this.rutaNombre = rutaNombre;
    }
}

