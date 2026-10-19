package org.campuslab.bff.controller;

import java.util.List;

import org.campuslab.bff.dto.AuditEventDTO;
import org.campuslab.bff.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Historial de auditoría para la pantalla "Auditoría y Trazabilidad" (solo lectura).
 *
 * Base Path: /api/audit
 *
 * Solo ADMIN y AUDITOR pueden verlo. El BFF reenvía el JWT al servicio de auditoría, que vuelve a exigir el rol.
 * (No confundir con /api/auditor/**, que son ejemplos fijos de autorización.)
 */
@RestController
@RequestMapping("/api/audit")
public class AuditApiController {

    private final AuditService auditService;

    public AuditApiController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * GET /api/audit?tipo=&userId=&from=&to=&limit=
     *
     * tipo: CREADA, APROBADA, EN_PREPARACION, EN_USO, DEVUELTA o CANCELADA.
     * userId: texto contenido en el correo o el nombre de quien hizo la acción.
     * from / to: fechas yyyy-MM-dd, ambas inclusivas.
     * Responde del evento más reciente al más antiguo.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public List<AuditEventDTO> getEvents(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "200") int limit) {
        return auditService.getEvents(tipo, userId, from, to, limit);
    }
}
