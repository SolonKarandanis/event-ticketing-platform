package com.etp.ticketservice.tickets.qrcode;

import com.etp.ticketservice.tickets.Ticket;

import java.util.UUID;

public interface QrCodeService {
    QrCode generateQrCode(Ticket ticket);

    byte[] getQrCodeImageForUserAndTicket(UUID userId, UUID ticketId);
}
