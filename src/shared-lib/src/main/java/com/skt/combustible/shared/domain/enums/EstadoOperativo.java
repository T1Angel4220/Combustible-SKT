package com.skt.combustible.shared.domain.enums;

/**
 * Enumeración de estados operativos para choferes y vehículos en el sistema SKT
 * 
 * Estados apropiados para choferes: DISPONIBLE, ASIGNADO, EN_RUTA, DESCANSANDO,
 * VACACIONES, ENFERMO, LICENCIA
 * Estados apropiados para vehículos: DISPONIBLE, EN_USO, MANTENIMIENTO,
 * FUERA_SERVICIO, RESERVADO
 * 
 * @author Sistema SKT
 * @version 2.0
 */
public enum EstadoOperativo {
    // Estados para choferes (principales)
    DISPONIBLE("Disponible"),
    ASIGNADO("Asignado"),
    EN_RUTA("En Ruta"),
    DESCANSANDO("Descansando"),
    VACACIONES("En Vacaciones"),
    ENFERMO("Enfermo"),
    LICENCIA("En Licencia"),

    // Estados para vehículos (compatibilidad)
    EN_USO("En Uso"),
    MANTENIMIENTO("En Mantenimiento"),
    FUERA_SERVICIO("Fuera de Servicio"),
    RESERVADO("Reservado");

    private final String descripcion;

    EstadoOperativo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Verifica si el estado es apropiado para choferes
     */
    public boolean isApropiadoParaChoferes() {
        return switch (this) {
            case DISPONIBLE, ASIGNADO, EN_RUTA, DESCANSANDO, VACACIONES, ENFERMO, LICENCIA -> true;
            case EN_USO, MANTENIMIENTO, FUERA_SERVICIO, RESERVADO -> false;
        };
    }

    /**
     * Verifica si el estado es apropiado para vehículos
     */
    public boolean isApropiadoParaVehiculos() {
        return switch (this) {
            case DISPONIBLE, EN_USO, MANTENIMIENTO, FUERA_SERVICIO, RESERVADO -> true;
            case ASIGNADO, EN_RUTA, DESCANSANDO, VACACIONES, ENFERMO, LICENCIA -> false;
        };
    }

    /**
     * Verifica si el chofer está en servicio activo (Asignado o En Ruta)
     */
    public boolean isEnServicio() {
        return this == ASIGNADO || this == EN_RUTA;
    }
}
