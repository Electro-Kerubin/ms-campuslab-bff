package org.campuslab.bff.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuracion propia de validacion de JWT, expuesta bajo el prefijo
 * {@code campuslab.security.jwt} en application.yml.
 *
 * Complementa a {@code spring.security.oauth2.resourceserver.jwt.issuer-uri},
 * que ya provee Spring Boot para configurar el issuer.
 */
@Component
@ConfigurationProperties(prefix = "campuslab.security.jwt")
public class JwtProperties {

    /**
     * Audience(s) aceptados en el claim "aud" del token (ej: "api://<API_CLIENT_ID>").
     * Un token que no incluya al menos uno de estos valores es rechazado.
     */
    private List<String> audiences = new ArrayList<>();

    /**
     * Nombre del claim del access token donde Azure AD expone los App Roles
     * asignados al usuario (Admin, Tecnico, Estudiante, Auditor).
     */
    private String rolesClaim = "roles";

    public List<String> getAudiences() {
        return audiences;
    }

    public void setAudiences(List<String> audiences) {
        this.audiences = audiences;
    }

    public String getRolesClaim() {
        return rolesClaim;
    }

    public void setRolesClaim(String rolesClaim) {
        this.rolesClaim = rolesClaim;
    }
}
