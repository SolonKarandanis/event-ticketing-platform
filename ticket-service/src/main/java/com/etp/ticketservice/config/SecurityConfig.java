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

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            UserProvisioningFilter userProvisioningFilter,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
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
}
