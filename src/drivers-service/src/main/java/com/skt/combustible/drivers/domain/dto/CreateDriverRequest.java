package com.skt.combustible.drivers.domain.dto;

import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para crear un nuevo chofer
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class CreateDriverRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;
    
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8,12}$", message = "El DNI debe contener entre 8 y 12 dígitos")
    private String dni;
    
    @NotBlank(message = "La licencia es obligatoria")
    @Size(max = 50, message = "La licencia no puede exceder 50 caracteres")
    private String licencia;
    
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Formato de teléfono inválido")
    private String telefono;
    
    @Email(message = "Formato de email inválido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;
    
    private LocalDate fechaContratacion;
    
    private TipoMaquinaria tipoMaquinariaAsignada;
    
    // Constructores
    public CreateDriverRequest() {}
    
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
    
    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }
    
    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }
    
    public TipoMaquinaria getTipoMaquinariaAsignada() {
        return tipoMaquinariaAsignada;
    }
    
    public void setTipoMaquinariaAsignada(TipoMaquinaria tipoMaquinariaAsignada) {
        this.tipoMaquinariaAsignada = tipoMaquinariaAsignada;
    }
}
