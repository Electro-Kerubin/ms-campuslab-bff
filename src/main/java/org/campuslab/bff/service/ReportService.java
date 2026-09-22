package org.campuslab.bff.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.campuslab.bff.client.ReportClient;
import org.campuslab.bff.dto.BookingHourlyMetricDTO;
import org.campuslab.bff.dto.ResourceUsageMetricDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de orquestación para reportería (proxy delgado sobre ms-report).
 */
@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ReportClient reportClient;

    @CircuitBreaker(name = "reportService", fallbackMethod = "getHourlyMetricsFallback")
    public List<BookingHourlyMetricDTO> getHourlyMetrics() {
        ResponseEntity<List<BookingHourlyMetricDTO>> response = reportClient.getHourlyMetrics("last24h");
        List<BookingHourlyMetricDTO> metrics = response.getBody();
        logger.info("Métricas horarias obtenidas: {}", metrics != null ? metrics.size() : 0);
        return metrics != null ? metrics : List.of();
    }

    @CircuitBreaker(name = "reportService", fallbackMethod = "getTopResourcesFallback")
    public List<ResourceUsageMetricDTO> getTopResources() {
        ResponseEntity<List<ResourceUsageMetricDTO>> response = reportClient.getTopResources("last7d");
        List<ResourceUsageMetricDTO> metrics = response.getBody();
        logger.info("Métricas de uso de recursos obtenidas: {}", metrics != null ? metrics.size() : 0);
        return metrics != null ? metrics : List.of();
    }

    public List<BookingHourlyMetricDTO> getHourlyMetricsFallback(Exception ex) {
        logger.error("Error al obtener métricas horarias de ms-report", ex);
        return List.of();
    }

    public List<ResourceUsageMetricDTO> getTopResourcesFallback(Exception ex) {
        logger.error("Error al obtener top de recursos de ms-report", ex);
        return List.of();
    }
}
