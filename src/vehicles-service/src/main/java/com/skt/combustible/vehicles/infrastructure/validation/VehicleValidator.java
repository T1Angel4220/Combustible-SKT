package com.skt.combustible.vehicles.infrastructure.validation;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.skt.combustible.vehicles.domain.entity.Vehicle;
import com.skt.combustible.vehicles.domain.repository.VehicleRepository;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.shared.domain.enums.EstadoOperativo;

import java.util.ArrayList;
import java.util.List;

/**
 * Validador para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Component
public class VehicleValidator {
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Valida un vehículo antes de guardarlo
     */
    public List<String> validateVehicle(Vehicle vehicle) {
        List<String> errors = new ArrayList<>();
        
        // Validar placa
        if (vehicle.getPlaca() == null || vehicle.getPlaca().trim().isEmpty()) {
            errors.add("La placa es obligatoria");
        } else if (vehicle.getPlaca().length() > 10) {
            errors.add("La placa no puede tener más de 10 caracteres");
        } else if (vehicleRepository.existsByPlaca(vehicle.getPlaca())) {
            errors.add("Ya existe un vehículo con la placa: " + vehicle.getPlaca());
        }
        
        // Validar marca
        if (vehicle.getMarca() == null || vehicle.getMarca().trim().isEmpty()) {
            errors.add("La marca es obligatoria");
        } else if (vehicle.getMarca().length() > 50) {
            errors.add("La marca no puede tener más de 50 caracteres");
        }
        
        // Validar modelo
        if (vehicle.getModelo() == null || vehicle.getModelo().trim().isEmpty()) {
            errors.add("El modelo es obligatorio");
        } else if (vehicle.getModelo().length() > 50) {
            errors.add("El modelo no puede tener más de 50 caracteres");
        }
        
        // Validar año
        if (vehicle.getAnio() == null) {
            errors.add("El año es obligatorio");
        } else if (vehicle.getAnio() < 1900 || vehicle.getAnio() > 2030) {
            errors.add("El año debe estar entre 1900 y 2030");
        }
        
        // Validar tipo de maquinaria
        if (vehicle.getTipoMaquinaria() == null) {
            errors.add("El tipo de maquinaria es obligatorio");
        }
        
        // Validar estado operativo
        if (vehicle.getEstadoOperativo() == null) {
            errors.add("El estado operativo es obligatorio");
        }
        
        // Validar capacidad de tanque
        if (vehicle.getCapacidadTanque() != null && vehicle.getCapacidadTanque() < 0) {
            errors.add("La capacidad del tanque no puede ser negativa");
        }
        
        // Validar consumo promedio
        if (vehicle.getConsumoPromedio() != null && vehicle.getConsumoPromedio() < 0) {
            errors.add("El consumo promedio no puede ser negativo");
        }
        
        // Validar kilometraje actual
        if (vehicle.getKilometrajeActual() != null && vehicle.getKilometrajeActual() < 0) {
            errors.add("El kilometraje actual no puede ser negativo");
        }
        
        return errors;
    }
    
    /**
     * Valida la actualización de un vehículo
     */
    public List<String> validateVehicleUpdate(Vehicle vehicle) {
        List<String> errors = new ArrayList<>();
        
        // Validar que el vehículo exista
        if (vehicle.getId() == null || !vehicleRepository.existsById(vehicle.getId())) {
            errors.add("El vehículo no existe");
            return errors;
        }
        
        // Validar marca si se proporciona
        if (vehicle.getMarca() != null && vehicle.getMarca().trim().isEmpty()) {
            errors.add("La marca no puede estar vacía");
        } else if (vehicle.getMarca() != null && vehicle.getMarca().length() > 50) {
            errors.add("La marca no puede tener más de 50 caracteres");
        }
        
        // Validar modelo si se proporciona
        if (vehicle.getModelo() != null && vehicle.getModelo().trim().isEmpty()) {
            errors.add("El modelo no puede estar vacío");
        } else if (vehicle.getModelo() != null && vehicle.getModelo().length() > 50) {
            errors.add("El modelo no puede tener más de 50 caracteres");
        }
        
        // Validar año si se proporciona
        if (vehicle.getAnio() != null && (vehicle.getAnio() < 1900 || vehicle.getAnio() > 2030)) {
            errors.add("El año debe estar entre 1900 y 2030");
        }
        
        // Validar capacidad de tanque si se proporciona
        if (vehicle.getCapacidadTanque() != null && vehicle.getCapacidadTanque() < 0) {
            errors.add("La capacidad del tanque no puede ser negativa");
        }
        
        // Validar consumo promedio si se proporciona
        if (vehicle.getConsumoPromedio() != null && vehicle.getConsumoPromedio() < 0) {
            errors.add("El consumo promedio no puede ser negativo");
        }
        
        // Validar kilometraje actual si se proporciona
        if (vehicle.getKilometrajeActual() != null && vehicle.getKilometrajeActual() < 0) {
            errors.add("El kilometraje actual no puede ser negativo");
        }
        
        return errors;
    }
    
    /**
     * Valida el cambio de estado de un vehículo
     */
    public List<String> validateStateChange(Vehicle vehicle, EstadoOperativo newState) {
        List<String> errors = new ArrayList<>();
        
        // Validar que el vehículo exista
        if (vehicle == null) {
            errors.add("El vehículo no existe");
            return errors;
        }
        
        // Validar que el nuevo estado sea válido
        if (newState == null) {
            errors.add("El nuevo estado es obligatorio");
            return errors;
        }
        
        // Validar transiciones de estado válidas
        EstadoOperativo currentState = vehicle.getEstadoOperativo();
        
        if (currentState == EstadoOperativo.FUERA_SERVICIO && newState != EstadoOperativo.DISPONIBLE) {
            errors.add("Un vehículo fuera de servicio solo puede cambiar a disponible");
        }
        
        if (currentState == EstadoOperativo.MANTENIMIENTO && 
            (newState != EstadoOperativo.DISPONIBLE && newState != EstadoOperativo.FUERA_SERVICIO)) {
            errors.add("Un vehículo en mantenimiento solo puede cambiar a disponible o fuera de servicio");
        }
        
        if (currentState == EstadoOperativo.EN_USO && newState != EstadoOperativo.DISPONIBLE) {
            errors.add("Un vehículo en uso solo puede cambiar a disponible");
        }
        
        return errors;
    }
    
    /**
     * Valida la actualización de kilometraje
     */
    public List<String> validateMileageUpdate(Vehicle vehicle, Double newMileage) {
        List<String> errors = new ArrayList<>();
        
        // Validar que el vehículo exista
        if (vehicle == null) {
            errors.add("El vehículo no existe");
            return errors;
        }
        
        // Validar que el nuevo kilometraje sea válido
        if (newMileage == null) {
            errors.add("El nuevo kilometraje es obligatorio");
            return errors;
        }
        
        if (newMileage < 0) {
            errors.add("El kilometraje no puede ser negativo");
            return errors;
        }
        
        // Validar que el nuevo kilometraje sea mayor al actual
        if (vehicle.getKilometrajeActual() != null && newMileage <= vehicle.getKilometrajeActual()) {
            errors.add("El nuevo kilometraje debe ser mayor al actual");
        }
        
        return errors;
    }
}
