package com.skt.combustible.drivers.persistence.repository;

import com.skt.combustible.drivers.domain.entity.Driver;
import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio MongoDB para operaciones de base de datos de choferes
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {

    /**
     * Busca un chofer por DNI
     * 
     * @param dni DNI del chofer
     * @return Optional con el chofer encontrado
     */
    Optional<Driver> findByDni(String dni);

    /**
     * Busca un chofer por número de licencia
     * 
     * @param licencia Número de licencia
     * @return Optional con el chofer encontrado
     */
    Optional<Driver> findByLicencia(String licencia);

    /**
     * Verifica si existe un chofer con el DNI dado
     * 
     * @param dni DNI a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByDni(String dni);

    /**
     * Verifica si existe un chofer con la licencia dada
     * 
     * @param licencia Licencia a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByLicencia(String licencia);

    /**
     * Busca choferes por estado operativo
     * 
     * @param estado Estado operativo
     * @return Lista de choferes con el estado dado
     */
    List<Driver> findByEstado(EstadoOperativo estado);

    /**
     * Busca choferes por tipo de maquinaria asignada
     * 
     * @param tipoMaquinaria Tipo de maquinaria
     * @return Lista de choferes asignados al tipo de maquinaria
     */
    List<Driver> findByTipoMaquinariaAsignada(TipoMaquinaria tipoMaquinaria);

    /**
     * Busca choferes activos
     * 
     * @return Lista de choferes activos
     */
    List<Driver> findByActivoTrue();

    /**
     * Busca choferes disponibles (activos y con estado DISPONIBLE)
     * 
     * @return Lista de choferes disponibles
     */
    @Query("{ 'activo': true, 'estado': 'DISPONIBLE' }")
    List<Driver> findChoferesDisponibles();

    /**
     * Busca choferes disponibles por tipo de maquinaria
     * 
     * @param tipoMaquinaria Tipo de maquinaria
     * @return Lista de choferes disponibles para el tipo de maquinaria
     */
    @Query("{ 'activo': true, 'estado': 'DISPONIBLE', '$or': [{'tipo_maquinaria_asignada': ?0}, {'tipo_maquinaria_asignada': null}] }")
    List<Driver> findChoferesDisponiblesPorTipoMaquinaria(TipoMaquinaria tipoMaquinaria);

    /**
     * Busca choferes por nombre o apellido (búsqueda parcial)
     * 
     * @param nombre Nombre o apellido a buscar
     * @return Lista de choferes que coinciden con el criterio
     */
    @Query("{ 'activo': true, '$or': [{'nombre': {$regex: ?0, $options: 'i'}}, {'apellido': {$regex: ?0, $options: 'i'}}] }")
    List<Driver> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca choferes con paginación
     * 
     * @param pageable Configuración de paginación
     * @return Página de choferes
     */
    Page<Driver> findByActivoTrue(Pageable pageable);

    /**
     * Cuenta choferes disponibles
     * 
     * @return Número de choferes disponibles
     */
    @Query(value = "{ 'activo': true, 'estado': 'DISPONIBLE' }", count = true)
    long countChoferesDisponibles();

    /**
     * Cuenta choferes disponibles por tipo de maquinaria
     * 
     * @param tipoMaquinaria Tipo de maquinaria
     * @return Número de choferes disponibles para el tipo de maquinaria
     */
    @Query(value = "{ 'activo': true, 'estado': 'DISPONIBLE', '$or': [{'tipo_maquinaria_asignada': ?0}, {'tipo_maquinaria_asignada': null}] }", count = true)
    long countChoferesDisponiblesPorTipoMaquinaria(TipoMaquinaria tipoMaquinaria);

    /**
     * Busca choferes por múltiples criterios con paginación
     * 
     * @param estado         Estado operativo (opcional)
     * @param tipoMaquinaria Tipo de maquinaria (opcional)
     * @param activo         Estado activo (opcional)
     * @param pageable       Configuración de paginación
     * @return Página de choferes que coinciden con los criterios
     */
    @Query("{ '$and': [{'$or': [{'estado': ?0}, {'$expr': {'$eq': [?0, null]}}]}, {'$or': [{'tipo_maquinaria_asignada': ?1}, {'$expr': {'$eq': [?1, null]}}]}, {'$or': [{'activo': ?2}, {'$expr': {'$eq': [?2, null]}}]}] }")
    Page<Driver> findByCriterios(EstadoOperativo estado, TipoMaquinaria tipoMaquinaria, Boolean activo,
            Pageable pageable);

    /**
     * Busca choferes en servicio (activos y con estado ASIGNADO o EN_RUTA)
     * 
     * @param estados Lista de estados operativos
     * @return Lista de choferes en servicio
     */
    List<Driver> findByActivoTrueAndEstadoIn(List<EstadoOperativo> estados);

    /**
     * Busca un chofer por usuarioId
     * 
     * @param usuarioId ID del usuario en auth-service
     * @return Optional con el chofer encontrado
     */
    Optional<Driver> findByUsuarioId(String usuarioId);

    /**
     * Busca un chofer por email
     * 
     * @param email Email del chofer
     * @return Optional con el chofer encontrado
     */
    Optional<Driver> findByEmail(String email);
}
