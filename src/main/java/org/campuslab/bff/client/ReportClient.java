package org.campuslab.bff.client;

import org.campuslab.bff.dto.ReportDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Cliente Feign para comunicación con ms-report.
 *
 * Maneja todas las operaciones relacionadas con reportería y KPIs.
 */
@FeignClient(
        name = "ms-report",
        url = "${microservice.report.url:http://localhost:8084}",
        configuration = org.campuslab.bff.config.FeignConfig.class
)
public interface ReportClient {

    /**
     * Obtener KPIs de ocupación general.
     */
    @GetMapping("/api/reports/kpi/ocupacion")
    ResponseEntity<Map<String, Object>> getOcupacionKPI();

    /**
     * Obtener KPIs de ocupación por laboratorio.
     */
    @GetMapping("/api/reports/kpi/ocupacion/{labId}")
    ResponseEntity<Map<String, Object>> getLabOcupacionKPI(@PathVariable String labId);

    /**
     * Obtener KPIs de tiempo de ciclo.
     */
    @GetMapping("/api/reports/kpi/tiempo-ciclo")
    ResponseEntity<Map<String, Object>> getCicloTimeKPI();

    /**
     * Obtener KPIs de equipos ocupados.
     */
    @GetMapping("/api/reports/kpi/equipos-ocupados")
    ResponseEntity<Map<String, Object>> getEquiposOcupadosKPI();

    /**
     * Obtener reporte de ocupación en rango de fechas.
     */
    @GetMapping("/api/reports/ocupacion")
    ResponseEntity<ReportDTO> getOcupacionReport(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin
    );

    /**
     * Obtener reporte de ocupación por laboratorio.
     */
    @GetMapping("/api/reports/ocupacion-por-lab")
    ResponseEntity<List<ReportDTO>> getOcupacionPorLabReport(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin
    );

    /**
     * Obtener reporte de equipos no devueltos.
     */
    @GetMapping("/api/reports/equipos-no-devueltos")
    ResponseEntity<ReportDTO> getEquiposNoDevueltosReport();

    /**
     * Obtener reporte de uso por estudiante.
     */
    @GetMapping("/api/reports/uso-por-estudiante/{estudianteId}")
    ResponseEntity<ReportDTO> getUsoEstudianteReport(@PathVariable String estudianteId);

    /**
     * Generar reporte personalizado.
     */
    @PostMapping("/api/reports/generar")
    ResponseEntity<ReportDTO> generarReporte(@RequestBody Map<String, Object> parametros);
}
