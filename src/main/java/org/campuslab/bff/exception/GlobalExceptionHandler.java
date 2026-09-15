package org.campuslab.bff.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * Manejador global de excepciones para seguridad y errores generales.
 *
 * Maneja:
 * - AccessDeniedException: El usuario no tiene permisos para acceder al recurso
 * - AuthenticationException: El usuario no está autenticado
 * - MicroserviceException (y subclases): errores propagados desde un
 *   microservicio de dominio vía Feign (ver FeignErrorDecoder) — sin este
 *   handler, TODOS caían en el handler genérico de abajo y se devolvían
 *   como 500 "ERROR_INTERNO" perdiendo el status y el mensaje real
 *   (ej: un 409 de validación de negocio como "Los EQUIPOS requieren
 *   'equipment' con número de serie" llegaba al frontend como un 500 opaco).
 * - Excepciones generales
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Maneja errores propagados desde un microservicio de dominio, preservando
     * su status HTTP real y extrayendo un mensaje legible del body (los
     * microservicios de este proyecto devuelven {"error": "..."} o
     * {"message": "..."}).
     */
    @ExceptionHandler(MicroserviceException.class)
    public ResponseEntity<ErrorResponse> handleMicroserviceException(
            MicroserviceException ex,
            WebRequest request) {

        HttpStatus status;
        try {
            status = HttpStatus.valueOf(ex.getHttpStatus());
        } catch (IllegalArgumentException e) {
            status = HttpStatus.BAD_GATEWAY;
        }

        String friendlyMessage = extractMessage(ex.getResponseBody());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                "ERROR_MICROSERVICIO",
                friendlyMessage != null ? friendlyMessage : ex.getMessage(),
                ex.getResponseBody(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /** Intenta sacar "error" o "message" del body JSON devuelto por el microservicio. */
    private String extractMessage(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(body);
            if (node.hasNonNull("error")) {
                return node.get("error").asText();
            }
            if (node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            // El body no era JSON parseable; se usa el mensaje genérico de la excepción.
        }
        return null;
    }

    /**
     * Maneja errores de acceso denegado (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "ACCESO_DENEGADO",
                "No tienes permisos para acceder a este recurso",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Maneja errores de autenticación (401 Unauthorized).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "NO_AUTENTICADO",
                "Debes autenticarte para acceder a este recurso",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja excepciones generales no capturadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "ERROR_INTERNO",
                "Ocurrió un error interno en el servidor",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Estructura de respuesta de error.
     */
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String details;
        private String path;

        public ErrorResponse(LocalDateTime timestamp, int status, String error,
                           String message, String details, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.details = details;
            this.path = path;
        }

        // Getters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public String getDetails() {
            return details;
        }

        public String getPath() {
            return path;
        }
    }
}
