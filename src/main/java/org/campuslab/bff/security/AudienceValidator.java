package org.campuslab.bff.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Valida que el claim "aud" del JWT contenga al menos uno de los audiences
 * esperados por este servicio (configurados en {@link JwtProperties#getAudiences()}).
 *
 * Spring Security solo valida issuer, firma y expiracion por defecto: la
 * validacion de audience hay que agregarla explicitamente, ya que sin ella
 * un token emitido para OTRA aplicacion cliente registrada en el mismo
 * tenant de Azure AD tambien seria aceptado.
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_AUDIENCE_ERROR = new OAuth2Error(
            "invalid_token",
            "El token no contiene ninguno de los audiences esperados por ms-campuslab-bff",
            null);

    private final List<String> allowedAudiences;

    public AudienceValidator(List<String> allowedAudiences) {
        this.allowedAudiences = allowedAudiences;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience() != null && jwt.getAudience().stream().anyMatch(allowedAudiences::contains)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(INVALID_AUDIENCE_ERROR);
    }
}
