package com.etp.ticketservice.controller.support;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

// Authenticates a MockMvc request with a fake JWT whose subject is the given user id --
// bypasses the real JwtDecoder entirely, no network call, no running Keycloak needed.
// Role-based authorization (hasRole(ORGANIZER), etc.) is a SecurityConfig concern, not
// exercised by these controller slices (none of them import the real SecurityConfig --
// see each test class's own comment), so this only ever asserts who the caller is,
// never what they're allowed to do.
public final class TestJwts {

    private TestJwts() {
    }

    public static RequestPostProcessor withSubject(UUID userId) {
        return jwt().jwt(builder -> builder.subject(userId.toString()));
    }
}
