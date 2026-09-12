package org.campuslab.bff.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de administración - Solo accesible por ADMIN.
 *
 * Ejemplos de endpoints protegidos solo para administradores.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /**
     * Obtener estadísticas del sistema (solo ADMIN).
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("usuarios_totales", 1250);
        estadisticas.put("cursos_activos", 45);
        estadisticas.put("estudiantes_registrados", 980);
        estadisticas.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Crear nuevo usuario (solo ADMIN).
     */
    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> crearUsuario(
            @RequestBody Map<String, String> usuario) {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario creado exitosamente");
        response.put("id", "USR-001");
        response.put("email", usuario.get("email"));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Eliminar usuario (solo ADMIN).
     */
    @DeleteMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarUsuario(
            @PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario eliminado correctamente");
        response.put("id", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Resetear base de datos (solo ADMIN - operación crítica).
     */
    @PostMapping("/reset-database")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetearBaseDatos() {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Base de datos reseteada");
        response.put("advertencia", "Acción irreversible ejecutada");

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener información del usuario autenticado (ejemplo).
     */
    @GetMapping("/mi-perfil")
    public ResponseEntity<Map<String, Object>> obtenerMiPerfil(
            Authentication authentication) {
        Map<String, Object> perfil = new HashMap<>();
        perfil.put("usuario", authentication.getName());
        perfil.put("rol", authentication.getAuthorities());
        perfil.put("autenticado", authentication.isAuthenticated());

        return ResponseEntity.ok(perfil);
    }
}
