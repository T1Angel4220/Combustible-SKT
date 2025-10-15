package com.skt.combustible.auth.domain.dto;

import com.skt.combustible.shared.domain.enums.RolUsuario;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuesta de autenticación con JWT
 * 
 * @author Sistema SKT
 * @version 1.0
 */
public class AuthResponse {

    private String token;
    private String refreshToken; // Refresh token para renovar access token
    private String tipoToken = "Bearer";
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private RolUsuario rol;
    private List<String> permisos;
    private LocalDateTime expiracion;

    // Constructores
    public AuthResponse() {
    }

    public AuthResponse(String token, String username, String email, String nombre, String apellido,
            RolUsuario rol, List<String> permisos, LocalDateTime expiracion) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;
        this.permisos = permisos;
        this.expiracion = expiracion;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public void setTipoToken(String tipoToken) {
        this.tipoToken = tipoToken;
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

    public List<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<String> permisos) {
        this.permisos = permisos;
    }

    public LocalDateTime getExpiracion() {
        return expiracion;
    }

    public void setExpiracion(LocalDateTime expiracion) {
        this.expiracion = expiracion;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='" + token + '\'' +
                ", tipoToken='" + tipoToken + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", rol=" + rol +
                ", permisos=" + permisos +
                ", expiracion=" + expiracion +
                '}';
    }
}
