package com.skt.combustible.drivers.domain.dto;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;

import java.util.List;

/**
 * DTO para consultas de disponibilidad de choferes
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class DriverAvailabilityRequest {
    
    private TipoMaquinaria tipoMaquinaria;
    private EstadoOperativo estado;
    private Boolean soloActivos;
    
    // Constructores
    public DriverAvailabilityRequest() {}
    
    public DriverAvailabilityRequest(TipoMaquinaria tipoMaquinaria) {
        this.tipoMaquinaria = tipoMaquinaria;
        this.soloActivos = true;
    }
    
    // Getters y Setters
    public TipoMaquinaria getTipoMaquinaria() {
        return tipoMaquinaria;
    }
    
    public void setTipoMaquinaria(TipoMaquinaria tipoMaquinaria) {
        this.tipoMaquinaria = tipoMaquinaria;
    }
    
    public EstadoOperativo getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoOperativo estado) {
        this.estado = estado;
    }
    
    public Boolean getSoloActivos() {
        return soloActivos;
    }
    
    public void setSoloActivos(Boolean soloActivos) {
        this.soloActivos = soloActivos;
    }
}

/**
 * DTO de respuesta para disponibilidad de choferes
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
class DriverAvailabilityResponse {
    
    private List<DriverResponse> choferesDisponibles;
    private int totalDisponibles;
    private TipoMaquinaria tipoMaquinariaFiltrada;
    
    // Constructores
    public DriverAvailabilityResponse() {}
    
    public DriverAvailabilityResponse(List<DriverResponse> choferesDisponibles, int totalDisponibles) {
        this.choferesDisponibles = choferesDisponibles;
        this.totalDisponibles = totalDisponibles;
    }
    
    // Getters y Setters
    public List<DriverResponse> getChoferesDisponibles() {
        return choferesDisponibles;
    }
    
    public void setChoferesDisponibles(List<DriverResponse> choferesDisponibles) {
        this.choferesDisponibles = choferesDisponibles;
    }
    
    public int getTotalDisponibles() {
        return totalDisponibles;
    }
    
    public void setTotalDisponibles(int totalDisponibles) {
        this.totalDisponibles = totalDisponibles;
    }
    
    public TipoMaquinaria getTipoMaquinariaFiltrada() {
        return tipoMaquinariaFiltrada;
    }
    
    public void setTipoMaquinariaFiltrada(TipoMaquinaria tipoMaquinariaFiltrada) {
        this.tipoMaquinariaFiltrada = tipoMaquinariaFiltrada;
    }
}
