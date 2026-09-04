package com.etp.ticketservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// Pure unit test, no Spring context needed -- this class has no dependencies of its own.
// Complements SecurityAuthorizationTest, which asserts what SecurityConfig's
// authorizeHttpRequests rules do given a set of authorities, but sets those authorities
// directly rather than deriving them from a real token (see that class's own comment).
// This is the other half: does a real Jwt's realm_access.roles claim actually get turned
// into the right GrantedAuthority set in the first place.
class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    @Test
    void convert_noRealmAccessClaim_returnsNoAuthorities() {
        Jwt jwt = jwtWithClaims(Map.of());

        JwtAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void convert_realmAccessWithoutRolesKey_returnsNoAuthorities() {
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of()));

        JwtAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void convert_dropsRolesNotPrefixedWithRole() {
        // "offline_access" and "uma_authorization" are real Keycloak default realm
        // roles that show up here alongside the app's own -- neither should leak
        // through as a Spring Security authority.
        Jwt jwt = jwtWithClaims(Map.of("realm_access",
                Map.of("roles", List.of("offline_access", "uma_authorization", "ROLE_ORGANIZER"))));

        JwtAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ORGANIZER");
    }

    @Test
    void convert_mapsEveryRolePrefixedRoleToAGrantedAuthority() {
        Jwt jwt = jwtWithClaims(Map.of("realm_access",
                Map.of("roles", List.of("ROLE_ORGANIZER", "ROLE_STAFF"))));

        JwtAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_ORGANIZER", "ROLE_STAFF");
    }

    @Test
    void convert_preservesTheJwtItselfAsThePrincipal() {
        Jwt jwt = jwtWithClaims(Map.of());

        JwtAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getToken()).isEqualTo(jwt);
    }

    private Jwt jwtWithClaims(Map<String, Object> extraClaims) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(java.util.UUID.randomUUID().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
        extraClaims.forEach(builder::claim);
        return builder.build();
    }
}
