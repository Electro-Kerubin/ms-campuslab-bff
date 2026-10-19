package org.campuslab.bff.dto;

import java.time.Instant;
import java.util.List;

/**
 * Refleja DlqStatus de ms-campuslab-notify (GET /api/notify/dlq).
 *
 * status: "OK" (sin mensajes en las DLQ), "ALERT" (hay mensajes fallidos) o
 * "UNAVAILABLE" (lo agrega el BFF cuando ms-notify o RabbitMQ no responden).
 */
public record DlqStatusDTO(
        String status,
        int totalInDlq,
        List<QueueStatusDTO> queues,
        Instant checkedAt
) {
    /** pending = mensajes esperando en la cola principal; failed = mensajes en su DLQ. */
    public record QueueStatusDTO(String label, String queue, String dlq, int pending, int failed) {
    }
}
