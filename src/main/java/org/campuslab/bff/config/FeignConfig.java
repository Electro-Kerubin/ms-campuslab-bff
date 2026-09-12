package org.campuslab.bff.config;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración centralizada de Feign para comunicación con microservicios.
 *
 * Configura:
 * - Decodificador de errores personalizado
 * - Nivel de logging
 * - Retry y timeout policies
 */
@Configuration
public class FeignConfig {

    /**
     * Nivel de logging de Feign.
     * FULL: Registra cabeceras, cuerpo y metadatos de la solicitud/respuesta.
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Decodificador de errores personalizado para Feign.
     * Convierte errores HTTP en excepciones personalizadas.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
