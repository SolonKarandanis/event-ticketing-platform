package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.entity.QrCode;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.entity.User;
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import com.etp.ticketservice.domain.enums.QrCodeStatusEnum;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
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
class QrCodeRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Test
    void findByTicketDomainIdAndTicketPurchaserDomainId_returnsQrCode_whenOwnedByThatPurchaser() {
        Ticket ticket = persistTicketOwnedBy(persistUser("Jane Attendee"));
        QrCode qrCode = persistQrCode(ticket, QrCodeStatusEnum.ACTIVE);

        Optional<QrCode> found = qrCodeRepository.findByTicketDomainIdAndTicketPurchaserDomainId(
                ticket.getDomainId(), ticket.getPurchaser().getDomainId());

        assertThat(found).contains(qrCode);
    }

    @Test
    void findByTicketDomainIdAndTicketPurchaserDomainId_returnsEmpty_whenOwnedBySomeoneElse() {
        Ticket ticket = persistTicketOwnedBy(persistUser("Jane Attendee"));
        persistQrCode(ticket, QrCodeStatusEnum.ACTIVE);
        User someoneElse = persistUser("Someone Else");

        Optional<QrCode> found = qrCodeRepository.findByTicketDomainIdAndTicketPurchaserDomainId(
                ticket.getDomainId(), someoneElse.getDomainId());

        assertThat(found).isEmpty();
    }

    @Test
    void findByDomainIdAndStatus_returnsEmpty_whenStatusDoesNotMatch() {
        Ticket ticket = persistTicketOwnedBy(persistUser("Jane Attendee"));
        QrCode expiredQrCode = persistQrCode(ticket, QrCodeStatusEnum.EXPIRED);

        Optional<QrCode> asExpired = qrCodeRepository.findByDomainIdAndStatus(
                expiredQrCode.getDomainId(), QrCodeStatusEnum.EXPIRED);
        Optional<QrCode> asActive = qrCodeRepository.findByDomainIdAndStatus(
                expiredQrCode.getDomainId(), QrCodeStatusEnum.ACTIVE);

        assertThat(asExpired).contains(expiredQrCode);
        assertThat(asActive).isEmpty();
    }

    private Ticket persistTicketOwnedBy(User purchaser) {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User organizer = persistUser("Jane Organizer");
        Event event = persistEvent("Test Event", venue, organizer, EventStatusEnum.PUBLISHED, LocalDateTime.now().plusDays(1));
        TicketType ticketType = persistTicketType(event, "General", 25.0);
        return persistTicket(ticketType, purchaser, TicketStatusEnum.PURCHASED);
    }
}
