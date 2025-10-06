package com.skt.combustible.auth.domain.entity;

import com.skt.combustible.shared.domain.enums.RolUsuario;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad Usuario para el sistema de autenticación SKT
 * Compatible con MongoDB
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Document(collection = "usuarios")
public class Usuario {
    
    @Id
    private String id;
    
    @Field("username")
    private String username;
    
    @Field("email")
    private String email;
    
    @Field("password")
    private String password;
    
    @Field("nombre")
    private String nombre;
    
    @Field("apellido")
    private String apellido;
    
    @Field("rol")
    private RolUsuario rol;
    
    @Field("activo")
    private boolean activo;
    
    @Field("fechaCreacion")
    private LocalDateTime fechaCreacion;
    
    @Field("ultimoAcceso")
    private LocalDateTime ultimoAcceso;
    
    @Field("permisos")
    private List<String> permisos;
    
    // Constructores
    public Usuario() {
        this.fechaCreacion = LocalDateTime.now();
        this.activo = true;
    }
    
    public Usuario(String username, String email, String password, String nombre, String apellido, RolUsuario rol) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
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
    
    public RolUsuario getRol() {
        return rol;
    }
    
    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }
    
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }
    
    public List<String> getPermisos() {
        return permisos;
    }
    
    public void setPermisos(List<String> permisos) {
        this.permisos = permisos;
    }
    
    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    public boolean tieneRol(RolUsuario rol) {
        return this.rol == rol;
    }
    
    public boolean tienePermiso(String permiso) {
        return permisos != null && permisos.contains(permiso);
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", rol=" + rol +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
