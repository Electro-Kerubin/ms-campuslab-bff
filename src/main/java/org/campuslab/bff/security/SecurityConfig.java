package org.campuslab.bff.security;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracion de seguridad del BFF: valida el JWT emitido por Azure AD en
 * cada request (issuer, firma, expiracion y audience) y mapea sus roles a
 * authorities de Spring Security, siguiendo el flujo descrito en
 * context/CONTEXT.md seccion 4:
 *
 * <pre>JWT (Azure AD) -> API Gateway -> ms-campuslab-bff -> microservicio de dominio</pre>
 *
 * Este servicio no persiste sesion (STATELESS): cada request debe traer su
 * propio Bearer token. La autorizacion fina por rol/endpoint se declara con
 * {@code @PreAuthorize} en los controladores (habilitado via
 * {@link EnableMethodSecurity}), a medida que se agreguen.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtProperties jwtProperties;
    private final AzureAdJwtAuthenticationConverter jwtAuthenticationConverter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtProperties jwtProperties,
                           AzureAdJwtAuthenticationConverter jwtAuthenticationConverter,
                           RestAuthenticationEntryPoint authenticationEntryPoint,
                           RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtProperties = jwtProperties;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API sin estado consumida via Bearer token: no se necesita CSRF ni sesion HTTP.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    /**
     * Decoder de JWT: reutiliza el descubrimiento OIDC estandar de Spring a
     * partir del issuer-uri configurado, pero le agrega validacion de
     * audience, que Spring NO valida por defecto.
     */
    @Bean
    public JwtDecoder jwtDecoder(OAuth2ResourceServerProperties resourceServerProperties) {
        String issuerUri = resourceServerProperties.getJwt().getIssuerUri();
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(jwtProperties.getAudiences());
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidators, audienceValidator));

        return jwtDecoder;
    }
}
