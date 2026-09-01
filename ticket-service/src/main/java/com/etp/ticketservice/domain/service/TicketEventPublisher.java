package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.event.TicketCancelledEvent;
import com.etp.ticketservice.domain.event.TicketPurchasedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishTicketPurchased(Ticket ticket) {
        applicationEventPublisher.publishEvent(new TicketPurchasedEvent(
                ticket.getDomainId(),
                ticket.getTicketType().getDomainId(),
                ticket.getTicketType().getEvent().getDomainId(),
                ticket.getTicketType().getEvent().getOrganizer().getDomainId(),
                ticket.getPurchaser().getDomainId(),
                ticket.getTicketType().getPrice(),
                ticket.getCreatedAt()
        ));
    }

    // Same "extract everything into flat values while the entity is still attached"
    // reasoning as publishTicketPurchased -- the AFTER_COMMIT listener that eventually
    // reads this runs after this transaction's Hibernate session has already closed, so
    // nothing here can be a live entity reference.
    public void publishTicketCancelled(Ticket ticket) {
        applicationEventPublisher.publishEvent(new TicketCancelledEvent(
                ticket.getDomainId(),
                ticket.getTicketType().getDomainId(),
                ticket.getTicketType().getEvent().getDomainId(),
                ticket.getTicketType().getEvent().getOrganizer().getDomainId(),
                ticket.getPurchaser().getDomainId(),
                ticket.getCancelledAt(),
                ticket.getCancelReason()
        ));
    }
}
