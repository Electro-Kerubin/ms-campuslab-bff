package org.campuslab.bff.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AzureAdJwtAuthenticationConverterTest {

    private final JwtProperties jwtProperties = new JwtProperties();
    private final AzureAdJwtAuthenticationConverter converter = new AzureAdJwtAuthenticationConverter(jwtProperties);

    @Test
    void mapsRolesClaimToPrefixedRoleAuthorities() {
        Jwt jwt = jwtWithRoles(List.of("ADMIN", "TECNICO"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_TECNICO");
    }

    @Test
    void uppercasesRoleValuesWhenBuildingAuthorities() {
        Jwt jwt = jwtWithRoles(List.of("estudiante"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ESTUDIANTE");
    }

    @Test
    void returnsNoAuthoritiesWhenRolesClaimIsMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .subject("user-1")
                .build();

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void usesJwtSubjectAsPrincipalName() {
        Jwt jwt = jwtWithRoles(List.of("AUDITOR"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getName()).isEqualTo("user-1");
    }

    private Jwt jwtWithRoles(List<String> roles) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(jwtProperties.getRolesClaim(), roles)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .subject("user-1")
                .build();
    }
}
