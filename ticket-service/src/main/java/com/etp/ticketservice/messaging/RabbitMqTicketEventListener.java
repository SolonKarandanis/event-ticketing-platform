package com.etp.ticketservice.messaging;

import com.etp.ticketservice.config.RabbitMqConfig;
import com.etp.ticketservice.domain.event.TicketCancelledEvent;
import com.etp.ticketservice.domain.event.TicketPurchasedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RabbitMqTicketEventListener {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketPurchased(TicketPurchasedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EVENTS_EXCHANGE, "ticket.purchased", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketCancelled(TicketCancelledEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EVENTS_EXCHANGE, "ticket.cancelled", event);
    }
}
