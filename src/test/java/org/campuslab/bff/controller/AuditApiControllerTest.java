package org.campuslab.bff.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.campuslab.bff.client.AuditClient;
import org.campuslab.bff.client.CatalogClient;
import org.campuslab.bff.dto.AuditEventRemoteDTO;
import org.campuslab.bff.dto.ResourceDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** GET /api/audit: permisos por rol, formato para el frontend y errores (con el BFF completo y los clientes mockeados). */
@SpringBootTest
@AutoConfigureMockMvc
class AuditApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private AuditClient auditClient;
    @MockBean
    private CatalogClient catalogClient;

    private static RequestPostProcessor role(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private void auditReturns(List<AuditEventRemoteDTO> events) {
        when(auditClient.getEvents(any(), any(), any(), any(), any(), anyInt())).thenReturn(ResponseEntity.ok(events));
    }

    private static AuditEventRemoteDTO event() {
        return new AuditEventRemoteDTO(5L, "evt-5", "BOOKING_APPROVED", "Reserva aprobada por el técnico", 7L, 3L,
                "tec@duocuc.cl", "Rodrigo Muñoz", "TECNICO", "200.1.2.3",
                Instant.parse("2026-10-19T04:52:24Z"), "trace-5", "corr-5", Map.of("status", "APROBADA"));
    }

    @Test
    void elAuditorYElAdminVenElHistorialEnElFormatoDelFrontend() throws Exception {
        auditReturns(List.of(event()));
        ResourceDTO resource = new ResourceDTO();
        resource.setLabName("Laboratorio A102");
        resource.setName("Impresora 3D");
        when(catalogClient.getResourceById(3L)).thenReturn(ResponseEntity.ok(resource));

        for (String role : List.of("AUDITOR", "ADMIN")) {
            mockMvc.perform(get("/api/audit").with(role(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value("EVT-5"))
                    .andExpect(jsonPath("$[0].reservaId").value("#7"))
                    .andExpect(jsonPath("$[0].tipo").value("APROBADA"))
                    .andExpect(jsonPath("$[0].usuario").value("Rodrigo Muñoz"))
                    .andExpect(jsonPath("$[0].rol").value("Técnico"))
                    .andExpect(jsonPath("$[0].lab").value("Laboratorio A102"))
                    .andExpect(jsonPath("$[0].timestamp").value("2026-10-19 01:52:24"))
                    .andExpect(jsonPath("$[0].ip").value("200.1.2.3"))
                    .andExpect(jsonPath("$[0].traceId").value("trace-5"))
                    .andExpect(jsonPath("$[0].detalle").value("Reserva aprobada por el técnico · Impresora 3D"));
        }
    }

    @Test
    void losFiltrosDeLaPantallaLleganAlServicioDeAuditoria() throws Exception {
        auditReturns(List.of());

        mockMvc.perform(get("/api/audit").with(role("AUDITOR"))
                        .param("tipo", "CANCELADA").param("userId", "ana").param("from", "2026-10-19").param("to", "2026-10-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(auditClient).getEvents(eq("BOOKING_CANCELLED"), eq("ana"), isNull(),
                eq("2026-10-19T03:00:00Z"), eq("2026-10-21T03:00:00Z"), eq(200));
    }

    @Test
    void tecnicosYEstudiantesNoVenLaAuditoria() throws Exception {
        for (String role : List.of("TECNICO", "ESTUDIANTE")) {
            mockMvc.perform(get("/api/audit").with(role(role))).andExpect(status().isForbidden());
        }
        verifyNoInteractions(auditClient);
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/audit")).andExpect(status().isUnauthorized());
        verifyNoInteractions(auditClient);
    }

    @Test
    void unTipoDesconocidoDaUn400NoUn500() throws Exception {
        mockMvc.perform(get("/api/audit").with(role("ADMIN")).param("tipo", "INVENTADO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Tipo de evento desconocido: INVENTADO"));
    }

    @Test
    void siElServicioDeAuditoriaEstaCaidoSeResponde503NoUn500() throws Exception {
        when(auditClient.getEvents(any(), any(), any(), any(), any(), anyInt()))
                .thenThrow(new feign.RetryableException(-1, "Connection refused", feign.Request.HttpMethod.GET, (Long) null,
                        feign.Request.create(feign.Request.HttpMethod.GET, "http://localhost:8086", Map.of(), null, null, null)));

        mockMvc.perform(get("/api/audit").with(role("ADMIN")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("El servicio de auditoría no está disponible"));
    }

    @Test
    void siElServicioDeAuditoriaRechazaElFiltroSeConservaSuCodigo() throws Exception {
        when(auditClient.getEvents(any(), any(), any(), any(), any(), anyInt()))
                .thenThrow(new org.campuslab.bff.exception.MicroserviceException(
                        "Solicitud inválida", 400, "{\"message\":\"limit debe estar entre 1 y 1000\"}"));

        mockMvc.perform(get("/api/audit").with(role("ADMIN")).param("limit", "5000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("limit debe estar entre 1 y 1000"));
    }

    // ---------- Errores de ruta (antes se veían como 500) ----------

    @Test
    void unaRutaQueNoExisteDa404NoUn500() throws Exception {
        mockMvc.perform(get("/api/ruta-que-no-existe").with(role("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ruta no encontrada"));
    }

    @Test
    void laAuditoriaEsDeSoloLecturaUnPostDa405() throws Exception {
        mockMvc.perform(post("/api/audit").with(role("ADMIN")).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isMethodNotAllowed());
    }
}
