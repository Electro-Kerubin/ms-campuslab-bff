package org.campuslab.bff.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el comportamiento del filtro de seguridad de punta a punta.
 *
 * Se mockea {@link JwtDecoder} para que el contexto no intente resolver el
 * issuer de Azure AD por red al arrancar (JwtDecoders.fromIssuerLocation en
 * {@link SecurityConfig#jwtDecoder}); los tests que usan un token valido lo
 * inyectan directamente con el post-processor {@code jwt()} de
 * spring-security-test, que no pasa por el decoder.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void requestWithoutTokenIsRejectedWithJsonUnauthorizedBody() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/bookings"));
    }

    @Test
    void requestWithValidJwtPassesTheSecurityFilterChain() throws Exception {
        // 404 (no hay controlador mapeado todavia) en vez de 401/403 confirma
        // que la request supero la autenticacion.
        mockMvc.perform(get("/api/bookings")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ESTUDIANTE")))))
                .andExpect(status().isNotFound());
    }
}
