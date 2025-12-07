package com.skt.combustible.fuel.domain.entity;

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

/**
 * Documento FuelConsumption que representa un registro de consumo de combustible
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Document(collection = "fuel_consumptions")
public class FuelConsumption {
    
    @Id
    private String id;
    
    @NotNull(message = "La fecha y hora son obligatorias")
    @Field("fecha_hora")
    private LocalDateTime fechaHora;
    
    @NotNull(message = "La cantidad de litros es obligatoria")
    @Positive(message = "La cantidad de litros debe ser positiva")
    @Field("cantidad_litros")
    private Double cantidadLitros;
    
    @NotBlank(message = "El tipo de combustible es obligatorio")
    @Field("tipo_combustible")
    private String tipoCombustible; // DIESEL, GASOLINA, GAS, ELECTRICO
    
    @Field("precio_por_litro")
    private Double precioPorLitro;
    
    @Field("costo_total")
    private Double costoTotal;
    
    @NotBlank(message = "El ID del vehículo es obligatorio")
    @Field("vehiculo_id")
    private String vehiculoId;
    
    @NotBlank(message = "El ID del chofer es obligatorio")
    @Field("chofer_id")
    private String choferId;
    
    @Field("ruta_id")
    private String rutaId; // Opcional, puede ser null si no está asociado a una ruta
    
    @Field("lectura_odometro_horas")
    private Double lecturaOdometroHoras;
    
    @Field("observaciones")
    private String observaciones;
    
    @Field("tipo_maquinaria")
    private TipoMaquinaria tipoMaquinaria; // Para facilitar reportes y filtros
    
    @Field("activo")
    private Boolean activo = true;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    // Constructores
    public FuelConsumption() {
        this.activo = true;
        this.fechaHora = LocalDateTime.now();
    }
    
    public FuelConsumption(Double cantidadLitros, String tipoCombustible, String vehiculoId, String choferId) {
        this();
        this.cantidadLitros = cantidadLitros;
        this.tipoCombustible = tipoCombustible;
        this.vehiculoId = vehiculoId;
        this.choferId = choferId;
    }
    
    // Métodos de negocio
    /**
     * Calcula el costo total si se proporciona el precio por litro
     */
    public void calcularCostoTotal() {
        if (precioPorLitro != null && precioPorLitro > 0 && cantidadLitros != null && cantidadLitros > 0) {
            this.costoTotal = precioPorLitro * cantidadLitros;
        }
    }
    
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
        // Recalcular costo total si hay precio por litro
        if (precioPorLitro != null && precioPorLitro > 0) {
            calcularCostoTotal();
        }
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
        // Recalcular costo total
        calcularCostoTotal();
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
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
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

