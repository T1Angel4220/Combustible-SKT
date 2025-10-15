package com.skt.combustible.drivers.controller;

import com.skt.combustible.drivers.domain.dto.DriverResponse;
import com.skt.combustible.drivers.domain.exception.DriverNotFoundException;
import com.skt.combustible.drivers.domain.service.DriverService;
import com.skt.combustible.drivers.grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Controlador gRPC para el servicio de choferes
 * Implementa los métodos gRPC necesarios para la comunicación con el Gateway
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@GrpcService
public class DriverGrpcController extends DriverServiceGrpc.DriverServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(DriverGrpcController.class);

    @Autowired
    private DriverService driverService;

    @Override
    public void createDriver(com.skt.combustible.drivers.grpc.CreateDriverRequest request,
            StreamObserver<com.skt.combustible.drivers.grpc.DriverResponse> responseObserver) {
        try {
            logger.info("gRPC: Creando nuevo chofer: {}", request.getNombre() + " " + request.getApellido());

            // Convertir de gRPC a dominio
            com.skt.combustible.drivers.domain.dto.CreateDriverRequest domainRequest = new com.skt.combustible.drivers.domain.dto.CreateDriverRequest();

            domainRequest.setNombre(request.getNombre());
            domainRequest.setApellido(request.getApellido());
            domainRequest.setDni(request.getDni());
            domainRequest.setLicencia(request.getLicencia());
            domainRequest.setTelefono(request.getTelefono());
            domainRequest.setEmail(request.getEmail());
            if (!request.getFechaContratacion().isEmpty()) {
                domainRequest.setFechaContratacion(java.time.LocalDate.parse(request.getFechaContratacion()));
            }
            domainRequest.setEstado(mapEstadoFromGrpc(request.getEstado()));
            domainRequest.setTipoMaquinariaAsignada(mapTipoMaquinariaFromGrpc(request.getTipoMaquinariaAsignada()));

            // Llamar al servicio de dominio
            com.skt.combustible.drivers.domain.dto.DriverResponse domainResponse = driverService
                    .createDriver(domainRequest);

            // Convertir de dominio a gRPC
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = com.skt.combustible.drivers.grpc.DriverResponse
                    .newBuilder()
                    .setId(domainResponse.getId())
                    .setNombre(domainResponse.getNombre())
                    .setApellido(domainResponse.getApellido())
                    .setDni(domainResponse.getDni())
                    .setLicencia(domainResponse.getLicencia())
                    .setEmail(domainResponse.getEmail() != null ? domainResponse.getEmail() : "")
                    .setTelefono(domainResponse.getTelefono() != null ? domainResponse.getTelefono() : "")
                    .setFechaContratacion(domainResponse.getFechaContratacion() != null
                            ? domainResponse.getFechaContratacion().toString()
                            : "")
                    .setEstado(mapEstadoToGrpc(domainResponse.getEstado()))
                    .setTipoMaquinariaAsignada(mapTipoMaquinariaToGrpc(domainResponse.getTipoMaquinariaAsignada()))
                    .setActivo(domainResponse.getActivo() != null ? domainResponse.getActivo() : false)
                    .setCreatedAt(domainResponse.getCreatedAt() != null ? domainResponse.getCreatedAt().toString() : "")
                    .setUpdatedAt(domainResponse.getUpdatedAt() != null ? domainResponse.getUpdatedAt().toString() : "")
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            logger.info("gRPC: Chofer creado exitosamente con ID: {}", domainResponse.getId());
        } catch (Exception e) {
            logger.error("Error creando chofer: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getDriverById(GetDriverByIdRequest request,
            StreamObserver<com.skt.combustible.drivers.grpc.DriverResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo chofer por ID: {}", request.getId());

            // Llamar al servicio de dominio
            DriverResponse domainResponse = driverService.getDriverById(request.getId());

            // Convertir a respuesta gRPC
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = com.skt.combustible.drivers.grpc.DriverResponse
                    .newBuilder()
                    .setId(domainResponse.getId())
                    .setNombre(domainResponse.getNombre())
                    .setApellido(domainResponse.getApellido())
                    .setDni(domainResponse.getDni())
                    .setLicencia(domainResponse.getLicencia())
                    .setEmail(domainResponse.getEmail() != null ? domainResponse.getEmail() : "")
                    .setTelefono(domainResponse.getTelefono() != null ? domainResponse.getTelefono() : "")
                    .setFechaContratacion(domainResponse.getFechaContratacion() != null
                            ? domainResponse.getFechaContratacion().toString()
                            : "")
                    .setEstado(mapEstadoToGrpc(domainResponse.getEstado()))
                    .setTipoMaquinariaAsignada(mapTipoMaquinariaToGrpc(domainResponse.getTipoMaquinariaAsignada()))
                    .setActivo(domainResponse.getActivo() != null ? domainResponse.getActivo() : false)
                    .setCreatedAt(domainResponse.getCreatedAt() != null ? domainResponse.getCreatedAt().toString() : "")
                    .setUpdatedAt(domainResponse.getUpdatedAt() != null ? domainResponse.getUpdatedAt().toString() : "")
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            logger.info("gRPC: Chofer obtenido exitosamente: {}", domainResponse.getNombre());
        } catch (Exception e) {
            logger.error("Error obteniendo chofer por ID: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAllDrivers(GetAllDriversRequest request, StreamObserver<GetAllDriversResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo todos los choferes");

            // Crear Pageable para obtener todos los drivers
            Pageable pageable = PageRequest.of(0, 1000); // Obtener hasta 1000 drivers
            Page<DriverResponse> driversPage = driverService.getAllDrivers(pageable);
            List<DriverResponse> drivers = driversPage.getContent();

            // Construir respuesta gRPC
            GetAllDriversResponse.Builder responseBuilder = GetAllDriversResponse.newBuilder();

            for (DriverResponse driver : drivers) {
                com.skt.combustible.drivers.grpc.DriverResponse grpcDriver = com.skt.combustible.drivers.grpc.DriverResponse
                        .newBuilder()
                        .setId(driver.getId())
                        .setNombre(driver.getNombre())
                        .setApellido(driver.getApellido())
                        .setDni(driver.getDni())
                        .setLicencia(driver.getLicencia())
                        .setEmail(driver.getEmail() != null ? driver.getEmail() : "")
                        .setTelefono(driver.getTelefono() != null ? driver.getTelefono() : "")
                        .setFechaContratacion(
                                driver.getFechaContratacion() != null ? driver.getFechaContratacion().toString() : "")
                        .setEstado(mapEstadoToGrpc(driver.getEstado()))
                        .setTipoMaquinariaAsignada(mapTipoMaquinariaToGrpc(driver.getTipoMaquinariaAsignada()))
                        .setActivo(driver.getActivo() != null ? driver.getActivo() : false)
                        .setCreatedAt(driver.getCreatedAt() != null ? driver.getCreatedAt().toString() : "")
                        .setUpdatedAt(driver.getUpdatedAt() != null ? driver.getUpdatedAt().toString() : "")
                        .build();

                responseBuilder.addDrivers(grpcDriver);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

            logger.info("gRPC: {} choferes obtenidos exitosamente", drivers.size());
        } catch (Exception e) {
            logger.error("Error obteniendo todos los choferes: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    // Métodos auxiliares para mapear enums
    private com.skt.combustible.drivers.grpc.EstadoOperativo mapEstadoToGrpc(
            com.skt.combustible.shared.domain.enums.EstadoOperativo estado) {
        if (estado == null) {
            return com.skt.combustible.drivers.grpc.EstadoOperativo.DISPONIBLE;
        }
        return switch (estado) {
            case DISPONIBLE -> com.skt.combustible.drivers.grpc.EstadoOperativo.DISPONIBLE;
            case ASIGNADO -> com.skt.combustible.drivers.grpc.EstadoOperativo.ASIGNADO;
            case EN_RUTA -> com.skt.combustible.drivers.grpc.EstadoOperativo.EN_RUTA;
            case DESCANSANDO -> com.skt.combustible.drivers.grpc.EstadoOperativo.DESCANSANDO;
            case VACACIONES -> com.skt.combustible.drivers.grpc.EstadoOperativo.VACACIONES;
            case ENFERMO -> com.skt.combustible.drivers.grpc.EstadoOperativo.ENFERMO;
            case LICENCIA -> com.skt.combustible.drivers.grpc.EstadoOperativo.LICENCIA;
            default -> com.skt.combustible.drivers.grpc.EstadoOperativo.DISPONIBLE;
        };
    }

    private com.skt.combustible.drivers.grpc.TipoMaquinaria mapTipoMaquinariaToGrpc(
            com.skt.combustible.shared.domain.enums.TipoMaquinaria tipo) {
        if (tipo == null) {
            return com.skt.combustible.drivers.grpc.TipoMaquinaria.CAMION;
        }
        return switch (tipo) {
            case CAMION -> com.skt.combustible.drivers.grpc.TipoMaquinaria.CAMION;
            case VOLQUETE -> com.skt.combustible.drivers.grpc.TipoMaquinaria.VOLQUETE;
            case EXCAVADORA -> com.skt.combustible.drivers.grpc.TipoMaquinaria.EXCAVADORA;
            case CARGADOR -> com.skt.combustible.drivers.grpc.TipoMaquinaria.CARGADOR;
            case GRUA -> com.skt.combustible.drivers.grpc.TipoMaquinaria.GRUA;
            case MOTONIVELADORA -> com.skt.combustible.drivers.grpc.TipoMaquinaria.MOTONIVELADORA;
            default -> com.skt.combustible.drivers.grpc.TipoMaquinaria.CAMION;
        };
    }

    /**
     * Actualiza un chofer
     */
    @Override
    public void updateDriver(com.skt.combustible.drivers.grpc.UpdateDriverRequest request,
            StreamObserver<com.skt.combustible.drivers.grpc.DriverResponse> responseObserver) {
        try {
            logger.info("gRPC: Actualizando chofer con ID: {}", request.getId());

            // Convertir de gRPC a dominio
            com.skt.combustible.drivers.domain.dto.UpdateDriverRequest domainRequest = new com.skt.combustible.drivers.domain.dto.UpdateDriverRequest();

            if (!request.getNombre().isEmpty()) {
                domainRequest.setNombre(request.getNombre());
            }
            if (!request.getApellido().isEmpty()) {
                domainRequest.setApellido(request.getApellido());
            }
            if (!request.getTelefono().isEmpty()) {
                domainRequest.setTelefono(request.getTelefono());
            }
            if (!request.getEmail().isEmpty()) {
                domainRequest.setEmail(request.getEmail());
            }
            if (!request.getFechaContratacion().isEmpty()) {
                // Convertir String a LocalDate
                try {
                    java.time.LocalDate fechaContratacion = java.time.LocalDate.parse(request.getFechaContratacion());
                    domainRequest.setFechaContratacion(fechaContratacion);
                } catch (java.time.format.DateTimeParseException e) {
                    logger.warn("Error parseando fecha de contratación: {}, usando fecha actual",
                            request.getFechaContratacion());
                    domainRequest.setFechaContratacion(java.time.LocalDate.now());
                }
            }
            if (request.hasEstado()) {
                domainRequest.setEstado(mapEstadoFromGrpc(request.getEstado()));
            }
            if (request.hasTipoMaquinariaAsignada()) {
                domainRequest.setTipoMaquinariaAsignada(mapTipoMaquinariaFromGrpc(request.getTipoMaquinariaAsignada()));
            }
            domainRequest.setActivo(request.getActivo());

            // Llamar al servicio de dominio
            com.skt.combustible.drivers.domain.dto.DriverResponse domainResponse = driverService
                    .updateDriver(request.getId(), domainRequest);

            // Convertir de dominio a gRPC
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = com.skt.combustible.drivers.grpc.DriverResponse
                    .newBuilder()
                    .setId(domainResponse.getId())
                    .setNombre(domainResponse.getNombre())
                    .setApellido(domainResponse.getApellido())
                    .setDni(domainResponse.getDni())
                    .setLicencia(domainResponse.getLicencia())
                    .setEmail(domainResponse.getEmail() != null ? domainResponse.getEmail() : "")
                    .setTelefono(domainResponse.getTelefono() != null ? domainResponse.getTelefono() : "")
                    .setFechaContratacion(domainResponse.getFechaContratacion() != null
                            ? domainResponse.getFechaContratacion().toString()
                            : "")
                    .setEstado(mapEstadoToGrpc(domainResponse.getEstado()))
                    .setTipoMaquinariaAsignada(mapTipoMaquinariaToGrpc(domainResponse.getTipoMaquinariaAsignada()))
                    .setActivo(domainResponse.getActivo() != null ? domainResponse.getActivo() : false)
                    .setCreatedAt(domainResponse.getCreatedAt() != null ? domainResponse.getCreatedAt().toString() : "")
                    .setUpdatedAt(domainResponse.getUpdatedAt() != null ? domainResponse.getUpdatedAt().toString() : "")
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            logger.info("gRPC: Chofer con ID {} actualizado exitosamente", request.getId());
        } catch (DriverNotFoundException e) {
            logger.error("Error actualizando chofer: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Chofer no encontrado: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error actualizando chofer: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Desactiva un chofer
     */
    @Override
    public void deactivateDriver(DeactivateDriverRequest request,
            StreamObserver<com.skt.combustible.drivers.grpc.Empty> responseObserver) {
        try {
            logger.info("gRPC: Desactivando chofer con ID: {}", request.getId());

            // Llamar al servicio de dominio
            driverService.deactivateDriver(request.getId());

            responseObserver.onNext(com.skt.combustible.drivers.grpc.Empty.newBuilder().build());
            responseObserver.onCompleted();
            logger.info("gRPC: Chofer con ID {} desactivado exitosamente", request.getId());
        } catch (DriverNotFoundException e) {
            logger.error("Error desactivando chofer: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Chofer no encontrado: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error desactivando chofer: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Elimina permanentemente un chofer
     */
    @Override
    public void deleteDriver(DeleteDriverRequest request,
            StreamObserver<com.skt.combustible.drivers.grpc.Empty> responseObserver) {
        try {
            logger.info("gRPC: Eliminando permanentemente chofer con ID: {}", request.getId());

            // Llamar al servicio de dominio
            driverService.deleteDriverPermanently(request.getId());

            responseObserver.onNext(com.skt.combustible.drivers.grpc.Empty.newBuilder().build());
            responseObserver.onCompleted();
            logger.info("gRPC: Chofer con ID {} eliminado permanentemente exitosamente", request.getId());
        } catch (DriverNotFoundException e) {
            logger.error("Error eliminando chofer permanentemente: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Chofer no encontrado: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error eliminando chofer permanentemente: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Reactiva un chofer
     */
    @Override
    public void activateDriver(ActivateDriverRequest request,
            StreamObserver<com.skt.combustible.drivers.grpc.DriverResponse> responseObserver) {
        try {
            logger.info("gRPC: Reactivando chofer con ID: {}", request.getId());

            // Llamar al servicio de dominio
            com.skt.combustible.drivers.domain.dto.DriverResponse domainResponse = driverService
                    .activateDriver(request.getId());

            // Convertir de dominio a gRPC
            com.skt.combustible.drivers.grpc.DriverResponse grpcResponse = com.skt.combustible.drivers.grpc.DriverResponse
                    .newBuilder()
                    .setId(domainResponse.getId())
                    .setNombre(domainResponse.getNombre())
                    .setApellido(domainResponse.getApellido())
                    .setDni(domainResponse.getDni())
                    .setLicencia(domainResponse.getLicencia())
                    .setEmail(domainResponse.getEmail() != null ? domainResponse.getEmail() : "")
                    .setTelefono(domainResponse.getTelefono() != null ? domainResponse.getTelefono() : "")
                    .setFechaContratacion(domainResponse.getFechaContratacion() != null
                            ? domainResponse.getFechaContratacion().toString()
                            : "")
                    .setEstado(mapEstadoToGrpc(domainResponse.getEstado()))
                    .setTipoMaquinariaAsignada(mapTipoMaquinariaToGrpc(domainResponse.getTipoMaquinariaAsignada()))
                    .setActivo(domainResponse.getActivo() != null ? domainResponse.getActivo() : false)
                    .setCreatedAt(domainResponse.getCreatedAt() != null ? domainResponse.getCreatedAt().toString() : "")
                    .setUpdatedAt(domainResponse.getUpdatedAt() != null ? domainResponse.getUpdatedAt().toString() : "")
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            logger.info("gRPC: Chofer con ID {} reactivado exitosamente", request.getId());
        } catch (DriverNotFoundException e) {
            logger.error("Error reactivando chofer: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Chofer no encontrado: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error reactivando chofer: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Obtiene choferes en servicio (Asignado y En Ruta)
     */
    @Override
    public void getDriversInService(GetDriversInServiceRequest request,
            StreamObserver<GetDriversInServiceResponse> responseObserver) {
        try {
            logger.info("gRPC: Obteniendo choferes en servicio");

            // Llamar al servicio de dominio
            List<com.skt.combustible.drivers.domain.dto.DriverResponse> domainDrivers = driverService
                    .getDriversInService();

            // Construir respuesta gRPC
            GetDriversInServiceResponse.Builder responseBuilder = GetDriversInServiceResponse.newBuilder();

            for (com.skt.combustible.drivers.domain.dto.DriverResponse driver : domainDrivers) {
                com.skt.combustible.drivers.grpc.DriverResponse grpcDriver = com.skt.combustible.drivers.grpc.DriverResponse
                        .newBuilder()
                        .setId(driver.getId())
                        .setNombre(driver.getNombre())
                        .setApellido(driver.getApellido())
                        .setDni(driver.getDni())
                        .setLicencia(driver.getLicencia())
                        .setEmail(driver.getEmail() != null ? driver.getEmail() : "")
                        .setTelefono(driver.getTelefono() != null ? driver.getTelefono() : "")
                        .setFechaContratacion(
                                driver.getFechaContratacion() != null ? driver.getFechaContratacion().toString() : "")
                        .setEstado(mapEstadoToGrpc(driver.getEstado()))
                        .setTipoMaquinariaAsignada(mapTipoMaquinariaToGrpc(driver.getTipoMaquinariaAsignada()))
                        .setActivo(driver.getActivo() != null ? driver.getActivo() : false)
                        .setCreatedAt(driver.getCreatedAt() != null ? driver.getCreatedAt().toString() : "")
                        .setUpdatedAt(driver.getUpdatedAt() != null ? driver.getUpdatedAt().toString() : "")
                        .build();

                responseBuilder.addDrivers(grpcDriver);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            logger.info("gRPC: {} choferes en servicio obtenidos exitosamente", domainDrivers.size());
        } catch (Exception e) {
            logger.error("Error obteniendo choferes en servicio: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    // Métodos helper para mapeo de enums desde gRPC
    private com.skt.combustible.shared.domain.enums.EstadoOperativo mapEstadoFromGrpc(
            com.skt.combustible.drivers.grpc.EstadoOperativo estado) {
        return switch (estado) {
            case DISPONIBLE -> com.skt.combustible.shared.domain.enums.EstadoOperativo.DISPONIBLE;
            case ASIGNADO -> com.skt.combustible.shared.domain.enums.EstadoOperativo.ASIGNADO;
            case EN_RUTA -> com.skt.combustible.shared.domain.enums.EstadoOperativo.EN_RUTA;
            case DESCANSANDO -> com.skt.combustible.shared.domain.enums.EstadoOperativo.DESCANSANDO;
            case VACACIONES -> com.skt.combustible.shared.domain.enums.EstadoOperativo.VACACIONES;
            case ENFERMO -> com.skt.combustible.shared.domain.enums.EstadoOperativo.ENFERMO;
            case LICENCIA -> com.skt.combustible.shared.domain.enums.EstadoOperativo.LICENCIA;
            case EN_USO -> com.skt.combustible.shared.domain.enums.EstadoOperativo.EN_USO;
            case MANTENIMIENTO -> com.skt.combustible.shared.domain.enums.EstadoOperativo.MANTENIMIENTO;
            case FUERA_SERVICIO -> com.skt.combustible.shared.domain.enums.EstadoOperativo.FUERA_SERVICIO;
            case RESERVADO -> com.skt.combustible.shared.domain.enums.EstadoOperativo.RESERVADO;
            default -> com.skt.combustible.shared.domain.enums.EstadoOperativo.DISPONIBLE;
        };
    }

    private com.skt.combustible.shared.domain.enums.TipoMaquinaria mapTipoMaquinariaFromGrpc(
            com.skt.combustible.drivers.grpc.TipoMaquinaria tipo) {
        return switch (tipo) {
            case CAMION -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.CAMION;
            case VOLQUETE -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.VOLQUETE;
            case EXCAVADORA -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.EXCAVADORA;
            case CARGADOR -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.CARGADOR;
            case GRUA -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.GRUA;
            case MOTONIVELADORA -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.MOTONIVELADORA;
            default -> com.skt.combustible.shared.domain.enums.TipoMaquinaria.CAMION;
        };
    }
}