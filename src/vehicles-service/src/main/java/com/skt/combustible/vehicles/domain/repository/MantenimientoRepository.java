package com.skt.combustible.vehicles.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.skt.combustible.vehicles.domain.entity.Mantenimiento;
import com.skt.combustible.vehicles.domain.entity.Vehicle;

/**
 * Repositorio para el documento Mantenimiento
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Repository
public interface MantenimientoRepository extends MongoRepository<Mantenimiento, String> {
    
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
    @Query("{ 'estado': 'PROGRAMADO', 'activo': true }")
    List<Mantenimiento> findMantenimientosProgramados();
    
    /**
     * Busca mantenimientos próximos a vencer
     */
    @Query("{ 'fechaProximoMantenimiento': { $lte: ?0 }, 'activo': true }")
    List<Mantenimiento> findMantenimientosProximosAVencer(LocalDateTime fechaLimite);
    
    /**
     * Busca mantenimientos por rango de fechas
     */
    @Query("{ 'fechaMantenimiento': { $gte: ?0, $lte: ?1 }, 'activo': true }")
    List<Mantenimiento> findMantenimientosPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca el último mantenimiento de un vehículo
     */
    @Query(value = "{ 'vehicle': ?0, 'activo': true }", sort = "{ 'fechaMantenimiento': -1 }")
    Optional<Mantenimiento> findUltimoMantenimientoPorVehiculo(Vehicle vehicle);
    
    /**
     * Cuenta mantenimientos por vehículo
     */
    @Query(value = "{ 'vehicle': ?0, 'activo': true }", count = true)
    Long countMantenimientosPorVehiculo(Vehicle vehicle);
    
    /**
     * Calcula el costo total de mantenimientos por vehículo
     */
    @Query(value = "{ 'vehicle': ?0, 'activo': true }", fields = "{ 'costo': 1 }")
    List<Mantenimiento> findCostosPorVehiculo(Vehicle vehicle);
    
    /**
     * Calcula el costo total de mantenimientos por vehículo (método original)
     */
    default Double calcularCostoTotalMantenimientosPorVehiculo(Vehicle vehicle) {
        List<Mantenimiento> mantenimientos = findCostosPorVehiculo(vehicle);
        return mantenimientos.stream()
                .mapToDouble(m -> m.getCosto() != null ? m.getCosto() : 0.0)
                .sum();
    }
    
    /**
     * Busca mantenimientos por tipo de maquinaria
     */
    @Query("{ 'vehicle.tipoMaquinaria': ?0, 'activo': true }")
    List<Mantenimiento> findMantenimientosPorTipoMaquinaria(String tipoMaquinaria);
}
