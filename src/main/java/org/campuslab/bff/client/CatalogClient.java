package org.campuslab.bff.client;

import org.campuslab.bff.dto.CategoryDTO;
import org.campuslab.bff.dto.LabDTO;
import org.campuslab.bff.dto.LabRequestDTO;
import org.campuslab.bff.dto.ResourceDTO;
import org.campuslab.bff.dto.ResourceRequestDTO;
import org.campuslab.bff.dto.ResourceUpdateDTO;
import org.campuslab.bff.dto.StockUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cliente Feign para comunicación con ms-catalog.
 *
 * Los endpoints reflejan exactamente los controllers reales de
 * ms-campuslab-catalog (LabController, ResourceController, CategoryController).
 */
@FeignClient(
        name = "ms-catalog",
        url = "${microservice.catalog.url:http://localhost:8082}",
        configuration = org.campuslab.bff.config.FeignConfig.class
)
public interface CatalogClient {

    // ─── Labs ───────────────────────────────────────────────────────────
    @GetMapping("/api/catalog/labs")
    ResponseEntity<List<LabDTO>> getAllLabs();

    @GetMapping("/api/catalog/labs/{id}")
    ResponseEntity<LabDTO> getLabById(@PathVariable Long id);

    @PostMapping("/api/catalog/labs")
    ResponseEntity<LabDTO> createLab(@RequestBody LabRequestDTO lab);

    @PutMapping("/api/catalog/labs/{id}")
    ResponseEntity<LabDTO> updateLab(@PathVariable Long id, @RequestBody LabRequestDTO lab);

    @DeleteMapping("/api/catalog/labs/{id}")
    ResponseEntity<Void> deleteLab(@PathVariable Long id);

    // ─── Resources (SALA / EQUIPO / INSUMO) ────────────────────────────
    @GetMapping("/api/catalog/resources")
    ResponseEntity<List<ResourceDTO>> getAllResources(@RequestParam(required = false) Long labId);

    @GetMapping("/api/catalog/resources/{id}")
    ResponseEntity<ResourceDTO> getResourceById(@PathVariable Long id);

    @PostMapping("/api/catalog/resources")
    ResponseEntity<ResourceDTO> createResource(@RequestBody ResourceRequestDTO resource);

    @PutMapping("/api/catalog/resources/{id}")
    ResponseEntity<ResourceDTO> updateResource(@PathVariable Long id, @RequestBody ResourceUpdateDTO resource);

    @PutMapping("/api/catalog/resources/{id}/stock")
    ResponseEntity<ResourceDTO> adjustStock(@PathVariable Long id, @RequestBody StockUpdateDTO stock);

    @DeleteMapping("/api/catalog/resources/{id}")
    ResponseEntity<Void> deleteResource(@PathVariable Long id);

    // ─── Categories ─────────────────────────────────────────────────────
    @GetMapping("/api/catalog/categories")
    ResponseEntity<List<CategoryDTO>> getAllCategories();
}
