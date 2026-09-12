package org.campuslab.bff.config;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
