package org.campuslab.bff.controller;

import org.campuslab.bff.dto.BookingHourlyMetricDTO;
import org.campuslab.bff.dto.ResourceUsageMetricDTO;
import org.campuslab.bff.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para reportería.
 *
 * Antes de este controller no existía ningún @RestController exponiendo
 * /api/report/* en el BFF — el ReportClient/ReportService estaban escritos
 * pero nunca conectados a ningún endpoint, así que el frontend siempre
 * caía en datos de demostración. Este controller expone el mismo contrato
 * que ms-campuslab-report (ver su ReportController): solo dos endpoints
 * reales, de solo lectura para cualquier autenticado.
 *
 * Base Path: /api/report
 */
@RestController
@RequestMapping("/api/report")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportApiController {

    @Autowired
    private ReportService reportService;

    /**
     * GET /api/report/kpis
     * Reservas por hora en las últimas 24h, agrupadas por laboratorio
     * (bookingsCount, avgCycleMinutes). Viene vacío si Kafka no está
     * corriendo o no hubo reservas nuevas en el rango.
     */
    @GetMapping("/kpis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingHourlyMetricDTO>> getHourlyMetrics() {
        return ResponseEntity.ok(reportService.getHourlyMetrics());
    }

    /**
     * GET /api/report/top-resources
     * Recursos más usados en los últimos 7 días.
     */
    @GetMapping("/top-resources")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResourceUsageMetricDTO>> getTopResources() {
        return ResponseEntity.ok(reportService.getTopResources());
    }
}
