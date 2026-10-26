package org.campuslab.bff.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Responde 401 en JSON cuando la request no trae un JWT valido (ausente,
 * mal formado, expirado, firma invalida, issuer o audience incorrectos).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        // DIAGNOSTICO TEMPORAL (QA): incluir el motivo real del rechazo para depurar el 401
        String causa = authException.getMessage();
        Throwable anidada = authException.getCause();
        if (anidada != null) {
            causa += " | causa: " + anidada.getMessage();
        }
        SecurityErrorResponseWriter.write(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                "No autorizado: token JWT ausente o invalido. Motivo: " + causa);
    }
}
