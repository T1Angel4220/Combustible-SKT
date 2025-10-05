package com.skt.combustible.vehicles.application.service;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.domain.dto.VehicleCreateRequest;
import com.skt.combustible.vehicles.domain.dto.VehicleResponse;
import com.skt.combustible.vehicles.domain.dto.VehicleUpdateRequest;
import com.skt.combustible.vehicles.domain.entity.Vehicle;
import com.skt.combustible.vehicles.domain.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para la gestión de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Service
@Transactional
public class VehicleService {
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Crea un nuevo vehículo
     */
    public VehicleResponse crearVehiculo(VehicleCreateRequest request) {
        // Validar que la placa no exista
        if (vehicleRepository.existsByPlaca(request.getPlaca())) {
            throw new IllegalArgumentException("Ya existe un vehículo con la placa: " + request.getPlaca());
        }
        
        Vehicle vehicle = new Vehicle(
            request.getPlaca(),
            request.getMarca(),
            request.getModelo(),
            request.getAnio(),
            request.getTipoMaquinaria(),
            request.getEstadoOperativo()
        );
        
        // Asignar campos opcionales
        if (request.getCapacidadTanque() != null) {
            vehicle.setCapacidadTanque(request.getCapacidadTanque());
        }
        if (request.getConsumoPromedio() != null) {
            vehicle.setConsumoPromedio(request.getConsumoPromedio());
        }
        if (request.getKilometrajeActual() != null) {
            vehicle.setKilometrajeActual(request.getKilometrajeActual());
        }
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(savedVehicle);
    }
    
    /**
     * Obtiene un vehículo por ID
     */
    @Transactional(readOnly = true)
    public Optional<VehicleResponse> obtenerVehiculoPorId(String id) {
        return vehicleRepository.findById(id)
                .map(this::mapToResponse);
    }
    
    /**
     * Obtiene un vehículo por placa
     */
    @Transactional(readOnly = true)
    public Optional<VehicleResponse> obtenerVehiculoPorPlaca(String placa) {
        return vehicleRepository.findByPlaca(placa)
                .map(this::mapToResponse);
    }
    
    /**
     * Obtiene todos los vehículos
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> obtenerTodosLosVehiculos() {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene vehículos por tipo de maquinaria
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> obtenerVehiculosPorTipo(TipoMaquinaria tipoMaquinaria) {
        return vehicleRepository.findByTipoMaquinaria(tipoMaquinaria)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene vehículos por estado operativo
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> obtenerVehiculosPorEstado(EstadoOperativo estadoOperativo) {
        return vehicleRepository.findByEstadoOperativo(estadoOperativo)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene vehículos disponibles
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> obtenerVehiculosDisponibles() {
        return vehicleRepository.findVehiclesDisponibles()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene vehículos disponibles por tipo
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> obtenerVehiculosDisponiblesPorTipo(TipoMaquinaria tipoMaquinaria) {
        return vehicleRepository.findVehiclesDisponiblesByTipo(tipoMaquinaria)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Actualiza un vehículo
     */
    public VehicleResponse actualizarVehiculo(String id, VehicleUpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
        
        // Actualizar campos si se proporcionan
        if (request.getMarca() != null) {
            vehicle.setMarca(request.getMarca());
        }
        if (request.getModelo() != null) {
            vehicle.setModelo(request.getModelo());
        }
        if (request.getAnio() != null) {
            vehicle.setAnio(request.getAnio());
        }
        if (request.getTipoMaquinaria() != null) {
            vehicle.setTipoMaquinaria(request.getTipoMaquinaria());
        }
        if (request.getEstadoOperativo() != null) {
            vehicle.cambiarEstadoOperativo(request.getEstadoOperativo());
        }
        if (request.getCapacidadTanque() != null) {
            vehicle.setCapacidadTanque(request.getCapacidadTanque());
        }
        if (request.getConsumoPromedio() != null) {
            vehicle.setConsumoPromedio(request.getConsumoPromedio());
        }
        if (request.getKilometrajeActual() != null) {
            vehicle.actualizarKilometraje(request.getKilometrajeActual());
        }
        if (request.getActivo() != null) {
            if (!request.getActivo()) {
                vehicle.desactivar();
            } else {
                vehicle.setActivo(true);
            }
        }
        
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(updatedVehicle);
    }
    
