package org.campuslab.bff.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AudienceValidatorTest {

    private final AudienceValidator validator = new AudienceValidator(List.of("api://campuslab-bff"));

    @Test
    void succeedsWhenTokenContainsExpectedAudience() {
        Jwt jwt = jwtWithAudience(List.of("api://campuslab-bff"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void succeedsWhenTokenContainsExpectedAudienceAmongSeveral() {
        Jwt jwt = jwtWithAudience(List.of("api://otro-cliente", "api://campuslab-bff"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void failsWhenTokenDoesNotContainExpectedAudience() {
        Jwt jwt = jwtWithAudience(List.of("api://otro-cliente"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
    }

    private Jwt jwtWithAudience(List<String> audience) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .audience(audience)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .subject("user-1")
                .build();
    }
}
