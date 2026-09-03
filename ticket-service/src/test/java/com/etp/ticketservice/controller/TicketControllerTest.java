package com.etp.ticketservice.controller;

import com.etp.ticketservice.controller.support.UserProvisioningTestConfig;
import com.etp.ticketservice.domain.dto.request.CancelTicketRequestDto;
import com.etp.ticketservice.domain.dto.response.CancelTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.GetTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.ListTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.ListTicketTicketTypeResponseDto;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.enums.TicketCancelReasonEnum;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import com.etp.ticketservice.domain.service.QrCodeService;
import com.etp.ticketservice.domain.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.etp.ticketservice.controller.support.TestJwts.withSubject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Same slice-test approach as EventControllerTest -- see that class's comment for why
// SecurityConfig isn't imported. Every request here is authenticated as the ticket's
// own owner (ATTENDEE_ID); TicketController itself has no notion of roles at all, it
// just trusts parseUserId(jwt) and lets TicketServiceImpl enforce ownership.
@WebMvcTest(TicketController.class)
@Import(UserProvisioningTestConfig.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private QrCodeService qrCodeService;

    private static final UUID ATTENDEE_ID = UUID.randomUUID();

    @Test
    void listTickets_returnsPageOfTickets() throws Exception {
        Ticket ticket = new Ticket();
        ListTicketResponseDto dto = new ListTicketResponseDto(
                UUID.randomUUID(), TicketStatusEnum.PURCHASED,
                new ListTicketTicketTypeResponseDto(UUID.randomUUID(), "General", 25.0));

        when(ticketService.listTicketsForUser(eq(ATTENDEE_ID), any()))
                .thenReturn(new PageImpl<>(List.of(ticket)));
        when(ticketService.convertToListTicketResponseDto(ticket)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/tickets").with(attendee()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].ticketType.name").value("General"));
    }

    @Test
    void getTicket_returnsTicketDetails_whenFound() throws Exception {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        GetTicketResponseDto dto = new GetTicketResponseDto();
        dto.setId(ticketId);
        dto.setReferenceCode("XY3P9KRT");

        when(ticketService.getTicketForUser(ATTENDEE_ID, ticketId)).thenReturn(Optional.of(ticket));
        when(ticketService.convertToGetTicketResponseDto(ticket)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/tickets/{ticketId}", ticketId).with(attendee()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceCode").value("XY3P9KRT"));
    }

    @Test
    void getTicket_returns404_whenNotFoundOrNotOwnedByThisAttendee() throws Exception {
        UUID ticketId = UUID.randomUUID();
        when(ticketService.getTicketForUser(ATTENDEE_ID, ticketId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/tickets/{ticketId}", ticketId).with(attendee()))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelTicket_withNote_returnsCancelledTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();
        Ticket cancelledTicket = new Ticket();
        CancelTicketRequestDto requestDto = new CancelTicketRequestDto("Changed my mind");
        CancelTicketResponseDto responseDto = new CancelTicketResponseDto(
                ticketId, TicketStatusEnum.CANCELLED, null,
                TicketCancelReasonEnum.ATTENDEE_REQUEST, "Changed my mind");

        when(ticketService.cancelTicketForUser(ATTENDEE_ID, ticketId, "Changed my mind"))
                .thenReturn(cancelledTicket);
        when(ticketService.convertToCancelTicketResponseDto(cancelledTicket)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/tickets/{ticketId}/cancel", ticketId)
                        .with(attendee())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelNote").value("Changed my mind"));
    }

    // cancelTicketRequestDto is @RequestBody(required = false) -- an absent body is a
    // valid "cancel with no note" request, not a 400. Regression-shaped test mirroring
    // EventControllerTest's createEvent_withNoImages_succeeds: confirms the null check
    // right above ticketService.cancelTicketForUser is actually exercised.
    @Test
    void cancelTicket_withNoBody_treatsNoteAsNull() throws Exception {
        UUID ticketId = UUID.randomUUID();
        Ticket cancelledTicket = new Ticket();
        CancelTicketResponseDto responseDto = new CancelTicketResponseDto(
                ticketId, TicketStatusEnum.CANCELLED, null, TicketCancelReasonEnum.ATTENDEE_REQUEST, null);

        when(ticketService.cancelTicketForUser(eq(ATTENDEE_ID), eq(ticketId), isNull()))
                .thenReturn(cancelledTicket);
        when(ticketService.convertToCancelTicketResponseDto(cancelledTicket)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/tickets/{ticketId}/cancel", ticketId).with(attendee()))
                .andExpect(status().isOk());

        verify(ticketService).cancelTicketForUser(ATTENDEE_ID, ticketId, null);
    }

    @Test
    void getTicketQrCode_returnsPngBytes() throws Exception {
        UUID ticketId = UUID.randomUUID();
        byte[] pngBytes = {(byte) 0x89, 0x50, 0x4e, 0x47};
        when(qrCodeService.getQrCodeImageForUserAndTicket(ATTENDEE_ID, ticketId)).thenReturn(pngBytes);

        mockMvc.perform(get("/api/v1/tickets/{ticketId}/qr-codes", ticketId).with(attendee()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(pngBytes));
    }

    private static RequestPostProcessor attendee() {
        return withSubject(ATTENDEE_ID);
    }
}
