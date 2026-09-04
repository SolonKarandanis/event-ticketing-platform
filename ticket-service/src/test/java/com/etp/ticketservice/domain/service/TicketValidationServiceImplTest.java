package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.entity.QrCode;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketValidation;
import com.etp.ticketservice.domain.enums.QrCodeStatusEnum;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import com.etp.ticketservice.domain.enums.TicketValidationMethod;
import com.etp.ticketservice.domain.enums.TicketValidationStatusEnum;
import com.etp.ticketservice.domain.exception.QrCodeNotFoundException;
import com.etp.ticketservice.domain.exception.TicketNotFoundException;
import com.etp.ticketservice.domain.repository.QrCodeRepository;
import com.etp.ticketservice.domain.repository.TicketRepository;
import com.etp.ticketservice.domain.repository.TicketValidationRepository;
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
import static org.mockito.Mockito.when;

// Pure Mockito unit tests -- see EventServiceImplTest's class comment for why this layer
// is worth testing directly. The core logic worth pinning down: a cancelled ticket reads
// as CANCELLED regardless of validation history (checked first, not folded into the
// VALID/INVALID branch below it), a ticket with no prior VALID validation reads as
// VALID, and one that already has a VALID validation reads as INVALID ("already used").
@ExtendWith(MockitoExtension.class)
class TicketValidationServiceImplTest {

    @Mock
    private QrCodeRepository qrCodeRepository;
    @Mock
    private TicketValidationRepository ticketValidationRepository;
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketValidationServiceImpl ticketValidationService;

    @Test
    void validateTicketByQrCode_malformedId_throwsNotFound() {
        assertThatThrownBy(() -> ticketValidationService.validateTicketByQrCode("not-a-uuid"))
                .isInstanceOf(QrCodeNotFoundException.class);
    }

    @Test
    void validateTicketByQrCode_notActiveOrMissing_throwsNotFound() {
        UUID qrCodeId = UUID.randomUUID();
        when(qrCodeRepository.findByDomainIdAndStatus(qrCodeId, QrCodeStatusEnum.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketValidationService.validateTicketByQrCode(qrCodeId.toString()))
                .isInstanceOf(QrCodeNotFoundException.class);
    }

    @Test
    void validateTicketByQrCode_freshTicket_recordsValid() {
        UUID qrCodeId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        QrCode qrCode = new QrCode();
        qrCode.setTicket(ticket);
        when(qrCodeRepository.findByDomainIdAndStatus(qrCodeId, QrCodeStatusEnum.ACTIVE)).thenReturn(Optional.of(qrCode));
        when(ticketValidationRepository.save(any(TicketValidation.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketValidation result = ticketValidationService.validateTicketByQrCode(qrCodeId.toString());

        assertThat(result.getStatus()).isEqualTo(TicketValidationStatusEnum.VALID);
        assertThat(result.getValidationMethod()).isEqualTo(TicketValidationMethod.QR_SCAN);
    }

    @Test
    void validateTicketByQrCode_alreadyValidatedTicket_recordsInvalid() {
        UUID qrCodeId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        TicketValidation priorValidation = new TicketValidation();
        priorValidation.setStatus(TicketValidationStatusEnum.VALID);
        ticket.addValidation(priorValidation);
        QrCode qrCode = new QrCode();
        qrCode.setTicket(ticket);
        when(qrCodeRepository.findByDomainIdAndStatus(qrCodeId, QrCodeStatusEnum.ACTIVE)).thenReturn(Optional.of(qrCode));
        when(ticketValidationRepository.save(any(TicketValidation.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketValidation result = ticketValidationService.validateTicketByQrCode(qrCodeId.toString());

        assertThat(result.getStatus()).isEqualTo(TicketValidationStatusEnum.INVALID);
    }

    @Test
    void validateTicketByQrCode_cancelledTicket_recordsCancelledRegardlessOfHistory() {
        UUID qrCodeId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.CANCELLED);
        QrCode qrCode = new QrCode();
        qrCode.setTicket(ticket);
        when(qrCodeRepository.findByDomainIdAndStatus(qrCodeId, QrCodeStatusEnum.ACTIVE)).thenReturn(Optional.of(qrCode));
        when(ticketValidationRepository.save(any(TicketValidation.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketValidation result = ticketValidationService.validateTicketByQrCode(qrCodeId.toString());

        assertThat(result.getStatus()).isEqualTo(TicketValidationStatusEnum.CANCELLED);
    }

    @Test
    void validateTicketByReferenceCode_notFound_throws() {
        when(ticketRepository.findByReferenceCode("XY3P9KRT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketValidationService.validateTicketByReferenceCode("XY3P9KRT"))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void validateTicketByReferenceCode_freshTicket_recordsValidViaManualMethod() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        when(ticketRepository.findByReferenceCode("XY3P9KRT")).thenReturn(Optional.of(ticket));
        when(ticketValidationRepository.save(any(TicketValidation.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketValidation result = ticketValidationService.validateTicketByReferenceCode("XY3P9KRT");

        assertThat(result.getStatus()).isEqualTo(TicketValidationStatusEnum.VALID);
        assertThat(result.getValidationMethod()).isEqualTo(TicketValidationMethod.MANUAL);
    }

    @Test
    void convertToTicketValidationResponseDto_mapsTicketDomainIdAndStatus() {
        UUID ticketDomainId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setDomainId(ticketDomainId);
        TicketValidation validation = new TicketValidation();
        validation.setTicket(ticket);
        validation.setStatus(TicketValidationStatusEnum.VALID);

        var dto = ticketValidationService.convertToTicketValidationResponseDto(validation);

        assertThat(dto.getTicketId()).isEqualTo(ticketDomainId);
        assertThat(dto.getStatus()).isEqualTo(TicketValidationStatusEnum.VALID);
    }
}
