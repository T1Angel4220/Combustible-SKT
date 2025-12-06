package com.skt.combustible.routes.domain.dto;

import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;

/**
 * DTO para crear una nueva ruta
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class RouteCreateRequest {
    
    @NotBlank(message = "El nombre de la ruta es obligatorio")
    private String nombreRuta;
    
    @NotBlank(message = "El origen es obligatorio")
    private String origen;
    
    @NotBlank(message = "El destino es obligatorio")
    private String destino;
    
    @NotNull(message = "La distancia es obligatoria")
    @Positive(message = "La distancia debe ser positiva")
    private Double distanciaKm;
    
    private Double duracionEstimadaHoras;
    
    private String vehiculoId;
    
    private String choferId;
    
    private TipoMaquinaria tipoMaquinaria;
    
    private LocalTime horaInicio;
    
    private String observaciones;
    
    // Getters y Setters
    public String getNombreRuta() {
        return nombreRuta;
    }
    
    public void setNombreRuta(String nombreRuta) {
        this.nombreRuta = nombreRuta;
    }
    
    public String getOrigen() {
        return origen;
    }
    
    public void setOrigen(String origen) {
        this.origen = origen;
    }
    
    public String getDestino() {
        return destino;
    }
    
    public void setDestino(String destino) {
        this.destino = destino;
    }
    
    public Double getDistanciaKm() {
        return distanciaKm;
    }
    
    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }
    
    public Double getDuracionEstimadaHoras() {
        return duracionEstimadaHoras;
    }
    
    public void setDuracionEstimadaHoras(Double duracionEstimadaHoras) {
        this.duracionEstimadaHoras = duracionEstimadaHoras;
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
    
    public TipoMaquinaria getTipoMaquinaria() {
        return tipoMaquinaria;
    }
    
    public void setTipoMaquinaria(TipoMaquinaria tipoMaquinaria) {
        this.tipoMaquinaria = tipoMaquinaria;
    }
    
    public LocalTime getHoraInicio() {
        return horaInicio;
    }
    
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}

