package com.etp.ticketservice.tickets;

import com.etp.ticketservice.messaging.TicketEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

// Separate Spring bean from TicketServiceImpl, deliberately -- calling a @Transactional
// method via `this.` from another method on the SAME bean bypasses the AOP proxy and
// silently never starts a transaction (same precedent as TicketOrderServiceImpl).
// cancelAndPersist/recordRefundOutcome are two independent, short transactions with the
// Stripe refund call happening in between them, outside both -- TicketServiceImpl
// orchestrates this without its own @Transactional, same shape as
// CheckoutServiceImpl/StripeWebhookServiceImpl orchestrating around their own external
// calls.
@Service
@RequiredArgsConstructor
public class TicketCancellationServiceImpl implements TicketCancellationService {

    private final TicketRepository ticketRepository;
    private final TicketEventPublisher ticketEventPublisher;

    @Override
    @Transactional
    public TicketCancellationOutcome cancelAndPersist(UUID ticketDomainId, TicketCancelReasonEnum reason, String note) {
        Ticket ticket = ticketRepository.findByDomainIdForCancellation(ticketDomainId)
                .orElseThrow(() -> new IllegalStateException("Ticket " + ticketDomainId + " vanished mid-cancellation"));

        ticket.setStatus(TicketStatusEnum.CANCELLED);
        ticket.setCancelledAt(LocalDateTime.now());
        ticket.setCancelReason(reason);
        ticket.setCancelNote(note);

        Ticket savedTicket = ticketRepository.save(ticket);
        ticketEventPublisher.publishTicketCancelled(savedTicket);

        Long refundAmountMinorUnits = null;
        String providerCheckoutSessionId = null;
        if (null != savedTicket.getOrderItem()) {
            refundAmountMinorUnits = savedTicket.getOrderItem().getUnitPriceAtCheckoutMinorUnits();
            providerCheckoutSessionId = savedTicket.getOrderItem().getTicketOrder().getProviderCheckoutSessionId();
        }

        return TicketCancellationOutcome.builder()
                .ticket(savedTicket)
                .refundAmountMinorUnits(refundAmountMinorUnits)
                .providerCheckoutSessionId(providerCheckoutSessionId)
                .build();
    }

    @Override
    @Transactional
    public void recordRefundOutcome(UUID ticketDomainId, RefundStatusEnum refundStatus, String providerRefundId) {
        Ticket ticket = ticketRepository.findByDomainId(ticketDomainId)
                .orElseThrow(() -> new IllegalStateException(
                        "Ticket " + ticketDomainId + " vanished between cancellation and refund-outcome recording"));
        ticket.setRefundStatus(refundStatus);
        ticket.setProviderRefundId(providerRefundId);
        ticketRepository.save(ticket);
    }
}
