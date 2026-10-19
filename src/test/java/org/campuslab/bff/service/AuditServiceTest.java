package org.campuslab.bff.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import feign.Request;
import feign.RetryableException;
import org.campuslab.bff.client.AuditClient;
import org.campuslab.bff.client.CatalogClient;
import org.campuslab.bff.dto.AuditEventDTO;
import org.campuslab.bff.dto.AuditEventRemoteDTO;
import org.campuslab.bff.dto.ResourceDTO;
import org.campuslab.bff.exception.MicroserviceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/** Traducción del historial de auditoría al formato del frontend, con el nombre del laboratorio del catálogo. */
class AuditServiceTest {

    private static final Instant OCCURRED = Instant.parse("2026-10-19T04:52:24Z");   // 01:52:24 en Chile (UTC-3)

    private final AuditClient auditClient = mock(AuditClient.class);
    private final CatalogClient catalogClient = mock(CatalogClient.class);
    private AuditService service;

    @BeforeEach
    void setUp() {
        service = new AuditService(auditClient, catalogClient, "America/Santiago");
        stubAudit(List.of());
    }

    // ---------- Formato del frontend ----------

    @Test
    void traduceUnHechoAlFormatoDeLaPantallaDeAuditoria() {
        stubAudit(List.of(remote(5L, "BOOKING_APPROVED", 7L, 3L, "Rodrigo Muñoz", "tec@duocuc.cl", "TECNICO", "200.1.2.3")));
        stubResource(3L, "Laboratorio A102", "Impresora 3D");

        List<AuditEventDTO> result = service.getEvents(null, null, null, null, 200);

        assertThat(result).singleElement().satisfies(e -> {
            assertThat(e.id()).isEqualTo("EVT-5");
            assertThat(e.reservaId()).isEqualTo("#7");
            assertThat(e.tipo()).isEqualTo("APROBADA");
            assertThat(e.usuario()).isEqualTo("Rodrigo Muñoz");
            assertThat(e.rol()).isEqualTo("Técnico");
            assertThat(e.lab()).isEqualTo("Laboratorio A102");
            assertThat(e.timestamp()).isEqualTo("2026-10-19 01:52:24");   // en hora de Chile, no UTC
            assertThat(e.ip()).isEqualTo("200.1.2.3");
            assertThat(e.traceId()).isEqualTo("trace-5");
            assertThat(e.detalle()).isEqualTo("Reserva aprobada por el técnico · Impresora 3D");
        });
    }

    @ParameterizedTest
    @CsvSource({
            "BOOKING_CREATED,CREADA",
            "BOOKING_APPROVED,APROBADA",
            "BOOKING_PREP_STARTED,EN_PREPARACION",
            "BOOKING_IN_USE,EN_USO",
            "BOOKING_RETURNED,DEVUELTA",
            "BOOKING_CANCELLED,CANCELADA"})
    void cadaTipoDelServicioTieneSuEquivalenteEnElFrontend(String code, String tipo) {
        stubAudit(List.of(remote(1L, code, 7L, null, "x", "x@duocuc.cl", "ADMIN", null)));

        assertThat(service.getEvents(null, null, null, null, 200).get(0).tipo()).isEqualTo(tipo);
    }

    @Test
    void sinNombreSeUsaElCorreoYSinActorSeInformaElSistema() {
        stubAudit(List.of(
                remote(1L, "BOOKING_CREATED", 7L, null, null, "ana@duocuc.cl", "ESTUDIANTE", null),
                remote(2L, "BOOKING_CANCELLED", 7L, null, null, null, null, null)));

        List<AuditEventDTO> result = service.getEvents(null, null, null, null, 200);

        assertThat(result.get(0).usuario()).isEqualTo("ana@duocuc.cl");
        assertThat(result.get(0).rol()).isEqualTo("Estudiante");
        assertThat(result.get(0).ip()).isEqualTo("No disponible");
        assertThat(result.get(1).usuario()).isEqualTo("Sistema");
        assertThat(result.get(1).rol()).isEqualTo("Sistema");
    }

    // ---------- Catálogo ----------

    @Test
    void siElCatalogoFallaElEventoSeMuestraIgualConElIdDelRecurso() {
        stubAudit(List.of(remote(1L, "BOOKING_APPROVED", 7L, 3L, "T", "t@duocuc.cl", "TECNICO", null)));
        when(catalogClient.getResourceById(3L)).thenThrow(new RuntimeException("catalog caído"));

        AuditEventDTO event = service.getEvents(null, null, null, null, 200).get(0);

        assertThat(event.lab()).isEqualTo("Recurso #3");
        assertThat(event.detalle()).isEqualTo("Reserva aprobada por el técnico");
    }

