package org.campuslab.bff.client;

import java.util.List;

import org.campuslab.bff.config.FeignConfig;
import org.campuslab.bff.dto.AuditEventRemoteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cliente Feign para ms-campuslab-audit (puerto 8086).
 *
 * Solo lectura: el servicio registra los eventos por su cuenta, consumiéndolos de Kafka.
 * Solo los roles ADMIN y AUDITOR pueden consultarlo; el BFF reenvía el JWT del usuario.
 *
 * (Esta interfaz tenía antes métodos hacia rutas que ningún servicio exponía: /api/audit/eventos,
 * /estadisticas, /reportes/generar... Se reemplazaron por el contrato real del servicio.)
 */
@FeignClient(
        name = "ms-audit",
        url = "${microservice.audit.url:http://localhost:8086}",
        configuration = FeignConfig.class
)
public interface AuditClient {

    /**
     * Hechos auditables, del más reciente al más antiguo. Todos los filtros son opcionales.
     * from (inclusivo) y to (exclusivo) son instantes ISO-8601, p. ej. 2026-10-19T03:00:00Z.
     */
    @GetMapping("/api/audit/events")
    ResponseEntity<List<AuditEventRemoteDTO>> getEvents(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "actor", required = false) String actor,
            @RequestParam(name = "bookingId", required = false) Long bookingId,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to,
            @RequestParam(name = "limit", defaultValue = "200") int limit
    );

    /** Historia completa de una reserva, del primer hecho al último. */
    @GetMapping("/api/audit/bookings/{bookingId}/events")
    ResponseEntity<List<AuditEventRemoteDTO>> getBookingTimeline(@PathVariable("bookingId") Long bookingId);
}
