package org.campuslab.bff.client;

import org.campuslab.bff.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Cliente Feign para comunicación con ms-audit.
 *
 * Maneja todas las operaciones relacionadas con auditoría y registro de eventos.
 * Solo lectura - el servicio registra eventos automáticamente.
 */
@FeignClient(
        name = "ms-audit",
        url = "${microservice.audit.url:http://localhost:8083}",
        configuration = FeignConfig.class
)
public interface AuditClient {

    /**
     * Obtener todos los registros de auditoría con filtros opcionales.
     *
     * Parámetros:
     * - action: Tipo de acción (CREATE, UPDATE, DELETE, APPROVE, REJECT)
     * - usuario: ID del usuario que realizó la acción
     * - fechaInicio: Fecha inicio del rango
     * - fechaFin: Fecha fin del rango
     * - limit: Límite de registros (default: 100)
     *
     * Response: Lista de eventos de auditoría
     */
    @GetMapping("/api/audit/eventos")
    ResponseEntity<List<Map<String, Object>>> getEventos(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false, defaultValue = "100") int limit
    );

    /**
     * Obtener un registro de auditoría específico por ID.
     *
     * Path Parameters:
     * - id: ID del evento de auditoría
     *
     * Response: Evento de auditoría completo
     */
    @GetMapping("/api/audit/eventos/{id}")
    ResponseEntity<Map<String, Object>> getEventoById(@PathVariable String id);

    /**
     * Obtener eventos relacionados a un recurso específico.
     *
     * Path Parameters:
     * - recursoId: ID del recurso (booking, lab, equipment, etc.)
     *
     * Response: Lista de eventos que afectaron al recurso
     */
    @GetMapping("/api/audit/recursos/{recursoId}/eventos")
    ResponseEntity<List<Map<String, Object>>> getEventosPorRecurso(
            @PathVariable String recursoId,
            @RequestParam(required = false, defaultValue = "100") int limit
    );

    /**
     * Obtener historial completo de cambios de un recurso.
     *
     * Path Parameters:
     * - recursoId: ID del recurso
     *
     * Response: Timeline con todos los cambios
     * {
     *   "recursoId": "LAB-001",
     *   "cambios": [
     *     { "timestamp": "2024-09-01T10:00:00", "accion": "CREATE", "usuario": "admin" },
     *     { "timestamp": "2024-09-05T14:30:00", "accion": "UPDATE", "usuario": "admin" }
     *   ]
     * }
     */
    @GetMapping("/api/audit/recursos/{recursoId}/timeline")
    ResponseEntity<Map<String, Object>> getTimelineRecurso(@PathVariable String recursoId);

    /**
     * Obtener eventos por usuario específico.
     *
     * Path Parameters:
     * - usuarioId: ID del usuario
     *
     * Response: Lista de acciones realizadas por el usuario
     */
    @GetMapping("/api/audit/usuarios/{usuarioId}/eventos")
    ResponseEntity<List<Map<String, Object>>> getEventosPorUsuario(
            @PathVariable String usuarioId,
            @RequestParam(required = false, defaultValue = "100") int limit
    );

    /**
     * Obtener estadísticas de auditoría.
     *
     * Response:
     * {
     *   "totalEventos": 1500,
     *   "eventosPorAccion": {
     *     "CREATE": 300,
     *     "UPDATE": 800,
     *     "DELETE": 50,
     *     "APPROVE": 200,
     *     "REJECT": 150
     *   },
     *   "usuariosMasActivos": [
     *     { "usuarioId": "admin", "eventos": 450 },
     *     { "usuarioId": "tecnico1", "eventos": 320 }
     *   ],
     *   "recursosMasModificados": [
     *     { "recursoId": "BOOK-001", "eventos": 45 },
     *     { "recursoId": "LAB-001", "eventos": 38 }
     *   ]
     * }
     */
    @GetMapping("/api/audit/estadisticas")
    ResponseEntity<Map<String, Object>> getEstadisticas();

    /**
     * Obtener estadísticas en rango de fechas.
     *
     * Query Parameters:
     * - fechaInicio: Inicio del período
     * - fechaFin: Fin del período
     */
    @GetMapping("/api/audit/estadisticas/periodo")
    ResponseEntity<Map<String, Object>> getEstadisticasPeriodo(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin
    );

    /**
     * Generar reporte de auditoría.
     *
     * Request Body:
     * {
     *   "tipo": "COMPLETO" | "RESUMEN" | "PERSONALIZADO",
     *   "fechaInicio": "2024-09-01",
     *   "fechaFin": "2024-09-30",
     *   "filtros": {
     *     "usuarios": ["admin", "tecnico1"],
     *     "acciones": ["UPDATE", "DELETE"],
     *     "recursos": ["BOOKING", "LAB"]
     *   },
     *   "formato": "JSON" | "PDF" | "CSV"
     * }
     *
     * Response: Reporte generado o URL para descarga
     */
    @PostMapping("/api/audit/reportes/generar")
    ResponseEntity<Map<String, Object>> generarReporte(@RequestBody Map<String, Object> parametros);

    /**
     * Verificar integridad de los registros de auditoría.
     *
     * Response:
     * {
     *   "estado": "OK" | "WARNINGS" | "ERROR",
     *   "registrosVerificados": 1500,
     *   "inconsistencias": 0,
     *   "ultimaVerificacion": "2024-09-12T10:30:00"
     * }
     */
    @GetMapping("/api/audit/integridad")
    ResponseEntity<Map<String, Object>> verificarIntegridad();

    /**
     * Obtener logs de acceso a recursos sensibles.
     *
     * Response: Lista de accesos a recursos con control de acceso
     */
    @GetMapping("/api/audit/accesos-sensibles")
    ResponseEntity<List<Map<String, Object>>> getAccesosSensibles(
            @RequestParam(required = false, defaultValue = "50") int limit
    );
}
