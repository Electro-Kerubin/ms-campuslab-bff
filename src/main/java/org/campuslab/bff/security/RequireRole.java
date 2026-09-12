package org.campuslab.bff.security;

import java.lang.annotation.*;

/**
 * Anotación personalizada para facilitar la protección de métodos por rol.
 *
 * Ejemplo de uso:
 * {@code
 *   @RequireRole(Role.ADMIN)
 *   public ResponseEntity<String> deleteUser(Long id) { ... }
 *
 *   @RequireRole({Role.TECNICO, Role.ADMIN})
 *   public ResponseEntity<String> configureSystem() { ... }
 * }
 *
 * La anotación genera automáticamente la expresión SpEL correspondiente.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * Rol(es) requerido(s) para acceder al método.
     * Si se especifican múltiples roles, el usuario debe tener al menos uno.
     */
    Role[] value() default {};

    /**
     * Si es true, el usuario debe tener todos los roles especificados.
     * Si es false (default), el usuario debe tener al menos uno.
     */
    boolean requireAll() default false;
}
