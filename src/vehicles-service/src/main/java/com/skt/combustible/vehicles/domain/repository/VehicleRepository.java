package com.skt.combustible.vehicles.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.domain.entity.Vehicle;

/**
 * Repositorio para la entidad Vehicle
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    
    /**
     * Busca un vehículo por placa
     */
    Optional<Vehicle> findByPlaca(String placa);
    
    /**
     * Busca vehículos por tipo de maquinaria
     */
    List<Vehicle> findByTipoMaquinaria(TipoMaquinaria tipoMaquinaria);
    
    /**
     * Busca vehículos por estado operativo
     */
    List<Vehicle> findByEstadoOperativo(EstadoOperativo estadoOperativo);
    
    /**
     * Busca vehículos activos
     */
    List<Vehicle> findByActivoTrue();
    
    /**
     * Busca vehículos inactivos
     */
    List<Vehicle> findByActivoFalse();
    
    /**
     * Busca vehículos por tipo de maquinaria y estado operativo
     */
    List<Vehicle> findByTipoMaquinariaAndEstadoOperativo(TipoMaquinaria tipoMaquinaria, EstadoOperativo estadoOperativo);
    
    /**
     * Busca vehículos disponibles (activos y en estado DISPONIBLE)
     */
    @Query("{ 'activo': true, 'estadoOperativo': 'DISPONIBLE' }")
    List<Vehicle> findVehiclesDisponibles();

    /**
     * Busca vehículos disponibles por tipo de maquinaria
     */
    @Query("{ 'activo': true, 'estadoOperativo': 'DISPONIBLE', 'tipoMaquinaria': ?0 }")
    List<Vehicle> findVehiclesDisponiblesByTipo(TipoMaquinaria tipoMaquinaria);

    /**
     * Busca vehículos en uso
     */
    @Query("{ 'activo': true, 'estadoOperativo': 'EN_USO' }")
    List<Vehicle> findVehiclesEnUso();

    /**
     * Busca vehículos en mantenimiento
     */
    @Query("{ 'activo': true, 'estadoOperativo': 'MANTENIMIENTO' }")
    List<Vehicle> findVehiclesEnMantenimiento();

    /**
     * Cuenta vehículos por tipo de maquinaria
     */
    @Query(value = "{ 'activo': true, 'tipoMaquinaria': ?0 }", count = true)
    Long countByTipoMaquinaria(TipoMaquinaria tipoMaquinaria);

    /**
     * Cuenta vehículos por estado operativo
     */
    @Query(value = "{ 'activo': true, 'estadoOperativo': ?0 }", count = true)
    Long countByEstadoOperativo(EstadoOperativo estadoOperativo);
    
    /**
     * Busca vehículos por marca
     */
    List<Vehicle> findByMarcaContainingIgnoreCase(String marca);
    
    /**
     * Busca vehículos por modelo
     */
    List<Vehicle> findByModeloContainingIgnoreCase(String modelo);
    
    /**
     * Busca vehículos por rango de años
     */
    @Query("{ 'activo': true, 'anio': { $gte: ?0, $lte: ?1 } }")
    List<Vehicle> findVehiclesByAnioRange(Integer anioInicio, Integer anioFin);

    /**
     * Verifica si existe un vehículo con la placa especificada
     */
    boolean existsByPlaca(String placa);

    /**
     * Busca vehículos con consumo promedio mayor al especificado
     */
    @Query("{ 'activo': true, 'consumoPromedio': { $gt: ?0 } }")
    List<Vehicle> findVehiclesWithConsumoMayorA(Double consumo);
    
    /**
     * Cuenta vehículos activos
     */
    Long countByActivoTrue();
    
    /**
     * Cuenta vehículos por estado operativo y activos
     */
    Long countByEstadoOperativoAndActivoTrue(EstadoOperativo estadoOperativo);
    
    /**
     * Cuenta vehículos por tipo de maquinaria y activos
     */
    Long countByTipoMaquinariaAndActivoTrue(TipoMaquinaria tipoMaquinaria);
}
