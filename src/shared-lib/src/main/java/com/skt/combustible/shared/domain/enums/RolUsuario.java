package com.skt.combustible.shared.domain.enums;

/**
 * Enumeración de roles de usuario en el sistema SKT
 * 
 * @author Sistema SKT
 * @version 1.0
 */
public enum RolUsuario {
    ADMIN("Administrador"),
    OPERADOR("Operador"),
    SUPERVISOR("Supervisor"),
    CONDUCTOR("Conductor");

    private final String descripcion;

    RolUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
