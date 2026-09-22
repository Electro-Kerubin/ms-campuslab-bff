package org.campuslab.bff.client;

import org.campuslab.bff.dto.BookingHourlyMetricDTO;
import org.campuslab.bff.dto.ResourceUsageMetricDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Cliente Feign para comunicación con ms-report.
 *
 * Solo dos endpoints reales — ver ReportController de ms-campuslab-report.
 * Ambos leen de tablas que solo se llenan consumiendo eventos Kafka del
 * topic "bookings.events"; sin Kafka corriendo o sin reservas recientes,
 * ambos devuelven listas vacías (no es un error).
 */
@FeignClient(
        name = "ms-report",
        url = "${microservice.report.url:http://localhost:8083}",
        configuration = org.campuslab.bff.config.FeignConfig.class
)
public interface ReportClient {

    /** range solo admite "last24h" (único valor que acepta el backend real). */
    @GetMapping("/api/report/kpis")
    ResponseEntity<List<BookingHourlyMetricDTO>> getHourlyMetrics(@RequestParam String range);

    /** range solo admite "last7d" (único valor que acepta el backend real). */
    @GetMapping("/api/report/top-resources")
    ResponseEntity<List<ResourceUsageMetricDTO>> getTopResources(@RequestParam String range);
}
