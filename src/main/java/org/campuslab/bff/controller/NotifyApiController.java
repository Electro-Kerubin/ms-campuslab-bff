package org.campuslab.bff.controller;

import org.campuslab.bff.dto.DlqStatusDTO;
import org.campuslab.bff.service.NotifyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el estado de la mensajería (alertas de DLQ).
 *
 * Base Path: /api/notify
 */
@RestController
@RequestMapping("/api/notify")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotifyApiController {

    private final NotifyService notifyService;

    public NotifyApiController(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    /**
     * GET /api/notify/dlq
     * Cantidad de mensajes pendientes y fallidos (en DLQ) por cada flujo de
     * RabbitMQ. Es información operativa: solo TECNICO y ADMIN.
     */
    @GetMapping("/dlq")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<DlqStatusDTO> getDlqStatus() {
        return ResponseEntity.ok(notifyService.getDlqStatus());
    }
}
