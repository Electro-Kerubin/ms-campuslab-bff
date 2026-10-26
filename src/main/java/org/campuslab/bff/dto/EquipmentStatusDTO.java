package org.campuslab.bff.dto;

/**
 * Estado de un grupo de equipos para el Dashboard.
 * Contrato del frontend: EquipmentStatusSummary { name, ocupado, disponible, mantenimiento }.
 */
public record EquipmentStatusDTO(
        String name,
        long ocupado,
        long disponible,
        long mantenimiento
) {}
