package com.skt.combustible.routes.application.service;

import com.skt.combustible.drivers.grpc.DriverResponse;
import com.skt.combustible.routes.domain.dto.RouteCreateRequest;
import com.skt.combustible.routes.domain.dto.RouteResponse;
import com.skt.combustible.routes.domain.dto.RouteUpdateRequest;
import com.skt.combustible.routes.domain.entity.Route;
import com.skt.combustible.routes.domain.exception.RouteNotFoundException;
import com.skt.combustible.routes.domain.repository.RouteRepository;
import com.skt.combustible.routes.infrastructure.client.AssignmentsRestClient;
import com.skt.combustible.routes.infrastructure.client.DriversGrpcClient;
import com.skt.combustible.routes.infrastructure.client.VehiclesGrpcClient;
import com.skt.combustible.shared.domain.enums.TipoMaquinaria;
import com.skt.combustible.vehicles.grpc.VehicleResponseProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para la gestión de rutas
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Service
@Transactional
public class RouteService {

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private DriversGrpcClient driversGrpcClient;

    @Autowired
    private VehiclesGrpcClient vehiclesGrpcClient;

    @Autowired
    private AssignmentsRestClient assignmentsRestClient;

    /**
     * Genera un código único para una nueva ruta
     */
    private String generarCodigoRuta() {
        long totalRutas = routeRepository.count();
        return String.format("RUT-%03d", totalRutas + 1);
    }

    /**
     * Calcula el consumo estimado de combustible basado en distancia y tipo de maquinaria
     */
    private Double calcularConsumoEstimado(Double distanciaKm, VehicleResponseProto vehicle) {
        if (distanciaKm == null || distanciaKm <= 0) {
            return 0.0;
        }

        // Si el vehículo tiene consumo promedio definido, usarlo
        if (vehicle.getConsumoPromedio() > 0) {
            return (distanciaKm / 100.0) * vehicle.getConsumoPromedio();
        }

        // Si no, usar valores por defecto según tipo de maquinaria
        TipoMaquinaria tipo = mapGrpcTipoToDomain(vehicle.getTipoMaquinaria());
        double consumoPor100Km;

        if (tipo == TipoMaquinaria.CAMION || tipo == TipoMaquinaria.VOLQUETE) {
            // Maquinaria liviana: consumo promedio 25-30 L/100km
            consumoPor100Km = 27.5;
        } else {
            // Maquinaria pesada: consumo promedio 35-45 L/100km
            consumoPor100Km = 40.0;
        }

        return (distanciaKm / 100.0) * consumoPor100Km;
    }

    /**
     * Calcula la duración estimada basada en distancia y tipo de maquinaria
     */
    private Double calcularDuracionEstimada(Double distanciaKm, TipoMaquinaria tipoMaquinaria) {
        if (distanciaKm == null || distanciaKm <= 0) {
            return 0.0;
        }

        // Velocidad promedio según tipo de maquinaria (km/h)
        double velocidadPromedio;

        if (tipoMaquinaria == TipoMaquinaria.CAMION || tipoMaquinaria == TipoMaquinaria.VOLQUETE) {
            // Maquinaria liviana: velocidad promedio 60-80 km/h
            velocidadPromedio = 70.0;
        } else {
            // Maquinaria pesada: velocidad promedio 30-50 km/h
            velocidadPromedio = 40.0;
        }

        return distanciaKm / velocidadPromedio;
    }

