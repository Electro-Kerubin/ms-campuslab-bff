package org.campuslab.bff.exception;

/**
 * Excepción base para errores al comunicarse con microservicios.
 */
public class MicroserviceException extends RuntimeException {

    private final int httpStatus;
    private final String responseBody;

    public MicroserviceException(String message, int httpStatus, String responseBody) {
        super(message);
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
