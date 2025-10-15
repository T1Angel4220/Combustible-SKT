package com.skt.combustible.drivers.domain.service;

import com.skt.combustible.drivers.domain.dto.CreateDriverRequest;
import com.skt.combustible.drivers.domain.dto.DriverResponse;
import com.skt.combustible.drivers.domain.dto.UpdateDriverRequest;
import com.skt.combustible.drivers.domain.entity.Driver;
import com.skt.combustible.drivers.domain.exception.DriverDuplicateException;
import com.skt.combustible.drivers.domain.exception.DriverNotFoundException;
import com.skt.combustible.drivers.infrastructure.mapper.DriverMapper;
import com.skt.combustible.drivers.persistence.repository.DriverRepository;
import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Servicio de gestión de choferes con lógica de negocio
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Service
@Transactional
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    // Método para validar estados apropiados para choferes
    private boolean esEstadoValidoParaChoferes(EstadoOperativo estado) {
        return estado.isApropiadoParaChoferes();
    }

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    public DriverService(DriverRepository driverRepository, DriverMapper driverMapper) {
        this.driverRepository = driverRepository;
        this.driverMapper = driverMapper;
    }

    /**
     * Crea un nuevo chofer
     * 
     * @param request Datos del chofer a crear
     * @return DriverResponse con los datos del chofer creado
     * @throws DriverDuplicateException si ya existe un chofer con el mismo DNI o
     *                                  licencia
     */
    public DriverResponse createDriver(CreateDriverRequest request) {
        logger.info("Creando nuevo chofer: {}", request.getNombre() + " " + request.getApellido());

        // Validar que el estado sea válido para choferes
        if (!esEstadoValidoParaChoferes(request.getEstado())) {
            throw new IllegalArgumentException("Estado no válido para choferes: " + request.getEstado() +
                    ". Estados válidos para choferes: DISPONIBLE, ASIGNADO, EN_RUTA, DESCANSANDO, VACACIONES, ENFERMO, LICENCIA");
        }

        // Validar que no exista un chofer con el mismo DNI
        if (driverRepository.existsByDni(request.getDni())) {
            throw new DriverDuplicateException("DNI", request.getDni());
        }

        // Validar que no exista un chofer con la misma licencia
        if (driverRepository.existsByLicencia(request.getLicencia())) {
            throw new DriverDuplicateException("licencia", request.getLicencia());
        }

        Driver driver = driverMapper.toEntity(request);
        Driver savedDriver = driverRepository.save(driver);

        logger.info("Chofer creado exitosamente con ID: {}", savedDriver.getId());
        return driverMapper.toResponse(savedDriver);
    }

    /**
     * Obtiene un chofer por ID
     * 
     * @param id ID del chofer
     * @return DriverResponse con los datos del chofer
     * @throws DriverNotFoundException si no se encuentra el chofer
     */
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(String id) {
        logger.debug("Buscando chofer por ID: {}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> DriverNotFoundException.withId(id));

        return driverMapper.toResponse(driver);
    }

    /**
     * Obtiene un chofer por DNI
     * 
     * @param dni DNI del chofer
     * @return DriverResponse con los datos del chofer
     * @throws DriverNotFoundException si no se encuentra el chofer
     */
    @Transactional(readOnly = true)
    public DriverResponse getDriverByDni(String dni) {
        logger.debug("Buscando chofer por DNI: {}", dni);

        Driver driver = driverRepository.findByDni(dni)
                .orElseThrow(() -> new DriverNotFoundException("DNI", dni));

        return driverMapper.toResponse(driver);
    }

    /**
     * Obtiene un chofer por número de licencia
     * 
     * @param licencia Número de licencia
     * @return DriverResponse con los datos del chofer
     * @throws DriverNotFoundException si no se encuentra el chofer
     */
    @Transactional(readOnly = true)
    public DriverResponse getDriverByLicencia(String licencia) {
        logger.debug("Buscando chofer por licencia: {}", licencia);

        Driver driver = driverRepository.findByLicencia(licencia)
                .orElseThrow(() -> new DriverNotFoundException("licencia", licencia));

        return driverMapper.toResponse(driver);
    }

    /**
     * Obtiene todos los choferes con paginación (activos e inactivos, pero no
     * eliminados)
     * 
     * @param pageable Configuración de paginación
     * @return Página de DriverResponse
     */
    @Transactional(readOnly = true)
    public Page<DriverResponse> getAllDrivers(Pageable pageable) {
        logger.debug("Obteniendo todos los choferes con paginación: {}", pageable);

        // Cambiar para obtener todos los choferes (activos e inactivos)
        // Los eliminados permanentemente ya no existen en la BD, así que no aparecerán
        Page<Driver> drivers = driverRepository.findAll(pageable);
        return drivers.map(driverMapper::toResponse);
    }

    /**
     * Obtiene todos los choferes activos
     * 
     * @return Lista de DriverResponse
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllActiveDrivers() {
        logger.debug("Obteniendo todos los choferes activos");

        List<Driver> drivers = driverRepository.findByActivoTrue();
        return driverMapper.toResponseList(drivers);
    }

    /**
     * Obtiene choferes disponibles (activos y con estado DISPONIBLE)
     * 
     * @return Lista de DriverResponse
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getAvailableDrivers() {
        logger.debug("Obteniendo choferes disponibles");

        List<Driver> drivers = driverRepository.findChoferesDisponibles();
        return driverMapper.toResponseList(drivers);
    }

    /**
     * Obtiene choferes disponibles por tipo de maquinaria
     * 
     * @param tipoMaquinaria Tipo de maquinaria
     * @return Lista de DriverResponse
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getAvailableDriversByMachineryType(TipoMaquinaria tipoMaquinaria) {
        logger.debug("Obteniendo choferes disponibles para tipo de maquinaria: {}", tipoMaquinaria);

        List<Driver> drivers = driverRepository.findChoferesDisponiblesPorTipoMaquinaria(tipoMaquinaria);
        return driverMapper.toResponseList(drivers);
    }

    /**
     * Busca choferes por nombre o apellido
     * 
     * @param nombre Nombre o apellido a buscar
     * @return Lista de DriverResponse
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> searchDriversByName(String nombre) {
        logger.debug("Buscando choferes por nombre: {}", nombre);

        List<Driver> drivers = driverRepository.findByNombreContainingIgnoreCase(nombre);
        return driverMapper.toResponseList(drivers);
    }

    /**
     * Obtiene choferes por estado operativo
     * 
     * @param estado Estado operativo
     * @return Lista de DriverResponse
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getDriversByStatus(EstadoOperativo estado) {
        logger.debug("Obteniendo choferes por estado: {}", estado);

        List<Driver> drivers = driverRepository.findByEstado(estado);
        return driverMapper.toResponseList(drivers);
    }

    /**
     * Obtiene todos los choferes incluyendo los inactivos
     * 
     * @param pageable Configuración de paginación
     * @return Página de DriverResponse
     */
    @Transactional(readOnly = true)
    public Page<DriverResponse> getAllDriversIncludingInactive(Pageable pageable) {
        logger.debug("Obteniendo todos los choferes incluyendo inactivos con paginación: {}", pageable);

        Page<Driver> drivers = driverRepository.findAll(pageable);
        return drivers.map(driverMapper::toResponse);
    }

    /**
     * Actualiza un chofer existente
     * 
     * @param id      ID del chofer a actualizar
     * @param request Datos de actualización
     * @return DriverResponse con los datos actualizados
     * @throws DriverNotFoundException  si no se encuentra el chofer
     * @throws DriverDuplicateException si el DNI o licencia ya existe en otro
     *                                  chofer
     */
    public DriverResponse updateDriver(String id, UpdateDriverRequest request) {
        logger.info("Actualizando chofer con ID: {}", id);

        Driver existingDriver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException(id));

        // Validar que el estado sea válido para choferes si se está actualizando
        if (request.getEstado() != null && !esEstadoValidoParaChoferes(request.getEstado())) {
            throw new IllegalArgumentException("Estado no válido para choferes: " + request.getEstado() +
                    ". Estados válidos para choferes: DISPONIBLE, ASIGNADO, EN_RUTA, DESCANSANDO, VACACIONES, ENFERMO, LICENCIA");
        }

        // Validar DNI si se está actualizando y es diferente al actual
        if (request.getDni() != null && !request.getDni().equals(existingDriver.getDni())) {
            if (driverRepository.existsByDni(request.getDni())) {
                throw new DriverDuplicateException("DNI", request.getDni());
            }
        }

        // Validar licencia si se está actualizando y es diferente a la actual
        if (request.getLicencia() != null && !request.getLicencia().equals(existingDriver.getLicencia())) {
            if (driverRepository.existsByLicencia(request.getLicencia())) {
                throw new DriverDuplicateException("licencia", request.getLicencia());
            }
        }

        driverMapper.updateFromRequest(request, existingDriver);
        Driver updatedDriver = driverRepository.save(existingDriver);

        logger.info("Chofer actualizado exitosamente con ID: {}", updatedDriver.getId());
        return driverMapper.toResponse(updatedDriver);
    }

    /**
     * Cambia el estado de un chofer
     * 
     * @param id          ID del chofer
     * @param nuevoEstado Nuevo estado operativo
     * @return DriverResponse con el estado actualizado
     * @throws DriverNotFoundException si no se encuentra el chofer
     */
    public DriverResponse changeDriverStatus(String id, EstadoOperativo nuevoEstado) {
        logger.info("Cambiando estado del chofer ID: {} a {}", id, nuevoEstado);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> DriverNotFoundException.withId(id));

        driver.setEstado(nuevoEstado);
        Driver updatedDriver = driverRepository.save(driver);

        logger.info("Estado del chofer actualizado exitosamente");
        return driverMapper.toResponse(updatedDriver);
    }

    /**
     * Asigna tipo de maquinaria a un chofer
     * 
     * @param id             ID del chofer
     * @param tipoMaquinaria Tipo de maquinaria a asignar
     * @return DriverResponse con la asignación actualizada
     * @throws DriverNotFoundException si no se encuentra el chofer
     */
    public DriverResponse assignMachineryType(String id, TipoMaquinaria tipoMaquinaria) {
        logger.info("Asignando tipo de maquinaria {} al chofer ID: {}", tipoMaquinaria, id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> DriverNotFoundException.withId(id));

        driver.setTipoMaquinariaAsignada(tipoMaquinaria);
        Driver updatedDriver = driverRepository.save(driver);

        logger.info("Tipo de maquinaria asignado exitosamente");
        return driverMapper.toResponse(updatedDriver);
    }

    /**
     * Desactiva un chofer (soft delete)
     * 
     * @param id ID del chofer a desactivar
     * @throws DriverNotFoundException si no se encuentra el chofer
     */
    public void deactivateDriver(String id) {
        logger.info("Desactivando chofer con ID: {}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> DriverNotFoundException.withId(id));

        driver.setActivo(false);
        driverRepository.save(driver);

        logger.info("Chofer desactivado exitosamente");
    }

    /**
     * Reactiva un chofer (cambia activo = true)
     * 
     * @param id ID del chofer a reactivar
     * @return DriverResponse con los datos del chofer reactivado
     * @throws DriverNotFoundException si no se encuentra el chofer
     */
    public DriverResponse activateDriver(String id) {
        logger.info("Reactivando chofer con ID: {}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> DriverNotFoundException.withId(id));

        driver.setActivo(true);
        driver.setEstado(EstadoOperativo.DISPONIBLE); // Reactivar como disponible por defecto
        Driver savedDriver = driverRepository.save(driver);

        logger.info("Chofer reactivado exitosamente");

        return driverMapper.toResponse(savedDriver);
    }

    /**
     * Elimina permanentemente un chofer
     * 
     * @param id ID del chofer a eliminar
     * @throws DriverNotFoundException si no se encuentra el chofer
     */
    public void deleteDriver(String id) {
        logger.info("Eliminando permanentemente chofer con ID: {}", id);

        if (!driverRepository.existsById(id)) {
            throw DriverNotFoundException.withId(id);
        }

        driverRepository.deleteById(id);
        logger.info("Chofer eliminado permanentemente");
    }

    /**
     * Alias para deleteDriver - elimina permanentemente un chofer
     * 
     * @param id ID del chofer a eliminar
     * @throws DriverNotFoundException si no se encuentra el chofer
     */
    public void deleteDriverPermanently(String id) {
        deleteDriver(id);
    }

    /**
     * Obtiene todos los choferes activos
     * 
     * @return Lista de choferes activos
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getActiveDrivers() {
        logger.info("Obteniendo todos los choferes activos");

        List<Driver> drivers = driverRepository.findByActivoTrue();
        logger.info("Encontrados {} choferes activos", drivers.size());

        return driverMapper.toResponseList(drivers);
    }

    /**
     * Cuenta choferes disponibles
     * 
     * @return Número de choferes disponibles
     */
    @Transactional(readOnly = true)
    public long countAvailableDrivers() {
        return driverRepository.countChoferesDisponibles();
    }

    /**
     * Cuenta choferes disponibles por tipo de maquinaria
     * 
     * @param tipoMaquinaria Tipo de maquinaria
     * @return Número de choferes disponibles para el tipo de maquinaria
     */
    @Transactional(readOnly = true)
    public long countAvailableDriversByMachineryType(TipoMaquinaria tipoMaquinaria) {
        return driverRepository.countChoferesDisponiblesPorTipoMaquinaria(tipoMaquinaria);
    }

    /**
     * Verifica si un chofer está disponible
     * 
     * @param id ID del chofer
     * @return true si está disponible, false en caso contrario
     * @throws DriverNotFoundException si no se encuentra el chofer
     */
    @Transactional(readOnly = true)
    public boolean isDriverAvailable(String id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> DriverNotFoundException.withId(id));

        return driver.isDisponible();
    }
}
