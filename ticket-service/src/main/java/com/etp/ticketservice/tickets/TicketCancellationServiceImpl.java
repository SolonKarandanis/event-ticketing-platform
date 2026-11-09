package com.etp.ticketservice.tickets;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.messaging.TicketEventPublisher;
import com.etp.ticketservice.payments.RefundRequest;
import com.etp.ticketservice.tickets.exception.TicketAlreadyCancelledException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public TicketCancellationOutcome cancelAndPersist(
            UUID ticketDomainId, TicketCancelReasonEnum reason, String note) {
        Ticket locked =
                ticketRepository
                        .findByDomainIdWithLock(ticketDomainId)
                        .orElseThrow(() -> vanished(ticketDomainId));

        if (TicketStatusEnum.CANCELLED == locked.getStatus()) {
            if (TicketCancelReasonEnum.EVENT_CANCELLED == reason) {
                // Throwing here would roll back the whole event-cancellation cascade.
                return TicketCancellationOutcome.builder().ticket(locked).build();
            }
            throw new TicketAlreadyCancelledException(
                    ErrorCode.TICKET_ALREADY_CANCELLED, ticketDomainId);
        }

        Ticket ticket =
                ticketRepository
                        .findByDomainIdForCancellation(ticketDomainId)
                        .orElseThrow(() -> vanished(ticketDomainId));

        ticket.setStatus(TicketStatusEnum.CANCELLED);
        ticket.setCancelledAt(LocalDateTime.now());
        ticket.setCancelReason(reason);
        ticket.setCancelNote(note);
        if (null != ticket.getOrderItem()) {
            ticket.setRefundStatus(RefundStatusEnum.PENDING);
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        ticketEventPublisher.publishTicketCancelled(savedTicket);

        return toOutcome(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketCancellationOutcome loadRefundContext(UUID ticketDomainId) {
        return toOutcome(
                ticketRepository
                        .findByDomainIdForCancellation(ticketDomainId)
                        .orElseThrow(() -> vanished(ticketDomainId)));
    }

    @Override
    @Transactional
    public void recordRefundOutcome(
            UUID ticketDomainId, RefundStatusEnum refundStatus, String providerRefundId) {
        Ticket ticket =
                ticketRepository
                        .findByDomainId(ticketDomainId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Ticket "
                                                + ticketDomainId
                                                + " vanished between cancellation and refund-outcome recording"));
        ticket.setRefundStatus(refundStatus);
        ticket.setProviderRefundId(providerRefundId);
        ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public RefundStatusEnum recordRefundAttemptFailed(UUID ticketDomainId) {
        Ticket ticket =
                ticketRepository
                        .findByDomainId(ticketDomainId)
                        .orElseThrow(() -> vanished(ticketDomainId));
        ticket.setRefundAttempts(ticket.getRefundAttempts() + 1);
        if (ticket.getRefundAttempts() >= RefundRequest.MAX_ATTEMPTS) {
            ticket.setRefundStatus(RefundStatusEnum.FAILED);
        }
        ticketRepository.save(ticket);
        return ticket.getRefundStatus();
    }

    @Override
    @Transactional
    public void applyProviderRefundUpdate(String providerRefundId, RefundStatusEnum refundStatus) {
        ticketRepository
                .findByProviderRefundId(providerRefundId)
                .ifPresent(ticket -> {
                    ticket.setRefundStatus(refundStatus);
                    ticketRepository.save(ticket);
                });
    }

    private TicketCancellationOutcome toOutcome(Ticket ticket) {
        Long refundAmountMinorUnits = null;
        String providerCheckoutSessionId = null;
        if (null != ticket.getOrderItem()) {
            refundAmountMinorUnits = ticket.getOrderItem().getUnitPriceAtCheckoutMinorUnits();
            providerCheckoutSessionId =
                    ticket.getOrderItem().getTicketOrder().getProviderCheckoutSessionId();
        }

        return TicketCancellationOutcome.builder()
                .ticket(ticket)
                .refundAmountMinorUnits(refundAmountMinorUnits)
                .providerCheckoutSessionId(providerCheckoutSessionId)
                .build();
    }

    private IllegalStateException vanished(UUID ticketDomainId) {
        return new IllegalStateException("Ticket " + ticketDomainId + " vanished mid-cancellation");
    }
}
