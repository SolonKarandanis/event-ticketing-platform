package com.etp.ticketservice.controller;

import com.etp.ticketservice.controller.support.UserProvisioningTestConfig;
import com.etp.ticketservice.domain.dto.request.TicketValidationRequestDto;
import com.etp.ticketservice.domain.dto.response.TicketValidationResponseDto;
import com.etp.ticketservice.domain.entity.TicketValidation;
import com.etp.ticketservice.domain.enums.TicketValidationMethod;
import com.etp.ticketservice.domain.enums.TicketValidationStatusEnum;
import com.etp.ticketservice.domain.service.TicketValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static com.etp.ticketservice.controller.support.TestJwts.withSubject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Same slice-test approach as EventControllerTest. Staff-only per SecurityConfig, not
// exercised in this slice (see that class's comment) -- authenticated as a generic
// STAFF_ID here purely to model who calls this in practice.
@WebMvcTest(TicketValidationController.class)
@Import(UserProvisioningTestConfig.class)
class TicketValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TicketValidationService ticketValidationService;

    private static final UUID STAFF_ID = UUID.randomUUID();

    @Test
    void validateTicket_withQrScanMethod_validatesByQrCode() throws Exception {
        TicketValidationRequestDto requestDto = new TicketValidationRequestDto(
                "qr-domain-id", TicketValidationMethod.QR_SCAN);
        TicketValidation ticketValidation = new TicketValidation();
        TicketValidationResponseDto responseDto = new TicketValidationResponseDto(
                UUID.randomUUID(), TicketValidationStatusEnum.VALID);

        when(ticketValidationService.validateTicketByQrCode("qr-domain-id")).thenReturn(ticketValidation);
        when(ticketValidationService.convertToTicketValidationResponseDto(ticketValidation))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/ticket-validations")
                        .with(withSubject(STAFF_ID))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALID"));

        verify(ticketValidationService, never()).validateTicketByReferenceCode(any());
    }

    @Test
    void validateTicket_withManualMethod_validatesByReferenceCode() throws Exception {
        TicketValidationRequestDto requestDto = new TicketValidationRequestDto(
                "XY3P9KRT", TicketValidationMethod.MANUAL);
        TicketValidation ticketValidation = new TicketValidation();
        TicketValidationResponseDto responseDto = new TicketValidationResponseDto(
                UUID.randomUUID(), TicketValidationStatusEnum.INVALID);

        when(ticketValidationService.validateTicketByReferenceCode("XY3P9KRT")).thenReturn(ticketValidation);
        when(ticketValidationService.convertToTicketValidationResponseDto(ticketValidation))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/ticket-validations")
                        .with(withSubject(STAFF_ID))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVALID"));

        verify(ticketValidationService, never()).validateTicketByQrCode(any());
    }
}
