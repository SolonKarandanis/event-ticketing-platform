package com.etp.ticketservice.controller.support;

import com.etp.ticketservice.domain.repository.UserRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

// Every @WebMvcTest controller slice in this app needs this: @WebMvcTest auto-registers
// any Filter bean it finds (UserProvisioningFilter, a plain @Component
// OncePerRequestFilter) regardless of whether the app's own SecurityConfig is imported,
// and that filter's constructor needs a UserRepository. @Import this instead of
// redeclaring a @MockitoBean UserRepository in every test class -- a bare, unstubbed
// mock is enough: it returns false from existsByDomainId(...), so the filter just calls
// save(...) once per request and moves on. No real behavior to assert on here.
@TestConfiguration
public class UserProvisioningTestConfig {

    @Bean
    UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }
}
