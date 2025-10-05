package com.skt.combustible.vehicles.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skt.combustible.vehicles.domain.entity.Mantenimiento;
import com.skt.combustible.vehicles.domain.entity.Vehicle;

/**
 * Repositorio para la entidad Mantenimiento
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {
    
    /**
     * Busca mantenimientos por vehículo
     */
    List<Mantenimiento> findByVehicleAndActivoTrue(Vehicle vehicle);
    
    /**
     * Busca mantenimientos por ID de vehículo
     */
    List<Mantenimiento> findByVehicleIdAndActivoTrue(Long vehicleId);
    
    /**
     * Busca mantenimientos por tipo
     */
    List<Mantenimiento> findByTipoMantenimientoAndActivoTrue(String tipoMantenimiento);
    
    /**
     * Busca mantenimientos por estado
     */
    List<Mantenimiento> findByEstadoAndActivoTrue(Mantenimiento.EstadoMantenimiento estado);
    
    /**
     * Busca mantenimientos por proveedor
     */
    List<Mantenimiento> findByProveedorAndActivoTrue(String proveedor);
    
    /**
     * Busca mantenimientos programados
     */
    @Query("SELECT m FROM Mantenimiento m WHERE m.estado = 'PROGRAMADO' AND m.activo = true")
    List<Mantenimiento> findMantenimientosProgramados();
    
    /**
     * Busca mantenimientos próximos a vencer
     */
    @Query("SELECT m FROM Mantenimiento m WHERE m.fechaProximoMantenimiento <= :fechaLimite AND m.activo = true")
    List<Mantenimiento> findMantenimientosProximosAVencer(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    /**
     * Busca mantenimientos por rango de fechas
     */
    @Query("SELECT m FROM Mantenimiento m WHERE m.fechaMantenimiento BETWEEN :fechaInicio AND :fechaFin AND m.activo = true")
    List<Mantenimiento> findMantenimientosPorRangoFechas(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                                         @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Busca el último mantenimiento de un vehículo
     */
    @Query("SELECT m FROM Mantenimiento m WHERE m.vehicle = :vehicle AND m.activo = true ORDER BY m.fechaMantenimiento DESC")
    Optional<Mantenimiento> findUltimoMantenimientoPorVehiculo(@Param("vehicle") Vehicle vehicle);
    
    /**
     * Cuenta mantenimientos por vehículo
     */
    @Query("SELECT COUNT(m) FROM Mantenimiento m WHERE m.vehicle = :vehicle AND m.activo = true")
    Long countMantenimientosPorVehiculo(@Param("vehicle") Vehicle vehicle);
    
    /**
     * Calcula el costo total de mantenimientos por vehículo
     */
    @Query("SELECT COALESCE(SUM(m.costo), 0) FROM Mantenimiento m WHERE m.vehicle = :vehicle AND m.activo = true")
    Double calcularCostoTotalMantenimientosPorVehiculo(@Param("vehicle") Vehicle vehicle);
    
    /**
     * Busca mantenimientos por tipo de maquinaria
     */
    @Query("SELECT m FROM Mantenimiento m JOIN m.vehicle v WHERE v.tipoMaquinaria = :tipoMaquinaria AND m.activo = true")
    List<Mantenimiento> findMantenimientosPorTipoMaquinaria(@Param("tipoMaquinaria") String tipoMaquinaria);
}
