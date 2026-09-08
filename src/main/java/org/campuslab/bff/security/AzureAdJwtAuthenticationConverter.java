package org.campuslab.bff.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convierte un {@link Jwt} ya validado (issuer, firma, expiracion, audience)
 * en un {@link AbstractAuthenticationToken}, mapeando el claim de roles de
 * Azure AD (por defecto "roles") a authorities de Spring Security con el
 * prefijo "ROLE_" (ej: "ADMIN" -> "ROLE_ADMIN").
 *
 * Ver {@link Roles} para los valores de rol esperados.
 */
@Component
public class AzureAdJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLE_PREFIX = "ROLE_";

    private final String rolesClaim;

    public AzureAdJwtAuthenticationConverter(JwtProperties jwtProperties) {
        this.rolesClaim = jwtProperties.getRolesClaim();
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, extractAuthorities(jwt), jwt.getSubject());
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(rolesClaim);
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> ROLE_PREFIX + role.toUpperCase(Locale.ROOT))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }
}
