package com.skt.combustible.vehicles.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skt.combustible.vehicles.domain.dto.MantenimientoCreateRequest;
import com.skt.combustible.vehicles.domain.dto.MantenimientoResponse;
import com.skt.combustible.vehicles.domain.entity.Mantenimiento;
import com.skt.combustible.vehicles.domain.entity.Vehicle;
import com.skt.combustible.vehicles.domain.repository.MantenimientoRepository;
import com.skt.combustible.vehicles.domain.repository.VehicleRepository;

/**
 * Servicio de aplicación para la gestión de mantenimientos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Service
@Transactional
public class MantenimientoService {
    
    @Autowired
    private MantenimientoRepository mantenimientoRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Crea un nuevo mantenimiento
     */
    public MantenimientoResponse crearMantenimiento(MantenimientoCreateRequest request) {
        // Validar que el vehículo existe
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId().toString())
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + request.getVehicleId()));
        
        // Validar que el vehículo esté activo
        if (!vehicle.getActivo()) {
            throw new IllegalArgumentException("No se puede crear mantenimiento para un vehículo inactivo");
        }
        
        Mantenimiento mantenimiento = new Mantenimiento(
            vehicle,
            request.getTipoMantenimiento(),
            request.getDescripcion(),
            request.getFechaMantenimiento(),
            request.getProveedor()
        );
        
        // Asignar campos opcionales
        if (request.getFechaProximoMantenimiento() != null) {
            mantenimiento.setFechaProximoMantenimiento(request.getFechaProximoMantenimiento());
        }
        if (request.getCosto() != null) {
            mantenimiento.setCosto(request.getCosto());
        }
        if (request.getKilometrajeMantenimiento() != null) {
            mantenimiento.setKilometrajeMantenimiento(request.getKilometrajeMantenimiento());
        }
        if (request.getObservaciones() != null) {
            mantenimiento.setObservaciones(request.getObservaciones());
        }
        
        Mantenimiento savedMantenimiento = mantenimientoRepository.save(mantenimiento);
        return mapToResponse(savedMantenimiento);
    }
    
    /**
     * Obtiene un mantenimiento por ID
     */
    @Transactional(readOnly = true)
    public Optional<MantenimientoResponse> obtenerMantenimientoPorId(String id) {
        return mantenimientoRepository.findById(id)
                .filter(Mantenimiento::getActivo)
                .map(this::mapToResponse);
    }
    
    /**
     * Obtiene todos los mantenimientos de un vehículo
     */
    @Transactional(readOnly = true)
    public List<MantenimientoResponse> obtenerMantenimientosPorVehiculo(String vehicleId) {
        return mantenimientoRepository.findByVehicleIdAndActivoTrue(Long.parseLong(vehicleId))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los mantenimientos
     */
    @Transactional(readOnly = true)
    public List<MantenimientoResponse> obtenerTodosLosMantenimientos() {
        return mantenimientoRepository.findAll()
                .stream()
                .filter(Mantenimiento::getActivo)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene mantenimientos por tipo
     */
    @Transactional(readOnly = true)
    public List<MantenimientoResponse> obtenerMantenimientosPorTipo(String tipoMantenimiento) {
        return mantenimientoRepository.findByTipoMantenimientoAndActivoTrue(tipoMantenimiento)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene mantenimientos por estado
     */
    @Transactional(readOnly = true)
    public List<MantenimientoResponse> obtenerMantenimientosPorEstado(Mantenimiento.EstadoMantenimiento estado) {
        return mantenimientoRepository.findByEstadoAndActivoTrue(estado)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene mantenimientos próximos a vencer
     */
    @Transactional(readOnly = true)
    public List<MantenimientoResponse> obtenerMantenimientosProximosAVencer(int diasAdelante) {
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(diasAdelante);
        return mantenimientoRepository.findMantenimientosProximosAVencer(fechaLimite)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Actualiza el estado de un mantenimiento
     */
    public MantenimientoResponse actualizarEstadoMantenimiento(String id, Mantenimiento.EstadoMantenimiento nuevoEstado) {
        Mantenimiento mantenimiento = mantenimientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mantenimiento no encontrado con ID: " + id));
        
        if (!mantenimiento.getActivo()) {
            throw new IllegalArgumentException("No se puede actualizar un mantenimiento inactivo");
        }
        
        mantenimiento.setEstado(nuevoEstado);
        Mantenimiento updatedMantenimiento = mantenimientoRepository.save(mantenimiento);
        return mapToResponse(updatedMantenimiento);
    }
    
    /**
     * Elimina un mantenimiento (soft delete)
     */
    public void eliminarMantenimiento(String id) {
        Mantenimiento mantenimiento = mantenimientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mantenimiento no encontrado con ID: " + id));
        
        mantenimiento.setActivo(false);
        mantenimientoRepository.save(mantenimiento);
    }
    
    /**
     * Obtiene estadísticas de mantenimientos por vehículo
     */
    @Transactional(readOnly = true)
    public MantenimientoStatsDTO obtenerEstadisticasMantenimiento(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId.toString())
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + vehicleId));
        
        Long totalMantenimientos = mantenimientoRepository.countMantenimientosPorVehiculo(vehicle);
        Double costoTotal = mantenimientoRepository.calcularCostoTotalMantenimientosPorVehiculo(vehicle);
        
        return new MantenimientoStatsDTO(vehicleId, vehicle.getPlaca(), totalMantenimientos, costoTotal);
    }
    
    /**
     * Mapea entidad a DTO de respuesta
     */
    private MantenimientoResponse mapToResponse(Mantenimiento mantenimiento) {
        MantenimientoResponse response = new MantenimientoResponse();
        response.setId(Long.parseLong(mantenimiento.getId()));
        response.setVehicleId(Long.parseLong(mantenimiento.getVehicle().getId()));
        response.setPlacaVehiculo(mantenimiento.getVehicle().getPlaca());
        response.setMarcaVehiculo(mantenimiento.getVehicle().getMarca());
        response.setModeloVehiculo(mantenimiento.getVehicle().getModelo());
        response.setTipoMantenimiento(mantenimiento.getTipoMantenimiento());
        response.setDescripcion(mantenimiento.getDescripcion());
        response.setFechaMantenimiento(mantenimiento.getFechaMantenimiento());
        response.setFechaProximoMantenimiento(mantenimiento.getFechaProximoMantenimiento());
        response.setCosto(mantenimiento.getCosto());
        response.setKilometrajeMantenimiento(mantenimiento.getKilometrajeMantenimiento());
        response.setProveedor(mantenimiento.getProveedor());
        response.setObservaciones(mantenimiento.getObservaciones());
        response.setEstado(mantenimiento.getEstado().toString());
        response.setFechaCreacion(mantenimiento.getFechaCreacion());
        response.setFechaActualizacion(mantenimiento.getFechaActualizacion());
        response.setActivo(mantenimiento.getActivo());
        
        return response;
    }
    
    /**
     * DTO para estadísticas de mantenimiento
     */
    public static class MantenimientoStatsDTO {
        private Long vehicleId;
        private String placaVehiculo;
        private Long totalMantenimientos;
        private Double costoTotal;
        
        public MantenimientoStatsDTO(Long vehicleId, String placaVehiculo, Long totalMantenimientos, Double costoTotal) {
            this.vehicleId = vehicleId;
            this.placaVehiculo = placaVehiculo;
            this.totalMantenimientos = totalMantenimientos;
            this.costoTotal = costoTotal;
        }
        
        // Getters y Setters
        public Long getVehicleId() { return vehicleId; }
        public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
        
        public String getPlacaVehiculo() { return placaVehiculo; }
        public void setPlacaVehiculo(String placaVehiculo) { this.placaVehiculo = placaVehiculo; }
        
        public Long getTotalMantenimientos() { return totalMantenimientos; }
        public void setTotalMantenimientos(Long totalMantenimientos) { this.totalMantenimientos = totalMantenimientos; }
        
        public Double getCostoTotal() { return costoTotal; }
        public void setCostoTotal(Double costoTotal) { this.costoTotal = costoTotal; }
    }
}
