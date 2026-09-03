package com.etp.ticketservice.controller;

import com.etp.ticketservice.domain.dto.request.CreateEventRequestDto;
import com.etp.ticketservice.domain.dto.request.CreateTicketTypeRequestDto;
import com.etp.ticketservice.domain.dto.response.CreateEventResponseDto;
import com.etp.ticketservice.domain.dto.response.GetEventDetailsResponseDto;
import com.etp.ticketservice.controller.support.UserProvisioningTestConfig;
import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.model.CreateEventRequest;
import com.etp.ticketservice.domain.model.antivirus.VirusScannable;
import com.etp.ticketservice.domain.service.EventService;
import com.etp.ticketservice.domain.service.TicketService;
import com.etp.ticketservice.domain.service.antivirus.AntivirusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.ObjectMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.etp.ticketservice.controller.support.TestJwts.withSubject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// A slice test, not a full @SpringBootTest -- @WebMvcTest loads only EventController
// (plus GlobalExceptionHandler, since @RestControllerAdvice beans are picked up too)
// and its Spring MVC infrastructure, with every service-layer dependency mocked via
// @MockitoBean. Deliberately doesn't import the app's own SecurityConfig -- that would
// also require mocking UserProvisioningFilter/JwtAuthenticationConverter to wire up,
// and role-based authorization (hasRole(ORGANIZER), etc.) is a SecurityConfig concern,
// not something EventController itself implements. jwt() here authenticates the
// request (bypassing the real JwtDecoder entirely -- no network call, no running
// Keycloak needed) without asserting anything about which roles are allowed through;
// that's left for a dedicated security-focused test.
@WebMvcTest(EventController.class)
@Import(UserProvisioningTestConfig.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // A plain, locally-built mapper -- not the Spring-autoconfigured bean -- since all
    // this needs is turning a simple DTO (String/UUID/List fields, no LocalDateTime set
    // in these tests) into JSON bytes for a multipart part. Jackson 3 (tools.jackson.*,
    // not com.fasterxml.jackson.*) is what this project's on, per its resolved test
    // classpath -- confirmed directly rather than assumed, the same way the actuator
    // package move was.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private AntivirusService antivirusService;

    private static final UUID ORGANIZER_ID = UUID.randomUUID();

    @Test
    void getEvent_returnsEventDetails_whenFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        Event event = new Event();
        GetEventDetailsResponseDto responseDto = new GetEventDetailsResponseDto();
        responseDto.setId(eventId);
        responseDto.setName("Summer Fest");

        when(eventService.getEventForOrganizer(ORGANIZER_ID, eventId)).thenReturn(Optional.of(event));
        when(eventService.convertToGetEventDetailsResponseDto(event)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/events/{eventId}", eventId).with(organizer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.name").value("Summer Fest"));
    }

    @Test
    void getEvent_returns404_whenNotFoundOrNotOwnedByThisOrganizer() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(eventService.getEventForOrganizer(ORGANIZER_ID, eventId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/events/{eventId}", eventId).with(organizer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createEvent_withOneImage_scansItAndReturns201() throws Exception {
        MockMultipartFile eventPart = eventPart(validCreateEventRequestDto());
        MockMultipartFile imagePart = new MockMultipartFile(
                "newImages", "cover.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        Event createdEvent = new Event();
        CreateEventResponseDto responseDto = new CreateEventResponseDto();
        responseDto.setId(UUID.randomUUID());
        responseDto.setName("New Event");

        when(eventService.convertFromDto(any(CreateEventRequestDto.class)))
                .thenReturn(new CreateEventRequest());
        when(eventService.createEvent(eq(ORGANIZER_ID), any(), any())).thenReturn(createdEvent);
        when(eventService.convertToCreateEventResponseDto(createdEvent)).thenReturn(responseDto);

        mockMvc.perform(multipart("/api/v1/events").file(eventPart).file(imagePart).with(organizer()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Event"));

        // The one image actually gets scanned -- not just accepted and forwarded.
        verify(antivirusService, times(1)).scan(any(VirusScannable.class));
    }

    // Regression test for a real bug this test caught on first run: EventController
    // used to call scanNewImages(newImages) with the raw, possibly-null @RequestPart
    // list directly, instead of the null-safe resolveNewImages(newImages) result used
    // for the actual event creation right below it. Creating an event with zero images
    // -- a perfectly valid request, images is optional -- threw a NullPointerException
    // from inside scanNewImages' for-each loop before eventService.createEvent was ever
    // reached, which GlobalExceptionHandler's generic Exception handler turned into a
    // 500 instead of the 201 this request should get. Fixed by routing scanNewImages'
    // argument through resolveNewImages too.
    @Test
    void createEvent_withNoImages_succeeds() throws Exception {
        MockMultipartFile eventPart = eventPart(validCreateEventRequestDto());

        Event createdEvent = new Event();
        CreateEventResponseDto responseDto = new CreateEventResponseDto();
        responseDto.setId(UUID.randomUUID());

        when(eventService.convertFromDto(any(CreateEventRequestDto.class)))
                .thenReturn(new CreateEventRequest());
        when(eventService.createEvent(eq(ORGANIZER_ID), any(), any())).thenReturn(createdEvent);
        when(eventService.convertToCreateEventResponseDto(createdEvent)).thenReturn(responseDto);

        mockMvc.perform(multipart("/api/v1/events").file(eventPart).with(organizer()))
                .andExpect(status().isCreated());

        verify(eventService).createEvent(eq(ORGANIZER_ID), any(), eq(List.of()));
        verify(antivirusService, never()).scan(any(VirusScannable.class));
    }

    @Test
    void deleteEvent_returns204() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/events/{eventId}", eventId).with(organizer()))
                .andExpect(status().isNoContent());

        verify(eventService).deleteEventForOrganizer(ORGANIZER_ID, eventId);
    }

    @Test
    void publishEvent_returns204() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/events/{eventId}/publish", eventId).with(organizer()))
                .andExpect(status().isNoContent());

        verify(eventService).publishEvent(ORGANIZER_ID, eventId);
    }

    // A JWT whose subject is ORGANIZER_ID -- parseUserId(jwt) reads exactly that claim
    // (UUID.fromString(jwt.getSubject())). Named for what it represents in these tests,
    // not for what SecurityConfig would actually require to reach this far (role
    // checks aren't part of this slice -- see the class-level comment).
    private static RequestPostProcessor organizer() {
        return withSubject(ORGANIZER_ID);
    }

    private MockMultipartFile eventPart(CreateEventRequestDto dto) throws Exception {
        return new MockMultipartFile(
                "event", "", "application/json", objectMapper.writeValueAsBytes(dto));
    }

    // images stays at its default empty list regardless of whether the test attaches
    // an actual newImages file part -- EventController doesn't validate that images[]
    // entries correlate to real file parts (that's EventServiceImpl's job, mocked out
    // in this slice), so only the file part itself needs to vary between tests, not
    // this DTO.
    private static CreateEventRequestDto validCreateEventRequestDto() {
        CreateTicketTypeRequestDto ticketType = new CreateTicketTypeRequestDto();
        ticketType.setName("General");
        ticketType.setPrice(10.0);

        CreateEventRequestDto dto = new CreateEventRequestDto();
        dto.setName("New Event");
        dto.setVenueId(UUID.randomUUID());
        dto.setTicketTypes(List.of(ticketType));
        return dto;
    }
}
