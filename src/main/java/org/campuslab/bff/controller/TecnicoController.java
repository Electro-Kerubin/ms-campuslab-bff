package org.campuslab.bff.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador técnico - Accesible por TECNICO y ADMIN.
 *
 * Ejemplos de endpoints para tareas técnicas y de configuración.
 */
@RestController
@RequestMapping("/api/tecnico")
public class TecnicoController {

    /**
     * Ver configuración del sistema (TECNICO, ADMIN).
     */
    @GetMapping("/configuracion")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerConfiguracion() {
        Map<String, Object> config = new HashMap<>();
        config.put("version", "1.0.0");
        config.put("database", "PostgreSQL 15");
        config.put("cache", "Redis 7.0");
        config.put("timeout_ms", 30000);

        return ResponseEntity.ok(config);
    }

    /**
     * Actualizar parámetros de configuración (TECNICO, ADMIN).
     */
    @PutMapping("/configuracion")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<Map<String, String>> actualizarConfiguracion(
            @RequestBody Map<String, String> parametros) {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Configuración actualizada");
        response.put("parametros_actualizados", String.valueOf(parametros.size()));

        return ResponseEntity.ok(response);
    }

    /**
     * Ver logs del sistema (TECNICO, ADMIN).
     */
    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerLogs(
            @RequestParam(defaultValue = "100") int limit) {
        Map<String, Object> logs = new HashMap<>();
        logs.put("total_registros", 10500);
        logs.put("registros_devueltos", limit);
        logs.put("nivel_minimo", "INFO");
        logs.put("periodo", "últimas 24 horas");

        return ResponseEntity.ok(logs);
    }

    /**
     * Reiniciar servicio (solo TECNICO, ADMIN).
     */
    @PostMapping("/reiniciar")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<Map<String, String>> reiniciarServicio() {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Reinicio programado");
        response.put("tiempo_estimado", "2 minutos");
        response.put("estado", "EN_PROCESO");

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Validar integridad de datos (TECNICO, ADMIN).
     */
    @PostMapping("/validar-integridad")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> validarIntegridad() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("estado", "COMPLETADO");
        resultado.put("errores_encontrados", 0);
        resultado.put("advertencias", 2);
        resultado.put("tiempo_ejecucion_ms", 5432);

        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtener mi información de técnico.
     */
    @GetMapping("/mi-info")
    public ResponseEntity<Map<String, Object>> obtenerMiInfo(
            Authentication authentication) {
        Map<String, Object> info = new HashMap<>();
        info.put("usuario", authentication.getName());
        info.put("roles", authentication.getAuthorities());
        info.put("autenticado", authentication.isAuthenticated());

        return ResponseEntity.ok(info);
    }
}
