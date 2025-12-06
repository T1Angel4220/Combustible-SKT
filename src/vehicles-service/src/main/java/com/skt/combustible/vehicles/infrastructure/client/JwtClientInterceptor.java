package com.skt.combustible.vehicles.infrastructure.client;

import com.skt.combustible.vehicles.infrastructure.security.JwtTokenHolder;
import io.grpc.*;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Interceptor gRPC cliente para agregar token JWT a las llamadas gRPC
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
@GrpcGlobalClientInterceptor
public class JwtClientInterceptor implements ClientInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtClientInterceptor.class);
    private static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization",
            Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Obtener el token JWT del ThreadLocal
                String token = JwtTokenHolder.getToken();
                
                if (token != null && !token.isEmpty()) {
                    // Agregar el token al header Authorization
                    headers.put(AUTHORIZATION_METADATA_KEY, "Bearer " + token);
                    logger.debug("Token JWT agregado a la llamada gRPC: {}", method.getFullMethodName());
                } else {
                    logger.warn("No se encontró token JWT para la llamada gRPC: {}", method.getFullMethodName());
                }
                
                super.start(responseListener, headers);
            }
        };
    }
}

