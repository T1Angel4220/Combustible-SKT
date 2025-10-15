package com.skt.combustible.gateway.domain.dto;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;

/**
 * DTO para actualizar un chofer
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class UpdateDriverRequest {

    private String nombre;
    private String apellido;
    private String dni;
    private String licencia;
    private String email;
    private String telefono;
    private EstadoOperativo estado;
    private TipoMaquinaria tipoMaquinariaAsignada;
    private Boolean activo;

    // Constructores
    public UpdateDriverRequest() {
    }

    public UpdateDriverRequest(String nombre, String apellido, String dni, String licencia,
            String email, String telefono, EstadoOperativo estado,
            TipoMaquinaria tipoMaquinariaAsignada, Boolean activo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.licencia = licencia;
        this.email = email;
        this.telefono = telefono;
        this.estado = estado;
        this.tipoMaquinariaAsignada = tipoMaquinariaAsignada;
        this.activo = activo;
    }

    // Getters y Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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

    @Override
    public String toString() {
        return "UpdateDriverRequest{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", dni='" + dni + '\'' +
                ", licencia='" + licencia + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", estado=" + estado +
                ", tipoMaquinariaAsignada=" + tipoMaquinariaAsignada +
                ", activo=" + activo +
                '}';
    }
}
