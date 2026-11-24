package com.etp.ticketservice.config;

import com.etp.ticketservice.security.JwtAuthenticationConverter;
import com.etp.ticketservice.security.Role;
import com.etp.ticketservice.security.UserProvisioningFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            UserProvisioningFilter userProvisioningFilter,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            @Value("${app.cors.allowed-origins}") List<String> allowedOrigins)
            throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource(allowedOrigins)))
                .authorizeHttpRequests(authorize ->
                        authorize
                                // Local-dev-only scope throughout this project -- the generated
                                // spec
                                // and its UI are left open rather than gated behind a role, the
                                // same reasoning that already applies to published-events reads.
                                .requestMatchers(
                                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                .permitAll()
                                // Load balancers / container probes call these without a JWT.
                                // Only health is opened -- every other actuator path still
                                // falls through to anyRequest().authenticated() -- and its
                                // body is just {"status": ...}: show-details stays "never".
                                .requestMatchers(
                                        HttpMethod.GET, "/actuator/health", "/actuator/health/**")
                                .permitAll()
                                // Prometheus scrapes without a JWT too. Metrics carry no
                                // request bodies or user data, only counters and gauges.
                                .requestMatchers(HttpMethod.GET, "/actuator/prometheus")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/published-events/**")
                                .permitAll()
                                // The search endpoint is POST (see PublishedEventController), so
                                // the
                                // GET rule above doesn't cover it -- HTTP method is part of what a
                                // requestMatcher matches on, not just the path.
                                .requestMatchers(HttpMethod.POST, "/api/v1/published-events/search")
                                .permitAll()
                                // No JWT at all here, by design -- trusted only by Stripe's
                                // cryptographic signature (verified in
                                // StripeWebhookServiceImpl), a different kind of "public" than
                                // the permitAll rules above.
                                .requestMatchers(
                                        HttpMethod.POST, "/api/v1/payments/webhooks/stripe")
                                .permitAll()
                                // Ticket purchase and checkout both live under /api/v1/events/**
                                // but
                                // are attendee actions, not organizer management -- must be matched
                                // before the broader ORGANIZER-only rule below.
                                .requestMatchers(
                                        HttpMethod.POST, "/api/v1/events/*/ticket-types/*/tickets")
                                .authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/v1/events/*/checkout")
                                .authenticated()
                                .requestMatchers("/api/v1/events/**")
                                .hasRole(Role.ORGANIZER.name())
                                .requestMatchers("/api/v1/venues/**")
                                .hasRole(Role.ORGANIZER.name())
                                .requestMatchers("/api/v1/ticket-validations")
                                .hasRole(Role.STAFF.name())
                                .anyRequest()
                                .authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(
                                jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .addFilterAfter(userProvisioningFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    // The frontend is the first browser client this API has ever had --
    // curl/Postman in every "Ui Testing" lesson so far aren't subject to CORS at all, so
    // nothing exposed the gap until now. Spring Security auto-permits the OPTIONS preflight
    // for any path this configuration source covers, ahead of the authorizeHttpRequests
    // rules above, so the preflight itself doesn't need a matcher/rule of its own.
    //
    // Allowed origins come from app.cors.allowed-origins, filtered per environment (see
    // environments/): localhost:3000 for local dev, the deployed frontend's URL elsewhere.
    private CorsConfigurationSource corsConfigurationSource(List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept-Language"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
