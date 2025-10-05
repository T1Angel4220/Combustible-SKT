package com.skt.combustible.vehicles.application.service;

import com.skt.combustible.shared.domain.enums.EstadoOperativo;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.domain.entity.Vehicle;
import com.skt.combustible.vehicles.domain.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de reportes y consultas para vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class VehicleReportService {
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Genera reporte de vehículos por tipo de maquinaria
     */
    public Map<TipoMaquinaria, Long> reporteVehiculosPorTipo() {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .collect(Collectors.groupingBy(
                    Vehicle::getTipoMaquinaria,
                    Collectors.counting()
                ));
    }
    
    /**
     * Genera reporte de vehículos por estado operativo
     */
    public Map<EstadoOperativo, Long> reporteVehiculosPorEstado() {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .collect(Collectors.groupingBy(
                    Vehicle::getEstadoOperativo,
                    Collectors.counting()
                ));
    }
    
    /**
     * Genera reporte de vehículos por marca
     */
    public Map<String, Long> reporteVehiculosPorMarca() {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .collect(Collectors.groupingBy(
                    Vehicle::getMarca,
                    Collectors.counting()
                ));
    }
    
    /**
     * Genera reporte de vehículos por año
     */
    public Map<Integer, Long> reporteVehiculosPorAnio() {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .collect(Collectors.groupingBy(
                    Vehicle::getAnio,
                    Collectors.counting()
                ));
    }
    
    /**
     * Genera reporte de consumo promedio por tipo de maquinaria
     */
    public Map<TipoMaquinaria, Double> reporteConsumoPromedioPorTipo() {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .filter(v -> v.getConsumoPromedio() != null)
                .collect(Collectors.groupingBy(
                    Vehicle::getTipoMaquinaria,
                    Collectors.averagingDouble(Vehicle::getConsumoPromedio)
                ));
    }
    
    /**
     * Genera reporte de vehículos con mayor consumo
     */
    public List<Vehicle> reporteVehiculosConMayorConsumo(int limite) {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .filter(v -> v.getConsumoPromedio() != null)
                .sorted((v1, v2) -> Double.compare(v2.getConsumoPromedio(), v1.getConsumoPromedio()))
                .limit(limite)
                .collect(Collectors.toList());
    }
    
    /**
     * Genera reporte de vehículos con menor consumo
     */
    public List<Vehicle> reporteVehiculosConMenorConsumo(int limite) {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .filter(v -> v.getConsumoPromedio() != null)
                .sorted((v1, v2) -> Double.compare(v1.getConsumoPromedio(), v2.getConsumoPromedio()))
                .limit(limite)
                .collect(Collectors.toList());
    }
    
    /**
     * Genera reporte de vehículos por rango de años
     */
    public List<Vehicle> reporteVehiculosPorRangoAnios(Integer anioInicio, Integer anioFin) {
        return vehicleRepository.findVehiclesByAnioRange(anioInicio, anioFin);
    }
    
    /**
     * Genera reporte de vehículos por marca (búsqueda)
     */
    public List<Vehicle> reporteVehiculosPorMarca(String marca) {
        return vehicleRepository.findByMarcaContainingIgnoreCase(marca);
    }
    
    /**
     * Genera reporte de vehículos por modelo (búsqueda)
     */
    public List<Vehicle> reporteVehiculosPorModelo(String modelo) {
        return vehicleRepository.findByModeloContainingIgnoreCase(modelo);
    }
    
    /**
     * Genera reporte de vehículos con consumo mayor a un valor
     */
    public List<Vehicle> reporteVehiculosConConsumoMayorA(Double consumo) {
        return vehicleRepository.findVehiclesWithConsumoMayorA(consumo);
    }
    
    /**
     * Genera reporte de vehículos en mantenimiento
     */
    public List<Vehicle> reporteVehiculosEnMantenimiento() {
        return vehicleRepository.findVehiclesEnMantenimiento();
    }
    
    /**
     * Genera reporte de vehículos en uso
     */
    public List<Vehicle> reporteVehiculosEnUso() {
        return vehicleRepository.findVehiclesEnUso();
    }
    
    /**
     * Genera reporte de vehículos disponibles
     */
    public List<Vehicle> reporteVehiculosDisponibles() {
        return vehicleRepository.findVehiclesDisponibles();
    }
    
    /**
     * Genera reporte de vehículos disponibles por tipo
     */
    public List<Vehicle> reporteVehiculosDisponiblesPorTipo(TipoMaquinaria tipoMaquinaria) {
        return vehicleRepository.findVehiclesDisponiblesByTipo(tipoMaquinaria);
    }
    
    /**
     * Genera reporte de vehículos por tipo y estado
     */
    public List<Vehicle> reporteVehiculosPorTipoYEstado(TipoMaquinaria tipoMaquinaria, EstadoOperativo estadoOperativo) {
        return vehicleRepository.findByTipoMaquinariaAndEstadoOperativo(tipoMaquinaria, estadoOperativo);
    }
    
    /**
     * Genera reporte de vehículos creados en un rango de fechas
     */
    public List<Vehicle> reporteVehiculosCreadosEnRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .filter(v -> v.getFechaCreacion().isAfter(fechaInicio) && v.getFechaCreacion().isBefore(fechaFin))
                .collect(Collectors.toList());
    }
    
    /**
     * Genera reporte de vehículos actualizados en un rango de fechas
     */
    public List<Vehicle> reporteVehiculosActualizadosEnRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .filter(v -> v.getFechaActualizacion() != null)
                .filter(v -> v.getFechaActualizacion().isAfter(fechaInicio) && v.getFechaActualizacion().isBefore(fechaFin))
                .collect(Collectors.toList());
    }
    
    /**
     * Genera reporte de vehículos por capacidad de tanque
     */
    public Map<String, Long> reporteVehiculosPorCapacidadTanque() {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .filter(v -> v.getCapacidadTanque() != null)
                .collect(Collectors.groupingBy(
                    v -> {
                        Double capacidad = v.getCapacidadTanque();
                        if (capacidad <= 50) return "0-50L";
                        else if (capacidad <= 100) return "51-100L";
                        else if (capacidad <= 200) return "101-200L";
                        else return "200L+";
                    },
                    Collectors.counting()
                ));
    }
    
    /**
     * Genera reporte de vehículos por kilometraje
     */
    public Map<String, Long> reporteVehiculosPorKilometraje() {
        return vehicleRepository.findByActivoTrue()
                .stream()
                .filter(v -> v.getKilometrajeActual() != null)
                .collect(Collectors.groupingBy(
                    v -> {
                        Double kilometraje = v.getKilometrajeActual();
                        if (kilometraje <= 10000) return "0-10K km";
                        else if (kilometraje <= 50000) return "10K-50K km";
                        else if (kilometraje <= 100000) return "50K-100K km";
                        else return "100K+ km";
                    },
                    Collectors.counting()
                ));
    }
}
