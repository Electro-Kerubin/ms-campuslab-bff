package org.campuslab.bff.security;

/**
 * Nombres de rol esperados en el claim "roles" del JWT emitido por Azure AD
 * (ver tabla de roles/actores en context/CONTEXT.md, seccion 2).
 *
 * Se declaran sin el prefijo "ROLE_": {@link AzureAdJwtAuthenticationConverter}
 * agrega ese prefijo al construir las authorities, por lo que estas constantes
 * se usan directamente en anotaciones como {@code @PreAuthorize("hasRole(Roles.ADMIN)")}.
 */
public final class Roles {

    public static final String ADMIN = "ADMIN";
    public static final String TECNICO = "TECNICO";
    public static final String ESTUDIANTE = "ESTUDIANTE";
    public static final String AUDITOR = "AUDITOR";

    private Roles() {
    }
}
