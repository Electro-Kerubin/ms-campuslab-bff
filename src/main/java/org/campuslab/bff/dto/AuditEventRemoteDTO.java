package org.campuslab.bff.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Refleja AuditEventResponse de ms-campuslab-audit (GET /api/audit/events).
 *
 * type es el código del servicio de auditoría (BOOKING_APPROVED...), no el vocabulario del frontend:
 * AuditService lo traduce a {@link AuditEventDTO}.
 */
public record AuditEventRemoteDTO(
        Long id,
        String eventId,
        String type,
        String typeDescription,
        Long bookingId,
        Long resourceId,
        String actorEmail,
        String actorName,
        String actorRole,
        String clientIp,
        Instant occurredAt,
        String traceId,
        String correlationId,
        Map<String, Object> details
) {
}
