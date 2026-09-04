package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.entity.QrCode;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.enums.QrCodeStatusEnum;
import com.etp.ticketservice.domain.exception.QrCodeGenerationException;
import com.etp.ticketservice.domain.exception.QrCodeNotFoundException;
import com.etp.ticketservice.domain.repository.QrCodeRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// Pure Mockito unit tests -- see EventServiceImplTest's class comment for why this layer
// is worth testing directly. QRCodeWriter is mocked (not the real zxing encoder) so the
// WriterException-wrapping branch is actually reachable -- a real encoder essentially
// never fails for a plain UUID string at a fixed size.
@ExtendWith(MockitoExtension.class)
class QrCodeServiceImplTest {

    @Mock
    private QRCodeWriter qrCodeWriter;
    @Mock
    private QrCodeRepository qrCodeRepository;

    @InjectMocks
    private QrCodeServiceImpl qrCodeService;

    @Test
    void generateQrCode_happyPath_savesActiveQrCodeLinkedToTicket() throws WriterException {
        when(qrCodeWriter.encode(anyString(), any(BarcodeFormat.class), anyInt(), anyInt()))
                .thenReturn(new BitMatrix(1, 1));
        when(qrCodeRepository.saveAndFlush(any(QrCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket ticket = new Ticket();

        QrCode qrCode = qrCodeService.generateQrCode(ticket);

        assertThat(qrCode.getStatus()).isEqualTo(QrCodeStatusEnum.ACTIVE);
        assertThat(qrCode.getDomainId()).isNotNull();
        assertThat(qrCode.getValue()).isNotBlank();
        assertThat(ticket.getQrCodes()).contains(qrCode);
        // The stored value is base64-encoded PNG bytes -- decodable is the contract
        // getQrCodeImageForUserAndTicket relies on.
        assertThat(Base64.getDecoder().decode(qrCode.getValue())).isNotEmpty();
    }

    @Test
    void generateQrCode_writerException_wrapsAsQrCodeGenerationException() throws WriterException {
        when(qrCodeWriter.encode(anyString(), any(BarcodeFormat.class), anyInt(), anyInt()))
                .thenThrow(new WriterException("encoding failed"));

        assertThatThrownBy(() -> qrCodeService.generateQrCode(new Ticket()))
                .isInstanceOf(QrCodeGenerationException.class);
    }

    @Test
    void getQrCodeImageForUserAndTicket_notFound_throws() {
        UUID userId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        when(qrCodeRepository.findByTicketDomainIdAndTicketPurchaserDomainId(ticketId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qrCodeService.getQrCodeImageForUserAndTicket(userId, ticketId))
                .isInstanceOf(QrCodeNotFoundException.class);
    }

    @Test
    void getQrCodeImageForUserAndTicket_happyPath_returnsDecodedBytes() {
        UUID userId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        byte[] pngBytes = {(byte) 0x89, 0x50, 0x4e, 0x47};
        QrCode qrCode = new QrCode();
        qrCode.setValue(Base64.getEncoder().encodeToString(pngBytes));
        when(qrCodeRepository.findByTicketDomainIdAndTicketPurchaserDomainId(ticketId, userId)).thenReturn(Optional.of(qrCode));

        byte[] result = qrCodeService.getQrCodeImageForUserAndTicket(userId, ticketId);

        assertThat(result).isEqualTo(pngBytes);
    }

    @Test
    void getQrCodeImageForUserAndTicket_corruptBase64_throwsNotFound() {
        UUID userId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        QrCode qrCode = new QrCode();
        qrCode.setValue("not-valid-base64!!!");
        when(qrCodeRepository.findByTicketDomainIdAndTicketPurchaserDomainId(ticketId, userId)).thenReturn(Optional.of(qrCode));

        assertThatThrownBy(() -> qrCodeService.getQrCodeImageForUserAndTicket(userId, ticketId))
                .isInstanceOf(QrCodeNotFoundException.class);
    }
}
