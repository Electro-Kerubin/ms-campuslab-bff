package org.campuslab.bff.service;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import feign.RetryableException;
import org.campuslab.bff.client.AuditClient;
import org.campuslab.bff.client.CatalogClient;
import org.campuslab.bff.dto.AuditEventDTO;
import org.campuslab.bff.dto.AuditEventRemoteDTO;
import org.campuslab.bff.dto.ResourceDTO;
import org.campuslab.bff.exception.MicroserviceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Traduce el historial de ms-campuslab-audit al formato de la pantalla de Auditoría del frontend y le agrega
 * el nombre del laboratorio (dato del catálogo: el servicio de auditoría solo conoce el id del recurso).
 *
 * Filtros del frontend: tipo (CREADA, APROBADA...), userId (texto en el correo o el nombre) y rango de fechas
 * yyyy-MM-dd. Las fechas se interpretan en la zona horaria de visualización, no en UTC.
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private static final Map<String, String> TIPO_TO_CODE = Map.of(
            "CREADA", "BOOKING_CREATED",
            "APROBADA", "BOOKING_APPROVED",
            "EN_PREPARACION", "BOOKING_PREP_STARTED",
            "EN_USO", "BOOKING_IN_USE",
            "DEVUELTA", "BOOKING_RETURNED",
            "CANCELADA", "BOOKING_CANCELLED");

    private static final Map<String, String> CODE_TO_TIPO = TIPO_TO_CODE.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    private static final Map<String, String> ROLE_LABELS = Map.of(
            "ADMIN", "Admin", "TECNICO", "Técnico", "ESTUDIANTE", "Estudiante", "AUDITOR", "Auditor");

    /** El frontend define un tipo MODIFICADA que ningún servicio genera todavía: es válido pero no tiene eventos. */
    private static final String TIPO_WITHOUT_EVENTS = "MODIFICADA";

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuditClient auditClient;
    private final CatalogClient catalogClient;
    private final ZoneId zone;

    public AuditService(AuditClient auditClient, CatalogClient catalogClient,
                        @Value("${campuslab.audit.display-zone:America/Santiago}") String zone) {
        this.auditClient = auditClient;
        this.catalogClient = catalogClient;
        this.zone = ZoneId.of(zone);
    }

    public List<AuditEventDTO> getEvents(String tipo, String userId, String from, String to, int limit) {
        String code = toEventCode(tipo);
        if (tipo != null && !tipo.isBlank() && code == null) {
            return List.of();   // MODIFICADA: tipo válido sin eventos
        }
        Instant fromInstant = startOfDay(from);
        Instant toInstant = endOfDay(to);
        if (fromInstant != null && toInstant != null && !fromInstant.isBefore(toInstant)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha 'hasta' no puede ser anterior a 'desde'");
        }

        List<AuditEventRemoteDTO> events = fetch(code, blankToNull(userId), fromInstant, toInstant, limit);
        Map<Long, ResourceDTO> resources = resourcesOf(events);
        return events.stream().map(event -> toDto(event, resources.get(event.resourceId()))).toList();
    }

    // ---------- Llamada al servicio de auditoría ----------

    private List<AuditEventRemoteDTO> fetch(String code, String actor, Instant from, Instant to, int limit) {
        try {
            List<AuditEventRemoteDTO> body = auditClient.getEvents(
                    code, actor, null, from == null ? null : from.toString(), to == null ? null : to.toString(), limit).getBody();
            return body == null ? List.of() : body;
        } catch (RetryableException ex) {
            // Feign lanza esto cuando no puede conectarse (servicio caído, puerto cerrado)
            logger.error("ms-audit no responde: {}", ex.getMessage());
            throw new MicroserviceException("El servicio de auditoría no está disponible", HttpStatus.SERVICE_UNAVAILABLE.value(), null);
        }
    }

    // ---------- Enriquecimiento con el catálogo ----------

    private Map<Long, ResourceDTO> resourcesOf(List<AuditEventRemoteDTO> events) {
        Set<Long> ids = events.stream().map(AuditEventRemoteDTO::resourceId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, ResourceDTO> resources = new HashMap<>();
        for (Long id : ids) {
            try {
                ResourceDTO resource = catalogClient.getResourceById(id).getBody();
                if (resource != null) resources.put(id, resource);
            } catch (Exception ex) {
                // Que el catálogo falle o el recurso ya no exista no debe esconder el evento de auditoría
                logger.warn("No se pudo obtener el recurso {} del catálogo: {}", id, ex.getMessage());
            }
        }
        return resources;
    }

    // ---------- Traducción ----------

    private AuditEventDTO toDto(AuditEventRemoteDTO event, ResourceDTO resource) {
        String tipo = CODE_TO_TIPO.getOrDefault(event.type(), event.type());
        String usuario = firstNonBlank(event.actorName(), event.actorEmail(), "Sistema");
        String rol = event.actorRole() == null ? "Sistema" : ROLE_LABELS.getOrDefault(event.actorRole(), event.actorRole());
        String lab = resource != null && resource.getLabName() != null ? resource.getLabName()
                : event.resourceId() != null ? "Recurso #" + event.resourceId() : "—";
        String detalle = resource != null && resource.getName() != null
                ? event.typeDescription() + " · " + resource.getName()
                : event.typeDescription();

        return new AuditEventDTO(
                "EVT-" + event.id(),
                "#" + event.bookingId(),
                tipo,
                usuario,
                rol,
                lab,
                DISPLAY_FORMAT.format(event.occurredAt().atZone(zone)),
                firstNonBlank(event.clientIp(), "No disponible"),
                event.traceId(),
                detalle);
    }

    private String toEventCode(String tipo) {
        if (tipo == null || tipo.isBlank()) return null;
        String normalized = tipo.trim().toUpperCase();
        if (TIPO_WITHOUT_EVENTS.equals(normalized)) return null;
        String code = TIPO_TO_CODE.get(normalized);
        if (code == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de evento desconocido: " + tipo);
        }
        return code;
    }

    // ---------- Fechas ----------

    /** Primer instante del día indicado en la zona de visualización. */
    private Instant startOfDay(String date) {
        LocalDate day = parseDate(date, "desde");
        return day == null ? null : day.atStartOfDay(zone).toInstant();
    }

    /** Fin del día indicado = inicio del siguiente (el servicio de auditoría trata 'to' como exclusivo). */
    private Instant endOfDay(String date) {
        LocalDate day = parseDate(date, "hasta");
        return day == null ? null : day.plusDays(1).atStartOfDay(zone).toInstant();
    }

    private LocalDate parseDate(String date, String field) {
        if (date == null || date.isBlank()) return null;
        try {
            return LocalDate.parse(date.trim());
        } catch (DateTimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha '" + field + "' debe tener el formato yyyy-MM-dd");
        }
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
}
