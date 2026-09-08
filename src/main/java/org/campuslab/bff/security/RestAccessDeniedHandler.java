package org.campuslab.bff.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Responde 403 en JSON cuando el JWT es valido pero el rol del usuario no
 * tiene permiso para el recurso solicitado.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        SecurityErrorResponseWriter.write(
                request,
                response,
                HttpStatus.FORBIDDEN,
                "Acceso denegado: el rol del usuario no tiene permiso sobre este recurso");
    }
}
