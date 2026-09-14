package org.campuslab.bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de auditoría - Accesible por AUDITOR y ADMIN.
 *
 * Ejemplos de endpoints para acceso a logs, registros de auditoría, etc.
 * Los auditores solo tienen acceso de lectura.
 */
@RestController
@RequestMapping("/api/auditor")
public class AuditorController {

    /**
     * Ver registros de auditoría (AUDITOR, ADMIN).
     * Solo lectura.
     */
    @GetMapping("/registros")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerRegistrosAuditoria() {
        Map<String, Object> auditoria = new HashMap<>();
        List<Map<String, String>> registros = new ArrayList<>();

        Map<String, String> registro1 = new HashMap<>();
        registro1.put("id", "AUD-001");
        registro1.put("usuario", "usuario.admin@campus.lab");
        registro1.put("accion", "CREAR_USUARIO");
        registro1.put("recurso", "Usuario: juan.perez@campus.lab");
        registro1.put("timestamp", "2024-09-12T10:15:30");
        registro1.put("resultado", "EXITOSO");

        Map<String, String> registro2 = new HashMap<>();
        registro2.put("id", "AUD-002");
        registro2.put("usuario", "tecnico.soporte@campus.lab");
        registro2.put("accion", "ACTUALIZAR_CONFIGURACION");
        registro2.put("recurso", "Sistema de Logs");
        registro2.put("timestamp", "2024-09-12T09:45:20");
        registro2.put("resultado", "EXITOSO");

        registros.add(registro1);
        registros.add(registro2);

        auditoria.put("total_registros", 2);
        auditoria.put("registros", registros);

        return ResponseEntity.ok(auditoria);
    }

    /**
     * Ver acciones de un usuario específico (AUDITOR, ADMIN).
     * Solo lectura.
     */
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerAccionesUsuario(
            @PathVariable String usuarioId) {
        Map<String, Object> acciones = new HashMap<>();
        List<Map<String, String>> listaAcciones = new ArrayList<>();

        Map<String, String> accion1 = new HashMap<>();
        accion1.put("accion", "LOGIN");
        accion1.put("timestamp", "2024-09-12T08:00:00");
        accion1.put("ip_origen", "192.168.1.100");

        Map<String, String> accion2 = new HashMap<>();
        accion2.put("accion", "CREAR_TAREA");
        accion2.put("timestamp", "2024-09-12T10:30:00");
        accion2.put("ip_origen", "192.168.1.100");

        listaAcciones.add(accion1);
        listaAcciones.add(accion2);

        acciones.put("usuario_id", usuarioId);
        acciones.put("total_acciones", 2);
        acciones.put("acciones", listaAcciones);

        return ResponseEntity.ok(acciones);
    }

    /**
     * Ver cambios realizados en un recurso (AUDITOR, ADMIN).
     * Solo lectura.
     */
    @GetMapping("/cambios/{recursoId}")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerCambiosRecurso(
            @PathVariable String recursoId) {
        Map<String, Object> cambios = new HashMap<>();
        List<Map<String, String>> listaCambios = new ArrayList<>();

        Map<String, String> cambio1 = new HashMap<>();
        cambio1.put("cambio_id", "CAMBIO-001");
        cambio1.put("usuario", "admin@campus.lab");
        cambio1.put("tipo", "ACTUALIZACION");
        cambio1.put("campo_modificado", "estado");
        cambio1.put("valor_anterior", "INACTIVO");
        cambio1.put("valor_nuevo", "ACTIVO");
        cambio1.put("timestamp", "2024-09-12T11:20:00");

        listaCambios.add(cambio1);

        cambios.put("recurso_id", recursoId);
        cambios.put("total_cambios", 1);
        cambios.put("cambios", listaCambios);

        return ResponseEntity.ok(cambios);
    }

    /**
     * Generar reporte de auditoría (AUDITOR, ADMIN).
     * Solo lectura.
     */
    @PostMapping("/generar-reporte")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> generarReporte(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("reporte_id", "REP-2024-001");
        reporte.put("fecha_generacion", "2024-09-12T12:00:00");
        reporte.put("periodo_desde", desde != null ? desde : "2024-09-01");
        reporte.put("periodo_hasta", hasta != null ? hasta : "2024-09-12");
        reporte.put("total_eventos_auditados", 1250);
        reporte.put("estado", "GENERADO");
        reporte.put("url_descarga", "/api/auditor/reporte/REP-2024-001/download");

        return ResponseEntity.ok(reporte);
    }

    /**
     * Ver mi perfil de auditor.
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
