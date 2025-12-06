package com.skt.combustible.routes.domain.entity;

import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Documento Route que representa una ruta en el sistema SKT
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Document(collection = "routes")
public class Route {
    
    @Id
    private String id;
    
    @NotBlank(message = "El código de ruta es obligatorio")
    @Field("codigo")
    private String codigo; // RUT-001, RUT-002, etc.
    
    @NotBlank(message = "El nombre de la ruta es obligatorio")
    @Field("nombre_ruta")
    private String nombreRuta;
    
    @NotBlank(message = "El origen es obligatorio")
    @Field("origen")
    private String origen;
    
    @Field("origen_lat")
    private Double origenLat; // Latitud del origen (opcional)
    
    @Field("origen_lng")
    private Double origenLng; // Longitud del origen (opcional)
    
    @NotBlank(message = "El destino es obligatorio")
    @Field("destino")
    private String destino;
    
    @Field("destino_lat")
    private Double destinoLat; // Latitud del destino (opcional)
    
    @Field("destino_lng")
    private Double destinoLng; // Longitud del destino (opcional)
    
    @NotNull(message = "La distancia es obligatoria")
    @Positive(message = "La distancia debe ser positiva")
    @Field("distancia_km")
    private Double distanciaKm;
    
    @Field("duracion_estimada_horas")
    private Double duracionEstimadaHoras;
    
    @Field("consumo_estimado_litros")
    private Double consumoEstimadoLitros;
    
    @Field("hora_inicio")
    private LocalTime horaInicio;
    
    @Field("vehiculo_id")
    private String vehiculoId; // Referencia al vehículo (String ID)
    
    @Field("chofer_id")
    private String choferId; // Referencia al chofer (String ID)
    
    @Field("tipo_maquinaria")
    private TipoMaquinaria tipoMaquinaria;
    
    @NotNull(message = "El estado es obligatorio")
    @Field("estado")
    private EstadoRuta estado = EstadoRuta.PENDIENTE;
    
    @Field("activa")
    private Boolean activa = true;
    
    @Field("fecha_inicio")
    private LocalDateTime fechaInicio;
    
    @Field("fecha_fin")
    private LocalDateTime fechaFin;
    
    @Field("observaciones")
    private String observaciones;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Enum para estados de ruta
     */
    public enum EstadoRuta {
        PENDIENTE,
        EN_CURSO,
        COMPLETADA,
        CANCELADA
    }
    
    // Constructores
    public Route() {
        this.activa = true;
        this.estado = EstadoRuta.PENDIENTE;
    }
    
    public Route(String codigo, String nombreRuta, String origen, String destino, Double distanciaKm) {
        this();
        this.codigo = codigo;
        this.nombreRuta = nombreRuta;
        this.origen = origen;
        this.destino = destino;
        this.distanciaKm = distanciaKm;
    }
    
    // Métodos de negocio
    public void iniciarRuta() {
        if (this.estado != EstadoRuta.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden iniciar rutas pendientes");
        }
        this.estado = EstadoRuta.EN_CURSO;
        this.fechaInicio = LocalDateTime.now();
    }
    
    public void completarRuta() {
        if (this.estado != EstadoRuta.EN_CURSO) {
            throw new IllegalStateException("Solo se pueden completar rutas en curso");
        }
        this.estado = EstadoRuta.COMPLETADA;
        this.fechaFin = LocalDateTime.now();
    }
    
    public void cancelarRuta() {
        if (this.estado == EstadoRuta.COMPLETADA) {
            throw new IllegalStateException("No se puede cancelar una ruta completada");
        }
        this.estado = EstadoRuta.CANCELADA;
        this.activa = false;
    }
    
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
    
    public Double getOrigenLat() {
        return origenLat;
    }
    
    public void setOrigenLat(Double origenLat) {
        this.origenLat = origenLat;
    }
    
    public Double getOrigenLng() {
        return origenLng;
    }
    
    public void setOrigenLng(Double origenLng) {
        this.origenLng = origenLng;
    }
    
    public Double getDestinoLat() {
        return destinoLat;
    }
    
    public void setDestinoLat(Double destinoLat) {
        this.destinoLat = destinoLat;
    }
    
    public Double getDestinoLng() {
        return destinoLng;
    }
    
    public void setDestinoLng(Double destinoLng) {
        this.destinoLng = destinoLng;
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
    
    public EstadoRuta getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoRuta estado) {
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

