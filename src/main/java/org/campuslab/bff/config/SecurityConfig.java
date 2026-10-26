package org.campuslab.bff.config;

import org.campuslab.bff.security.RestAccessDeniedHandler;
import org.campuslab.bff.security.RestAuthenticationEntryPoint;
import org.campuslab.bff.security.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
@Profile("!dev")
public class SecurityConfig {

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * Si el BFF corre detrás de Azure API Management, APIM es el único dueño de CORS:
     * con CAMPUSLAB_CORS_ENABLED=false el BFF no agrega cabeceras CORS (si las agregara, la
     * respuesta llevaría Access-Control-Allow-Origin duplicado y el navegador la rechazaría).
     * Por defecto sigue activo para el desarrollo local (ng serve llama directo al BFF).
     */
    @Value("${campuslab.cors.enabled:true}")
    private boolean corsEnabled;

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
            // CORS habilitado (se apaga con campuslab.cors.enabled=false cuando lo responde APIM)
            .cors(cors -> {
                if (corsEnabled) {
                    cors.configurationSource(corsConfigurationSource());
                } else {
                    cors.disable();
                }
            })

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
                .requestMatchers("/api/admin/**").hasRole(Role.ADMIN.name())

                // Rutas TECNICO
                .requestMatchers("/api/tecnico/**").hasAnyRole(Role.TECNICO.name(), Role.ADMIN.name())

                // Rutas ESTUDIANTE
                .requestMatchers("/api/estudiante/**").hasAnyRole(Role.ESTUDIANTE.name(), Role.ADMIN.name())

                // Rutas AUDITOR
                .requestMatchers("/api/auditor/**").hasAnyRole(Role.AUDITOR.name(), Role.ADMIN.name())
                // Historial de auditoría (/api/audit): solo ADMIN y AUDITOR, también a nivel de filtro
                .requestMatchers("/api/audit/**").hasAnyRole(Role.AUDITOR.name(), Role.ADMIN.name())

                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )

            // OAuth2 Resource Server con JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )

            // 401/403 en JSON consistente (antes el entry point existía pero nunca
            // se registraba: los 401 salían con cuerpo vacío)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            );

        return http.build();
    }

    /**
     * Convierte los claims del JWT en autoridades (roles) de Spring Security.
     *
     * Para Azure EntraID:
     * - Espera el claim "roles" con los roles del usuario
     * - Si no existe, intenta con "appRoles"
     * - Convierte los roles a autoridades de Spring Security con prefijo "ROLE_"
     *
     * Ejemplo de claim en Azure:
     * {
     *   "roles": ["ADMIN", "ESTUDIANTE"],
     *   "oid": "12345...",
     *   "preferred_username": "user@example.com"
     * }
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // Azure EntraID usa el claim "roles" para los roles de aplicación
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
