package com.etp.ticketservice.tickets;

import com.etp.ticketservice.messaging.TicketEventPublisher;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.events.Event;
import com.etp.ticketservice.orders.TicketOrderItem;
import com.etp.ticketservice.payments.PaymentGatewayService;
import com.etp.ticketservice.payments.RefundResult;
import com.etp.ticketservice.payments.exception.PaymentGatewayException;
import com.etp.ticketservice.tickets.qrcode.QrCodeService;
import com.etp.ticketservice.tickettypes.TicketType;
import com.etp.ticketservice.ticketvalidation.TicketValidation;
import com.etp.ticketservice.events.EventStatusEnum;
import com.etp.ticketservice.ticketvalidation.TicketValidationStatusEnum;
import com.etp.ticketservice.tickets.exception.ReferenceCodeGenerationException;
import com.etp.ticketservice.tickets.exception.TicketAlreadyCancelledException;
import com.etp.ticketservice.tickets.exception.TicketAlreadyValidatedException;
import com.etp.ticketservice.tickets.exception.TicketEventAlreadyCompletedException;
import com.etp.ticketservice.tickets.exception.TicketNotFoundException;
import com.etp.ticketservice.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pure Mockito unit tests -- see EventServiceImplTest's class comment for why this layer
// is worth testing directly rather than only through TicketController's mocked-service
// slice. guardCancellable is shared by the two single-ticket entry points; its three
// branches are exercised once via cancelTicketForUser, and cancelTicketForOrganizer's
// own tests mainly confirm it applies the guard too and delegates with the right reason.
// The actual cancel-and-persist/refund-recording work now lives on
// TicketCancellationService (mocked out here, see TicketCancellationServiceImplTest for
// that layer) -- this class's job is the orchestration around it: deciding whether a
// refund is attempted at all, translating Stripe's result, and never letting a refund
// failure block the cancellation (issue #22).
@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketEventPublisher ticketEventPublisher;
    @Mock
    private QrCodeService qrCodeService;
    @Mock
    private TicketCancellationService ticketCancellationService;
    @Mock
    private PaymentGatewayService paymentGatewayService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORGANIZER_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID TICKET_ID = UUID.randomUUID();

    @Test
    void issueTicket_happyPath_withOrderItem_generatesQrAndPublishesAndLinksOrderItem() {
        User purchaser = new User();
        TicketType ticketType = new TicketType();
        TicketOrderItem orderItem = new TicketOrderItem();
        when(ticketRepository.findByReferenceCode(any())).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket issued = ticketService.issueTicket(purchaser, ticketType, orderItem);

        assertThat(issued.getStatus()).isEqualTo(TicketStatusEnum.PURCHASED);
        assertThat(issued.getPurchaser()).isEqualTo(purchaser);
        assertThat(issued.getOrderItem()).isEqualTo(orderItem);
        assertThat(issued.getTicketType()).isEqualTo(ticketType);
        verify(qrCodeService).generateQrCode(issued);
        verify(ticketEventPublisher).publishTicketPurchased(issued);
    }

    @Test
    void issueTicket_happyPath_withoutOrderItem_leavesOrderItemNull() {
        User purchaser = new User();
        TicketType ticketType = new TicketType();
        when(ticketRepository.findByReferenceCode(any())).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket issued = ticketService.issueTicket(purchaser, ticketType, null);

        assertThat(issued.getOrderItem()).isNull();
    }

    @Test
    void issueTicket_referenceCodeCollisions_retriesThenGivesUp() {
        User purchaser = new User();
        TicketType ticketType = new TicketType();
        // Every candidate "collides" -- forces every one of the 5 generation attempts to
        // be exhausted rather than succeeding on the first try.
        when(ticketRepository.findByReferenceCode(any())).thenReturn(Optional.of(new Ticket()));

        assertThatThrownBy(() -> ticketService.issueTicket(purchaser, ticketType, null))
                .isInstanceOf(ReferenceCodeGenerationException.class);

        verify(ticketRepository, never()).save(any());
    }

    // ---- guardCancellable, exercised via cancelTicketForUser ----

    @Test
    void cancelTicketForUser_notFound_throws() {
        when(ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(TICKET_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null))
                .isInstanceOf(TicketNotFoundException.class);

        verify(ticketCancellationService, never()).cancelAndPersist(any(), any(), any());
    }

    @Test
    void cancelTicketForUser_alreadyCancelled_throws() {
        Ticket ticket = purchasableTicket();
        ticket.setStatus(TicketStatusEnum.CANCELLED);
        when(ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null))
                .isInstanceOf(TicketAlreadyCancelledException.class);

        verify(ticketCancellationService, never()).cancelAndPersist(any(), any(), any());
    }

    @Test
    void cancelTicketForUser_eventAlreadyCompleted_throws() {
        Ticket ticket = purchasableTicket();
        ticket.getTicketType().getEvent().setStatus(EventStatusEnum.COMPLETED);
        when(ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null))
                .isInstanceOf(TicketEventAlreadyCompletedException.class);
    }

    @Test
    void cancelTicketForUser_alreadyValidated_throws() {
        Ticket ticket = purchasableTicket();
        TicketValidation validation = new TicketValidation();
        validation.setStatus(TicketValidationStatusEnum.VALID);
        ticket.addValidation(validation);
        when(ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null))
                .isInstanceOf(TicketAlreadyValidatedException.class);
    }

    @Test
    void cancelTicketForUser_invalidatedValidationDoesNotBlockCancellation() {
        Ticket ticket = purchasableTicket();
        TicketValidation validation = new TicketValidation();
        validation.setStatus(TicketValidationStatusEnum.INVALID);
        ticket.addValidation(validation);
        when(ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));
        when(ticketCancellationService.cancelAndPersist(eq(TICKET_ID), eq(TicketCancelReasonEnum.ATTENDEE_REQUEST), any()))
                .thenReturn(noRefundOutcome(cancelledTicket()));

        Ticket cancelled = ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null);

        assertThat(cancelled.getStatus()).isEqualTo(TicketStatusEnum.CANCELLED);
    }

    // ---- orchestration: deciding whether/how to attempt a refund ----

    @Test
    void cancelTicketForUser_noOrderItem_skipsRefundEntirely() {
        Ticket ticket = purchasableTicket();
        when(ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));
        when(ticketCancellationService.cancelAndPersist(TICKET_ID, TicketCancelReasonEnum.ATTENDEE_REQUEST, "Changed my mind"))
                .thenReturn(noRefundOutcome(cancelledTicket()));

        Ticket cancelled = ticketService.cancelTicketForUser(USER_ID, TICKET_ID, "Changed my mind");

        assertThat(cancelled.getStatus()).isEqualTo(TicketStatusEnum.CANCELLED);
        assertThat(cancelled.getRefundStatus()).isNull();
        verify(paymentGatewayService, never()).refund(any());
        verify(ticketCancellationService, never()).recordRefundOutcome(any(), any(), any());
    }

    @Test
    void cancelTicketForUser_refundSucceeds_recordsSucceededStatus() {
        Ticket ticket = purchasableTicket();
        when(ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));
        when(ticketCancellationService.cancelAndPersist(TICKET_ID, TicketCancelReasonEnum.ATTENDEE_REQUEST, null))
                .thenReturn(refundableOutcome(cancelledTicket(), 1999L, "cs_test_123"));
        when(paymentGatewayService.refund(any())).thenReturn(
                RefundResult.builder().providerRefundId("re_test_123").status("succeeded").build());

        Ticket cancelled = ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null);

        assertThat(cancelled.getRefundStatus()).isEqualTo(RefundStatusEnum.SUCCEEDED);
        assertThat(cancelled.getProviderRefundId()).isEqualTo("re_test_123");
        verify(ticketCancellationService).recordRefundOutcome(TICKET_ID, RefundStatusEnum.SUCCEEDED, "re_test_123");
    }

    @Test
    void cancelTicketForUser_refundPending_recordsPendingStatus() {
        Ticket ticket = purchasableTicket();
        when(ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));
        when(ticketCancellationService.cancelAndPersist(TICKET_ID, TicketCancelReasonEnum.ATTENDEE_REQUEST, null))
                .thenReturn(refundableOutcome(cancelledTicket(), 1999L, "cs_test_123"));
        when(paymentGatewayService.refund(any())).thenReturn(
                RefundResult.builder().providerRefundId("re_test_123").status("pending").build());

        ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null);

        verify(ticketCancellationService).recordRefundOutcome(TICKET_ID, RefundStatusEnum.PENDING, "re_test_123");
    }

    @Test
    void cancelTicketForUser_refundStatusUnrecognized_mapsToFailed() {
        Ticket ticket = purchasableTicket();
        when(ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));
        when(ticketCancellationService.cancelAndPersist(TICKET_ID, TicketCancelReasonEnum.ATTENDEE_REQUEST, null))
                .thenReturn(refundableOutcome(cancelledTicket(), 1999L, "cs_test_123"));
        when(paymentGatewayService.refund(any())).thenReturn(
                RefundResult.builder().providerRefundId("re_test_123").status("canceled").build());

        ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null);

        verify(ticketCancellationService).recordRefundOutcome(TICKET_ID, RefundStatusEnum.FAILED, "re_test_123");
    }

    @Test
    void cancelTicketForUser_gatewayThrows_recordsFailedStatusWithoutBlockingCancellation() {
        Ticket ticket = purchasableTicket();
        when(ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));
        when(ticketCancellationService.cancelAndPersist(TICKET_ID, TicketCancelReasonEnum.ATTENDEE_REQUEST, null))
                .thenReturn(refundableOutcome(cancelledTicket(), 1999L, "cs_test_123"));
        when(paymentGatewayService.refund(any())).thenThrow(new PaymentGatewayException(ErrorCode.PAYMENT_GATEWAY_ERROR));

        // Cancellation already took effect (cancelAndPersist already "ran") regardless
        // of the gateway failure -- no exception propagates out of this call.
        Ticket cancelled = ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null);

        assertThat(cancelled.getRefundStatus()).isEqualTo(RefundStatusEnum.FAILED);
        assertThat(cancelled.getProviderRefundId()).isNull();
        verify(ticketCancellationService).recordRefundOutcome(TICKET_ID, RefundStatusEnum.FAILED, null);
    }

    @Test
    void cancelTicketForUser_missingProviderCheckoutSessionIdDespiteAmount_recordsFailedWithoutCallingGateway() {
        Ticket ticket = purchasableTicket();
        when(ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));
        // Amount present but no session id -- the "should never happen" case (see
        // TicketOrder.providerCheckoutSessionId's own comment).
        when(ticketCancellationService.cancelAndPersist(TICKET_ID, TicketCancelReasonEnum.ATTENDEE_REQUEST, null))
                .thenReturn(TicketCancellationOutcome.builder()
                        .ticket(cancelledTicket())
                        .refundAmountMinorUnits(1999L)
                        .providerCheckoutSessionId(null)
                        .build());

        Ticket cancelled = ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null);

        assertThat(cancelled.getRefundStatus()).isEqualTo(RefundStatusEnum.FAILED);
        verify(paymentGatewayService, never()).refund(any());
        verify(ticketCancellationService).recordRefundOutcome(TICKET_ID, RefundStatusEnum.FAILED, null);
    }

    // ---- cancelTicketForOrganizer / cancelTicketForEventCancellation: delegation only ----

    @Test
    void cancelTicketForOrganizer_notFound_throws() {
        when(ticketRepository.findByDomainIdAndEventDomainIdAndOrganizerDomainId(TICKET_ID, EVENT_ID, ORGANIZER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.cancelTicketForOrganizer(ORGANIZER_ID, EVENT_ID, TICKET_ID, null))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void cancelTicketForOrganizer_happyPath_delegatesWithOrganizerActionReason() {
        Ticket ticket = purchasableTicket();
        when(ticketRepository.findByDomainIdAndEventDomainIdAndOrganizerDomainId(TICKET_ID, EVENT_ID, ORGANIZER_ID))
                .thenReturn(Optional.of(ticket));
        when(ticketCancellationService.cancelAndPersist(TICKET_ID, TicketCancelReasonEnum.ORGANIZER_ACTION, "Event rescheduled"))
                .thenReturn(noRefundOutcome(cancelledTicket()));

        ticketService.cancelTicketForOrganizer(ORGANIZER_ID, EVENT_ID, TICKET_ID, "Event rescheduled");

        verify(ticketCancellationService).cancelAndPersist(TICKET_ID, TicketCancelReasonEnum.ORGANIZER_ACTION, "Event rescheduled");
    }

    @Test
    void cancelTicketForEventCancellation_delegatesWithEventCancelledReasonAndNoNote() {
        when(ticketCancellationService.cancelAndPersist(TICKET_ID, TicketCancelReasonEnum.EVENT_CANCELLED, null))
                .thenReturn(noRefundOutcome(cancelledTicket()));

        ticketService.cancelTicketForEventCancellation(TICKET_ID);

        verify(ticketCancellationService).cancelAndPersist(eq(TICKET_ID), eq(TicketCancelReasonEnum.EVENT_CANCELLED), isNull());
    }

    private Ticket purchasableTicket() {
        Event event = new Event();
        event.setStatus(EventStatusEnum.PUBLISHED);
        TicketType ticketType = new TicketType();
        ticketType.setEvent(event);

        Ticket ticket = new Ticket();
        ticket.setDomainId(TICKET_ID);
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setTicketType(ticketType);
        return ticket;
    }

    private Ticket cancelledTicket() {
        Ticket ticket = new Ticket();
        ticket.setDomainId(TICKET_ID);
        ticket.setStatus(TicketStatusEnum.CANCELLED);
        return ticket;
    }

    private TicketCancellationOutcome noRefundOutcome(Ticket ticket) {
        return TicketCancellationOutcome.builder().ticket(ticket).build();
    }

    private TicketCancellationOutcome refundableOutcome(Ticket ticket, long amountMinorUnits, String providerCheckoutSessionId) {
        return TicketCancellationOutcome.builder()
                .ticket(ticket)
                .refundAmountMinorUnits(amountMinorUnits)
                .providerCheckoutSessionId(providerCheckoutSessionId)
                .build();
    }
}
