package com.etp.ticketservice.tickets;

import com.etp.ticketservice.messaging.TicketEventPublisher;

import com.etp.ticketservice.common.util.MoneyUtils;
import com.etp.ticketservice.orders.TicketOrderItem;
import com.etp.ticketservice.payments.PaymentGatewayService;
import com.etp.ticketservice.payments.RefundRequest;
import com.etp.ticketservice.payments.RefundResult;
import com.etp.ticketservice.payments.exception.PaymentGatewayException;
import com.etp.ticketservice.tickets.dto.CancelTicketResponseDto;
import com.etp.ticketservice.tickets.dto.GetTicketResponseDto;
import com.etp.ticketservice.tickets.dto.ListTicketResponseDto;
import com.etp.ticketservice.tickets.dto.ListTicketTicketTypeResponseDto;
import com.etp.ticketservice.tickets.dto.TicketSaleResponseDto;
import com.etp.ticketservice.tickets.dto.TicketSaleTicketTypeResponseDto;
import com.etp.ticketservice.tickets.exception.ReferenceCodeGenerationException;
import com.etp.ticketservice.tickets.qrcode.QrCodeService;
import com.etp.ticketservice.tickettypes.TicketType;
import com.etp.ticketservice.events.EventStatusEnum;
import com.etp.ticketservice.ticketvalidation.TicketValidationStatusEnum;
import com.etp.ticketservice.user.User;
import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.tickets.exception.TicketAlreadyCancelledException;
import com.etp.ticketservice.tickets.exception.TicketAlreadyValidatedException;
import com.etp.ticketservice.tickets.exception.TicketEventAlreadyCompletedException;
import com.etp.ticketservice.tickets.exception.TicketNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    // Excludes visually ambiguous characters (0/O, 1/I/L) -- this code is meant to be read
    // off a phone screen and typed by hand at a door under time pressure.
    private static final String REFERENCE_CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int REFERENCE_CODE_LENGTH = 8;
    private static final int REFERENCE_CODE_MAX_ATTEMPTS = 5;

    private final TicketRepository ticketRepository;
    private final TicketEventPublisher ticketEventPublisher;
    private final QrCodeService qrCodeService;
    private final TicketCancellationService ticketCancellationService;
    private final PaymentGatewayService paymentGatewayService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public Ticket issueTicket(User purchaser, TicketType ticketType, TicketOrderItem orderItem) {
        Ticket ticket = new Ticket();
        ticket.setDomainId(UUID.randomUUID());
        ticket.setReferenceCode(generateReferenceCode());
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setPurchaser(purchaser);
        ticket.setOrderItem(orderItem);
        ticketType.addTicket(ticket);

        // The save below is what assigns the ticket's generated id -- QrCode owns the
        // ticket_id FK (Ticket.qrCodes is the inverse, mappedBy side), so QrCode needs
        // that id to exist before it can be saved. No second ticket save afterward:
        // addQrCode only mutates an in-memory Set on this already-managed entity, so
        // there's no tickets-table column left to flush.
        Ticket savedTicket = ticketRepository.save(ticket);
        qrCodeService.generateQrCode(savedTicket);

        ticketEventPublisher.publishTicketPurchased(savedTicket);

        return savedTicket;
    }

    // Unlike domainId (a UUID, collision-proof enough to generate-and-save with the DB's
    // unique constraint as the only backstop), this code is short enough that a collision,
    // while astronomically unlikely, is worth actually checking for.
    private String generateReferenceCode() {
        for (int attempt = 0; attempt < REFERENCE_CODE_MAX_ATTEMPTS; attempt++) {
            StringBuilder code = new StringBuilder(REFERENCE_CODE_LENGTH);
            for (int i = 0; i < REFERENCE_CODE_LENGTH; i++) {
                code.append(REFERENCE_CODE_ALPHABET.charAt(secureRandom.nextInt(REFERENCE_CODE_ALPHABET.length())));
            }
            String candidate = code.toString();
            if (ticketRepository.findByReferenceCode(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new ReferenceCodeGenerationException(ErrorCode.REFERENCE_CODE_GENERATION_FAILED);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable) {
        return ticketRepository.findByPurchaserDomainId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> getTicketForUser(UUID userId, UUID ticketId) {
        return ticketRepository.findByDomainIdAndPurchaserDomainId(ticketId, userId);
    }

    // Deliberately NOT @Transactional -- cancelAndAttemptRefund orchestrates two
    // independent TicketCancellationService transactions around one external Stripe
    // HTTP call, so that call never runs with a DB lock/connection held
    // (spring.jpa.open-in-view=false means there's no session once this method itself
    // isn't transactional, which is exactly why the lookup below fetch-joins
    // everything guardCancellable needs instead of relying on lazy loading).
    @Override
    public Ticket cancelTicketForUser(UUID userId, UUID ticketId, String note) {
        Ticket ticket = ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(ticketId, userId)
                .orElseThrow(() -> new TicketNotFoundException(ErrorCode.TICKET_NOT_FOUND, ticketId));

        guardCancellable(ticket);

        return cancelAndAttemptRefund(ticketId, TicketCancelReasonEnum.ATTENDEE_REQUEST, note);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> listTicketsForEvent(UUID organizerId, UUID eventId, Pageable pageable) {
        return ticketRepository.findByEventDomainIdAndOrganizerDomainId(eventId, organizerId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> listTicketsForOrganizer(UUID organizerId, Pageable pageable) {
        return ticketRepository.findByOrganizerDomainId(organizerId, pageable);
    }

    // See cancelTicketForUser's comment -- same reasoning, not @Transactional.
    @Override
    public Ticket cancelTicketForOrganizer(UUID organizerId, UUID eventId, UUID ticketId, String note) {
        Ticket ticket = ticketRepository.findByDomainIdAndEventDomainIdAndOrganizerDomainId(ticketId, eventId, organizerId)
                .orElseThrow(() -> new TicketNotFoundException(ErrorCode.TICKET_NOT_FOUND, ticketId));

        guardCancellable(ticket);

        return cancelAndAttemptRefund(ticketId, TicketCancelReasonEnum.ORGANIZER_ACTION, note);
    }

    // Called once per ticket by EventServiceImpl's event-cancellation cascade. No
    // guardCancellable call -- the cascade has already decided this ticket is
    // cancellable itself (it skips an already-validated one rather than throwing), same
    // deliberate asymmetry that already existed before #22.
    @Override
    public Ticket cancelTicketForEventCancellation(UUID ticketDomainId) {
        return cancelAndAttemptRefund(ticketDomainId, TicketCancelReasonEnum.EVENT_CANCELLED, null);
    }

    // Shared by all three cancel paths above (and cancelTicketForEventCancellation).
    // Not shared with EventServiceImpl#cancelEvent's bulk cascade's OWN guard logic --
    // that path deliberately skips an already-validated ticket rather than erroring the
    // whole cascade over one attendee who already got in; this method only guards the
    // two single-ticket, user-facing entry points.
    private void guardCancellable(Ticket ticket) {
        if (TicketStatusEnum.CANCELLED.equals(ticket.getStatus())) {
            throw new TicketAlreadyCancelledException(ErrorCode.TICKET_ALREADY_CANCELLED, ticket.getDomainId());
        }

        if (EventStatusEnum.COMPLETED.equals(ticket.getTicketType().getEvent().getStatus())) {
            throw new TicketEventAlreadyCompletedException(ErrorCode.TICKET_EVENT_ALREADY_COMPLETED, ticket.getDomainId());
        }

        boolean alreadyValidated = ticket.getValidations().stream()
                .anyMatch(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()));
        if (alreadyValidated) {
            throw new TicketAlreadyValidatedException(ErrorCode.TICKET_ALREADY_VALIDATED, ticket.getDomainId());
        }
    }

    // Deliberately NOT @Transactional -- orchestrates TicketCancellationService's two
    // independent transactions around one external Stripe HTTP call, so that call never
    // runs with a DB lock held. See TicketCancellationServiceImpl for the split itself,
    // and CheckoutServiceImpl/StripeWebhookServiceImpl for the same shape used elsewhere.
    private Ticket cancelAndAttemptRefund(UUID ticketDomainId, TicketCancelReasonEnum reason, String note) {
        TicketCancellationOutcome outcome = ticketCancellationService.cancelAndPersist(ticketDomainId, reason, note);

        if (null == outcome.getRefundAmountMinorUnits()) {
            // orderItem was null -- this ticket was never paid via Stripe at all (the
            // legacy direct-purchase path), so there's nothing to refund.
            return outcome.getTicket();
        }

        RefundStatusEnum refundStatus;
        String providerRefundId = null;
        if (null == outcome.getProviderCheckoutSessionId()) {
            // Should never happen -- see TicketOrder.providerCheckoutSessionId's own
            // comment -- but NOT thrown: cancelAndPersist already committed the
            // cancellation, and nothing here may undo that. Treated as a failed refund
            // instead, logged so it's observable, and still surfaced via refundStatus.
            log.error("Ticket {} has an orderItem but its TicketOrder has no providerCheckoutSessionId -- cannot attempt refund", ticketDomainId);
            refundStatus = RefundStatusEnum.FAILED;
        } else {
            try {
                RefundResult result = paymentGatewayService.refund(RefundRequest.builder()
                        .providerCheckoutSessionId(outcome.getProviderCheckoutSessionId())
                        .amountMinorUnits(outcome.getRefundAmountMinorUnits())
                        .build());
                providerRefundId = result.getProviderRefundId();
                refundStatus = translateRefundStatus(result.getStatus());
            } catch (PaymentGatewayException e) {
                // Cancellation already took effect regardless -- a failed refund is
                // recorded for organizer follow-up, never thrown back to the caller.
                refundStatus = RefundStatusEnum.FAILED;
            }
        }

        ticketCancellationService.recordRefundOutcome(ticketDomainId, refundStatus, providerRefundId);

        Ticket ticket = outcome.getTicket();
        ticket.setRefundStatus(refundStatus);
        ticket.setProviderRefundId(providerRefundId);
        return ticket;
    }

    private RefundStatusEnum translateRefundStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "succeeded" -> RefundStatusEnum.SUCCEEDED;
            case "pending" -> RefundStatusEnum.PENDING;
            default -> RefundStatusEnum.FAILED; // "failed", "canceled", or any future value
        };
    }

    @Override
    public ListTicketTicketTypeResponseDto convertToListTicketTicketTypeResponseDto(TicketType ticketType) {
        ListTicketTicketTypeResponseDto dto = new ListTicketTicketTypeResponseDto();
        dto.setId(ticketType.getDomainId());
        dto.setName(ticketType.getName());
        dto.setPrice(MoneyUtils.toMajorUnits(ticketType.getPriceMinorUnits()));
        return dto;
    }

    @Override
    public ListTicketResponseDto convertToListTicketResponseDto(Ticket ticket) {
        ListTicketResponseDto dto = new ListTicketResponseDto();
        dto.setId(ticket.getDomainId());
        dto.setStatus(ticket.getStatus());
        dto.setTicketType(convertToListTicketTicketTypeResponseDto(ticket.getTicketType()));
        return dto;
    }

    @Override
    public GetTicketResponseDto convertToGetTicketResponseDto(Ticket ticket) {
        GetTicketResponseDto dto = new GetTicketResponseDto();
        dto.setId(ticket.getDomainId());
        dto.setReferenceCode(ticket.getReferenceCode());
        dto.setStatus(ticket.getStatus());
        dto.setPrice(MoneyUtils.toMajorUnits(ticket.getTicketType().getPriceMinorUnits()));
        dto.setDescription(ticket.getTicketType().getDescription());
        dto.setEventName(ticket.getTicketType().getEvent().getName());
        dto.setEventVenueName(ticket.getTicketType().getEvent().getVenue().getName());
        dto.setEventStart(ticket.getTicketType().getEvent().getStart());
        dto.setEventEnd(ticket.getTicketType().getEvent().getEnd());
        return dto;
    }

    @Override
    public CancelTicketResponseDto convertToCancelTicketResponseDto(Ticket ticket) {
        CancelTicketResponseDto dto = new CancelTicketResponseDto();
        dto.setId(ticket.getDomainId());
        dto.setStatus(ticket.getStatus());
        dto.setCancelledAt(ticket.getCancelledAt());
        dto.setCancelReason(ticket.getCancelReason());
        dto.setCancelNote(ticket.getCancelNote());
        dto.setRefundStatus(ticket.getRefundStatus());
        return dto;
    }

    @Override
    public TicketSaleTicketTypeResponseDto convertToTicketSaleTicketTypeResponseDto(TicketType ticketType) {
        TicketSaleTicketTypeResponseDto dto = new TicketSaleTicketTypeResponseDto();
        dto.setId(ticketType.getDomainId());
        dto.setName(ticketType.getName());
        dto.setPrice(MoneyUtils.toMajorUnits(ticketType.getPriceMinorUnits()));
        return dto;
    }

    @Override
    public TicketSaleResponseDto convertToTicketSaleResponseDto(Ticket ticket) {
        TicketSaleResponseDto dto = new TicketSaleResponseDto();
        dto.setId(ticket.getDomainId());
        dto.setReferenceCode(ticket.getReferenceCode());
        dto.setStatus(ticket.getStatus());
        dto.setTicketType(convertToTicketSaleTicketTypeResponseDto(ticket.getTicketType()));
        dto.setPurchaserName(ticket.getPurchaser().getName());
        dto.setPurchaserEmail(ticket.getPurchaser().getEmail());
        dto.setEventId(ticket.getTicketType().getEvent().getDomainId());
        dto.setEventName(ticket.getTicketType().getEvent().getName());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setRefundStatus(ticket.getRefundStatus());
        return dto;
    }
}
