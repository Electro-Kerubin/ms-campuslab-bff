package org.campuslab.bff.client;

import org.campuslab.bff.dto.EquipmentDTO;
import org.campuslab.bff.dto.LabDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cliente Feign para comunicación con ms-catalog.
 *
 * Maneja todas las operaciones relacionadas con catálogo de laboratorios y equipos.
 */
@FeignClient(
        name = "ms-catalog",
        url = "${microservice.catalog.url:http://localhost:8082}",
        configuration = org.campuslab.bff.config.FeignConfig.class
)
public interface CatalogClient {

    /**
     * Obtener todos los laboratorios.
     */
    @GetMapping("/api/labs")
    ResponseEntity<List<LabDTO>> getAllLabs();

    /**
     * Obtener un laboratorio específico por ID.
     */
    @GetMapping("/api/labs/{id}")
    ResponseEntity<LabDTO> getLabById(@PathVariable String id);

    /**
     * Obtener laboratorios por ubicación.
     */
    @GetMapping("/api/labs/ubicacion/{ubicacion}")
    ResponseEntity<List<LabDTO>> getLabsByUbicacion(@PathVariable String ubicacion);

    /**
     * Obtener laboratorios con disponibilidad.
     */
    @GetMapping("/api/labs/disponibles")
    ResponseEntity<List<LabDTO>> getAvailableLabs();

    /**
     * Crear un nuevo laboratorio (solo ADMIN).
     */
    @PostMapping("/api/labs")
    ResponseEntity<LabDTO> createLab(@RequestBody LabDTO labDTO);

    /**
     * Actualizar un laboratorio (solo ADMIN).
     */
    @PutMapping("/api/labs/{id}")
    ResponseEntity<LabDTO> updateLab(
            @PathVariable String id,
            @RequestBody LabDTO labDTO
    );

    /**
     * Eliminar un laboratorio (solo ADMIN).
     */
    @DeleteMapping("/api/labs/{id}")
    ResponseEntity<Void> deleteLab(@PathVariable String id);

    /**
     * Obtener todos los equipos.
     */
    @GetMapping("/api/equipment")
    ResponseEntity<List<EquipmentDTO>> getAllEquipment();

    /**
     * Obtener equipos por laboratorio.
     */
    @GetMapping("/api/equipment/lab/{labId}")
    ResponseEntity<List<EquipmentDTO>> getEquipmentByLab(@PathVariable String labId);

    /**
     * Obtener un equipo específico por ID.
     */
    @GetMapping("/api/equipment/{id}")
    ResponseEntity<EquipmentDTO> getEquipmentById(@PathVariable String id);

    /**
     * Crear un nuevo equipo (solo ADMIN).
     */
    @PostMapping("/api/equipment")
    ResponseEntity<EquipmentDTO> createEquipment(@RequestBody EquipmentDTO equipmentDTO);

    /**
     * Actualizar un equipo (solo ADMIN).
     */
    @PutMapping("/api/equipment/{id}")
    ResponseEntity<EquipmentDTO> updateEquipment(
            @PathVariable String id,
            @RequestBody EquipmentDTO equipmentDTO
    );

    /**
     * Eliminar un equipo (solo ADMIN).
     */
    @DeleteMapping("/api/equipment/{id}")
    ResponseEntity<Void> deleteEquipment(@PathVariable String id);
}
