package com.skt.combustible.vehicles.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.domain.dto.AsignacionCreateRequest;
import com.skt.combustible.vehicles.domain.dto.AsignacionResponse;
import com.skt.combustible.vehicles.domain.entity.AsignacionVehiculo;
import com.skt.combustible.vehicles.domain.entity.Vehicle;
import com.skt.combustible.vehicles.domain.repository.AsignacionRepository;
import com.skt.combustible.vehicles.domain.repository.VehicleRepository;
import com.skt.combustible.vehicles.infrastructure.client.DriversGrpcClient;

/**
 * Servicio de aplicación para la gestión de asignaciones de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Service
@Transactional
public class AsignacionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsignacionService.class);
    
    @Autowired
    private AsignacionRepository asignacionRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private DriversGrpcClient driversGrpcClient;
    
    /**
     * Asigna un vehículo a un chofer
     * Implementa lógica diferenciada por tipo de maquinaria (liviana/pesada)
     */
    public AsignacionResponse asignarVehiculoAChofer(AsignacionCreateRequest request) {
        logger.info("Iniciando asignación de vehículo {} a chofer {}", request.getVehicleId(), request.getChoferId());
        
        // Validar que el vehículo existe
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + request.getVehicleId()));
        
        // Validar que el vehículo esté activo
        if (!vehicle.getActivo()) {
            throw new IllegalArgumentException("No se puede asignar un vehículo inactivo");
        }
        
        // Validar que el vehículo esté disponible
        if (vehicle.getEstadoOperativo() != EstadoOperativo.DISPONIBLE) {
            throw new IllegalArgumentException("El vehículo no está disponible para asignación. Estado actual: " + vehicle.getEstadoOperativo());
        }
        
        // Validar que el vehículo no esté ya asignado
        if (asignacionRepository.isVehiculoAsignado(vehicle)) {
            throw new IllegalArgumentException("El vehículo ya está asignado a otro chofer");
        }
        
        // Validar disponibilidad del chofer vía gRPC
        try {
            boolean driverAvailable = driversGrpcClient.isDriverAvailable(request.getChoferId());
            if (!driverAvailable) {
                throw new IllegalArgumentException("El chofer no está disponible para asignación");
            }
            logger.debug("Chofer {} verificado como disponible vía gRPC", request.getChoferId());
        } catch (RuntimeException e) {
            logger.error("Error verificando disponibilidad del chofer vía gRPC: {}", e.getMessage());
            throw new IllegalArgumentException("No se pudo verificar la disponibilidad del chofer: " + e.getMessage());
        }
        
        // Validar compatibilidad del chofer con el tipo de maquinaria
        TipoMaquinaria tipoMaquinaria = vehicle.getTipoMaquinaria();
        try {
            boolean canHandle = driversGrpcClient.canDriverHandleMachineryType(request.getChoferId(), tipoMaquinaria);
            if (!canHandle) {
                throw new IllegalArgumentException(
                    String.format("El chofer no está autorizado para manejar maquinaria tipo %s. " +
                                  "Solo puede manejar el tipo asignado en su perfil.", tipoMaquinaria));
            }
            logger.debug("Chofer {} verificado como compatible con tipo de maquinaria {}", 
                        request.getChoferId(), tipoMaquinaria);
        } catch (RuntimeException e) {
            logger.error("Error verificando compatibilidad chofer-maquinaria: {}", e.getMessage());
            throw new IllegalArgumentException("No se pudo verificar la compatibilidad del chofer: " + e.getMessage());
        }
        
        // Aplicar lógica diferenciada por tipo de maquinaria
        aplicarReglasPorTipoMaquinaria(vehicle, request.getChoferId());
        
        // Crear la asignación
        AsignacionVehiculo asignacion = new AsignacionVehiculo(
            vehicle,
            request.getChoferId(),
            request.getFechaAsignacion() != null ? request.getFechaAsignacion() : LocalDateTime.now()
        );
        
        if (request.getObservaciones() != null) {
            asignacion.setObservaciones(request.getObservaciones());
        }
        
        AsignacionVehiculo savedAsignacion = asignacionRepository.save(asignacion);
        
        // Cambiar el estado del vehículo a EN_USO
        vehicle.setEstadoOperativo(EstadoOperativo.EN_USO);
        vehicleRepository.save(vehicle);
        
        logger.info("Asignación completada exitosamente: vehículo {} asignado a chofer {}", 
                   vehicle.getPlaca(), request.getChoferId());
        
        return mapToResponse(savedAsignacion);
    }
    
    /**
     * Aplica reglas de negocio diferenciadas según el tipo de maquinaria
     */
    private void aplicarReglasPorTipoMaquinaria(Vehicle vehicle, String choferId) {
        TipoMaquinaria tipo = vehicle.getTipoMaquinaria();
        
        if (vehicle.esLiviano()) {
            // Reglas para maquinaria liviana (CAMION, VOLQUETE)
            aplicarReglasMaquinariaLiviana(vehicle, choferId);
        } else if (vehicle.esPesado()) {
            // Reglas para maquinaria pesada (EXCAVADORA, CARGADOR, GRUA, MOTONIVELADORA)
            aplicarReglasMaquinariaPesada(vehicle, choferId);
        } else {
            logger.warn("Tipo de maquinaria no reconocido: {}", tipo);
        }
    }
    
    /**
     * Reglas específicas para maquinaria liviana
     */
    private void aplicarReglasMaquinariaLiviana(Vehicle vehicle, String choferId) {
        logger.debug("Aplicando reglas para maquinaria liviana: {}", vehicle.getTipoMaquinaria());
        
        // Regla 1: Máximo 2 vehículos livianos por chofer
        Long asignacionesActivas = asignacionRepository.countAsignacionesActivasPorChofer(choferId);
        if (asignacionesActivas != null && asignacionesActivas >= 2) {
            throw new IllegalArgumentException(
                "Un chofer no puede tener más de 2 vehículos asignados simultáneamente");
        }
        
        // Regla 2: Verificar que no tenga vehículos pesados asignados
        List<AsignacionVehiculo> asignacionesActivasList = 
            asignacionRepository.findAsignacionesActivasPorChofer(choferId);
        
        boolean tienePesado = asignacionesActivasList.stream()
            .anyMatch(a -> a.getVehicle() != null && a.getVehicle().esPesado());
        
        if (tienePesado) {
            throw new IllegalArgumentException(
                "Un chofer no puede tener asignados simultáneamente vehículos livianos y pesados. " +
                "Debe desasignar primero los vehículos pesados.");
        }
        
        // Regla 3: Validar capacidad del tanque (maquinaria liviana generalmente tiene tanques más pequeños)
        if (vehicle.getCapacidadTanque() != null && vehicle.getCapacidadTanque() > 200) {
            logger.warn("Vehículo liviano con capacidad de tanque inusualmente grande: {} L", 
                       vehicle.getCapacidadTanque());
        }
    }
    
    /**
     * Reglas específicas para maquinaria pesada
     */
    private void aplicarReglasMaquinariaPesada(Vehicle vehicle, String choferId) {
        logger.debug("Aplicando reglas para maquinaria pesada: {}", vehicle.getTipoMaquinaria());
        
        // Regla 1: Máximo 1 vehículo pesado por chofer (más restrictivo que liviana)
        Long asignacionesActivas = asignacionRepository.countAsignacionesActivasPorChofer(choferId);
        if (asignacionesActivas != null && asignacionesActivas >= 1) {
            // Verificar si tiene vehículos livianos asignados
            List<AsignacionVehiculo> asignacionesActivasList = 
                asignacionRepository.findAsignacionesActivasPorChofer(choferId);
            
            boolean tieneLiviano = asignacionesActivasList.stream()
                .anyMatch(a -> a.getVehicle() != null && a.getVehicle().esLiviano());
            
            if (tieneLiviano) {
                throw new IllegalArgumentException(
                    "Un chofer no puede tener asignados simultáneamente vehículos livianos y pesados. " +
                    "Debe desasignar primero los vehículos livianos.");
            }
            
            // Si ya tiene un pesado, no puede tener otro
            boolean tienePesado = asignacionesActivasList.stream()
                .anyMatch(a -> a.getVehicle() != null && a.getVehicle().esPesado());
            
            if (tienePesado) {
                throw new IllegalArgumentException(
                    "Un chofer solo puede tener 1 vehículo pesado asignado a la vez. " +
                    "Debe desasignar el vehículo pesado actual antes de asignar uno nuevo.");
            }
        }
        
        // Regla 2: Validar estado de mantenimiento (maquinaria pesada requiere más mantenimiento)
        if (vehicle.getEstadoOperativo() == EstadoOperativo.MANTENIMIENTO) {
            throw new IllegalArgumentException(
                "No se puede asignar maquinaria pesada que está en mantenimiento. " +
                "Debe completar el mantenimiento primero.");
        }
        
        // Regla 3: Validar capacidad del tanque (maquinaria pesada generalmente tiene tanques más grandes)
        if (vehicle.getCapacidadTanque() != null && vehicle.getCapacidadTanque() < 100) {
            logger.warn("Vehículo pesado con capacidad de tanque inusualmente pequeña: {} L", 
                       vehicle.getCapacidadTanque());
        }
        
        // Regla 4: Validar consumo promedio (maquinaria pesada consume más)
        if (vehicle.getConsumoPromedio() != null && vehicle.getConsumoPromedio() < 15) {
            logger.warn("Vehículo pesado con consumo promedio inusualmente bajo: {} L/100km", 
                       vehicle.getConsumoPromedio());
        }
    }
    
    /**
     * Desasigna un vehículo de un chofer
     */
    public AsignacionResponse desasignarVehiculo(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + vehicleId));
        
        // Buscar asignación activa filtrando por ID del vehículo
        // Como DBRef almacena el vehículo como referencia, buscamos todas las activas y filtramos
        AsignacionVehiculo asignacionActiva = asignacionRepository.findAsignacionesActivas()
                .stream()
                .filter(a -> a.getVehicle() != null && vehicleId.equals(a.getVehicle().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay asignación activa para este vehículo"));
        
        // Finalizar la asignación
        asignacionActiva.setEstado(AsignacionVehiculo.EstadoAsignacion.FINALIZADA);
        asignacionActiva.setFechaDesasignacion(LocalDateTime.now());
        AsignacionVehiculo updatedAsignacion = asignacionRepository.save(asignacionActiva);
        
        // Cambiar el estado del vehículo a DISPONIBLE
        vehicle.setEstadoOperativo(EstadoOperativo.DISPONIBLE);
        vehicleRepository.save(vehicle);
        
        return mapToResponse(updatedAsignacion);
    }
    
    /**
     * Obtiene una asignación por ID
     */
    @Transactional(readOnly = true)
    public Optional<AsignacionResponse> obtenerAsignacionPorId(String id) {
        return asignacionRepository.findById(id)
                .filter(AsignacionVehiculo::getActivo)
                .map(this::mapToResponse);
    }
    
    /**
     * Obtiene asignaciones por vehículo
     */
    @Transactional(readOnly = true)
    public List<AsignacionResponse> obtenerAsignacionesPorVehiculo(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElse(null);
        if (vehicle == null) {
            return java.util.Collections.emptyList();
        }
        return asignacionRepository.findByVehicleAndActivoTrue(vehicle)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene asignaciones activas por chofer
     */
    @Transactional(readOnly = true)
    public List<AsignacionResponse> obtenerAsignacionesActivasPorChofer(String choferId) {
        return asignacionRepository.findAsignacionesActivasPorChofer(choferId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las asignaciones
     */
    @Transactional(readOnly = true)
    public List<AsignacionResponse> obtenerTodasLasAsignaciones() {
        return asignacionRepository.findAll()
                .stream()
                .filter(AsignacionVehiculo::getActivo)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene asignaciones por estado
     */
    @Transactional(readOnly = true)
    public List<AsignacionResponse> obtenerAsignacionesPorEstado(AsignacionVehiculo.EstadoAsignacion estado) {
        return asignacionRepository.findByEstadoAndActivoTrue(estado)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene vehículos disponibles para asignación
     */
    @Transactional(readOnly = true)
    public List<Vehicle> obtenerVehiculosDisponiblesParaAsignacion() {
        return asignacionRepository.findVehiculosDisponiblesParaAsignacion();
    }
    
    /**
     * Verifica si un chofer puede recibir más asignaciones
     */
    @Transactional(readOnly = true)
    public boolean puedeAsignarMasVehiculos(String choferId) {
        Long asignacionesActivas = asignacionRepository.countAsignacionesActivasPorChofer(choferId);
        return asignacionesActivas == null || asignacionesActivas < 2;
    }
    
    /**
     * Obtiene estadísticas de asignaciones
     */
    @Transactional(readOnly = true)
    public AsignacionStatsDTO obtenerEstadisticasAsignacion() {
        Long totalAsignaciones = asignacionRepository.count();
        Long asignacionesActivas = asignacionRepository.countByEstadoAndActivoTrue(AsignacionVehiculo.EstadoAsignacion.ACTIVA);
        Long asignacionesFinalizadas = asignacionRepository.countByEstadoAndActivoTrue(AsignacionVehiculo.EstadoAsignacion.FINALIZADA);
        
        return new AsignacionStatsDTO(totalAsignaciones, asignacionesActivas, asignacionesFinalizadas);
    }
    
    /**
     * Mapea entidad a DTO de respuesta
     */
    private AsignacionResponse mapToResponse(AsignacionVehiculo asignacion) {
        AsignacionResponse response = new AsignacionResponse();
        response.setId(asignacion.getId());
        response.setVehicleId(asignacion.getVehicle().getId());
        response.setPlacaVehiculo(asignacion.getVehicle().getPlaca());
        response.setMarcaVehiculo(asignacion.getVehicle().getMarca());
        response.setModeloVehiculo(asignacion.getVehicle().getModelo());
        response.setChoferId(asignacion.getChoferId());
        response.setFechaAsignacion(asignacion.getFechaAsignacion());
        response.setFechaDesasignacion(asignacion.getFechaDesasignacion());
        response.setEstado(asignacion.getEstado().toString());
        response.setObservaciones(asignacion.getObservaciones());
        response.setFechaCreacion(asignacion.getFechaCreacion());
        response.setFechaActualizacion(asignacion.getFechaActualizacion());
        response.setActivo(asignacion.getActivo());
        
        return response;
    }
    
    /**
     * DTO para estadísticas de asignación
     */
    public static class AsignacionStatsDTO {
        private Long totalAsignaciones;
        private Long asignacionesActivas;
        private Long asignacionesFinalizadas;
        
        public AsignacionStatsDTO(Long totalAsignaciones, Long asignacionesActivas, Long asignacionesFinalizadas) {
            this.totalAsignaciones = totalAsignaciones;
            this.asignacionesActivas = asignacionesActivas;
            this.asignacionesFinalizadas = asignacionesFinalizadas;
        }
        
        // Getters y Setters
        public Long getTotalAsignaciones() { return totalAsignaciones; }
        public void setTotalAsignaciones(Long totalAsignaciones) { this.totalAsignaciones = totalAsignaciones; }
        
        public Long getAsignacionesActivas() { return asignacionesActivas; }
        public void setAsignacionesActivas(Long asignacionesActivas) { this.asignacionesActivas = asignacionesActivas; }
        
        public Long getAsignacionesFinalizadas() { return asignacionesFinalizadas; }
        public void setAsignacionesFinalizadas(Long asignacionesFinalizadas) { this.asignacionesFinalizadas = asignacionesFinalizadas; }
    }
}
