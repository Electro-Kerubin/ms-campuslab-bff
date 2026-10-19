package org.campuslab.bff.controller;

import org.campuslab.bff.client.NotifyClient;
import org.campuslab.bff.dto.DlqStatusDTO;
import org.campuslab.bff.dto.DlqStatusDTO.QueueStatusDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /api/notify/dlq: permisos por rol y comportamiento cuando ms-notify no responde.
 * Se mockea JwtDecoder (para no resolver el issuer de Azure por red) y NotifyClient.
 */
@SpringBootTest
@AutoConfigureMockMvc
class NotifyApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private NotifyClient notifyClient;

    @Test
    void tecnicoVeElEstadoDeLasDlq() throws Exception {
        DlqStatusDTO alert = new DlqStatusDTO(
                "ALERT", 2,
                List.of(new QueueStatusDTO("Email", "q.cmd.email", "q.cmd.email.dlq", 0, 2)),
                Instant.now());
        when(notifyClient.getDlqStatus()).thenReturn(ResponseEntity.ok(alert));

        mockMvc.perform(get("/api/notify/dlq")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TECNICO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALERT"))
                .andExpect(jsonPath("$.totalInDlq").value(2))
                .andExpect(jsonPath("$.queues[0].dlq").value("q.cmd.email.dlq"))
                .andExpect(jsonPath("$.queues[0].failed").value(2));
    }

    @Test
    void estudianteNoPuedeVerElEstadoDeLasDlq() throws Exception {
        mockMvc.perform(get("/api/notify/dlq")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void siNotifyNoRespondeSeInformaUnavailableEnVezDeOk() throws Exception {
        when(notifyClient.getDlqStatus()).thenThrow(new RuntimeException("ms-notify caído"));

        mockMvc.perform(get("/api/notify/dlq")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.totalInDlq").value(0));
    }
}
