package org.campuslab.bff.security;

/**
 * Enumeración de roles disponibles en la aplicación.
 *
 * Roles soportados:
 * - ADMIN: Acceso completo a todas las funcionalidades
 * - TECNICO: Acceso técnico a servicios y configuración
 * - ESTUDIANTE: Acceso limitado a sus propios datos y recursos
 * - AUDITOR: Acceso de lectura a registros y auditoría
 */
public enum Role {
    ADMIN("ROLE_ADMIN", "Administrador"),
    TECNICO("ROLE_TECNICO", "Técnico"),
    ESTUDIANTE("ROLE_ESTUDIANTE", "Estudiante"),
    AUDITOR("ROLE_AUDITOR", "Auditor");

    private final String authority;
    private final String descripcion;

    Role(String authority, String descripcion) {
        this.authority = authority;
        this.descripcion = descripcion;
    }

    public String getAuthority() {
        return authority;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
