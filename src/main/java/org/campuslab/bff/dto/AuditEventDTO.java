package org.campuslab.bff.dto;

/**
 * Un evento de auditoría tal como lo espera la pantalla "Auditoría y Trazabilidad" del frontend
 * (interfaz AuditEvent en src/app/models/index.ts). Los nombres están en español a propósito: son los del frontend.
 *
 * tipo: CREADA, APROBADA, EN_PREPARACION, EN_USO, DEVUELTA o CANCELADA.
 * timestamp: "yyyy-MM-dd HH:mm:ss" en la zona horaria de visualización.
 */
public record AuditEventDTO(
        String id,
        String reservaId,
        String tipo,
        String usuario,
        String rol,
        String lab,
        String timestamp,
        String ip,
        String traceId,
        String detalle
) {
}
