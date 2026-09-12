package org.campuslab.bff.exception;

/**
 * Excepción lanzada cuando hay timeout al conectar con un microservicio (408, 504).
 */
public class MicroserviceTimeoutException extends MicroserviceException {

    public MicroserviceTimeoutException(String message, String responseBody) {
        super(message, 504, responseBody);
    }
}
