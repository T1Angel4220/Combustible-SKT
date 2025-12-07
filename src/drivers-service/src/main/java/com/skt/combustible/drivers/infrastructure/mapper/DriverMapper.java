package com.skt.combustible.drivers.infrastructure.mapper;

import com.skt.combustible.drivers.domain.dto.CreateDriverRequest;
import com.skt.combustible.drivers.domain.dto.DriverResponse;
import com.skt.combustible.drivers.domain.dto.UpdateDriverRequest;
import com.skt.combustible.drivers.domain.entity.Driver;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper para conversión entre entidades Driver y DTOs usando MapStruct
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface DriverMapper {

    /**
     * Convierte CreateDriverRequest a Driver
     * 
     * @param request Request de creación
     * @return Entidad Driver
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", constant = "DISPONIBLE")
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    Driver toEntity(CreateDriverRequest request);

    /**
     * Convierte Driver a DriverResponse
     * 
     * @param driver Entidad Driver
     * @return DriverResponse
     */
    DriverResponse toResponse(Driver driver);

    /**
     * Convierte lista de Driver a lista de DriverResponse
     * 
     * @param drivers Lista de entidades Driver
     * @return Lista de DriverResponse
     */
    List<DriverResponse> toResponseList(List<Driver> drivers);

    /**
     * Actualiza un Driver existente con datos de UpdateDriverRequest
     * 
     * @param request Request de actualización
     * @param driver  Entidad Driver existente
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "usuarioId", source = "usuarioId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateFromRequest(UpdateDriverRequest request, @MappingTarget Driver driver);

    /**
     * Convierte UpdateDriverRequest a Driver (para casos especiales)
     * 
     * @param request Request de actualización
     * @return Entidad Driver parcial
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Driver toEntityFromUpdate(UpdateDriverRequest request);
}
