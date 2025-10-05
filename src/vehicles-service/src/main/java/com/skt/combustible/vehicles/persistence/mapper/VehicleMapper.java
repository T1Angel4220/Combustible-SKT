package com.skt.combustible.vehicles.persistence.mapper;

import com.skt.combustible.vehicles.domain.dto.VehicleCreateRequest;
import com.skt.combustible.vehicles.domain.dto.VehicleDTO;
import com.skt.combustible.vehicles.domain.dto.VehicleResponse;
import com.skt.combustible.vehicles.domain.dto.VehicleUpdateRequest;
import com.skt.combustible.vehicles.domain.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper para conversiones entre entidades y DTOs del servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VehicleMapper {
    
    /**
     * Convierte VehicleCreateRequest a Vehicle
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", constant = "true")
    Vehicle toEntity(VehicleCreateRequest request);
    
    /**
     * Convierte Vehicle a VehicleResponse
     */
    VehicleResponse toResponse(Vehicle vehicle);
    
    /**
     * Convierte Vehicle a VehicleDTO
     */
    VehicleDTO toDTO(Vehicle vehicle);
    
    /**
     * Convierte VehicleDTO a Vehicle
     */
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    Vehicle toEntity(VehicleDTO dto);
    
    /**
     * Actualiza una entidad Vehicle con datos de VehicleUpdateRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "placa", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    void updateEntity(@MappingTarget Vehicle vehicle, VehicleUpdateRequest request);
    
    /**
     * Convierte Vehicle a VehicleCreateRequest
     */
    VehicleCreateRequest toCreateRequest(Vehicle vehicle);
    
    /**
     * Convierte Vehicle a VehicleUpdateRequest
     */
    VehicleUpdateRequest toUpdateRequest(Vehicle vehicle);
}
