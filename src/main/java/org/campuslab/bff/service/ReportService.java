package org.campuslab.bff.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.campuslab.bff.client.ReportClient;
import org.campuslab.bff.dto.ReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de orquestación para operaciones de reportería.
 *
 * Agrega datos desde ms-report:
 * - KPIs de ocupación
 * - Reportes de uso
 * - Estadísticas
 */
@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ReportClient reportClient;

    /**
     * Obtener KPIs de ocupación general.
     */
    @CircuitBreaker(name = "reportService", fallbackMethod = "getOcupacionKPIFallback")
    public Map<String, Object> getOcupacionKPI() {
        logger.info("Obteniendo KPI de ocupación general");

        ResponseEntity<Map<String, Object>> response = reportClient.getOcupacionKPI();
        Map<String, Object> kpi = response.getBody();

        return kpi != null ? kpi : new HashMap<>();
    }

    /**
     * Obtener KPIs de ocupación por laboratorio.
     */
    @CircuitBreaker(name = "reportService", fallbackMethod = "getLabOcupacionKPIFallback")
    public Map<String, Object> getLabOcupacionKPI(String labId) {
        logger.info("Obteniendo KPI de ocupación del laboratorio: {}", labId);

        ResponseEntity<Map<String, Object>> response = reportClient.getLabOcupacionKPI(labId);
        Map<String, Object> kpi = response.getBody();

        return kpi != null ? kpi : new HashMap<>();
    }

    /**
     * Obtener KPIs de tiempo de ciclo.
     */
    @CircuitBreaker(name = "reportService", fallbackMethod = "getCicloTimeKPIFallback")
    public Map<String, Object> getCicloTimeKPI() {
        logger.info("Obteniendo KPI de tiempo de ciclo");

        ResponseEntity<Map<String, Object>> response = reportClient.getCicloTimeKPI();
        Map<String, Object> kpi = response.getBody();

        return kpi != null ? kpi : new HashMap<>();
    }

    /**
     * Obtener KPIs de equipos ocupados.
     */
    @CircuitBreaker(name = "reportService", fallbackMethod = "getEquiposOcupadosKPIFallback")
    public Map<String, Object> getEquiposOcupadosKPI() {
        logger.info("Obteniendo KPI de equipos ocupados");

        ResponseEntity<Map<String, Object>> response = reportClient.getEquiposOcupadosKPI();
        Map<String, Object> kpi = response.getBody();

        return kpi != null ? kpi : new HashMap<>();
    }

    /**
     * Obtener reporte de ocupación en rango de fechas.
     */
    @CircuitBreaker(name = "reportService", fallbackMethod = "getOcupacionReportFallback")
    public ReportDTO getOcupacionReport(String fechaInicio, String fechaFin) {
        logger.info("Obteniendo reporte de ocupación: {} a {}", fechaInicio, fechaFin);

        ResponseEntity<ReportDTO> response = reportClient.getOcupacionReport(fechaInicio, fechaFin);
        return response.getBody();
    }

    /**
     * Obtener reporte de ocupación por laboratorio.
     */
    @CircuitBreaker(name = "reportService", fallbackMethod = "getOcupacionPorLabReportFallback")
    public List<ReportDTO> getOcupacionPorLabReport(String fechaInicio, String fechaFin) {
        logger.info("Obteniendo reporte de ocupación por laboratorio: {} a {}", fechaInicio, fechaFin);

        ResponseEntity<List<ReportDTO>> response = reportClient.getOcupacionPorLabReport(fechaInicio, fechaFin);
        List<ReportDTO> reports = response.getBody();

        return reports != null ? reports : List.of();
    }

    /**
     * Obtener reporte de equipos no devueltos.
     */
    @CircuitBreaker(name = "reportService", fallbackMethod = "getEquiposNoDevueltosReportFallback")
    public ReportDTO getEquiposNoDevueltosReport() {
        logger.info("Obteniendo reporte de equipos no devueltos");

        ResponseEntity<ReportDTO> response = reportClient.getEquiposNoDevueltosReport();
        return response.getBody();
    }

    /**
     * Obtener reporte de uso por estudiante.
     */
    @CircuitBreaker(name = "reportService", fallbackMethod = "getUsoEstudianteReportFallback")
    public ReportDTO getUsoEstudianteReport(String estudianteId) {
        logger.info("Obteniendo reporte de uso del estudiante: {}", estudianteId);

        ResponseEntity<ReportDTO> response = reportClient.getUsoEstudianteReport(estudianteId);
        return response.getBody();
    }

    /**
     * Generar reporte personalizado.
     */
    @CircuitBreaker(name = "reportService", fallbackMethod = "generarReporteFallback")
    public ReportDTO generarReporte(Map<String, Object> parametros) {
        logger.info("Generando reporte personalizado");

        ResponseEntity<ReportDTO> response = reportClient.generarReporte(parametros);
        return response.getBody();
    }

    // Métodos fallback para Circuit Breaker
    public Map<String, Object> getOcupacionKPIFallback(Exception ex) {
        logger.error("Error al obtener KPI de ocupación", ex);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Servicio no disponible");
        return fallback;
    }

    public Map<String, Object> getLabOcupacionKPIFallback(String labId, Exception ex) {
        logger.error("Error al obtener KPI de ocupación del laboratorio", ex);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Servicio no disponible");
        return fallback;
    }

    public Map<String, Object> getCicloTimeKPIFallback(Exception ex) {
        logger.error("Error al obtener KPI de tiempo de ciclo", ex);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Servicio no disponible");
        return fallback;
    }

    public Map<String, Object> getEquiposOcupadosKPIFallback(Exception ex) {
        logger.error("Error al obtener KPI de equipos ocupados", ex);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Servicio no disponible");
        return fallback;
    }

    public ReportDTO getOcupacionReportFallback(String fechaInicio, String fechaFin, Exception ex) {
        logger.error("Error al obtener reporte de ocupación", ex);
        return null;
    }

    public List<ReportDTO> getOcupacionPorLabReportFallback(String fechaInicio, String fechaFin, Exception ex) {
        logger.error("Error al obtener reporte de ocupación por laboratorio", ex);
        return List.of();
    }

    public ReportDTO getEquiposNoDevueltosReportFallback(Exception ex) {
        logger.error("Error al obtener reporte de equipos no devueltos", ex);
        return null;
    }

    public ReportDTO getUsoEstudianteReportFallback(String estudianteId, Exception ex) {
        logger.error("Error al obtener reporte de uso del estudiante", ex);
        return null;
    }

    public ReportDTO generarReporteFallback(Map<String, Object> parametros, Exception ex) {
        logger.error("Error al generar reporte", ex);
        return null;
    }
}
