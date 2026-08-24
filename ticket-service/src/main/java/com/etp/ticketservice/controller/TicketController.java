package com.etp.ticketservice.controller;

import com.etp.ticketservice.domain.dto.response.GetTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.ListTicketResponseDto;
import com.etp.ticketservice.domain.service.QrCodeService;
import com.etp.ticketservice.domain.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    private final TicketService ticketService;
    private final QrCodeService qrCodeService;

    @GetMapping
    public Page<ListTicketResponseDto> listTickets(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        log.info("TicketController --> listTickets");
        return ticketService.listTicketsForUser(
                parseUserId(jwt),
                pageable
        ).map(ticketService::convertToListTicketResponseDto);
    }

    @GetMapping(path = "/{ticketId}")
    public ResponseEntity<GetTicketResponseDto> getTicket(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID ticketId) {
        log.info("TicketController --> getTicket");
        return ticketService
                .getTicketForUser(parseUserId(jwt), ticketId)
                .map(ticketService::convertToGetTicketResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/{ticketId}/qr-codes")
    public ResponseEntity<byte[]> getTicketQrCode(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID ticketId) {
        log.info("TicketController --> getTicketQrCode");
        byte[] qrCodeImage = qrCodeService.getQrCodeImageForUserAndTicket(
                parseUserId(jwt),
                ticketId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCodeImage.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(qrCodeImage);
    }

    private UUID parseUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
