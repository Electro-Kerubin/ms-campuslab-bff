package org.campuslab.bff.config;

import org.campuslab.bff.security.AzureAdJwtAuthenticationConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de seguridad para desarrollo LOCAL.
 *
 * ⚠️ IMPORTANTE: Esta configuración DESHABILITA autenticación para testing local.
 * NO USAR EN PRODUCCIÓN.
 *
 * Se activa solo con -Dspring.profiles.active=dev o --spring.profiles.active=dev
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
@ConditionalOnProperty(
    name = "spring.profiles.active",
    havingValue = "dev",
    matchIfMissing = false
)
@Primary
public class DevSecurityConfig {

    private final AzureAdJwtAuthenticationConverter jwtAuthenticationConverter;

    public DevSecurityConfig(AzureAdJwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    /**
     * Configuración de seguridad para desarrollo: no exige roles por ruta (todo
     * queda permitido a nivel de filtro), pero SÍ procesa el Bearer JWT si viene
     * presente, para que la Authentication quede poblada con los roles del
     * usuario (claim "roles") y los @PreAuthorize de los controllers (p.ej.
     * isAuthenticated(), hasAnyRole(...)) funcionen igual que en producción.
     *
     * ⚠️ SOLO para testing local. En producción, usar SecurityConfig.
     */
    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS habilitado
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // CSRF deshabilitado
            .csrf(csrf -> csrf.disable())

            // Sesión sin estado
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ⚠️ ALLOW ALL - Solo para desarrollo
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )

            // Procesa el JWT (si viene) para poblar roles/Authentication
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            );

        return http.build();
    }

    /**
     * Configuración de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
