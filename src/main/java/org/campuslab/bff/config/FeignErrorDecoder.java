package org.campuslab.bff.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.campuslab.bff.exception.MicroserviceException;
import org.campuslab.bff.exception.MicroserviceNotFoundException;
import org.campuslab.bff.exception.MicroserviceTimeoutException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Decodificador de errores personalizado para Feign.
 *
 * Convierte respuestas HTTP de error en excepciones específicas
 * que pueden ser manejadas por el GlobalExceptionHandler.
 */
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = "";
        try {
            if (response.body() != null) {
                body = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            body = "No body provided";
        }

        // Mapeo de códigos HTTP a excepciones específicas
        return switch (response.status()) {
            case 404 -> new MicroserviceNotFoundException(
                    "Recurso no encontrado en microservicio: " + methodKey,
                    body
            );
            case 408, 504 -> new MicroserviceTimeoutException(
                    "Timeout al conectar con microservicio: " + methodKey,
                    body
            );
            case 401, 403 -> new MicroserviceException(
                    "Acceso denegado al microservicio: " + methodKey,
                    response.status(),
                    body
            );
            case 500, 502, 503 -> new MicroserviceException(
                    "Error en microservicio: " + methodKey,
                    response.status(),
                    body
            );
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}
