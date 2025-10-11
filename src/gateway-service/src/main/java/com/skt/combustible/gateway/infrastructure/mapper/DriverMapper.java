package com.skt.combustible.gateway.infrastructure.mapper;

import com.skt.combustible.drivers.grpc.DriverResponse;
import com.skt.combustible.gateway.domain.dto.DriverRestResponse;
import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre objetos gRPC y DTOs REST
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class DriverMapper {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    /**
     * Convierte DriverResponse de gRPC a DriverRestResponse para REST
     */
    public DriverRestResponse toRestResponse(DriverResponse grpcResponse) {
        DriverRestResponse restResponse = new DriverRestResponse();
        
        restResponse.setId(grpcResponse.getId());
        restResponse.setNombre(grpcResponse.getNombre());
        restResponse.setApellido(grpcResponse.getApellido());
        restResponse.setDni(grpcResponse.getDni());
        restResponse.setLicencia(grpcResponse.getLicencia());
        
        if (!grpcResponse.getTelefono().isEmpty()) {
            restResponse.setTelefono(grpcResponse.getTelefono());
        }
        
        if (!grpcResponse.getEmail().isEmpty()) {
            restResponse.setEmail(grpcResponse.getEmail());
        }
        
        if (!grpcResponse.getFechaContratacion().isEmpty()) {
            restResponse.setFechaContratacion(parseFechaContratacion(grpcResponse.getFechaContratacion()));
        }
        
        restResponse.setEstado(mapEstadoOperativo(grpcResponse.getEstado()));
        restResponse.setActivo(grpcResponse.getActivo());
        
        if (grpcResponse.hasTipoMaquinariaAsignada()) {
            restResponse.setTipoMaquinariaAsignada(mapTipoMaquinaria(grpcResponse.getTipoMaquinariaAsignada()));
        }
        
        // Parsear fechas de auditoría
        if (!grpcResponse.getCreatedAt().isEmpty()) {
            restResponse.setCreatedAt(parseDateTime(grpcResponse.getCreatedAt()));
        }
        
        if (!grpcResponse.getUpdatedAt().isEmpty()) {
            restResponse.setUpdatedAt(parseDateTime(grpcResponse.getUpdatedAt()));
        }
        
        return restResponse;
    }
    
    /**
     * Convierte lista de DriverResponse de gRPC a lista de DriverRestResponse
     */
    public List<DriverRestResponse> toRestResponseList(List<DriverResponse> grpcResponses) {
        return grpcResponses.stream()
                .map(this::toRestResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Mapea EstadoOperativo de gRPC a enum del dominio
     */
    private EstadoOperativo mapEstadoOperativo(com.skt.combustible.drivers.grpc.EstadoOperativo estado) {
        return switch (estado) {
            // Estados para choferes
            case DISPONIBLE -> EstadoOperativo.DISPONIBLE;
            case ASIGNADO -> EstadoOperativo.ASIGNADO;
            case EN_RUTA -> EstadoOperativo.EN_RUTA;
            case DESCANSANDO -> EstadoOperativo.DESCANSANDO;
            case VACACIONES -> EstadoOperativo.VACACIONES;
            case ENFERMO -> EstadoOperativo.ENFERMO;
            case LICENCIA -> EstadoOperativo.LICENCIA;
            // Estados para vehículos (compatibilidad)
            case EN_USO -> EstadoOperativo.EN_USO;
            case MANTENIMIENTO -> EstadoOperativo.MANTENIMIENTO;
            case FUERA_SERVICIO -> EstadoOperativo.FUERA_SERVICIO;
            case RESERVADO -> EstadoOperativo.RESERVADO;
            case UNRECOGNIZED -> EstadoOperativo.DISPONIBLE;
        };
    }
    
    /**
     * Mapea TipoMaquinaria de gRPC a enum del dominio
     */
    private TipoMaquinaria mapTipoMaquinaria(com.skt.combustible.drivers.grpc.TipoMaquinaria tipo) {
        return switch (tipo) {
            case CAMION -> TipoMaquinaria.CAMION;
            case VOLQUETE -> TipoMaquinaria.VOLQUETE;
            case EXCAVADORA -> TipoMaquinaria.EXCAVADORA;
            case CARGADOR -> TipoMaquinaria.CARGADOR;
            case GRUA -> TipoMaquinaria.GRUA;
            case MOTONIVELADORA -> TipoMaquinaria.MOTONIVELADORA;
            case UNRECOGNIZED -> TipoMaquinaria.CAMION;
        };
    }
    
    /**
     * Parsea la fecha de contratación desde string
     */
    private LocalDate parseFechaContratacion(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(fechaStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Parsea fecha y hora desde string
     */
    private java.time.LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            return null;
        }
    }
}