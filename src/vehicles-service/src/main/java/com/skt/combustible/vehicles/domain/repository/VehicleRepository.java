package com.skt.combustible.vehicles.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
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
    @Query("SELECT v FROM Vehicle v WHERE v.activo = true AND v.estadoOperativo = 'DISPONIBLE'")
    List<Vehicle> findVehiclesDisponibles();
    
    /**
     * Busca vehículos disponibles por tipo de maquinaria
     */
    @Query("SELECT v FROM Vehicle v WHERE v.activo = true AND v.estadoOperativo = 'DISPONIBLE' AND v.tipoMaquinaria = :tipo")
    List<Vehicle> findVehiclesDisponiblesByTipo(@Param("tipo") TipoMaquinaria tipoMaquinaria);
    
    /**
     * Busca vehículos en uso
     */
    @Query("SELECT v FROM Vehicle v WHERE v.activo = true AND v.estadoOperativo = 'EN_USO'")
    List<Vehicle> findVehiclesEnUso();
    
    /**
     * Busca vehículos en mantenimiento
     */
    @Query("SELECT v FROM Vehicle v WHERE v.activo = true AND v.estadoOperativo = 'MANTENIMIENTO'")
    List<Vehicle> findVehiclesEnMantenimiento();
    
    /**
     * Cuenta vehículos por tipo de maquinaria
     */
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.activo = true AND v.tipoMaquinaria = :tipo")
    Long countByTipoMaquinaria(@Param("tipo") TipoMaquinaria tipoMaquinaria);
    
    /**
     * Cuenta vehículos por estado operativo
     */
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.activo = true AND v.estadoOperativo = :estado")
    Long countByEstadoOperativo(@Param("estado") EstadoOperativo estadoOperativo);
    
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
    @Query("SELECT v FROM Vehicle v WHERE v.activo = true AND v.anio BETWEEN :anioInicio AND :anioFin")
    List<Vehicle> findVehiclesByAnioRange(@Param("anioInicio") Integer anioInicio, @Param("anioFin") Integer anioFin);
    
    /**
     * Verifica si existe un vehículo con la placa especificada
     */
    boolean existsByPlaca(String placa);
    
    /**
     * Busca vehículos con consumo promedio mayor al especificado
     */
    @Query("SELECT v FROM Vehicle v WHERE v.activo = true AND v.consumoPromedio > :consumo")
    List<Vehicle> findVehiclesWithConsumoMayorA(@Param("consumo") Double consumo);
}
