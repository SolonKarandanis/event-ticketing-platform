package com.etp.ticketservice.controller.support;

import com.etp.ticketservice.security.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

// Authenticates a MockMvc request with a fake JWT whose subject is the given user id --
// bypasses the real JwtDecoder entirely, no network call, no running Keycloak needed.
// Role-based authorization (hasRole(ORGANIZER), etc.) is a SecurityConfig concern, not
// exercised by most controller slices (they don't import the real SecurityConfig -- see
// each test class's own comment), so withSubject alone only ever asserts who the caller
// is, never what they're allowed to do. withSubjectAndRole is for the slice that DOES
// import SecurityConfig (SecurityAuthorizationTest) and needs to assert on roles too.
public final class TestJwts {

    private TestJwts() {
    }

    public static RequestPostProcessor withSubject(UUID userId) {
        return jwt().jwt(builder -> builder.subject(userId.toString()));
    }

    // .authorities(...) sets the resulting Authentication's granted authorities directly,
    // bypassing the app's own JwtAuthenticationConverter entirely (jwt() never invokes
    // it) -- deliberately: this asserts what SecurityConfig's authorizeHttpRequests rules
    // do once a given set of authorities exists, independent of whether the converter
    // correctly derives that set from a real token's claims. See
    // JwtAuthenticationConverterTest for that other half, tested on its own.
    public static RequestPostProcessor withSubjectAndRole(UUID userId, Role role) {
        return jwt()
                .jwt(builder -> builder.subject(userId.toString()))
                .authorities(new SimpleGrantedAuthority(role.getAuthority()));
    }
}
