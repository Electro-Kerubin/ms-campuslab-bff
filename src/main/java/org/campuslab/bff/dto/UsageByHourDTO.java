package org.campuslab.bff.dto;

/**
 * Uso por hora para el Dashboard (últimas 24h).
 * Contrato del frontend: UsageByHour { h, impresoras, microscopios, pcs, labs },
 * donde h es la etiqueta de la hora ("00".."23").
 */
public record UsageByHourDTO(
        String h,
        long impresoras,
        long microscopios,
        long pcs,
        long labs
) {}
