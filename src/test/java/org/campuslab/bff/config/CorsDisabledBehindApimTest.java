package org.campuslab.bff.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Detrás de Azure API Management (CAMPUSLAB_CORS_ENABLED=false) el BFF no debe agregar cabeceras
 * CORS: APIM es el único dueño. Si el BFF también las agregara, el navegador recibiría
 * Access-Control-Allow-Origin duplicado y rechazaría la respuesta.
 */
@SpringBootTest(properties = "campuslab.cors.enabled=false")
@AutoConfigureMockMvc
class CorsDisabledBehindApimTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void elBffNoAgregaCabecerasCors_ni_siquieraConOrigin() throws Exception {
        mockMvc.perform(get("/api/public/health").header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }
}
