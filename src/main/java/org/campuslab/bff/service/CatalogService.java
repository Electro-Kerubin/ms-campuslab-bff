package org.campuslab.bff.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.campuslab.bff.client.CatalogClient;
import org.campuslab.bff.dto.EquipmentDTO;
import org.campuslab.bff.dto.LabDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de orquestación para operaciones de catálogo.
 *
 * Agrega datos desde ms-catalog:
 * - Laboratorios
 * - Equipos
 * - Insumos
 */
@Service
public class CatalogService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogService.class);

    @Autowired
    private CatalogClient catalogClient;

    /**
     * Obtener todos los laboratorios.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getAllLabsFallback")
    public List<LabDTO> getAllLabs() {
        logger.info("Obteniendo todos los laboratorios");

        ResponseEntity<List<LabDTO>> response = catalogClient.getAllLabs();
        List<LabDTO> labs = response.getBody();

        logger.info("Laboratorios obtenidos: {}", labs != null ? labs.size() : 0);
        return labs != null ? labs : List.of();
    }

    /**
     * Obtener un laboratorio específico por ID.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getLabByIdFallback")
    public LabDTO getLabById(String id) {
        logger.info("Obteniendo laboratorio: id={}", id);

        ResponseEntity<LabDTO> response = catalogClient.getLabById(id);
        return response.getBody();
    }

    /**
     * Obtener laboratorios por ubicación.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getLabsByUbicacionFallback")
    public List<LabDTO> getLabsByUbicacion(String ubicacion) {
        logger.info("Obteniendo laboratorios por ubicación: {}", ubicacion);

        ResponseEntity<List<LabDTO>> response = catalogClient.getLabsByUbicacion(ubicacion);
        List<LabDTO> labs = response.getBody();

        return labs != null ? labs : List.of();
    }

    /**
     * Obtener laboratorios disponibles.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getAvailableLabsFallback")
    public List<LabDTO> getAvailableLabs() {
        logger.info("Obteniendo laboratorios disponibles");

        ResponseEntity<List<LabDTO>> response = catalogClient.getAvailableLabs();
        List<LabDTO> labs = response.getBody();

        return labs != null ? labs : List.of();
    }

    /**
     * Crear un nuevo laboratorio.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "createLabFallback")
    public LabDTO createLab(LabDTO labDTO) {
        logger.info("Creando laboratorio: {}", labDTO.getNombre());

        ResponseEntity<LabDTO> response = catalogClient.createLab(labDTO);
        return response.getBody();
    }

    /**
     * Actualizar un laboratorio.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "updateLabFallback")
    public LabDTO updateLab(String id, LabDTO labDTO) {
        logger.info("Actualizando laboratorio: id={}", id);

        ResponseEntity<LabDTO> response = catalogClient.updateLab(id, labDTO);
        return response.getBody();
    }

    /**
     * Eliminar un laboratorio.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "deleteLabFallback")
    public void deleteLab(String id) {
        logger.info("Eliminando laboratorio: id={}", id);
        catalogClient.deleteLab(id);
    }

    /**
     * Obtener todos los equipos.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getAllEquipmentFallback")
    public List<EquipmentDTO> getAllEquipment() {
        logger.info("Obteniendo todos los equipos");

        ResponseEntity<List<EquipmentDTO>> response = catalogClient.getAllEquipment();
        List<EquipmentDTO> equipment = response.getBody();

        logger.info("Equipos obtenidos: {}", equipment != null ? equipment.size() : 0);
        return equipment != null ? equipment : List.of();
    }

    /**
     * Obtener equipos por laboratorio.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getEquipmentByLabFallback")
    public List<EquipmentDTO> getEquipmentByLab(String labId) {
        logger.info("Obteniendo equipos del laboratorio: {}", labId);

        ResponseEntity<List<EquipmentDTO>> response = catalogClient.getEquipmentByLab(labId);
        List<EquipmentDTO> equipment = response.getBody();

        return equipment != null ? equipment : List.of();
    }

    /**
     * Obtener un equipo específico por ID.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getEquipmentByIdFallback")
    public EquipmentDTO getEquipmentById(String id) {
        logger.info("Obteniendo equipo: id={}", id);

        ResponseEntity<EquipmentDTO> response = catalogClient.getEquipmentById(id);
        return response.getBody();
    }

    /**
     * Crear un nuevo equipo.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "createEquipmentFallback")
    public EquipmentDTO createEquipment(EquipmentDTO equipmentDTO) {
        logger.info("Creando equipo: {}", equipmentDTO.getNombre());

        ResponseEntity<EquipmentDTO> response = catalogClient.createEquipment(equipmentDTO);
        return response.getBody();
    }

    /**
     * Actualizar un equipo.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "updateEquipmentFallback")
    public EquipmentDTO updateEquipment(String id, EquipmentDTO equipmentDTO) {
        logger.info("Actualizando equipo: id={}", id);

        ResponseEntity<EquipmentDTO> response = catalogClient.updateEquipment(id, equipmentDTO);
        return response.getBody();
    }

    /**
     * Eliminar un equipo.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "deleteEquipmentFallback")
    public void deleteEquipment(String id) {
        logger.info("Eliminando equipo: id={}", id);
        catalogClient.deleteEquipment(id);
    }

    // Métodos fallback para Circuit Breaker
    public List<LabDTO> getAllLabsFallback(Exception ex) {
        logger.error("Error al obtener laboratorios", ex);
        return List.of();
    }

    public LabDTO getLabByIdFallback(String id, Exception ex) {
        logger.error("Error al obtener laboratorio {}", id, ex);
        return null;
    }

    public List<LabDTO> getLabsByUbicacionFallback(String ubicacion, Exception ex) {
        logger.error("Error al obtener laboratorios por ubicación", ex);
        return List.of();
    }

    public List<LabDTO> getAvailableLabsFallback(Exception ex) {
        logger.error("Error al obtener laboratorios disponibles", ex);
        return List.of();
    }

    public LabDTO createLabFallback(LabDTO labDTO, Exception ex) {
        logger.error("Error al crear laboratorio", ex);
        return null;
    }

    public LabDTO updateLabFallback(String id, LabDTO labDTO, Exception ex) {
        logger.error("Error al actualizar laboratorio {}", id, ex);
        return null;
    }

    public void deleteLabFallback(String id, Exception ex) {
        logger.error("Error al eliminar laboratorio {}", id, ex);
    }

    public List<EquipmentDTO> getAllEquipmentFallback(Exception ex) {
        logger.error("Error al obtener equipos", ex);
        return List.of();
    }

    public List<EquipmentDTO> getEquipmentByLabFallback(String labId, Exception ex) {
        logger.error("Error al obtener equipos del laboratorio", ex);
        return List.of();
    }

    public EquipmentDTO getEquipmentByIdFallback(String id, Exception ex) {
        logger.error("Error al obtener equipo {}", id, ex);
        return null;
    }

    public EquipmentDTO createEquipmentFallback(EquipmentDTO equipmentDTO, Exception ex) {
        logger.error("Error al crear equipo", ex);
        return null;
    }

    public EquipmentDTO updateEquipmentFallback(String id, EquipmentDTO equipmentDTO, Exception ex) {
        logger.error("Error al actualizar equipo {}", id, ex);
        return null;
    }

    public void deleteEquipmentFallback(String id, Exception ex) {
        logger.error("Error al eliminar equipo {}", id, ex);
    }
}
