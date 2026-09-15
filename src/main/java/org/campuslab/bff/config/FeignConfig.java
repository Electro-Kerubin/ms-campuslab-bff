package org.campuslab.bff.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Configuración centralizada de Feign para comunicación con microservicios.
 *
 * Características:
 * - Decodificador de errores personalizado
 * - Nivel de logging completo
 * - Logger customizado para debug
 * - Manejo de excepciones HTTP
 *
 * Nota: Timeouts y reintentos se configuran en application.yml
 */
@Configuration
public class FeignConfig {

    /**
     * Nivel de logging de Feign.
     *
     * NONE: Sin logging
     * BASIC: Registra solo método, URL y status
     * HEADERS: Registra cabeceras además de BASIC
     * FULL: Registra cabeceras, cuerpo y metadatos
     *
     * Se usa FULL para desarrollo; cambiar a BASIC en producción
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Logger customizado para Feign.
     * Registra información de debug para cada llamada HTTP realizada a los microservicios.
     */
    @Bean
    Logger feignLogger() {
        return new Logger() {
            private final org.slf4j.Logger logger = LoggerFactory.getLogger(FeignConfig.class);

            @Override
            protected void log(String configKey, String format, Object... args) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[Feign] [{}] {}", configKey, String.format(format, args));
                }
            }
        };
    }

    /**
     * Decodificador de errores personalizado para Feign.
     *
     * Convierte respuestas HTTP de error en excepciones específicas:
     * - 404 → MicroserviceNotFoundException
     * - 408/504 → MicroserviceTimeoutException
     * - 401/403 → MicroserviceException (Acceso denegado)
     * - 500/502/503 → MicroserviceException (Error del servidor)
     *
     * Estas excepciones son capturadas por GlobalExceptionHandler
     * y devuelven respuestas consistentes al cliente.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * Propaga el header Authorization (Bearer JWT) de la solicitud entrante
     * del frontend hacia las llamadas salientes a los microservicios de
     * dominio (ms-bookings, ms-catalog, etc.), que también validan JWT.
     *
     * Sin esto, Feign no reenvía ningún header por defecto y los
     * microservicios responden 401/403 aunque el BFF sí esté autenticado.
     */
    @Bean
    public RequestInterceptor authorizationForwardingInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null) {
                requestTemplate.header("Authorization", authorizationHeader);
            }
        };
    }
}
