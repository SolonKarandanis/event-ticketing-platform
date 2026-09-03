package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.entity.EventImage;
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

import static org.assertj.core.api.Assertions.assertThat;

// Real-Postgres slice test, same rationale as EventRepositoryTest.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventImageRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private EventImageRepository eventImageRepository;

    @Test
    void findByDomainIdAndEventDomainIdAndEventOrganizerDomainId_returnsImage_whenOwnedByThatOrganizer() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User owner = persistUser("Jane Organizer");
        Event event = persistEvent("Test Event", venue, owner, EventStatusEnum.DRAFT, LocalDateTime.now().plusDays(1));
        EventImage image = persistEventImage(event, 0, "Cover photo");

        Optional<EventImage> asOwner = eventImageRepository.findByDomainIdAndEventDomainIdAndEventOrganizerDomainId(
                image.getDomainId(), event.getDomainId(), owner.getDomainId());

        assertThat(asOwner).contains(image);
    }

    @Test
    void findByDomainIdAndEventDomainIdAndEventOrganizerDomainId_returnsEmpty_whenOwnedBySomeoneElse() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User owner = persistUser("Jane Organizer");
        User someoneElse = persistUser("Someone Else");
        Event event = persistEvent("Test Event", venue, owner, EventStatusEnum.DRAFT, LocalDateTime.now().plusDays(1));
        EventImage image = persistEventImage(event, 0, "Cover photo");

        Optional<EventImage> asSomeoneElse = eventImageRepository.findByDomainIdAndEventDomainIdAndEventOrganizerDomainId(
                image.getDomainId(), event.getDomainId(), someoneElse.getDomainId());

        assertThat(asSomeoneElse).isEmpty();
    }

    @Test
    void findByDomainIdAndEventDomainIdAndEventStatus_returnsImage_onlyWhenEventStatusMatches() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User organizer = persistUser("Jane Organizer");
        Event publishedEvent = persistEvent("Published Event", venue, organizer, EventStatusEnum.PUBLISHED, LocalDateTime.now().plusDays(1));
        EventImage image = persistEventImage(publishedEvent, 0, "Cover photo");

        Optional<EventImage> asPublished = eventImageRepository.findByDomainIdAndEventDomainIdAndEventStatus(
                image.getDomainId(), publishedEvent.getDomainId(), EventStatusEnum.PUBLISHED);
        Optional<EventImage> asDraft = eventImageRepository.findByDomainIdAndEventDomainIdAndEventStatus(
                image.getDomainId(), publishedEvent.getDomainId(), EventStatusEnum.DRAFT);

        assertThat(asPublished).contains(image);
        assertThat(asDraft).isEmpty();
    }
}
