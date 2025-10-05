package com.skt.combustible.vehicles.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skt.combustible.vehicles.domain.entity.AsignacionVehiculo;
import com.skt.combustible.vehicles.domain.entity.Vehicle;

/**
 * Repositorio para la entidad AsignacionVehiculo
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Repository
public interface AsignacionRepository extends JpaRepository<AsignacionVehiculo, Long> {
    
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
    @Query("SELECT a FROM AsignacionVehiculo a WHERE a.choferId = :choferId AND a.estado = 'ACTIVA' AND a.activo = true")
    List<AsignacionVehiculo> findAsignacionesActivasPorChofer(@Param("choferId") Long choferId);
    
    /**
     * Busca la asignación activa de un vehículo
     */
    @Query("SELECT a FROM AsignacionVehiculo a WHERE a.vehicle = :vehicle AND a.estado = 'ACTIVA' AND a.activo = true")
    Optional<AsignacionVehiculo> findAsignacionActivaPorVehiculo(@Param("vehicle") Vehicle vehicle);
    
    /**
     * Cuenta asignaciones activas por chofer
     */
    @Query("SELECT COUNT(a) FROM AsignacionVehiculo a WHERE a.choferId = :choferId AND a.estado = 'ACTIVA' AND a.activo = true")
    Long countAsignacionesActivasPorChofer(@Param("choferId") Long choferId);
    
    /**
     * Busca asignaciones por estado
     */
    List<AsignacionVehiculo> findByEstadoAndActivoTrue(AsignacionVehiculo.EstadoAsignacion estado);
    
    /**
     * Busca asignaciones por rango de fechas
     */
    @Query("SELECT a FROM AsignacionVehiculo a WHERE a.fechaAsignacion BETWEEN :fechaInicio AND :fechaFin AND a.activo = true")
    List<AsignacionVehiculo> findAsignacionesPorRangoFechas(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                                            @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Busca la última asignación de un vehículo
     */
    @Query("SELECT a FROM AsignacionVehiculo a WHERE a.vehicle = :vehicle AND a.activo = true ORDER BY a.fechaAsignacion DESC")
    Optional<AsignacionVehiculo> findUltimaAsignacionPorVehiculo(@Param("vehicle") Vehicle vehicle);
    
    /**
     * Busca asignaciones por tipo de maquinaria
     */
    @Query("SELECT a FROM AsignacionVehiculo a JOIN a.vehicle v WHERE v.tipoMaquinaria = :tipoMaquinaria AND a.activo = true")
    List<AsignacionVehiculo> findAsignacionesPorTipoMaquinaria(@Param("tipoMaquinaria") String tipoMaquinaria);
    
    /**
     * Verifica si un vehículo está asignado actualmente
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AsignacionVehiculo a WHERE a.vehicle = :vehicle AND a.estado = 'ACTIVA' AND a.activo = true")
    Boolean isVehiculoAsignado(@Param("vehicle") Vehicle vehicle);
    
    /**
     * Busca vehículos disponibles (sin asignación activa)
     */
    @Query("SELECT v FROM Vehicle v WHERE v.activo = true AND v.estadoOperativo = 'DISPONIBLE' AND NOT EXISTS (SELECT a FROM AsignacionVehiculo a WHERE a.vehicle = v AND a.estado = 'ACTIVA' AND a.activo = true)")
    List<Vehicle> findVehiculosDisponiblesParaAsignacion();
    
    /**
     * Cuenta asignaciones por estado
     */
    Long countByEstadoAndActivoTrue(AsignacionVehiculo.EstadoAsignacion estado);
}
