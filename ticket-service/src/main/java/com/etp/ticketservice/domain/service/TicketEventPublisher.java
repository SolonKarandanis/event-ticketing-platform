package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.entity.Ticket;
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
}
