package com.etp.ticketservice.config;

import com.etp.ticketservice.security.JwtAuthenticationConverter;
import com.etp.ticketservice.security.Role;
import com.etp.ticketservice.security.UserProvisioningFilter;
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

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            UserProvisioningFilter userProvisioningFilter,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(HttpMethod.GET, "/api/v1/published-events/**").permitAll()
                                // Ticket purchase lives under /api/v1/events/** but is an attendee
                                // action, not organizer management -- must be matched before the
                                // broader ORGANIZER-only rule below.
                                .requestMatchers(HttpMethod.POST, "/api/v1/events/*/ticket-types/*/tickets").authenticated()
                                .requestMatchers("/api/v1/events/**").hasRole(Role.ORGANIZER.name())
                                .requestMatchers("/api/v1/venues/**").hasRole(Role.ORGANIZER.name())
                                .requestMatchers("/api/v1/ticket-validations").hasRole(Role.STAFF.name())
                                .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                        ))
                .addFilterAfter(userProvisioningFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    // The frontend (localhost:3000) is the first browser client this API has ever had --
    // curl/Postman in every "Ui Testing" lesson so far aren't subject to CORS at all, so
    // nothing exposed the gap until now. Spring Security auto-permits the OPTIONS preflight
    // for any path this configuration source covers, ahead of the authorizeHttpRequests
    // rules above, so the preflight itself doesn't need a matcher/rule of its own.
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept-Language"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
