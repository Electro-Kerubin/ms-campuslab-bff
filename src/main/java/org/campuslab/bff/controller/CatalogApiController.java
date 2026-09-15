package org.campuslab.bff.controller;

import org.campuslab.bff.dto.CategoryDTO;
import org.campuslab.bff.dto.LabDTO;
import org.campuslab.bff.dto.LabRequestDTO;
import org.campuslab.bff.dto.ResourceDTO;
import org.campuslab.bff.dto.ResourceRequestDTO;
import org.campuslab.bff.dto.ResourceUpdateDTO;
import org.campuslab.bff.dto.StockUpdateDTO;
import org.campuslab.bff.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el catálogo (laboratorios, recursos y categorías).
 *
 * Antes de este controller, el frontend llamaba a /api/catalog/* en el BFF
 * pero no existía ningún @RestController que lo atendiera: siempre caía en
 * 404 y el frontend usaba datos semilla. Este controller expone el mismo
 * contrato que ms-campuslab-catalog (ver su LabController/ResourceController/
 * CategoryController), agregando la autorización a nivel de BFF: lectura
 * para cualquier autenticado, escritura solo ADMIN (mismo criterio que
 * ms-campuslab-catalog aplica igual en su propio SecurityConfig).
 *
 * Base Path: /api/catalog
 */
@RestController
@RequestMapping("/api/catalog")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CatalogApiController {

    @Autowired
    private CatalogService catalogService;

    // ─── Labs ───────────────────────────────────────────────────────────
    @GetMapping("/labs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LabDTO>> getAllLabs() {
        return ResponseEntity.ok(catalogService.getAllLabs());
    }

    @GetMapping("/labs/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LabDTO> getLabById(@PathVariable Long id) {
        LabDTO lab = catalogService.getLabById(id);
        return lab != null ? ResponseEntity.ok(lab) : ResponseEntity.notFound().build();
    }

    @PostMapping("/labs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LabDTO> createLab(@RequestBody LabRequestDTO body) {
        LabDTO created = catalogService.createLab(body);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/labs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LabDTO> updateLab(@PathVariable Long id, @RequestBody LabRequestDTO body) {
        LabDTO updated = catalogService.updateLab(id, body);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/labs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLab(@PathVariable Long id) {
        catalogService.deleteLab(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Resources (SALA / EQUIPO / INSUMO) ────────────────────────────
    @GetMapping("/resources")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResourceDTO>> getAllResources(@RequestParam(required = false) Long labId) {
        return ResponseEntity.ok(catalogService.getAllResources(labId));
    }

    @GetMapping("/resources/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResourceDTO> getResourceById(@PathVariable Long id) {
        ResourceDTO resource = catalogService.getResourceById(id);
        return resource != null ? ResponseEntity.ok(resource) : ResponseEntity.notFound().build();
    }

    @PostMapping("/resources")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceDTO> createResource(@RequestBody ResourceRequestDTO body) {
        ResourceDTO created = catalogService.createResource(body);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/resources/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceDTO> updateResource(@PathVariable Long id, @RequestBody ResourceUpdateDTO body) {
        ResourceDTO updated = catalogService.updateResource(id, body);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PutMapping("/resources/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<ResourceDTO> adjustStock(@PathVariable Long id, @RequestBody StockUpdateDTO body) {
        ResourceDTO updated = catalogService.adjustStock(id, body);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/resources/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        catalogService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Categories ─────────────────────────────────────────────────────
    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(catalogService.getAllCategories());
    }
}
