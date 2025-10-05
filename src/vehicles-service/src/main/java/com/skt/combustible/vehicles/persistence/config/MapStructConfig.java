package com.skt.combustible.vehicles.persistence.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de MapStruct para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE
)
public class MapStructConfig {
    // Configuración global de MapStruct
}
