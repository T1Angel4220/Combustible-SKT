package com.skt.combustible.drivers.domain.dto;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para información de chofer
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class DriverResponse {
    
    private String id;
    private String nombre;
    private String apellido;
    private String dni;
    private String licencia;
    private String telefono;
    private String email;
    private LocalDate fechaContratacion;
    private EstadoOperativo estado;
    private TipoMaquinaria tipoMaquinariaAsignada;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructores
    public DriverResponse() {}
    
    public DriverResponse(String id, String nombre, String apellido, String dni, String licencia) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.licencia = licencia;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    
    public String getDni() {
        return dni;
    }
    
    public void setDni(String dni) {
        this.dni = dni;
    }
    
    public String getLicencia() {
        return licencia;
    }
    
    public void setLicencia(String licencia) {
        this.licencia = licencia;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }
    
    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }
    
    public EstadoOperativo getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoOperativo estado) {
        this.estado = estado;
    }
    
    public TipoMaquinaria getTipoMaquinariaAsignada() {
        return tipoMaquinariaAsignada;
    }
    
    public void setTipoMaquinariaAsignada(TipoMaquinaria tipoMaquinariaAsignada) {
        this.tipoMaquinariaAsignada = tipoMaquinariaAsignada;
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
    
    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    public boolean isDisponible() {
        return activo && estado == EstadoOperativo.ACTIVO;
    }
}
