package org.campuslab.bff.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.campuslab.bff.client.CatalogClient;
import org.campuslab.bff.dto.CategoryDTO;
import org.campuslab.bff.dto.LabDTO;
import org.campuslab.bff.dto.LabRequestDTO;
import org.campuslab.bff.dto.ResourceDTO;
import org.campuslab.bff.dto.ResourceRequestDTO;
import org.campuslab.bff.dto.ResourceUpdateDTO;
import org.campuslab.bff.dto.StockUpdateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de orquestación para operaciones de catálogo (proxy delgado
 * sobre ms-catalog: labs, recursos —SALA/EQUIPO/INSUMO— y categorías).
 */
@Service
public class CatalogService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogService.class);

    @Autowired
    private CatalogClient catalogClient;

    // ─── Labs ───────────────────────────────────────────────────────────
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getAllLabsFallback")
    public List<LabDTO> getAllLabs() {
        ResponseEntity<List<LabDTO>> response = catalogClient.getAllLabs();
        List<LabDTO> labs = response.getBody();
        logger.info("Laboratorios obtenidos: {}", labs != null ? labs.size() : 0);
        return labs != null ? labs : List.of();
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "getLabByIdFallback")
    public LabDTO getLabById(Long id) {
        return catalogClient.getLabById(id).getBody();
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "createLabFallback")
    public LabDTO createLab(LabRequestDTO lab) {
        logger.info("Creando laboratorio: {}", lab.getName());
        return catalogClient.createLab(lab).getBody();
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "updateLabFallback")
    public LabDTO updateLab(Long id, LabRequestDTO lab) {
        return catalogClient.updateLab(id, lab).getBody();
    }

    public void deleteLab(Long id) {
        catalogClient.deleteLab(id);
    }

    // ─── Resources ──────────────────────────────────────────────────────
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getAllResourcesFallback")
    public List<ResourceDTO> getAllResources(Long labId) {
        ResponseEntity<List<ResourceDTO>> response = catalogClient.getAllResources(labId);
        List<ResourceDTO> resources = response.getBody();
        logger.info("Recursos obtenidos: {}", resources != null ? resources.size() : 0);
        return resources != null ? resources : List.of();
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "getResourceByIdFallback")
    public ResourceDTO getResourceById(Long id) {
        return catalogClient.getResourceById(id).getBody();
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "createResourceFallback")
    public ResourceDTO createResource(ResourceRequestDTO resource) {
        logger.info("Creando recurso: {} (tipo={})", resource.getName(), resource.getResourceType());
        return catalogClient.createResource(resource).getBody();
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "updateResourceFallback")
    public ResourceDTO updateResource(Long id, ResourceUpdateDTO resource) {
        return catalogClient.updateResource(id, resource).getBody();
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "adjustStockFallback")
    public ResourceDTO adjustStock(Long id, StockUpdateDTO stock) {
        logger.info("Ajustando stock de recurso {}: delta={}", id, stock.getDelta());
        return catalogClient.adjustStock(id, stock).getBody();
    }

    public void deleteResource(Long id) {
        catalogClient.deleteResource(id);
    }

    // ─── Categories ─────────────────────────────────────────────────────
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getAllCategoriesFallback")
    public List<CategoryDTO> getAllCategories() {
        ResponseEntity<List<CategoryDTO>> response = catalogClient.getAllCategories();
        List<CategoryDTO> categories = response.getBody();
        return categories != null ? categories : List.of();
    }

    // ─── Fallbacks (Circuit Breaker) ────────────────────────────────────
    public List<LabDTO> getAllLabsFallback(Exception ex) {
        logger.error("Error al obtener laboratorios", ex);
        return List.of();
    }

    public LabDTO getLabByIdFallback(Long id, Exception ex) {
        logger.error("Error al obtener laboratorio {}", id, ex);
        return null;
    }

    public LabDTO createLabFallback(LabRequestDTO lab, Exception ex) {
        logger.error("Error al crear laboratorio", ex);
        return null;
    }

    public LabDTO updateLabFallback(Long id, LabRequestDTO lab, Exception ex) {
        logger.error("Error al actualizar laboratorio {}", id, ex);
        return null;
    }

    public List<ResourceDTO> getAllResourcesFallback(Long labId, Exception ex) {
        logger.error("Error al obtener recursos", ex);
        return List.of();
    }

    public ResourceDTO getResourceByIdFallback(Long id, Exception ex) {
        logger.error("Error al obtener recurso {}", id, ex);
        return null;
    }

    public ResourceDTO createResourceFallback(ResourceRequestDTO resource, Exception ex) {
        logger.error("Error al crear recurso", ex);
        return null;
    }

    public ResourceDTO updateResourceFallback(Long id, ResourceUpdateDTO resource, Exception ex) {
        logger.error("Error al actualizar recurso {}", id, ex);
        return null;
    }

    public ResourceDTO adjustStockFallback(Long id, StockUpdateDTO stock, Exception ex) {
        logger.error("Error al ajustar stock del recurso {}", id, ex);
        return null;
    }

    public List<CategoryDTO> getAllCategoriesFallback(Exception ex) {
        logger.error("Error al obtener categorías", ex);
        return List.of();
    }
}
