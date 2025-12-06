package com.skt.combustible.routes.domain.dto;

import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.routes.domain.entity.Route;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO de respuesta para rutas
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class RouteResponse {
    
    private String id;
    private String codigo;
    private String nombreRuta;
    private String origen;
    private String destino;
    private Double distanciaKm;
    private Double duracionEstimadaHoras;
    private Double consumoEstimadoLitros;
    private LocalTime horaInicio;
    private String vehiculoId;
    private String placaVehiculo;
    private String marcaVehiculo;
    private String modeloVehiculo;
    private String choferId;
    private String nombreChofer;
    private String apellidoChofer;
    private TipoMaquinaria tipoMaquinaria;
    private Route.EstadoRuta estado;
    private Boolean activa;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
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
    
    public Double getConsumoEstimadoLitros() {
        return consumoEstimadoLitros;
    }
    
    public void setConsumoEstimadoLitros(Double consumoEstimadoLitros) {
        this.consumoEstimadoLitros = consumoEstimadoLitros;
    }
    
    public LocalTime getHoraInicio() {
        return horaInicio;
    }
    
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }
    
    public String getVehiculoId() {
        return vehiculoId;
    }
    
    public void setVehiculoId(String vehiculoId) {
        this.vehiculoId = vehiculoId;
    }
    
    public String getPlacaVehiculo() {
        return placaVehiculo;
    }
    
    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }
    
    public String getMarcaVehiculo() {
        return marcaVehiculo;
    }
    
    public void setMarcaVehiculo(String marcaVehiculo) {
        this.marcaVehiculo = marcaVehiculo;
    }
    
    public String getModeloVehiculo() {
        return modeloVehiculo;
    }
    
    public void setModeloVehiculo(String modeloVehiculo) {
        this.modeloVehiculo = modeloVehiculo;
    }
    
    public String getChoferId() {
        return choferId;
    }
    
    public void setChoferId(String choferId) {
        this.choferId = choferId;
    }
    
    public String getNombreChofer() {
        return nombreChofer;
    }
    
    public void setNombreChofer(String nombreChofer) {
        this.nombreChofer = nombreChofer;
    }
    
    public String getApellidoChofer() {
        return apellidoChofer;
    }
    
    public void setApellidoChofer(String apellidoChofer) {
        this.apellidoChofer = apellidoChofer;
    }
    
    public TipoMaquinaria getTipoMaquinaria() {
        return tipoMaquinaria;
    }
    
    public void setTipoMaquinaria(TipoMaquinaria tipoMaquinaria) {
        this.tipoMaquinaria = tipoMaquinaria;
    }
    
    public Route.EstadoRuta getEstado() {
        return estado;
    }
    
    public void setEstado(Route.EstadoRuta estado) {
        this.estado = estado;
    }
    
    public Boolean getActiva() {
        return activa;
    }
    
    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
    
    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public LocalDateTime getFechaFin() {
        return fechaFin;
    }
    
    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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
}

