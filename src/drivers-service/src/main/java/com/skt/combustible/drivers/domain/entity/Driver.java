package com.skt.combustible.drivers.domain.entity;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Documento que representa un chofer en el sistema SKT
 * Mapea con la colección drivers en MongoDB
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Document(collection = "drivers")
public class Driver {

    @Id
    private String id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Field("nombre")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    @Field("apellido")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8,12}$", message = "El DNI debe contener entre 8 y 12 dígitos")
    @Field("dni")
    private String dni;

    @NotBlank(message = "La licencia es obligatoria")
    @Size(max = 50, message = "La licencia no puede exceder 50 caracteres")
    @Field("licencia")
    private String licencia;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Formato de teléfono inválido")
    @Field("telefono")
    private String telefono;

    @Email(message = "Formato de email inválido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Field("email")
    private String email;

    @Field("fecha_contratacion")
    private LocalDate fechaContratacion;

    @Field("estado")
    private EstadoOperativo estado = EstadoOperativo.DISPONIBLE;

    @Field("tipo_maquinaria_asignada")
    private TipoMaquinaria tipoMaquinariaAsignada;

    @Field("activo")
    private Boolean activo = true;

    @Field("usuario_id")
    private String usuarioId; // ID del usuario asociado en auth-service

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    // Constructores
    public Driver() {
    }

    public Driver(String nombre, String apellido, String dni, String licencia) {
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

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
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
        return activo && estado == EstadoOperativo.DISPONIBLE;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", dni='" + dni + '\'' +
                ", licencia='" + licencia + '\'' +
                ", estado=" + estado +
                ", tipoMaquinariaAsignada=" + tipoMaquinariaAsignada +
                ", activo=" + activo +
                '}';
    }
}
