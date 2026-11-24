package com.etp.ticketservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// Authenticates the Prometheus scraper by a static shared bearer token (app.metrics.token,
// from METRICS_TOKEN). Only runs in SecurityConfig's metrics chain, never the JWT one --
// the token isn't a JWT, and the resource-server filter would reject it as one.
//
// Not a @Component on purpose: Boot would also register it as a servlet filter for every
// request. A missing or wrong token leaves the request unauthenticated and the chain
// answers 401. An empty configured token fails closed: nothing is ever accepted.
public class MetricsTokenFilter extends OncePerRequestFilter {

    public static final String SCRAPER_AUTHORITY = "METRICS_SCRAPER";

    private static final String BEARER_PREFIX = "Bearer ";

    private final byte[] expectedToken;

    public MetricsTokenFilter(String expectedToken) {
        this.expectedToken =
                expectedToken == null
                        ? new byte[0]
                        : expectedToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (expectedToken.length > 0 && presentsExpectedToken(request)) {
            var authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            "prometheus",
                            null,
                            List.of(new SimpleGrantedAuthority(SCRAPER_AUTHORITY)));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private boolean presentsExpectedToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return false;
        }
        byte[] presented =
                header.substring(BEARER_PREFIX.length()).getBytes(StandardCharsets.UTF_8);
        // Constant-time, so response timing doesn't reveal how much of a guess was right.
        return MessageDigest.isEqual(expectedToken, presented);
    }
}
