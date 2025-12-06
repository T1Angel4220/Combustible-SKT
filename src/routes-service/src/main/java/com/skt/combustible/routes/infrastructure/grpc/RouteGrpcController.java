package com.skt.combustible.routes.infrastructure.grpc;

import com.skt.combustible.routes.application.service.RouteService;
import com.skt.combustible.routes.domain.dto.RouteCreateRequest;
import com.skt.combustible.routes.domain.dto.RouteResponse;
import com.skt.combustible.routes.domain.dto.RouteUpdateRequest;
import com.skt.combustible.routes.domain.entity.Route;
import com.skt.combustible.routes.domain.exception.RouteNotFoundException;
import com.skt.combustible.routes.grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.List;

/**
 * Controlador gRPC para el servicio de rutas
 * Implementa los métodos gRPC necesarios para la comunicación con el Gateway
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@GrpcService
public class RouteGrpcController extends RouteServiceGrpc.RouteServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(RouteGrpcController.class);

    @Autowired
    private RouteService routeService;

    @Override
    public void createRoute(CreateRouteRequest request, StreamObserver<com.skt.combustible.routes.grpc.RouteResponse> responseObserver) {
        try {
            logger.info("gRPC: Creando nueva ruta: {} -> {}", request.getOrigen(), request.getDestino());

            // Convertir de gRPC a dominio
            RouteCreateRequest domainRequest = new RouteCreateRequest();
            domainRequest.setNombreRuta(request.getNombreRuta());
            domainRequest.setOrigen(request.getOrigen());
            domainRequest.setDestino(request.getDestino());
            domainRequest.setDistanciaKm(request.getDistanciaKm());
            if (request.getDuracionEstimadaHoras() > 0) {
                domainRequest.setDuracionEstimadaHoras(request.getDuracionEstimadaHoras());
            }
            if (!request.getVehiculoId().isEmpty()) {
                domainRequest.setVehiculoId(request.getVehiculoId());
            }
            if (!request.getChoferId().isEmpty()) {
                domainRequest.setChoferId(request.getChoferId());
            }
            if (request.getTipoMaquinaria() != com.skt.combustible.routes.grpc.TipoMaquinariaRuta.UNRECOGNIZED) {
                domainRequest.setTipoMaquinaria(mapTipoMaquinariaFromGrpc(request.getTipoMaquinaria()));
            }
            if (!request.getHoraInicio().isEmpty()) {
                domainRequest.setHoraInicio(LocalTime.parse(request.getHoraInicio()));
            }
            if (!request.getObservaciones().isEmpty()) {
                domainRequest.setObservaciones(request.getObservaciones());
            }

            // Llamar al servicio de dominio
            com.skt.combustible.routes.domain.dto.RouteResponse domainResponse = routeService.crearRuta(domainRequest);

            // Convertir de dominio a gRPC
            com.skt.combustible.routes.grpc.RouteResponse grpcResponse = mapToGrpcResponse(domainResponse);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            logger.info("gRPC: Ruta creada exitosamente con código: {}", domainResponse.getCodigo());
        } catch (Exception e) {
            logger.error("Error creando ruta: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getRouteById(GetRouteByIdRequest request, StreamObserver<com.skt.combustible.routes.grpc.RouteResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo ruta por ID: {}", request.getId());

            java.util.Optional<com.skt.combustible.routes.domain.dto.RouteResponse> domainResponse = routeService.obtenerRutaPorId(request.getId());
            
            if (domainResponse.isPresent()) {
                com.skt.combustible.routes.grpc.RouteResponse grpcResponse = mapToGrpcResponse(domainResponse.get());
                responseObserver.onNext(grpcResponse);
                responseObserver.onCompleted();
                logger.info("gRPC: Ruta obtenida exitosamente: {}", domainResponse.get().getCodigo());
            } else {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Ruta no encontrada con ID: " + request.getId())
                        .asRuntimeException());
            }
        } catch (RouteNotFoundException e) {
            logger.error("Error obteniendo ruta: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Ruta no encontrada: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error obteniendo ruta por ID: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAllRoutes(GetAllRoutesRequest request, StreamObserver<GetAllRoutesResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo todas las rutas");

            List<com.skt.combustible.routes.domain.dto.RouteResponse> routes = routeService.obtenerTodasLasRutas();

            GetAllRoutesResponse.Builder responseBuilder = GetAllRoutesResponse.newBuilder();
            for (com.skt.combustible.routes.domain.dto.RouteResponse route : routes) {
                responseBuilder.addRoutes(mapToGrpcResponse(route));
            }
            responseBuilder.setCount(routes.size());

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            logger.info("gRPC: {} rutas obtenidas exitosamente", routes.size());
        } catch (Exception e) {
            logger.error("Error obteniendo todas las rutas: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getRouteByCode(GetRouteByCodeRequest request, StreamObserver<com.skt.combustible.routes.grpc.RouteResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo ruta por código: {}", request.getCodigo());

            java.util.Optional<com.skt.combustible.routes.domain.dto.RouteResponse> domainResponse = routeService.obtenerRutaPorCodigo(request.getCodigo());
            
            if (domainResponse.isPresent()) {
                com.skt.combustible.routes.grpc.RouteResponse grpcResponse = mapToGrpcResponse(domainResponse.get());
                responseObserver.onNext(grpcResponse);
                responseObserver.onCompleted();
                logger.info("gRPC: Ruta obtenida exitosamente: {}", domainResponse.get().getCodigo());
            } else {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Ruta no encontrada con código: " + request.getCodigo())
                        .asRuntimeException());
            }
        } catch (RouteNotFoundException e) {
            logger.error("Error obteniendo ruta: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Ruta no encontrada: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error obteniendo ruta por código: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getRoutesByEstado(GetRoutesByEstadoRequest request, StreamObserver<GetRoutesByEstadoResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo rutas por estado: {}", request.getEstado());

            Route.EstadoRuta estado = mapEstadoFromGrpc(request.getEstado());
            List<com.skt.combustible.routes.domain.dto.RouteResponse> routes = routeService.obtenerRutasPorEstado(estado);

            GetRoutesByEstadoResponse.Builder responseBuilder = GetRoutesByEstadoResponse.newBuilder();
            for (com.skt.combustible.routes.domain.dto.RouteResponse route : routes) {
                responseBuilder.addRoutes(mapToGrpcResponse(route));
            }
            responseBuilder.setCount(routes.size());

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error obteniendo rutas por estado: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getActiveRoutes(GetActiveRoutesRequest request, StreamObserver<GetActiveRoutesResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo rutas activas");

            List<com.skt.combustible.routes.domain.dto.RouteResponse> routes = routeService.obtenerRutasActivas();

            GetActiveRoutesResponse.Builder responseBuilder = GetActiveRoutesResponse.newBuilder();
            for (com.skt.combustible.routes.domain.dto.RouteResponse route : routes) {
                responseBuilder.addRoutes(mapToGrpcResponse(route));
            }
            responseBuilder.setCount(routes.size());

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error obteniendo rutas activas: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getRoutesInProgress(GetRoutesInProgressRequest request, StreamObserver<GetRoutesInProgressResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo rutas en curso");

            List<com.skt.combustible.routes.domain.dto.RouteResponse> routes = routeService.obtenerRutasEnCurso();

            GetRoutesInProgressResponse.Builder responseBuilder = GetRoutesInProgressResponse.newBuilder();
            for (com.skt.combustible.routes.domain.dto.RouteResponse route : routes) {
                responseBuilder.addRoutes(mapToGrpcResponse(route));
            }
            responseBuilder.setCount(routes.size());

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error obteniendo rutas en curso: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getRoutesByVehicle(GetRoutesByVehicleRequest request, StreamObserver<GetRoutesByVehicleResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo rutas por vehículo: {}", request.getVehiculoId());

            List<com.skt.combustible.routes.domain.dto.RouteResponse> routes = routeService.obtenerRutasPorVehiculo(request.getVehiculoId());

            GetRoutesByVehicleResponse.Builder responseBuilder = GetRoutesByVehicleResponse.newBuilder();
            for (com.skt.combustible.routes.domain.dto.RouteResponse route : routes) {
                responseBuilder.addRoutes(mapToGrpcResponse(route));
            }
            responseBuilder.setCount(routes.size());

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error obteniendo rutas por vehículo: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getRoutesByDriver(GetRoutesByDriverRequest request, StreamObserver<GetRoutesByDriverResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo rutas por chofer: {}", request.getChoferId());

            List<com.skt.combustible.routes.domain.dto.RouteResponse> routes = routeService.obtenerRutasPorChofer(request.getChoferId());

            GetRoutesByDriverResponse.Builder responseBuilder = GetRoutesByDriverResponse.newBuilder();
            for (com.skt.combustible.routes.domain.dto.RouteResponse route : routes) {
                responseBuilder.addRoutes(mapToGrpcResponse(route));
            }
            responseBuilder.setCount(routes.size());

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error obteniendo rutas por chofer: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateRoute(UpdateRouteRequest request, StreamObserver<com.skt.combustible.routes.grpc.RouteResponse> responseObserver) {
        try {
            logger.info("gRPC: Actualizando ruta con ID: {}", request.getId());

            RouteUpdateRequest domainRequest = new RouteUpdateRequest();
            if (!request.getNombreRuta().isEmpty()) {
                domainRequest.setNombreRuta(request.getNombreRuta());
            }
            if (!request.getOrigen().isEmpty()) {
                domainRequest.setOrigen(request.getOrigen());
            }
            if (!request.getDestino().isEmpty()) {
                domainRequest.setDestino(request.getDestino());
            }
            if (request.getDistanciaKm() > 0) {
                domainRequest.setDistanciaKm(request.getDistanciaKm());
            }
            if (request.getDuracionEstimadaHoras() > 0) {
                domainRequest.setDuracionEstimadaHoras(request.getDuracionEstimadaHoras());
            }
            if (!request.getVehiculoId().isEmpty()) {
                domainRequest.setVehiculoId(request.getVehiculoId());
            }
            if (!request.getChoferId().isEmpty()) {
                domainRequest.setChoferId(request.getChoferId());
            }
            if (request.getTipoMaquinaria() != com.skt.combustible.routes.grpc.TipoMaquinariaRuta.UNRECOGNIZED) {
                domainRequest.setTipoMaquinaria(mapTipoMaquinariaFromGrpc(request.getTipoMaquinaria()));
            }
            if (!request.getHoraInicio().isEmpty()) {
                domainRequest.setHoraInicio(LocalTime.parse(request.getHoraInicio()));
            }
            if (request.getEstado() != EstadoRuta.UNRECOGNIZED) {
                domainRequest.setEstado(mapEstadoFromGrpc(request.getEstado()));
            }
            if (!request.getObservaciones().isEmpty()) {
                domainRequest.setObservaciones(request.getObservaciones());
            }

            com.skt.combustible.routes.domain.dto.RouteResponse domainResponse = routeService.actualizarRuta(request.getId(), domainRequest);
            com.skt.combustible.routes.grpc.RouteResponse grpcResponse = mapToGrpcResponse(domainResponse);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            logger.info("gRPC: Ruta actualizada exitosamente: {}", domainResponse.getCodigo());
        } catch (RouteNotFoundException e) {
            logger.error("Error actualizando ruta: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Ruta no encontrada: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error actualizando ruta: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void startRoute(StartRouteRequest request, StreamObserver<com.skt.combustible.routes.grpc.RouteResponse> responseObserver) {
        try {
            logger.info("gRPC: Iniciando ruta con ID: {}", request.getId());

            com.skt.combustible.routes.domain.dto.RouteResponse domainResponse = routeService.iniciarRuta(request.getId());
            com.skt.combustible.routes.grpc.RouteResponse grpcResponse = mapToGrpcResponse(domainResponse);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            logger.info("gRPC: Ruta iniciada exitosamente: {}", domainResponse.getCodigo());
        } catch (RouteNotFoundException e) {
            logger.error("Error iniciando ruta: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Ruta no encontrada: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error iniciando ruta: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void completeRoute(CompleteRouteRequest request, StreamObserver<com.skt.combustible.routes.grpc.RouteResponse> responseObserver) {
        try {
            logger.info("gRPC: Completando ruta con ID: {}", request.getId());

            com.skt.combustible.routes.domain.dto.RouteResponse domainResponse = routeService.completarRuta(request.getId());
            com.skt.combustible.routes.grpc.RouteResponse grpcResponse = mapToGrpcResponse(domainResponse);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            logger.info("gRPC: Ruta completada exitosamente: {}", domainResponse.getCodigo());
        } catch (RouteNotFoundException e) {
            logger.error("Error completando ruta: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Ruta no encontrada: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error completando ruta: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void cancelRoute(CancelRouteRequest request, StreamObserver<com.skt.combustible.routes.grpc.RouteResponse> responseObserver) {
        try {
            logger.info("gRPC: Cancelando ruta con ID: {}", request.getId());

            com.skt.combustible.routes.domain.dto.RouteResponse domainResponse = routeService.cancelarRuta(request.getId());
            com.skt.combustible.routes.grpc.RouteResponse grpcResponse = mapToGrpcResponse(domainResponse);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            logger.info("gRPC: Ruta cancelada exitosamente: {}", domainResponse.getCodigo());
        } catch (RouteNotFoundException e) {
            logger.error("Error cancelando ruta: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Ruta no encontrada: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error cancelando ruta: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteRoute(DeleteRouteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            logger.info("gRPC: Eliminando ruta con ID: {}", request.getId());

            routeService.eliminarRuta(request.getId());

            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
            logger.info("gRPC: Ruta eliminada exitosamente");
        } catch (RouteNotFoundException e) {
            logger.error("Error eliminando ruta: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Ruta no encontrada: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error eliminando ruta: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getRouteStats(GetRouteStatsRequest request, StreamObserver<RouteStatsResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo estadísticas de rutas");

            RouteService.RouteStatsDTO stats = routeService.obtenerEstadisticas();

            RouteStatsResponse grpcResponse = RouteStatsResponse.newBuilder()
                    .setTotalRutas(stats.getTotalRutas())
                    .setRutasEnCurso(stats.getRutasEnCurso())
                    .setRutasCompletadas(stats.getRutasCompletadas())
                    .setRutasCompletadasHoy(stats.getRutasCompletadasHoy())
                    .setDistanciaTotal(stats.getDistanciaTotal())
                    .setConsumoTotalEstimado(stats.getConsumoTotalEstimado())
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    // Métodos auxiliares de mapeo
    private com.skt.combustible.routes.grpc.RouteResponse mapToGrpcResponse(com.skt.combustible.routes.domain.dto.RouteResponse domain) {
        com.skt.combustible.routes.grpc.RouteResponse.Builder builder = com.skt.combustible.routes.grpc.RouteResponse.newBuilder()
                .setId(domain.getId() != null ? domain.getId() : "")
                .setCodigo(domain.getCodigo() != null ? domain.getCodigo() : "")
                .setNombreRuta(domain.getNombreRuta() != null ? domain.getNombreRuta() : "")
                .setOrigen(domain.getOrigen() != null ? domain.getOrigen() : "")
                .setDestino(domain.getDestino() != null ? domain.getDestino() : "")
                .setDistanciaKm(domain.getDistanciaKm() != null ? domain.getDistanciaKm() : 0.0)
                .setDuracionEstimadaHoras(domain.getDuracionEstimadaHoras() != null ? domain.getDuracionEstimadaHoras() : 0.0)
                .setConsumoEstimadoLitros(domain.getConsumoEstimadoLitros() != null ? domain.getConsumoEstimadoLitros() : 0.0)
                .setHoraInicio(domain.getHoraInicio() != null ? domain.getHoraInicio().toString() : "")
                .setVehiculoId(domain.getVehiculoId() != null ? domain.getVehiculoId() : "")
                .setPlacaVehiculo(domain.getPlacaVehiculo() != null ? domain.getPlacaVehiculo() : "")
                .setMarcaVehiculo(domain.getMarcaVehiculo() != null ? domain.getMarcaVehiculo() : "")
                .setModeloVehiculo(domain.getModeloVehiculo() != null ? domain.getModeloVehiculo() : "")
                .setChoferId(domain.getChoferId() != null ? domain.getChoferId() : "")
                .setNombreChofer(domain.getNombreChofer() != null ? domain.getNombreChofer() : "")
                .setApellidoChofer(domain.getApellidoChofer() != null ? domain.getApellidoChofer() : "")
                .setEstado(mapEstadoToGrpc(domain.getEstado()))
                .setActiva(domain.getActiva() != null ? domain.getActiva() : false)
                .setObservaciones(domain.getObservaciones() != null ? domain.getObservaciones() : "")
                .setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt().toString() : "")
                .setUpdatedAt(domain.getUpdatedAt() != null ? domain.getUpdatedAt().toString() : "");

        if (domain.getTipoMaquinaria() != null) {
            builder.setTipoMaquinaria(mapTipoMaquinariaToGrpc(domain.getTipoMaquinaria()));
        }
        if (domain.getFechaInicio() != null) {
            builder.setFechaInicio(domain.getFechaInicio().toString());
        }
        if (domain.getFechaFin() != null) {
            builder.setFechaFin(domain.getFechaFin().toString());
        }

        return builder.build();
    }

    private EstadoRuta mapEstadoToGrpc(Route.EstadoRuta estado) {
        return switch (estado) {
            case PENDIENTE -> EstadoRuta.PENDIENTE;
            case EN_CURSO -> EstadoRuta.EN_CURSO;
            case COMPLETADA -> EstadoRuta.COMPLETADA;
            case CANCELADA -> EstadoRuta.CANCELADA;
        };
    }

    private Route.EstadoRuta mapEstadoFromGrpc(EstadoRuta estado) {
        return switch (estado) {
            case PENDIENTE -> Route.EstadoRuta.PENDIENTE;
            case EN_CURSO -> Route.EstadoRuta.EN_CURSO;
            case COMPLETADA -> Route.EstadoRuta.COMPLETADA;
            case CANCELADA -> Route.EstadoRuta.CANCELADA;
            case UNRECOGNIZED -> Route.EstadoRuta.PENDIENTE;
        };
    }

    private com.skt.combustible.routes.grpc.TipoMaquinariaRuta mapTipoMaquinariaToGrpc(com.skt.combustible.shared.domain.enums.TipoMaquinaria tipo) {
        return switch (tipo) {
            case CAMION -> com.skt.combustible.routes.grpc.TipoMaquinariaRuta.CAMION;
            case VOLQUETE -> com.skt.combustible.routes.grpc.TipoMaquinariaRuta.VOLQUETE;
            case EXCAVADORA -> com.skt.combustible.routes.grpc.TipoMaquinariaRuta.EXCAVADORA;
            case CARGADOR -> com.skt.combustible.routes.grpc.TipoMaquinariaRuta.CARGADOR;
            case GRUA -> com.skt.combustible.routes.grpc.TipoMaquinariaRuta.GRUA;
            case MOTONIVELADORA -> com.skt.combustible.routes.grpc.TipoMaquinariaRuta.MOTONIVELADORA;
        };
    }

    private com.skt.combustible.shared.domain.enums.TipoMaquinaria mapTipoMaquinariaFromGrpc(com.skt.combustible.routes.grpc.TipoMaquinariaRuta tipo) {
        return switch (tipo) {
            case CAMION -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.CAMION;
            case VOLQUETE -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.VOLQUETE;
            case EXCAVADORA -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.EXCAVADORA;
            case CARGADOR -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.CARGADOR;
            case GRUA -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.GRUA;
            case MOTONIVELADORA -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.MOTONIVELADORA;
            case UNRECOGNIZED -> null;
        };
    }
}

