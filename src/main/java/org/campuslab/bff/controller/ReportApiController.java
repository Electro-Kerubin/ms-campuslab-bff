package org.campuslab.bff.controller;

import org.campuslab.bff.client.BookingsClient;
import org.campuslab.bff.client.CatalogClient;
import org.campuslab.bff.dto.BookingDTO;
import org.campuslab.bff.dto.BookingHourlyMetricDTO;
import org.campuslab.bff.dto.EquipmentStatusDTO;
import org.campuslab.bff.dto.ResourceDTO;
import org.campuslab.bff.dto.ResourceUsageMetricDTO;
import org.campuslab.bff.dto.UsageByHourDTO;
import org.campuslab.bff.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
 * Además expone dos agregados que el Dashboard consume (usage-by-hour y
 * equipment-status), calculados en vivo desde catálogo y reservas: hasta
 * ahora el frontend los resolvía con datos de demostración porque ningún
 * backend los exponía.
 *
 * Base Path: /api/report
 */
@RestController
@RequestMapping("/api/report")
public class ReportApiController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private CatalogClient catalogClient;

    @Autowired
    private BookingsClient bookingsClient;

    private static final DateTimeFormatter HOUR_LABEL = DateTimeFormatter.ofPattern("HH");

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

    /**
     * GET /api/report/equipment-status
     * Estado de los EQUIPOs del catálogo agrupados por categoría:
     * cuántos están en uso, disponibles o en mantenimiento. Calculado en
     * vivo desde ms-catalog (no depende de Kafka).
     */
    @GetMapping("/equipment-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentStatusDTO>> getEquipmentStatus() {
        List<ResourceDTO> resources = safeBody(() -> catalogClient.getAllResources(null));

        Map<String, long[]> porCategoria = new LinkedHashMap<>();
        for (ResourceDTO r : resources) {
            if (!"EQUIPO".equalsIgnoreCase(nullSafe(r.getResourceType()))) {
                continue;
            }
            String categoria = r.getCategoryName() != null ? r.getCategoryName() : "Sin categoría";
            // [ocupado, disponible, mantenimiento]
            long[] contadores = porCategoria.computeIfAbsent(categoria, k -> new long[3]);
            String status = nullSafe(r.getStatus()).toUpperCase(Locale.ROOT);
            switch (status) {
                case "EN_USO" -> contadores[0]++;
                case "DISPONIBLE" -> contadores[1]++;
                case "MANTENIMIENTO" -> contadores[2]++;
                default -> { /* otros estados no se grafican */ }
            }
        }

        List<EquipmentStatusDTO> out = porCategoria.entrySet().stream()
                .map(e -> new EquipmentStatusDTO(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]))
                .toList();
        return ResponseEntity.ok(out);
    }

    /**
     * GET /api/report/usage-by-hour
     * Reservas creadas en las últimas 24h agrupadas por hora y tipo de
     * recurso (series del Dashboard: impresoras, microscopios, pcs, labs).
     * Calculado en vivo desde ms-bookings + ms-catalog (no depende de Kafka).
     */
    @GetMapping("/usage-by-hour")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UsageByHourDTO>> getUsageByHour() {
        List<BookingDTO> bookings = safeBody(() ->
                bookingsClient.getAllBookings(null, LocalDateTime.now().minusHours(24).toString(), null));
        Map<Long, ResourceDTO> recursosPorId = safeBody(() -> catalogClient.getAllResources(null))
                .stream()
                .collect(Collectors.toMap(ResourceDTO::getId, Function.identity(), (a, b) -> a));

        // Acumula por etiqueta de hora ("00".."23") en orden ascendente
        Map<String, long[]> porHora = new LinkedHashMap<>();
        LocalDateTime ahora = LocalDateTime.now();
        for (int i = 23; i >= 0; i--) {
            porHora.put(ahora.minusHours(i).format(HOUR_LABEL), new long[4]);
        }

        for (BookingDTO b : bookings) {
            if (b.getStartTime() == null || b.getResourceId() == null) {
                continue;
            }
            String hora;
            try {
                hora = LocalDateTime.parse(b.getStartTime()).format(HOUR_LABEL);
            } catch (Exception e) {
                continue;
            }
            long[] contadores = porHora.get(hora);
            if (contadores == null) {
                continue; // fuera de las últimas 24h
            }
            ResourceDTO recurso = recursosPorId.get(b.getResourceId());
            String tipo = recurso != null ? nullSafe(recurso.getResourceType()).toUpperCase(Locale.ROOT) : "";
            String nombre = recurso != null ? nullSafe(recurso.getName()).toLowerCase(Locale.ROOT) : "";
            if ("SALA".equals(tipo)) {
                contadores[3]++; // labs
            } else if (nombre.contains("impresora") || nombre.contains("printer")) {
                contadores[0]++; // impresoras
            } else if (nombre.contains("microscopio") || nombre.contains("microscope")) {
                contadores[1]++; // microscopios
            } else {
                contadores[2]++; // pcs / resto de equipos
            }
        }

        List<UsageByHourDTO> out = new ArrayList<>();
        porHora.forEach((h, c) -> out.add(new UsageByHourDTO(h, c[0], c[1], c[2], c[3])));
        return ResponseEntity.ok(out);
    }

    private static <T> List<T> safeBody(java.util.function.Supplier<ResponseEntity<List<T>>> call) {
        try {
            ResponseEntity<List<T>> r = call.get();
            return r.getBody() != null ? r.getBody() : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }
}
