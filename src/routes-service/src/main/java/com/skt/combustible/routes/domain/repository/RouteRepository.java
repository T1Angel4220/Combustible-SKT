package com.skt.combustible.routes.domain.repository;

import com.skt.combustible.routes.domain.entity.Route;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio MongoDB para operaciones de base de datos de rutas
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Repository
public interface RouteRepository extends MongoRepository<Route, String> {
    
    /**
     * Busca una ruta por código
     */
    Optional<Route> findByCodigo(String codigo);
    
    /**
     * Verifica si existe una ruta con el código dado
     */
    boolean existsByCodigo(String codigo);
    
    /**
     * Busca rutas por estado
     */
    List<Route> findByEstado(Route.EstadoRuta estado);
    
    /**
     * Busca rutas activas
     */
    List<Route> findByActivaTrue();
    
    /**
     * Busca rutas por vehículo
     */
    List<Route> findByVehiculoId(String vehiculoId);
    
    /**
     * Busca rutas por chofer
     */
    List<Route> findByChoferId(String choferId);
    
    /**
     * Busca rutas por tipo de maquinaria
     */
    List<Route> findByTipoMaquinaria(TipoMaquinaria tipoMaquinaria);
    
    /**
     * Busca rutas activas por estado
     */
    @Query("{ 'activa': true, 'estado': ?0 }")
    List<Route> findActivasByEstado(Route.EstadoRuta estado);
    
    /**
     * Busca rutas en curso
     */
    @Query("{ 'activa': true, 'estado': 'EN_CURSO' }")
    List<Route> findRutasEnCurso();
    
    /**
     * Busca rutas completadas hoy
     */
    @Query("{ 'activa': true, 'estado': 'COMPLETADA', 'fecha_fin': { $gte: ?0, $lt: ?1 } }")
    List<Route> findRutasCompletadasHoy(LocalDateTime inicioDia, LocalDateTime finDia);
    
    /**
     * Busca rutas por vehículo y estado
     */
    @Query("{ 'vehiculo_id': ?0, 'estado': ?1, 'activa': true }")
    List<Route> findByVehiculoIdAndEstado(String vehiculoId, Route.EstadoRuta estado);
    
    /**
     * Busca rutas por chofer y estado
     */
    @Query("{ 'chofer_id': ?0, 'estado': ?1, 'activa': true }")
    List<Route> findByChoferIdAndEstado(String choferId, Route.EstadoRuta estado);
    
    /**
     * Busca rutas activas por chofer (que no estén completadas o canceladas)
     */
    @Query("{ 'chofer_id': ?0, 'activa': true, 'estado': { $nin: ['COMPLETADA', 'CANCELADA'] } }")
    List<Route> findRutasActivasByChoferId(String choferId);
    
    /**
     * Verifica si un chofer tiene rutas activas
     */
    @Query(value = "{ 'chofer_id': ?0, 'activa': true, 'estado': { $nin: ['COMPLETADA', 'CANCELADA'] } }", count = true)
    long countRutasActivasByChoferId(String choferId);
    
    /**
     * Cuenta rutas activas
     */
    @Query(value = "{ 'activa': true }", count = true)
    long countRutasActivas();
    
    /**
     * Cuenta rutas por estado
     */
    @Query(value = "{ 'activa': true, 'estado': ?0 }", count = true)
    long countByEstado(Route.EstadoRuta estado);
    
    /**
     * Suma distancia total de rutas activas
     */
    @Query(value = "{ 'activa': true }", fields = "{ 'distancia_km': 1 }")
    List<Route> findDistanciaTotal();
    
    /**
     * Busca rutas con paginación
     */
    Page<Route> findByActivaTrue(Pageable pageable);
    
    /**
     * Busca rutas por múltiples criterios
     */
    @Query("{ '$and': [{'activa': true}, {'$or': [{'estado': ?0}, {'$expr': {'$eq': [?0, null]}}]}, {'$or': [{'tipo_maquinaria': ?1}, {'$expr': {'$eq': [?1, null]}}]}, {'$or': [{'chofer_id': ?2}, {'$expr': {'$eq': [?2, null]}}]}, {'$or': [{'vehiculo_id': ?3}, {'$expr': {'$eq': [?3, null]}}]}] }")
    Page<Route> findByCriterios(Route.EstadoRuta estado, TipoMaquinaria tipoMaquinaria, 
                                 String choferId, String vehiculoId, Pageable pageable);
}

