package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.entity.User;
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import com.etp.ticketservice.domain.repository.support.AbstractPostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Real-Postgres slice test, same rationale as EventRepositoryTest. Only proves
// findByDomainIdWithLock resolves the right row -- it does NOT exercise the
// @Lock(PESSIMISTIC_WRITE) itself, since verifying that a second concurrent transaction
// actually blocks would need a genuinely concurrent test (a second thread/connection
// racing this one), which is a different, much flakier kind of test than the rest of
// this suite. Purchase concurrency (the actual reason this lock exists -- see
// TicketTypeServiceImpl#purchaseTicket) is out of scope here.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketTypeRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Test
    void findByDomainIdWithLock_returnsTicketType_whenFound() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User organizer = persistUser("Jane Organizer");
        Event event = persistEvent("Test Event", venue, organizer, EventStatusEnum.PUBLISHED, LocalDateTime.now().plusDays(1));
        TicketType ticketType = persistTicketType(event, "General", 25.0);

        Optional<TicketType> found = ticketTypeRepository.findByDomainIdWithLock(ticketType.getDomainId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("General");
    }

    @Test
    void findByDomainIdWithLock_returnsEmpty_whenNotFound() {
        assertThat(ticketTypeRepository.findByDomainIdWithLock(UUID.randomUUID())).isEmpty();
    }
}
