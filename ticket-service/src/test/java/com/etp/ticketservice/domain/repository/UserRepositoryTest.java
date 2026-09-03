package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.User;
import com.etp.ticketservice.domain.repository.support.AbstractPostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Real-Postgres slice test, same rationale as EventRepositoryTest. Nothing native here,
// but UserRepository backs UserProvisioningFilter -- a bean that runs on every single
// request the app serves (see EventControllerTest's own comment on it) -- so its two
// queries are worth pinning down against the real schema rather than trusting them by
// inspection alone.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByDomainId_returnsTrue_whenUserAlreadyProvisioned() {
        User user = persistUser("Jane Attendee");

        assertThat(userRepository.existsByDomainId(user.getDomainId())).isTrue();
    }

    @Test
    void existsByDomainId_returnsFalse_whenNeverProvisioned() {
        assertThat(userRepository.existsByDomainId(UUID.randomUUID())).isFalse();
    }

    @Test
    void findByDomainId_returnsUser_whenFound() {
        User user = persistUser("Jane Attendee");

        Optional<User> found = userRepository.findByDomainId(user.getDomainId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Jane Attendee");
    }

    @Test
    void findByDomainId_returnsEmpty_whenNotFound() {
        assertThat(userRepository.findByDomainId(UUID.randomUUID())).isEmpty();
    }
}
