package com.skt.combustible.shared.domain.enums;

/**
 * Enumeración de tipos de maquinaria en el sistema SKT
 * 
 * @author Sistema SKT
 * @version 1.0
 */
public enum TipoMaquinaria {
    CAMION("Camión"),
    VOLQUETE("Volquete"),
    EXCAVADORA("Excavadora"),
    CARGADOR("Cargador"),
    GRUA("Grúa"),
    MOTONIVELADORA("Motoniveladora");

    private final String descripcion;

    TipoMaquinaria(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
