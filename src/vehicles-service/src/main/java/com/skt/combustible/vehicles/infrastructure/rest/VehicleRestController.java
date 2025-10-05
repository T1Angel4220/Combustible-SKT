package com.skt.combustible.vehicles.infrastructure.rest;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.application.service.VehicleService;
import com.skt.combustible.vehicles.domain.dto.VehicleCreateRequest;
import com.skt.combustible.vehicles.domain.dto.VehicleResponse;
import com.skt.combustible.vehicles.domain.dto.VehicleUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = "*")
public class VehicleRestController {
    
    @Autowired
    private VehicleService vehicleService;
    
    /**
     * Crea un nuevo vehículo
     */
    @PostMapping
    public ResponseEntity<VehicleResponse> crearVehiculo(@Valid @RequestBody VehicleCreateRequest request) {
        try {
            VehicleResponse response = vehicleService.crearVehiculo(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene un vehículo por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> obtenerVehiculoPorId(@PathVariable String id) {
        Optional<VehicleResponse> response = vehicleService.obtenerVehiculoPorId(id);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Obtiene un vehículo por placa
     */
    @GetMapping("/placa/{placa}")
    public ResponseEntity<VehicleResponse> obtenerVehiculoPorPlaca(@PathVariable String placa) {
        Optional<VehicleResponse> response = vehicleService.obtenerVehiculoPorPlaca(placa);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Obtiene todos los vehículos
     */
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> obtenerTodosLosVehiculos() {
        List<VehicleResponse> responses = vehicleService.obtenerTodosLosVehiculos();
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Obtiene vehículos por tipo de maquinaria
     */
    @GetMapping("/tipo/{tipoMaquinaria}")
    public ResponseEntity<List<VehicleResponse>> obtenerVehiculosPorTipo(@PathVariable TipoMaquinaria tipoMaquinaria) {
        List<VehicleResponse> responses = vehicleService.obtenerVehiculosPorTipo(tipoMaquinaria);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Obtiene vehículos por estado operativo
     */
    @GetMapping("/estado/{estadoOperativo}")
    public ResponseEntity<List<VehicleResponse>> obtenerVehiculosPorEstado(@PathVariable EstadoOperativo estadoOperativo) {
        List<VehicleResponse> responses = vehicleService.obtenerVehiculosPorEstado(estadoOperativo);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Obtiene vehículos disponibles
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<VehicleResponse>> obtenerVehiculosDisponibles() {
        List<VehicleResponse> responses = vehicleService.obtenerVehiculosDisponibles();
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Obtiene vehículos disponibles por tipo
     */
    @GetMapping("/disponibles/tipo/{tipoMaquinaria}")
    public ResponseEntity<List<VehicleResponse>> obtenerVehiculosDisponiblesPorTipo(@PathVariable TipoMaquinaria tipoMaquinaria) {
        List<VehicleResponse> responses = vehicleService.obtenerVehiculosDisponiblesPorTipo(tipoMaquinaria);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Actualiza un vehículo
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> actualizarVehiculo(@PathVariable String id, 
                                                               @Valid @RequestBody VehicleUpdateRequest request) {
        try {
            VehicleResponse response = vehicleService.actualizarVehiculo(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Cambia el estado de un vehículo
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<VehicleResponse> cambiarEstadoVehiculo(@PathVariable String id, 
                                                               @RequestParam EstadoOperativo estado) {
        try {
            VehicleResponse response = vehicleService.cambiarEstadoVehiculo(id, estado);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Actualiza el kilometraje de un vehículo
     */
    @PatchMapping("/{id}/kilometraje")
    public ResponseEntity<VehicleResponse> actualizarKilometraje(@PathVariable String id, 
                                                                @RequestParam Double kilometraje) {
        try {
            VehicleResponse response = vehicleService.actualizarKilometraje(id, kilometraje);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Desactiva un vehículo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarVehiculo(@PathVariable String id) {
        try {
            vehicleService.desactivarVehiculo(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene estadísticas de vehículos
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<VehicleService.VehicleStatsDTO> obtenerEstadisticas() {
        VehicleService.VehicleStatsDTO stats = vehicleService.obtenerEstadisticas();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Vehicles Service is running");
    }
}
