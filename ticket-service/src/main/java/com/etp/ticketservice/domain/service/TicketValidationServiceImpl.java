package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.dto.response.TicketValidationResponseDto;
import com.etp.ticketservice.domain.entity.QrCode;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketValidation;
import com.etp.ticketservice.domain.enums.QrCodeStatusEnum;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import com.etp.ticketservice.domain.enums.TicketValidationMethod;
import com.etp.ticketservice.domain.enums.TicketValidationStatusEnum;
import com.etp.ticketservice.domain.exception.ErrorCode;
import com.etp.ticketservice.domain.exception.QrCodeNotFoundException;
import com.etp.ticketservice.domain.exception.TicketNotFoundException;
import com.etp.ticketservice.domain.repository.QrCodeRepository;
import com.etp.ticketservice.domain.repository.TicketRepository;
import com.etp.ticketservice.domain.repository.TicketValidationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketValidationServiceImpl implements TicketValidationService {

    private final QrCodeRepository qrCodeRepository;
    private final TicketValidationRepository ticketValidationRepository;
    private final TicketRepository ticketRepository;

    @Override
    public TicketValidation validateTicketByQrCode(String qrCodeId) {
        UUID qrCodeDomainId;
        try {
            qrCodeDomainId = UUID.fromString(qrCodeId);
        } catch (IllegalArgumentException e) {
            throw new QrCodeNotFoundException(ErrorCode.QR_CODE_NOT_FOUND, qrCodeId);
        }

        QrCode qrCode = qrCodeRepository.findByDomainIdAndStatus(qrCodeDomainId, QrCodeStatusEnum.ACTIVE)
                .orElseThrow(() -> new QrCodeNotFoundException(ErrorCode.QR_CODE_NOT_FOUND, qrCodeId));

        Ticket ticket = qrCode.getTicket();

        return validateTicket(ticket, TicketValidationMethod.QR_SCAN);
    }

    @Override
    public TicketValidation validateTicketByReferenceCode(String referenceCode) {
        Ticket ticket = ticketRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new TicketNotFoundException(ErrorCode.TICKET_NOT_FOUND, referenceCode));
        return validateTicket(ticket, TicketValidationMethod.MANUAL);
    }

    private TicketValidation validateTicket(Ticket ticket, TicketValidationMethod method) {
        // Checked before the VALID/INVALID history logic below, not folded into it --
        // a cancelled ticket should read as specifically CANCELLED to staff at the door,
        // not INVALID (which means "already used"), regardless of what its validation
        // history looks like.
        TicketValidationStatusEnum ticketValidationStatus;
        if (TicketStatusEnum.CANCELLED.equals(ticket.getStatus())) {
            ticketValidationStatus = TicketValidationStatusEnum.CANCELLED;
        } else {
            ticketValidationStatus = ticket.getValidations().stream()
                    .filter(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()))
                    .findFirst()
                    .map(v -> TicketValidationStatusEnum.INVALID)
                    .orElse(TicketValidationStatusEnum.VALID);
        }

        TicketValidation ticketValidation = new TicketValidation();
        ticketValidation.setDomainId(UUID.randomUUID());
        ticketValidation.setValidationMethod(method);
        ticketValidation.setStatus(ticketValidationStatus);
        ticket.addValidation(ticketValidation);

        return ticketValidationRepository.save(ticketValidation);
    }

    @Override
    public TicketValidationResponseDto convertToTicketValidationResponseDto(TicketValidation ticketValidation) {
        TicketValidationResponseDto dto = new TicketValidationResponseDto();
        dto.setTicketId(ticketValidation.getTicket().getDomainId());
        dto.setStatus(ticketValidation.getStatus());
        return dto;
    }
}