    /**
     * Cambia el estado operativo de un vehículo
     */
    public VehicleResponse cambiarEstadoVehiculo(String id, EstadoOperativo nuevoEstado) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
        
        vehicle.cambiarEstadoOperativo(nuevoEstado);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(updatedVehicle);
    }
    
    /**
     * Actualiza el kilometraje de un vehículo
     */
    public VehicleResponse actualizarKilometraje(String id, Double nuevoKilometraje) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
        
        vehicle.actualizarKilometraje(nuevoKilometraje);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(updatedVehicle);
    }
    
    /**
     * Desactiva un vehículo (eliminación lógica)
     */
    public void desactivarVehiculo(String id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
        
        vehicle.desactivar();
        vehicleRepository.save(vehicle);
    }
    
    /**
     * Elimina un vehículo físicamente
     */
    public void eliminarVehiculo(String id) {
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehículo no encontrado con ID: " + id);
        }
        vehicleRepository.deleteById(id);
    }
    
    /**
     * Obtiene estadísticas de vehículos
     */
    @Transactional(readOnly = true)
    public VehicleStatsDTO obtenerEstadisticas() {
        long totalVehiculos = vehicleRepository.countByTipoMaquinaria(TipoMaquinaria.CAMION) +
                             vehicleRepository.countByTipoMaquinaria(TipoMaquinaria.VOLQUETE) +
                             vehicleRepository.countByTipoMaquinaria(TipoMaquinaria.EXCAVADORA) +
                             vehicleRepository.countByTipoMaquinaria(TipoMaquinaria.CARGADOR) +
                             vehicleRepository.countByTipoMaquinaria(TipoMaquinaria.GRUA) +
                             vehicleRepository.countByTipoMaquinaria(TipoMaquinaria.MOTONIVELADORA);
        
        long vehiculosDisponibles = vehicleRepository.countByEstadoOperativo(EstadoOperativo.DISPONIBLE);
        long vehiculosEnUso = vehicleRepository.countByEstadoOperativo(EstadoOperativo.EN_USO);
        long vehiculosEnMantenimiento = vehicleRepository.countByEstadoOperativo(EstadoOperativo.MANTENIMIENTO);
        
        return new VehicleStatsDTO(totalVehiculos, vehiculosDisponibles, vehiculosEnUso, vehiculosEnMantenimiento);
    }
    
    /**
     * Mapea una entidad Vehicle a VehicleResponse
     */
    private VehicleResponse mapToResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(Long.parseLong(vehicle.getId()));
        response.setPlaca(vehicle.getPlaca());
        response.setMarca(vehicle.getMarca());
        response.setModelo(vehicle.getModelo());
        response.setAnio(vehicle.getAnio());
        response.setTipoMaquinaria(vehicle.getTipoMaquinaria());
        response.setEstadoOperativo(vehicle.getEstadoOperativo());
        response.setCapacidadTanque(vehicle.getCapacidadTanque());
        response.setConsumoPromedio(vehicle.getConsumoPromedio());
        response.setKilometrajeActual(vehicle.getKilometrajeActual());
        response.setFechaCreacion(vehicle.getFechaCreacion());
        response.setFechaActualizacion(vehicle.getFechaActualizacion());
        response.setActivo(vehicle.getActivo());
        return response;
    }
    
    /**
     * DTO para estadísticas de vehículos
     */
    public static class VehicleStatsDTO {
        private final long totalVehiculos;
        private final long vehiculosDisponibles;
        private final long vehiculosEnUso;
        private final long vehiculosEnMantenimiento;
        
        public VehicleStatsDTO(long totalVehiculos, long vehiculosDisponibles, 
                              long vehiculosEnUso, long vehiculosEnMantenimiento) {
            this.totalVehiculos = totalVehiculos;
            this.vehiculosDisponibles = vehiculosDisponibles;
            this.vehiculosEnUso = vehiculosEnUso;
            this.vehiculosEnMantenimiento = vehiculosEnMantenimiento;
        }
        
        // Getters
        public long getTotalVehiculos() { return totalVehiculos; }
        public long getVehiculosDisponibles() { return vehiculosDisponibles; }
        public long getVehiculosEnUso() { return vehiculosEnUso; }
        public long getVehiculosEnMantenimiento() { return vehiculosEnMantenimiento; }
    }
}
