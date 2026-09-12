package org.campuslab.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de Spring Security con autorización por roles.
 *
 * Características:
 * - Validación de JWT desde OAuth2 Resource Server
 * - Autorización basada en roles (ADMIN, TECNICO, ESTUDIANTE, AUDITOR)
 * - Protección de endpoints específicos
 * - Sin estado (Stateless) - apropiado para microservicios
 * - CORS habilitado
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
public class SecurityConfig {

    /**
     * Configuración de la cadena de filtros de seguridad HTTP.
     *
     * Define:
     * - Rutas públicas vs protegidas
     * - Autenticación basada en JWT
     * - Sesión sin estado (Stateless)
     * - CORS habilitado
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS habilitado
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // CSRF deshabilitado (apropiado para APIs stateless)
            .csrf(csrf -> csrf.disable())

            // Sesión sin estado
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Autorización de endpoints
            .authorizeHttpRequests(authz -> authz
                // Rutas públicas
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Rutas ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Rutas TECNICO
                .requestMatchers("/api/tecnico/**").hasAnyRole("TECNICO", "ADMIN")

                // Rutas ESTUDIANTE
                .requestMatchers("/api/estudiante/**").hasAnyRole("ESTUDIANTE", "ADMIN")

                // Rutas AUDITOR
                .requestMatchers("/api/auditor/**").hasAnyRole("AUDITOR", "ADMIN")

                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )

            // OAuth2 Resource Server con JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * Convierte los claims del JWT en autoridades (roles) de Spring Security.
     *
     * Espera que el JWT contenga un claim "roles" o "authorities"
     * con los roles del usuario.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // Configura el nombre del claim que contiene los roles
        // Puedes cambiar esto según tu proveedor de JWT (Keycloak, Auth0, etc.)
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }

    /**
     * Configuración de CORS para permitir solicitudes desde el frontend.
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
