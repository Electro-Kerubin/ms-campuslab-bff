package org.campuslab.bff.exception;

/**
 * Excepción lanzada cuando un recurso no se encuentra en un microservicio (404).
 */
public class MicroserviceNotFoundException extends MicroserviceException {

    public MicroserviceNotFoundException(String message, String responseBody) {
        super(message, 404, responseBody);
    }
}
