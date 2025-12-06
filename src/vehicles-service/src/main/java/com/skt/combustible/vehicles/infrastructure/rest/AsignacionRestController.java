package com.skt.combustible.vehicles.infrastructure.rest;

import com.skt.combustible.vehicles.application.service.AsignacionService;
import com.skt.combustible.vehicles.domain.dto.AsignacionCreateRequest;
import com.skt.combustible.vehicles.domain.dto.AsignacionResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el servicio de asignaciones de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/assignments")
@CrossOrigin(origins = "*")
public class AsignacionRestController {
    
    @Autowired
    private AsignacionService asignacionService;
    
    /**
     * Obtiene asignaciones activas por chofer
     */
    @GetMapping("/chofer/{choferId}")
    public ResponseEntity<List<AsignacionResponse>> obtenerAsignacionesPorChofer(@PathVariable("choferId") String choferId) {
        try {
            List<AsignacionResponse> asignaciones = asignacionService.obtenerAsignacionesActivasPorChofer(choferId);
            return ResponseEntity.ok(asignaciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Asigna un vehículo a un chofer
     */
    @PostMapping
    public ResponseEntity<?> asignarVehiculoAChofer(@Valid @RequestBody AsignacionCreateRequest request) {
        try {
            AsignacionResponse response = asignacionService.asignarVehiculoAChofer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Dejar que el ExceptionHandlerConfig maneje la excepción con el mensaje
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene asignaciones por vehículo
     */
    @GetMapping("/vehiculo/{vehicleId}")
    public ResponseEntity<List<AsignacionResponse>> obtenerAsignacionesPorVehiculo(@PathVariable("vehicleId") String vehicleId) {
        try {
            List<AsignacionResponse> asignaciones = asignacionService.obtenerAsignacionesPorVehiculo(vehicleId);
            return ResponseEntity.ok(asignaciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Desasigna un vehículo de un chofer
     */
    @DeleteMapping("/vehiculo/{vehicleId}")
    public ResponseEntity<Void> desasignarVehiculo(@PathVariable("vehicleId") String vehicleId) {
        try {
            asignacionService.desasignarVehiculo(vehicleId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

