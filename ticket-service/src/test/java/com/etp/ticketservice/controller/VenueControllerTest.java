package com.etp.ticketservice.controller;

import com.etp.ticketservice.controller.support.UserProvisioningTestConfig;
import com.etp.ticketservice.domain.dto.request.CreateVenueRequestDto;
import com.etp.ticketservice.domain.dto.request.UpdateVenueRequestDto;
import com.etp.ticketservice.domain.dto.response.VenueResponseDto;
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.model.CreateVenueRequest;
import com.etp.ticketservice.domain.model.UpdateVenueRequest;
import com.etp.ticketservice.domain.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.etp.ticketservice.controller.support.TestJwts.withSubject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Same slice-test approach as EventControllerTest. VenueController itself has no
// @AuthenticationPrincipal params -- ORGANIZER-only access is enforced entirely in
// SecurityConfig, which this slice doesn't import -- but every request still needs
// .with(withSubject(...)): without SecurityConfig, @WebMvcTest falls back to Spring
// Boot's own default security auto-config, which requires an authenticated principal
// (and rejects unauthenticated POST/PUT outright) regardless of what the real app's
// SecurityConfig would actually allow through for this endpoint. The subject's value is
// irrelevant here since the controller never reads it.
@WebMvcTest(VenueController.class)
@Import(UserProvisioningTestConfig.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private VenueService venueService;

    private static final UUID CALLER_ID = UUID.randomUUID();

    @Test
    void createVenue_returns201() throws Exception {
        CreateVenueRequestDto requestDto = validCreateVenueRequestDto();
        Venue createdVenue = new Venue();
        VenueResponseDto responseDto = new VenueResponseDto();
        responseDto.setId(UUID.randomUUID());
        responseDto.setName("Main Hall");

        when(venueService.convertFromDto(any(CreateVenueRequestDto.class)))
                .thenReturn(new CreateVenueRequest());
        when(venueService.createVenue(any())).thenReturn(createdVenue);
        when(venueService.convertToVenueResponseDto(createdVenue)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/venues")
                        .with(withSubject(CALLER_ID))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Main Hall"));
    }

    @Test
    void createVenue_withBlankName_returns400() throws Exception {
        CreateVenueRequestDto requestDto = validCreateVenueRequestDto();
        requestDto.setName("");

        mockMvc.perform(post("/api/v1/venues")
                        .with(withSubject(CALLER_ID))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateVenue_returns200() throws Exception {
        UUID venueId = UUID.randomUUID();
        UpdateVenueRequestDto requestDto = validUpdateVenueRequestDto(venueId);
        Venue updatedVenue = new Venue();
        VenueResponseDto responseDto = new VenueResponseDto();
        responseDto.setId(venueId);
        responseDto.setName("Renamed Hall");

        when(venueService.convertFromDto(any(UpdateVenueRequestDto.class)))
                .thenReturn(new UpdateVenueRequest());
        when(venueService.updateVenue(eq(venueId), any())).thenReturn(updatedVenue);
        when(venueService.convertToVenueResponseDto(updatedVenue)).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/venues/{venueId}", venueId)
                        .with(withSubject(CALLER_ID))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed Hall"));
    }

    @Test
    void listVenues_withBlankQuery_normalizesToNullSearchTerm() throws Exception {
        Venue venue = new Venue();
        VenueResponseDto responseDto = new VenueResponseDto();
        responseDto.setId(UUID.randomUUID());
        responseDto.setName("Main Hall");

        when(venueService.listVenues(isNull(), any())).thenReturn(new PageImpl<>(List.of(venue)));
        when(venueService.convertToVenueResponseDto(venue)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/venues").with(withSubject(CALLER_ID)).param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Main Hall"));
    }

    @Test
    void getVenue_returnsVenue_whenFound() throws Exception {
        UUID venueId = UUID.randomUUID();
        Venue venue = new Venue();
        VenueResponseDto responseDto = new VenueResponseDto();
        responseDto.setId(venueId);
        responseDto.setName("Main Hall");

        when(venueService.getVenue(venueId)).thenReturn(Optional.of(venue));
        when(venueService.convertToVenueResponseDto(venue)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/venues/{venueId}", venueId).with(withSubject(CALLER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Main Hall"));
    }

    @Test
    void getVenue_returns404_whenNotFound() throws Exception {
        UUID venueId = UUID.randomUUID();
        when(venueService.getVenue(venueId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/venues/{venueId}", venueId).with(withSubject(CALLER_ID)))
                .andExpect(status().isNotFound());
    }

    private static CreateVenueRequestDto validCreateVenueRequestDto() {
        CreateVenueRequestDto dto = new CreateVenueRequestDto();
        dto.setName("Main Hall");
        dto.setAddressLine1("1 Main St");
        dto.setCity("Testville");
        dto.setPostalCode("12345");
        dto.setCountry("Testland");
        return dto;
    }

    private static UpdateVenueRequestDto validUpdateVenueRequestDto(UUID venueId) {
        UpdateVenueRequestDto dto = new UpdateVenueRequestDto();
        dto.setId(venueId);
        dto.setName("Renamed Hall");
        dto.setAddressLine1("1 Main St");
        dto.setCity("Testville");
        dto.setPostalCode("12345");
        dto.setCountry("Testland");
        return dto;
    }
}
