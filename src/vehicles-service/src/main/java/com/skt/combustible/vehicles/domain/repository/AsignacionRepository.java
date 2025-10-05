package com.skt.combustible.vehicles.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.skt.combustible.vehicles.domain.entity.AsignacionVehiculo;
import com.skt.combustible.vehicles.domain.entity.Vehicle;

/**
 * Repositorio para el documento AsignacionVehiculo
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Repository
public interface AsignacionRepository extends MongoRepository<AsignacionVehiculo, String> {
    
    /**
     * Busca asignaciones por vehículo
     */
    List<AsignacionVehiculo> findByVehicleAndActivoTrue(Vehicle vehicle);
    
    /**
     * Busca asignaciones por ID de vehículo
     */
    List<AsignacionVehiculo> findByVehicleIdAndActivoTrue(Long vehicleId);
    
    /**
     * Busca asignaciones por chofer
     */
    List<AsignacionVehiculo> findByChoferIdAndActivoTrue(Long choferId);
    
    /**
     * Busca asignaciones activas por chofer
     */
    @Query("{ 'choferId': ?0, 'estado': 'ACTIVA', 'activo': true }")
    List<AsignacionVehiculo> findAsignacionesActivasPorChofer(Long choferId);
    
    /**
     * Busca la asignación activa de un vehículo
     */
    @Query("{ 'vehicle': ?0, 'estado': 'ACTIVA', 'activo': true }")
    Optional<AsignacionVehiculo> findAsignacionActivaPorVehiculo(Vehicle vehicle);
    
    /**
     * Cuenta asignaciones activas por chofer
     */
    @Query(value = "{ 'choferId': ?0, 'estado': 'ACTIVA', 'activo': true }", count = true)
    Long countAsignacionesActivasPorChofer(Long choferId);
    
    /**
     * Busca asignaciones por estado
     */
    List<AsignacionVehiculo> findByEstadoAndActivoTrue(AsignacionVehiculo.EstadoAsignacion estado);
    
    /**
     * Busca asignaciones por rango de fechas
     */
    @Query("{ 'fechaAsignacion': { $gte: ?0, $lte: ?1 }, 'activo': true }")
    List<AsignacionVehiculo> findAsignacionesPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca la última asignación de un vehículo
     */
    @Query(value = "{ 'vehicle': ?0, 'activo': true }", sort = "{ 'fechaAsignacion': -1 }")
    Optional<AsignacionVehiculo> findUltimaAsignacionPorVehiculo(Vehicle vehicle);
    
    /**
     * Busca asignaciones por tipo de maquinaria
     */
    @Query("{ 'vehicle.tipoMaquinaria': ?0, 'activo': true }")
    List<AsignacionVehiculo> findAsignacionesPorTipoMaquinaria(String tipoMaquinaria);
    
    /**
     * Verifica si un vehículo está asignado actualmente
     */
    @Query(value = "{ 'vehicle': ?0, 'estado': 'ACTIVA', 'activo': true }", count = true)
    Long countVehiculoAsignado(Vehicle vehicle);
    
    /**
     * Verifica si un vehículo está asignado (retorna boolean)
     */
    default Boolean isVehiculoAsignado(Vehicle vehicle) {
        return countVehiculoAsignado(vehicle) > 0;
    }
    
    /**
     * Busca vehículos disponibles (sin asignación activa)
     */
    @Query("{ 'activo': true, 'estadoOperativo': 'DISPONIBLE' }")
    List<Vehicle> findVehiculosDisponiblesParaAsignacion();
    
    /**
     * Cuenta asignaciones por estado
     */
    Long countByEstadoAndActivoTrue(AsignacionVehiculo.EstadoAsignacion estado);
}
