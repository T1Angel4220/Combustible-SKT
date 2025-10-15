package com.skt.combustible.gateway.domain.dto;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;

/**
 * DTO para crear un nuevo chofer
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class CreateDriverRequest {

    private String nombre;
    private String apellido;
    private String dni;
    private String licencia;
    private String telefono;
    private String email;
    private String fechaContratacion;
    private EstadoOperativo estado;
    private TipoMaquinaria tipoMaquinariaAsignada;

    // Constructores
    public CreateDriverRequest() {
    }

    public CreateDriverRequest(String nombre, String apellido, String dni, String licencia) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.licencia = licencia;
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

    public String getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(String fechaContratacion) {
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

    @Override
    public String toString() {
        return "CreateDriverRequest{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", dni='" + dni + '\'' +
                ", licencia='" + licencia + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", fechaContratacion='" + fechaContratacion + '\'' +
                ", estado=" + estado +
                ", tipoMaquinariaAsignada=" + tipoMaquinariaAsignada +
                '}';
    }
}