    @Test
    void cadaRecursoSeConsultaUnaSolaVezAunqueTengaVariosEventos() {
        stubAudit(List.of(
                remote(1L, "BOOKING_CREATED", 7L, 3L, "A", "a@duocuc.cl", "ESTUDIANTE", null),
                remote(2L, "BOOKING_APPROVED", 7L, 3L, "T", "t@duocuc.cl", "TECNICO", null),
                remote(3L, "BOOKING_CREATED", 8L, 4L, "A", "a@duocuc.cl", "ESTUDIANTE", null)));
        stubResource(3L, "Lab 1", "R1");
        stubResource(4L, "Lab 2", "R2");

        service.getEvents(null, null, null, null, 200);

        verify(catalogClient, times(1)).getResourceById(3L);
        verify(catalogClient, times(1)).getResourceById(4L);
    }

    // ---------- Filtros ----------

    @Test
    void losFiltrosDelFrontendLlegranTraducidosAlServicioDeAuditoria() {
        service.getEvents("aprobada", " tec ", "2026-10-19", "2026-10-19", 50);

        ArgumentCaptor<String> from = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> to = ArgumentCaptor.forClass(String.class);
        verify(auditClient).getEvents(eq("BOOKING_APPROVED"), eq("tec"), isNull(), from.capture(), to.capture(), eq(50));
        // el día 19 en Chile empieza a las 03:00 UTC; "hasta el 19" incluye todo el día => exclusivo al inicio del 20
        assertThat(from.getValue()).isEqualTo("2026-10-19T03:00:00Z");
        assertThat(to.getValue()).isEqualTo("2026-10-20T03:00:00Z");
    }

    @Test
    void sinFiltrosNoSeEnvianParametros() {
        service.getEvents(" ", "", null, "", 200);

        verify(auditClient).getEvents(isNull(), isNull(), isNull(), isNull(), isNull(), eq(200));
    }

    @Test
    void laZonaHorariaEsConfigurable() {
        AuditService utc = new AuditService(auditClient, catalogClient, "UTC");

        utc.getEvents(null, null, "2026-10-19", null, 200);

        verify(auditClient).getEvents(isNull(), isNull(), isNull(), eq("2026-10-19T00:00:00Z"), isNull(), eq(200));
    }

    @Test
    void elTipoModificadaEsValidoPeroNoTieneEventos() {
        assertThat(service.getEvents("MODIFICADA", null, null, null, 200)).isEmpty();
        verify(auditClient, never()).getEvents(any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    void losFiltrosInvalidosDanUn400ConMensajeClaro() {
        assertThatThrownBy(() -> service.getEvents("INVENTADO", null, null, null, 200))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(e.getReason()).contains("INVENTADO");
                });
        assertThatThrownBy(() -> service.getEvents(null, null, "19/10/2026", null, 200))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("yyyy-MM-dd");
        assertThatThrownBy(() -> service.getEvents(null, null, "2026-10-20", "2026-10-19", 200))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("hasta");
        verify(auditClient, never()).getEvents(any(), any(), any(), any(), any(), anyInt());
    }

    // ---------- Servicio de auditoría caído ----------

    @Test
    void siElServicioDeAuditoriaNoResponderSeInformaServicioNoDisponible() {
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost:8086/api/audit/events", Map.of(), null, null, null);
        when(auditClient.getEvents(any(), any(), any(), any(), any(), anyInt()))
                .thenThrow(new RetryableException(-1, "Connection refused", Request.HttpMethod.GET, (Long) null, request));

        assertThatThrownBy(() -> service.getEvents(null, null, null, null, 200))
                .isInstanceOfSatisfying(MicroserviceException.class, e -> {
                    assertThat(e.getHttpStatus()).isEqualTo(503);
                    assertThat(e.getMessage()).contains("auditoría");
                });
    }

    // ---------- helpers ----------

    private void stubAudit(List<AuditEventRemoteDTO> events) {
        when(auditClient.getEvents(any(), any(), any(), any(), any(), anyInt())).thenReturn(ResponseEntity.ok(events));
    }

    private void stubResource(long id, String labName, String name) {
        ResourceDTO resource = new ResourceDTO();
        resource.setId(id);
        resource.setLabName(labName);
        resource.setName(name);
        when(catalogClient.getResourceById(id)).thenReturn(ResponseEntity.ok(resource));
    }

    private static AuditEventRemoteDTO remote(long id, String type, Long bookingId, Long resourceId,
                                              String name, String email, String role, String ip) {
        return new AuditEventRemoteDTO(id, "evt-" + id, type, descriptionOf(type), bookingId, resourceId,
                email, name, role, ip, OCCURRED, "trace-" + id, "corr-" + id, Map.of());
    }

    private static String descriptionOf(String type) {
        return switch (type) {
            case "BOOKING_APPROVED" -> "Reserva aprobada por el técnico";
            case "BOOKING_CREATED" -> "Reserva solicitada por el estudiante";
            default -> "Evento " + type;
        };
    }
}
