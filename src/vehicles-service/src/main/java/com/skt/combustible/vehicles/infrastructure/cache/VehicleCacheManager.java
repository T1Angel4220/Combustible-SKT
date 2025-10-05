package com.skt.combustible.vehicles.infrastructure.cache;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import com.skt.combustible.vehicles.domain.entity.Vehicle;
import com.skt.combustible.vehicles.domain.repository.VehicleRepository;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.shared.domain.enums.EstadoOperativo;

import java.util.List;
import java.util.Optional;

/**
 * Gestor de caché para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Component
public class VehicleCacheManager {
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Obtiene un vehículo del caché o de la base de datos
     */
    public Optional<Vehicle> getVehicle(Long id) {
        Cache cache = cacheManager.getCache("vehicles");
        if (cache != null) {
            Vehicle vehicle = cache.get(id, Vehicle.class);
            if (vehicle != null) {
                return Optional.of(vehicle);
            }
        }
        
        Optional<Vehicle> vehicle = vehicleRepository.findById(id.toString());
        if (vehicle.isPresent() && cache != null) {
            cache.put(id.toString(), vehicle.get());
        }
        
        return vehicle;
    }
    
    /**
     * Obtiene vehículos por tipo del caché o de la base de datos
     */
    public List<Vehicle> getVehiclesByType(TipoMaquinaria tipo) {
        Cache cache = cacheManager.getCache("vehiclesByType");
        if (cache != null) {
            List<Vehicle> vehicles = cache.get(tipo, List.class);
            if (vehicles != null) {
                return vehicles;
            }
        }
        
        List<Vehicle> vehicles = vehicleRepository.findByTipoMaquinaria(tipo);
        if (cache != null) {
            cache.put(tipo, vehicles);
        }
        
        return vehicles;
    }
    
    /**
     * Obtiene vehículos por estado del caché o de la base de datos
     */
    public List<Vehicle> getVehiclesByState(EstadoOperativo estado) {
        Cache cache = cacheManager.getCache("vehiclesByState");
        if (cache != null) {
            List<Vehicle> vehicles = cache.get(estado, List.class);
            if (vehicles != null) {
                return vehicles;
            }
        }
        
        List<Vehicle> vehicles = vehicleRepository.findByEstadoOperativo(estado);
        if (cache != null) {
            cache.put(estado, vehicles);
        }
        
        return vehicles;
    }
    
    /**
     * Obtiene vehículos disponibles del caché o de la base de datos
     */
    public List<Vehicle> getAvailableVehicles() {
        Cache cache = cacheManager.getCache("vehicleStats");
        if (cache != null) {
            List<Vehicle> vehicles = cache.get("available", List.class);
            if (vehicles != null) {
                return vehicles;
            }
        }
        
        List<Vehicle> vehicles = vehicleRepository.findVehiclesDisponibles();
        if (cache != null) {
            cache.put("available", vehicles);
        }
        
        return vehicles;
    }
    
    /**
     * Invalida el caché de un vehículo específico
     */
    public void evictVehicle(Long id) {
        Cache cache = cacheManager.getCache("vehicles");
        if (cache != null) {
            cache.evict(id);
        }
    }
    
    /**
     * Invalida el caché de vehículos por tipo
     */
    public void evictVehiclesByType(TipoMaquinaria tipo) {
        Cache cache = cacheManager.getCache("vehiclesByType");
        if (cache != null) {
            cache.evict(tipo);
        }
    }
    
    /**
     * Invalida el caché de vehículos por estado
     */
    public void evictVehiclesByState(EstadoOperativo estado) {
        Cache cache = cacheManager.getCache("vehiclesByState");
        if (cache != null) {
            cache.evict(estado);
        }
    }
    
    /**
     * Invalida todo el caché
     */
    public void evictAllCache() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }
    
    /**
     * Pone un vehículo en el caché
     */
    public void putVehicle(Vehicle vehicle) {
        Cache cache = cacheManager.getCache("vehicles");
        if (cache != null) {
            cache.put(vehicle.getId(), vehicle);
        }
    }
    
    /**
     * Pone vehículos por tipo en el caché
     */
    public void putVehiclesByType(TipoMaquinaria tipo, List<Vehicle> vehicles) {
        Cache cache = cacheManager.getCache("vehiclesByType");
        if (cache != null) {
            cache.put(tipo, vehicles);
        }
    }
    
    /**
     * Pone vehículos por estado en el caché
     */
    public void putVehiclesByState(EstadoOperativo estado, List<Vehicle> vehicles) {
        Cache cache = cacheManager.getCache("vehiclesByState");
        if (cache != null) {
            cache.put(estado, vehicles);
        }
    }
}
