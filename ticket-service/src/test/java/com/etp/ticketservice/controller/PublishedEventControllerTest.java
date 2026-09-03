package com.etp.ticketservice.controller;

import com.etp.ticketservice.controller.support.UserProvisioningTestConfig;
import com.etp.ticketservice.domain.dto.request.ListPublishedEventsRequestDto;
import com.etp.ticketservice.domain.dto.response.GetPublishedEventDetailsResponseDto;
import com.etp.ticketservice.domain.dto.response.ListPublishedEventResponseDto;
import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.enums.PublishedEventsSortBy;
import com.etp.ticketservice.domain.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Every endpoint here is public per the real SecurityConfig (GET/POST
// /api/v1/published-events/** is permitAll), but this slice doesn't import
// SecurityConfig -- so, same as VenueControllerTest, every request still needs
// .with(withSubject(...)) to get past Spring Boot's own default security auto-config,
// which requires an authenticated principal absent any SecurityFilterChain bean of the
// app's own. The subject's value is irrelevant: PublishedEventController never reads a
// Jwt principal at all.
@WebMvcTest(PublishedEventController.class)
@Import(UserProvisioningTestConfig.class)
class PublishedEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EventService eventService;

    private static final UUID CALLER_ID = UUID.randomUUID();

    @Test
    void listPublishedEvents_withNoBody_defaultsToEmptyCriteriaAndSoonestSort() throws Exception {
        when(eventService.findPublishedEvents(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(PublishedEventsSortBy.SOONEST), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(post("/api/v1/published-events/search").with(withSubject(CALLER_ID)))
                .andExpect(status().isOk());
    }

    // Covers two conversions PublishedEventController does itself, at the API boundary:
    // paging.sortField's wire string ("priceAsc") -> the PublishedEventsSortBy enum, and
    // a blank q -> null (same normalization VenueController does for its own search).
    @Test
    void listPublishedEvents_mapsSortFieldAndNormalizesBlankQuery() throws Exception {
        ListPublishedEventsRequestDto request = new ListPublishedEventsRequestDto();
        request.setQ("   ");
        request.getPaging().setPagingStart(2);
        request.getPaging().setPagingSize(5);
        request.getPaging().setSortingColumn("priceAsc");

        Event event = new Event();
        ListPublishedEventResponseDto responseDto = new ListPublishedEventResponseDto();
        responseDto.setId(UUID.randomUUID());
        responseDto.setName("Summer Fest");

        when(eventService.findPublishedEvents(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(PublishedEventsSortBy.PRICE_ASC), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));
        when(eventService.convertToListPublishedEventResponseDto(event)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/published-events/search")
                        .with(withSubject(CALLER_ID))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Summer Fest"));

        verify(eventService).findPublishedEvents(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(PublishedEventsSortBy.PRICE_ASC), eq(Pageable.ofSize(5).withPage(2)));
    }

    @Test
    void listPublishedEventCities_returnsDistinctCityList() throws Exception {
        when(eventService.findPublishedEventCities()).thenReturn(List.of("Athens", "Testville"));

        mockMvc.perform(get("/api/v1/published-events/cities").with(withSubject(CALLER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Athens"))
                .andExpect(jsonPath("$[1]").value("Testville"));
    }

    @Test
    void getPublishedEventDetails_returnsDetails_whenFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        Event event = new Event();
        GetPublishedEventDetailsResponseDto responseDto = new GetPublishedEventDetailsResponseDto();
        responseDto.setId(eventId);
        responseDto.setName("Summer Fest");

        when(eventService.getPublishedEvent(eventId)).thenReturn(Optional.of(event));
        when(eventService.convertToGetPublishedEventDetailsResponseDto(event)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/published-events/{eventId}", eventId).with(withSubject(CALLER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Summer Fest"));
    }

    @Test
    void getPublishedEventDetails_returns404_whenNotFoundOrNotPublished() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(eventService.getPublishedEvent(eventId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/published-events/{eventId}", eventId).with(withSubject(CALLER_ID)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublishedEventImage_returnsJpegBytes_whenFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        byte[] jpegBytes = {(byte) 0xff, (byte) 0xd8, (byte) 0xff};
        when(eventService.getPublishedEventImage(eventId, imageId)).thenReturn(Optional.of(jpegBytes));

        mockMvc.perform(get("/api/v1/published-events/{eventId}/images/{imageId}", eventId, imageId)
                        .with(withSubject(CALLER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(jpegBytes));
    }

    @Test
    void getPublishedEventImage_returns404_whenNotFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        when(eventService.getPublishedEventImage(eventId, imageId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/published-events/{eventId}/images/{imageId}", eventId, imageId)
                        .with(withSubject(CALLER_ID)))
                .andExpect(status().isNotFound());
    }
}