    /**
     * Crea una nueva ruta
     */
    public RouteResponse crearRuta(RouteCreateRequest request) {
        logger.info("Creando nueva ruta: {} -> {}", request.getOrigen(), request.getDestino());

        // Generar código único
        String codigo = generarCodigoRuta();
        while (routeRepository.existsByCodigo(codigo)) {
            codigo = generarCodigoRuta();
        }

        // Validar y obtener chofer si está asignado
        DriverResponse driver = null;
        if (request.getChoferId() != null && !request.getChoferId().isEmpty()) {
            try {
                driver = driversGrpcClient.getDriverById(request.getChoferId());
                
                // Validar que el chofer esté disponible
                if (!driversGrpcClient.isDriverAvailable(request.getChoferId())) {
                    throw new IllegalArgumentException("El chofer no está disponible para asignación");
                }
                
                // Validar que el chofer no tenga rutas activas (PENDIENTE o EN_CURSO)
                long rutasActivas = routeRepository.countRutasActivasByChoferId(request.getChoferId());
                if (rutasActivas > 0) {
                    throw new IllegalArgumentException("El chofer tiene " + rutasActivas + " ruta(s) activa(s). Debe completar o cancelar las rutas existentes antes de asignar una nueva.");
                }
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Error validando chofer: " + e.getMessage());
            }
        }

        // Validar y obtener vehículo si está asignado
        TipoMaquinaria tipoMaquinaria = null;
        VehicleResponseProto vehicle = null;
        boolean vehiculoAsignadoAlChofer = false;
        
        // Si no se proporcionó vehículo, buscar asignaciones activas del conductor
        if ((request.getVehiculoId() == null || request.getVehiculoId().isEmpty()) && 
            request.getChoferId() != null && !request.getChoferId().isEmpty()) {
            try {
                // Obtener el token JWT del ThreadLocal para pasarlo al REST client
                String jwtToken = com.skt.combustible.routes.infrastructure.security.JwtTokenHolder.getToken();
                
                // Obtener asignaciones activas del conductor
                List<Map<String, Object>> asignaciones = assignmentsRestClient.getActiveAssignmentsByDriver(
                    request.getChoferId(), jwtToken);
                
                if (!asignaciones.isEmpty()) {
                    // Tomar la primera asignación activa
                    Map<String, Object> asignacion = asignaciones.get(0);
                    String vehiculoIdAsignado = asignacion.get("vehicleId") != null 
                        ? asignacion.get("vehicleId").toString() 
                        : null;
                    
                    if (vehiculoIdAsignado != null && !vehiculoIdAsignado.isEmpty()) {
                        // Obtener el vehículo asignado
                        vehicle = vehiclesGrpcClient.getVehicleById(vehiculoIdAsignado);
                        tipoMaquinaria = mapGrpcTipoToDomain(vehicle.getTipoMaquinaria());
                        
                        // Actualizar el request con el vehículo encontrado
                        request.setVehiculoId(vehiculoIdAsignado);
                        vehiculoAsignadoAlChofer = true;
                        logger.info("Vehículo asignado encontrado automáticamente para chofer {}: {}", 
                            request.getChoferId(), vehiculoIdAsignado);
                    }
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener asignaciones activas del conductor: {}", e.getMessage());
                // Continuar sin asignación automática
            }
        }
        
        // Si se proporcionó un vehículo explícitamente, validarlo
        if (request.getVehiculoId() != null && !request.getVehiculoId().isEmpty() && vehicle == null) {
            try {
                vehicle = vehiclesGrpcClient.getVehicleById(request.getVehiculoId());
                tipoMaquinaria = mapGrpcTipoToDomain(vehicle.getTipoMaquinaria());
                
                // Verificar si el vehículo está asignado al mismo chofer
                if (request.getChoferId() != null && !request.getChoferId().isEmpty()) {
                    try {
                        String jwtToken = com.skt.combustible.routes.infrastructure.security.JwtTokenHolder.getToken();
                        List<Map<String, Object>> asignaciones = assignmentsRestClient.getActiveAssignmentsByDriver(
                            request.getChoferId(), jwtToken);
                        
                        for (Map<String, Object> asignacion : asignaciones) {
                            String vehiculoIdAsignado = asignacion.get("vehicleId") != null 
                                ? asignacion.get("vehicleId").toString() 
                                : null;
                            if (request.getVehiculoId().equals(vehiculoIdAsignado)) {
                                vehiculoAsignadoAlChofer = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("No se pudo verificar si el vehículo está asignado al chofer: {}", e.getMessage());
                    }
                }
                
                // Validar disponibilidad solo si el vehículo NO está asignado al mismo chofer
                if (!vehiculoAsignadoAlChofer) {
                    if (!vehiclesGrpcClient.isVehicleAvailable(request.getVehiculoId())) {
                        throw new IllegalArgumentException("El vehículo no está disponible para asignación");
                    }
                } else {
                    // Si está asignado al mismo chofer, solo validar que no esté en mantenimiento o fuera de servicio
                    if (!vehicle.getActivo() || 
                        vehicle.getEstadoOperativo() == com.skt.combustible.vehicles.grpc.EstadoOperativoProto.MANTENIMIENTO ||
                        vehicle.getEstadoOperativo() == com.skt.combustible.vehicles.grpc.EstadoOperativoProto.FUERA_DE_SERVICIO) {
                        throw new IllegalArgumentException("El vehículo asignado no está operativo (en mantenimiento o fuera de servicio)");
                    }
                }
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Error validando vehículo: " + e.getMessage());
            }
        }

        // Usar tipo de maquinaria del request o del vehículo
        if (request.getTipoMaquinaria() != null) {
            tipoMaquinaria = request.getTipoMaquinaria();
        }

        // Calcular duración estimada
        Double duracionEstimada = request.getDuracionEstimadaHoras();
        if (duracionEstimada == null && tipoMaquinaria != null) {
            duracionEstimada = calcularDuracionEstimada(request.getDistanciaKm(), tipoMaquinaria);
        }

        // Calcular consumo estimado
        Double consumoEstimado = null;
        if (vehicle != null) {
            consumoEstimado = calcularConsumoEstimado(request.getDistanciaKm(), vehicle);
        } else if (tipoMaquinaria != null) {
            // Si no hay vehículo, usar valores por defecto según tipo
            double consumoPor100Km = (tipoMaquinaria == TipoMaquinaria.CAMION || 
                                     tipoMaquinaria == TipoMaquinaria.VOLQUETE) ? 27.5 : 40.0;
            consumoEstimado = (request.getDistanciaKm() / 100.0) * consumoPor100Km;
        }

        // Crear la ruta
        Route route = new Route();
        route.setCodigo(codigo);
        route.setNombreRuta(request.getNombreRuta());
        route.setOrigen(request.getOrigen());
        route.setDestino(request.getDestino());
        route.setDistanciaKm(request.getDistanciaKm());
        route.setDuracionEstimadaHoras(duracionEstimada != null ? duracionEstimada : request.getDuracionEstimadaHoras());
        route.setConsumoEstimadoLitros(consumoEstimado);
        route.setHoraInicio(request.getHoraInicio());
        route.setVehiculoId(request.getVehiculoId());
        route.setChoferId(request.getChoferId());
        route.setTipoMaquinaria(tipoMaquinaria);
        route.setEstado(Route.EstadoRuta.PENDIENTE);
        route.setObservaciones(request.getObservaciones());
        
        // Guardar coordenadas si están disponibles
        if (request.getOrigenLat() != null) {
            route.setOrigenLat(request.getOrigenLat());
        }
        if (request.getOrigenLng() != null) {
            route.setOrigenLng(request.getOrigenLng());
        }
        if (request.getDestinoLat() != null) {
            route.setDestinoLat(request.getDestinoLat());
        }
        if (request.getDestinoLng() != null) {
            route.setDestinoLng(request.getDestinoLng());
        }

        Route savedRoute = routeRepository.save(route);
        logger.info("Ruta creada exitosamente con código: {}", savedRoute.getCodigo());

        return mapToResponse(savedRoute, vehicle, driver);
    }

    /**
     * Obtiene una ruta por ID
     */
    @Transactional(readOnly = true)
    public Optional<RouteResponse> obtenerRutaPorId(String id) {
        logger.debug("Buscando ruta por ID: {}", id);
        
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> RouteNotFoundException.withId(id));

        VehicleResponseProto vehicle = null;
        DriverResponse driver = null;

        if (route.getVehiculoId() != null) {
            try {
                vehicle = vehiclesGrpcClient.getVehicleById(route.getVehiculoId());
            } catch (Exception e) {
                logger.warn("No se pudo obtener información del vehículo: {}", e.getMessage());
            }
        }

        if (route.getChoferId() != null) {
            try {
                driver = driversGrpcClient.getDriverById(route.getChoferId());
            } catch (Exception e) {
                logger.warn("No se pudo obtener información del chofer: {}", e.getMessage());
            }
        }

        return Optional.of(mapToResponse(route, vehicle, driver));
    }

    /**
     * Obtiene todas las rutas activas
     */
    @Transactional(readOnly = true)
    public List<RouteResponse> obtenerTodasLasRutas() {
        logger.debug("Obteniendo todas las rutas activas");
        return routeRepository.findByActivaTrue().stream()
                .map(route -> {
                    VehicleResponseProto vehicle = null;
                    DriverResponse driver = null;
                    
                    if (route.getVehiculoId() != null) {
                        try {
                            vehicle = vehiclesGrpcClient.getVehicleById(route.getVehiculoId());
                        } catch (Exception e) {
                            logger.debug("No se pudo obtener vehículo: {}", e.getMessage());
                        }
                    }
                    
                    if (route.getChoferId() != null) {
                        try {
                            driver = driversGrpcClient.getDriverById(route.getChoferId());
                        } catch (Exception e) {
                            logger.debug("No se pudo obtener chofer: {}", e.getMessage());
                        }
                    }
                    
                    return mapToResponse(route, vehicle, driver);
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una ruta por código
     */
    @Transactional(readOnly = true)
    public Optional<RouteResponse> obtenerRutaPorCodigo(String codigo) {
        logger.debug("Buscando ruta por código: {}", codigo);
        
        Route route = routeRepository.findByCodigo(codigo)
                .orElseThrow(() -> RouteNotFoundException.withCodigo(codigo));

        VehicleResponseProto vehicle = null;
        DriverResponse driver = null;

        if (route.getVehiculoId() != null) {
            try {
                vehicle = vehiclesGrpcClient.getVehicleById(route.getVehiculoId());
            } catch (Exception e) {
                logger.warn("No se pudo obtener información del vehículo: {}", e.getMessage());
            }
        }

        if (route.getChoferId() != null) {
            try {
                driver = driversGrpcClient.getDriverById(route.getChoferId());
            } catch (Exception e) {
                logger.warn("No se pudo obtener información del chofer: {}", e.getMessage());
            }
        }

        return Optional.of(mapToResponse(route, vehicle, driver));
    }

    /**
     * Obtiene rutas activas (alias de obtenerTodasLasRutas)
     */
    @Transactional(readOnly = true)
    public List<RouteResponse> obtenerRutasActivas() {
        return obtenerTodasLasRutas();
    }

    /**
     * Obtiene rutas en curso
     */
    @Transactional(readOnly = true)
    public List<RouteResponse> obtenerRutasEnCurso() {
        return obtenerRutasPorEstado(Route.EstadoRuta.EN_CURSO);
    }

    /**
     * Obtiene rutas por vehículo
     */
    @Transactional(readOnly = true)
    public List<RouteResponse> obtenerRutasPorVehiculo(String vehiculoId) {
        logger.debug("Obteniendo rutas por vehículo: {}", vehiculoId);
        return routeRepository.findByVehiculoId(vehiculoId).stream()
                .map(route -> {
                    VehicleResponseProto vehicle = null;
                    DriverResponse driver = null;
                    
                    try {
                        vehicle = vehiclesGrpcClient.getVehicleById(route.getVehiculoId());
                    } catch (Exception e) {
                        logger.debug("No se pudo obtener vehículo: {}", e.getMessage());
                    }
                    
                    if (route.getChoferId() != null) {
                        try {
                            driver = driversGrpcClient.getDriverById(route.getChoferId());
                        } catch (Exception e) {
                            logger.debug("No se pudo obtener chofer: {}", e.getMessage());
                        }
                    }
                    
                    return mapToResponse(route, vehicle, driver);
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene rutas por chofer
     */
    @Transactional(readOnly = true)
    public List<RouteResponse> obtenerRutasPorChofer(String choferId) {
        logger.debug("Obteniendo rutas por chofer: {}", choferId);
        return routeRepository.findByChoferId(choferId).stream()
                .map(route -> {
                    VehicleResponseProto vehicle = null;
                    DriverResponse driver = null;
                    
                    if (route.getVehiculoId() != null) {
                        try {
                            vehicle = vehiclesGrpcClient.getVehicleById(route.getVehiculoId());
                        } catch (Exception e) {
                            logger.debug("No se pudo obtener vehículo: {}", e.getMessage());
                        }
                    }
                    
                    try {
                        driver = driversGrpcClient.getDriverById(route.getChoferId());
                    } catch (Exception e) {
                        logger.debug("No se pudo obtener chofer: {}", e.getMessage());
                    }
                    
                    return mapToResponse(route, vehicle, driver);
                })
                .collect(Collectors.toList());
    }

    /**
     * Actualiza una ruta existente
     */
    public RouteResponse actualizarRuta(String id, RouteUpdateRequest request) {
        logger.info("Actualizando ruta con ID: {}", id);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> RouteNotFoundException.withId(id));

        // Validar vehículo si se está actualizando
        VehicleResponseProto vehicle = null;
        if (request.getVehiculoId() != null && !request.getVehiculoId().equals(route.getVehiculoId())) {
            try {
                vehicle = vehiclesGrpcClient.getVehicleById(request.getVehiculoId());
                if (!vehiclesGrpcClient.isVehicleAvailable(request.getVehiculoId())) {
                    throw new IllegalArgumentException("El vehículo no está disponible");
                }
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Error validando vehículo: " + e.getMessage());
            }
        } else if (route.getVehiculoId() != null) {
            try {
                vehicle = vehiclesGrpcClient.getVehicleById(route.getVehiculoId());
            } catch (Exception e) {
                logger.debug("No se pudo obtener vehículo: {}", e.getMessage());
            }
        }

        // Validar chofer si se está actualizando
        DriverResponse driver = null;
        if (request.getChoferId() != null && !request.getChoferId().equals(route.getChoferId())) {
            try {
                driver = driversGrpcClient.getDriverById(request.getChoferId());
                if (!driversGrpcClient.isDriverAvailable(request.getChoferId())) {
                    throw new IllegalArgumentException("El chofer no está disponible");
                }
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Error validando chofer: " + e.getMessage());
            }
        } else if (route.getChoferId() != null) {
            try {
                driver = driversGrpcClient.getDriverById(route.getChoferId());
            } catch (Exception e) {
                logger.debug("No se pudo obtener chofer: {}", e.getMessage());
            }
        }

        // Actualizar campos
        if (request.getNombreRuta() != null) {
            route.setNombreRuta(request.getNombreRuta());
        }
        if (request.getOrigen() != null) {
            route.setOrigen(request.getOrigen());
        }
        if (request.getDestino() != null) {
            route.setDestino(request.getDestino());
        }
        if (request.getDistanciaKm() != null) {
            route.setDistanciaKm(request.getDistanciaKm());
            // Recalcular consumo y duración si cambió la distancia
            if (vehicle != null) {
                route.setConsumoEstimadoLitros(calcularConsumoEstimado(request.getDistanciaKm(), vehicle));
            }
            if (route.getTipoMaquinaria() != null) {
                route.setDuracionEstimadaHoras(calcularDuracionEstimada(request.getDistanciaKm(), route.getTipoMaquinaria()));
            }
        }
        if (request.getDuracionEstimadaHoras() != null) {
            route.setDuracionEstimadaHoras(request.getDuracionEstimadaHoras());
        }
        if (request.getVehiculoId() != null) {
            route.setVehiculoId(request.getVehiculoId());
        }
        if (request.getChoferId() != null) {
            route.setChoferId(request.getChoferId());
        }
        if (request.getTipoMaquinaria() != null) {
            route.setTipoMaquinaria(request.getTipoMaquinaria());
        }
        if (request.getHoraInicio() != null) {
            route.setHoraInicio(request.getHoraInicio());
        }
        if (request.getEstado() != null) {
            route.setEstado(request.getEstado());
        }
        if (request.getObservaciones() != null) {
            route.setObservaciones(request.getObservaciones());
        }
        
        // Actualizar coordenadas si están disponibles
        if (request.getOrigenLat() != null) {
            route.setOrigenLat(request.getOrigenLat());
        }
        if (request.getOrigenLng() != null) {
            route.setOrigenLng(request.getOrigenLng());
        }
        if (request.getDestinoLat() != null) {
            route.setDestinoLat(request.getDestinoLat());
        }
        if (request.getDestinoLng() != null) {
            route.setDestinoLng(request.getDestinoLng());
        }

        Route updatedRoute = routeRepository.save(route);
        logger.info("Ruta actualizada exitosamente: {}", updatedRoute.getCodigo());

        return mapToResponse(updatedRoute, vehicle, driver);
    }

    /**
     * Inicia una ruta
     */
    public RouteResponse iniciarRuta(String id) {
        logger.info("Iniciando ruta con ID: {}", id);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> RouteNotFoundException.withId(id));

        route.iniciarRuta();
        Route updatedRoute = routeRepository.save(route);

        // Actualizar estado del vehículo a EN_USO
        if (updatedRoute.getVehiculoId() != null) {
            try {
                vehiclesGrpcClient.cambiarEstadoVehiculo(updatedRoute.getVehiculoId(), "EN_USO");
                logger.info("Estado del vehículo {} actualizado a EN_USO", updatedRoute.getVehiculoId());
            } catch (Exception e) {
                logger.warn("No se pudo actualizar el estado del vehículo {}: {}", updatedRoute.getVehiculoId(), e.getMessage());
            }
        }

        // Actualizar estado del conductor a EN_RUTA
        if (updatedRoute.getChoferId() != null) {
            try {
                driversGrpcClient.changeDriverStatus(updatedRoute.getChoferId(), 
                    com.skt.combustible.drivers.grpc.EstadoOperativo.EN_RUTA);
                logger.info("Estado del chofer {} actualizado a EN_RUTA", updatedRoute.getChoferId());
            } catch (Exception e) {
                logger.warn("No se pudo actualizar el estado del chofer {}: {}", updatedRoute.getChoferId(), e.getMessage());
            }
        }

        VehicleResponseProto vehicle = null;
        DriverResponse driver = null;
        if (updatedRoute.getVehiculoId() != null) {
            try {
                vehicle = vehiclesGrpcClient.getVehicleById(updatedRoute.getVehiculoId());
            } catch (Exception e) {
                logger.debug("No se pudo obtener vehículo: {}", e.getMessage());
            }
        }
        if (updatedRoute.getChoferId() != null) {
            try {
                driver = driversGrpcClient.getDriverById(updatedRoute.getChoferId());
            } catch (Exception e) {
                logger.debug("No se pudo obtener chofer: {}", e.getMessage());
            }
        }

        return mapToResponse(updatedRoute, vehicle, driver);
    }

    /**
     * Completa una ruta
     */
    public RouteResponse completarRuta(String id) {
        logger.info("Completando ruta con ID: {}", id);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> RouteNotFoundException.withId(id));

        route.completarRuta();
        Route updatedRoute = routeRepository.save(route);

        // Actualizar estado del vehículo a DISPONIBLE (si no tiene otras rutas activas)
        if (updatedRoute.getVehiculoId() != null) {
            try {
                // Verificar si el vehículo tiene otras rutas activas
                long rutasActivas = routeRepository.countRutasActivasByVehiculoId(updatedRoute.getVehiculoId());
                if (rutasActivas == 0) {
                    vehiclesGrpcClient.cambiarEstadoVehiculo(updatedRoute.getVehiculoId(), "DISPONIBLE");
                    logger.info("Estado del vehículo {} actualizado a DISPONIBLE", updatedRoute.getVehiculoId());
                } else {
                    logger.info("El vehículo {} tiene {} ruta(s) activa(s), manteniendo estado EN_USO", 
                        updatedRoute.getVehiculoId(), rutasActivas);
                }
            } catch (Exception e) {
                logger.warn("No se pudo actualizar el estado del vehículo {}: {}", updatedRoute.getVehiculoId(), e.getMessage());
            }
        }

        // Actualizar estado del conductor a DISPONIBLE (si no tiene otras rutas activas)
        if (updatedRoute.getChoferId() != null) {
            try {
                // Verificar si el chofer tiene otras rutas activas
                long rutasActivas = routeRepository.countRutasActivasByChoferId(updatedRoute.getChoferId());
                if (rutasActivas == 0) {
                    driversGrpcClient.changeDriverStatus(updatedRoute.getChoferId(), 
                        com.skt.combustible.drivers.grpc.EstadoOperativo.DISPONIBLE);
                    logger.info("Estado del chofer {} actualizado a DISPONIBLE", updatedRoute.getChoferId());
                } else {
                    logger.info("El chofer {} tiene {} ruta(s) activa(s), manteniendo estado EN_RUTA", 
                        updatedRoute.getChoferId(), rutasActivas);
                }
            } catch (Exception e) {
                logger.warn("No se pudo actualizar el estado del chofer {}: {}", updatedRoute.getChoferId(), e.getMessage());
            }
        }

        VehicleResponseProto vehicle = null;
        DriverResponse driver = null;
        if (updatedRoute.getVehiculoId() != null) {
            try {
                vehicle = vehiclesGrpcClient.getVehicleById(updatedRoute.getVehiculoId());
            } catch (Exception e) {
                logger.debug("No se pudo obtener vehículo: {}", e.getMessage());
            }
        }
        if (updatedRoute.getChoferId() != null) {
            try {
                driver = driversGrpcClient.getDriverById(updatedRoute.getChoferId());
            } catch (Exception e) {
                logger.debug("No se pudo obtener chofer: {}", e.getMessage());
            }
        }

        return mapToResponse(updatedRoute, vehicle, driver);
    }

    /**
     * Cancela una ruta
     */
    public RouteResponse cancelarRuta(String id) {
        logger.info("Cancelando ruta con ID: {}", id);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> RouteNotFoundException.withId(id));

        route.cancelarRuta();
        Route updatedRoute = routeRepository.save(route);

        VehicleResponseProto vehicle = null;
        DriverResponse driver = null;
        if (updatedRoute.getVehiculoId() != null) {
            try {
                vehicle = vehiclesGrpcClient.getVehicleById(updatedRoute.getVehiculoId());
            } catch (Exception e) {
                logger.debug("No se pudo obtener vehículo: {}", e.getMessage());
            }
        }
        if (updatedRoute.getChoferId() != null) {
            try {
                driver = driversGrpcClient.getDriverById(updatedRoute.getChoferId());
            } catch (Exception e) {
                logger.debug("No se pudo obtener chofer: {}", e.getMessage());
            }
        }

        return mapToResponse(updatedRoute, vehicle, driver);
    }

    /**
     * Elimina una ruta
     */
    public void eliminarRuta(String id) {
        logger.info("Eliminando ruta con ID: {}", id);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> RouteNotFoundException.withId(id));

        route.setActiva(false);
        routeRepository.save(route);
        logger.info("Ruta eliminada exitosamente: {}", route.getCodigo());
    }

    /**
     * Obtiene rutas por estado
     */
    @Transactional(readOnly = true)
    public List<RouteResponse> obtenerRutasPorEstado(Route.EstadoRuta estado) {
        return routeRepository.findActivasByEstado(estado).stream()
                .map(route -> {
                    VehicleResponseProto vehicle = null;
                    DriverResponse driver = null;
                    
                    if (route.getVehiculoId() != null) {
                        try {
                            vehicle = vehiclesGrpcClient.getVehicleById(route.getVehiculoId());
                        } catch (Exception e) {
                            logger.debug("No se pudo obtener vehículo: {}", e.getMessage());
                        }
                    }
                    
                    if (route.getChoferId() != null) {
                        try {
                            driver = driversGrpcClient.getDriverById(route.getChoferId());
                        } catch (Exception e) {
                            logger.debug("No se pudo obtener chofer: {}", e.getMessage());
                        }
                    }
                    
                    return mapToResponse(route, vehicle, driver);
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de rutas
     */
    @Transactional(readOnly = true)
    public RouteStatsDTO obtenerEstadisticas() {
        long totalRutas = routeRepository.countRutasActivas();
        long rutasEnCurso = routeRepository.countByEstado(Route.EstadoRuta.EN_CURSO);
        long rutasCompletadas = routeRepository.countByEstado(Route.EstadoRuta.COMPLETADA);
        
        // Calcular distancia total
        double distanciaTotal = routeRepository.findByActivaTrue().stream()
                .mapToDouble(r -> r.getDistanciaKm() != null ? r.getDistanciaKm() : 0.0)
                .sum();

        // Calcular consumo total estimado
        double consumoTotal = routeRepository.findByActivaTrue().stream()
                .mapToDouble(r -> r.getConsumoEstimadoLitros() != null ? r.getConsumoEstimadoLitros() : 0.0)
                .sum();

        // Rutas completadas hoy
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finDia = inicioDia.plusDays(1);
        long rutasCompletadasHoy = routeRepository.findRutasCompletadasHoy(inicioDia, finDia).size();

        return new RouteStatsDTO(totalRutas, rutasEnCurso, rutasCompletadas, rutasCompletadasHoy, distanciaTotal, consumoTotal);
    }

    /**
     * Mapea entidad Route a RouteResponse
     */
    private RouteResponse mapToResponse(Route route, VehicleResponseProto vehicle, DriverResponse driver) {
        RouteResponse response = new RouteResponse();
        response.setId(route.getId());
        response.setCodigo(route.getCodigo());
        response.setNombreRuta(route.getNombreRuta());
        response.setOrigen(route.getOrigen());
        response.setDestino(route.getDestino());
        response.setDistanciaKm(route.getDistanciaKm());
        response.setDuracionEstimadaHoras(route.getDuracionEstimadaHoras());
        response.setConsumoEstimadoLitros(route.getConsumoEstimadoLitros());
        response.setHoraInicio(route.getHoraInicio());
        response.setVehiculoId(route.getVehiculoId());
        response.setChoferId(route.getChoferId());
        response.setTipoMaquinaria(route.getTipoMaquinaria());
        response.setEstado(route.getEstado());
        response.setActiva(route.getActiva());
        response.setFechaInicio(route.getFechaInicio());
        response.setFechaFin(route.getFechaFin());
        response.setObservaciones(route.getObservaciones());
        response.setCreatedAt(route.getCreatedAt());
        response.setUpdatedAt(route.getUpdatedAt());

        if (vehicle != null) {
            response.setPlacaVehiculo(vehicle.getPlaca());
            response.setMarcaVehiculo(vehicle.getMarca());
            response.setModeloVehiculo(vehicle.getModelo());
        }

        if (driver != null) {
            response.setNombreChofer(driver.getNombre());
            response.setApellidoChofer(driver.getApellido());
        }

        return response;
    }

    /**
     * Mapea TipoMaquinaria de gRPC al dominio
     */
    private TipoMaquinaria mapGrpcTipoToDomain(com.skt.combustible.vehicles.grpc.TipoMaquinariaProto tipo) {
        switch (tipo) {
            case CAMION:
                return TipoMaquinaria.CAMION;
            case VOLQUETE:
                return TipoMaquinaria.VOLQUETE;
            case EXCAVADORA:
                return TipoMaquinaria.EXCAVADORA;
            case CARGADOR:
                return TipoMaquinaria.CARGADOR;
            case GRUA:
                return TipoMaquinaria.GRUA;
            case MOTONIVELADORA:
                return TipoMaquinaria.MOTONIVELADORA;
            default:
                throw new IllegalArgumentException("Tipo de maquinaria no válido: " + tipo);
        }
    }

    /**
     * DTO para estadísticas de rutas
     */
    public static class RouteStatsDTO {
        private long totalRutas;
        private long rutasEnCurso;
        private long rutasCompletadas;
        private long rutasCompletadasHoy;
        private double distanciaTotal;
        private double consumoTotalEstimado;

        public RouteStatsDTO(long totalRutas, long rutasEnCurso, long rutasCompletadas, 
                            long rutasCompletadasHoy, double distanciaTotal, double consumoTotalEstimado) {
            this.totalRutas = totalRutas;
            this.rutasEnCurso = rutasEnCurso;
            this.rutasCompletadas = rutasCompletadas;
            this.rutasCompletadasHoy = rutasCompletadasHoy;
            this.distanciaTotal = distanciaTotal;
            this.consumoTotalEstimado = consumoTotalEstimado;
        }

        // Getters y Setters
        public long getTotalRutas() { return totalRutas; }
        public void setTotalRutas(long totalRutas) { this.totalRutas = totalRutas; }

        public long getRutasEnCurso() { return rutasEnCurso; }
        public void setRutasEnCurso(long rutasEnCurso) { this.rutasEnCurso = rutasEnCurso; }

        public long getRutasCompletadas() { return rutasCompletadas; }
        public void setRutasCompletadas(long rutasCompletadas) { this.rutasCompletadas = rutasCompletadas; }

        public long getRutasCompletadasHoy() { return rutasCompletadasHoy; }
        public void setRutasCompletadasHoy(long rutasCompletadasHoy) { this.rutasCompletadasHoy = rutasCompletadasHoy; }

        public double getDistanciaTotal() { return distanciaTotal; }
        public void setDistanciaTotal(double distanciaTotal) { this.distanciaTotal = distanciaTotal; }

        public double getConsumoTotalEstimado() { return consumoTotalEstimado; }
        public void setConsumoTotalEstimado(double consumoTotalEstimado) { this.consumoTotalEstimado = consumoTotalEstimado; }
    }

    /**
     * Obtiene todos los conductores disponibles del drivers-service
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> obtenerTodosLosConductoresDisponibles() {
        try {
            return driversGrpcClient.getAvailableDrivers();
        } catch (Exception e) {
            logger.error("Error obteniendo conductores disponibles: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Cuenta las rutas activas de un chofer
     */
    @Transactional(readOnly = true)
    public long countRutasActivasByChofer(String choferId) {
        return routeRepository.countRutasActivasByChoferId(choferId);
    }
}

