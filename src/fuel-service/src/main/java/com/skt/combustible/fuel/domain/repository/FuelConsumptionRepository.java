package com.skt.combustible.fuel.domain.repository;

import com.skt.combustible.fuel.domain.entity.FuelConsumption;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio MongoDB para operaciones de base de datos de consumo de combustible
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Repository
public interface FuelConsumptionRepository extends MongoRepository<FuelConsumption, String> {
    
    /**
     * Busca registros por vehículo
     */
    List<FuelConsumption> findByVehiculoIdAndActivoTrue(String vehiculoId);
    
    /**
     * Busca registros por chofer
     */
    List<FuelConsumption> findByChoferIdAndActivoTrue(String choferId);
    
    /**
     * Busca registros por ruta
     */
    List<FuelConsumption> findByRutaIdAndActivoTrue(String rutaId);
    
    /**
     * Busca registros por tipo de maquinaria
     */
    List<FuelConsumption> findByTipoMaquinariaAndActivoTrue(TipoMaquinaria tipoMaquinaria);
    
    /**
     * Busca registros por rango de fechas
     */
    @Query("{ 'fecha_hora': { $gte: ?0, $lte: ?1 }, 'activo': true }")
    List<FuelConsumption> findByFechaHoraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca registros por vehículo y rango de fechas
     */
    @Query("{ 'vehiculo_id': ?0, 'fecha_hora': { $gte: ?1, $lte: ?2 }, 'activo': true }")
    List<FuelConsumption> findByVehiculoIdAndFechaHoraBetween(String vehiculoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca registros por chofer y rango de fechas
     */
    @Query("{ 'chofer_id': ?0, 'fecha_hora': { $gte: ?1, $lte: ?2 }, 'activo': true }")
    List<FuelConsumption> findByChoferIdAndFechaHoraBetween(String choferId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca registros por tipo de maquinaria y rango de fechas
     */
    @Query("{ 'tipo_maquinaria': ?0, 'fecha_hora': { $gte: ?1, $lte: ?2 }, 'activo': true }")
    List<FuelConsumption> findByTipoMaquinariaAndFechaHoraBetween(TipoMaquinaria tipoMaquinaria, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Cuenta registros activos
     */
    @Query(value = "{ 'activo': true }", count = true)
    long countByActivoTrue();
    
    /**
     * Cuenta registros por tipo de maquinaria
     */
    @Query(value = "{ 'tipo_maquinaria': ?0, 'activo': true }", count = true)
    long countByTipoMaquinariaAndActivoTrue(TipoMaquinaria tipoMaquinaria);
    
    /**
     * Busca registros activos con paginación
     */
    Page<FuelConsumption> findByActivoTrue(Pageable pageable);
    
    /**
     * Busca registros por tipo de maquinaria con paginación
     */
    Page<FuelConsumption> findByTipoMaquinariaAndActivoTrue(TipoMaquinaria tipoMaquinaria, Pageable pageable);
}

