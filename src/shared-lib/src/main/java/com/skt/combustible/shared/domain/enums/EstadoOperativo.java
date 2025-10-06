package com.skt.combustible.shared.domain.enums;

/**
 * Enumeración de estados operativos de vehículos en el sistema SKT
 * 
 * @author Sistema SKT
 * @version 1.0
 */
public enum EstadoOperativo {
    ACTIVO("Activo"),
    MANTENIMIENTO("En Mantenimiento"),
    FUERA_SERVICIO("Fuera de Servicio"),
    DISPONIBLE("Disponible"),
    EN_USO("En Uso"),
    RESERVADO("Reservado");

    private final String descripcion;

    EstadoOperativo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
