package com.skt.combustible.vehicles.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.vehicles.domain.dto.AsignacionCreateRequest;
import com.skt.combustible.vehicles.domain.dto.AsignacionResponse;
import com.skt.combustible.vehicles.domain.entity.AsignacionVehiculo;
import com.skt.combustible.vehicles.domain.entity.Vehicle;
import com.skt.combustible.vehicles.domain.repository.AsignacionRepository;
import com.skt.combustible.vehicles.domain.repository.VehicleRepository;

/**
 * Servicio de aplicación para la gestión de asignaciones de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Service
@Transactional
public class AsignacionService {
    
    @Autowired
    private AsignacionRepository asignacionRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Asigna un vehículo a un chofer
     */
    public AsignacionResponse asignarVehiculoAChofer(AsignacionCreateRequest request) {
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
        
        // Validar regla de negocio: máximo 2 vehículos por chofer
        Long asignacionesActivas = asignacionRepository.countAsignacionesActivasPorChofer(request.getChoferId());
        if (asignacionesActivas != null && asignacionesActivas >= 2) {
            throw new IllegalArgumentException("Un chofer no puede tener más de 2 vehículos asignados");
        }
        
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
        
        // Cambiar el estado del vehículo a EN_USO (temporalmente hasta que se resuelva el enum)
        vehicle.setEstadoOperativo(EstadoOperativo.EN_USO);
        vehicleRepository.save(vehicle);
        
        return mapToResponse(savedAsignacion);
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
