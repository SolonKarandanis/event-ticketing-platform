package com.etp.ticketservice.controller;

import com.etp.ticketservice.domain.dto.request.CancelTicketRequestDto;
import com.etp.ticketservice.domain.dto.request.CreateEventRequestDto;
import com.etp.ticketservice.domain.dto.request.UpdateEventRequestDto;
import com.etp.ticketservice.domain.dto.response.CancelTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.CreateEventResponseDto;
import com.etp.ticketservice.domain.dto.response.GetEventDetailsResponseDto;
import com.etp.ticketservice.domain.dto.response.ListEventResponseDto;
import com.etp.ticketservice.domain.dto.response.TicketSaleResponseDto;
import com.etp.ticketservice.domain.dto.response.UpdateEventResponseDto;
import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.model.CreateEventRequest;
import com.etp.ticketservice.domain.model.UpdateEventRequest;
import com.etp.ticketservice.domain.service.EventService;
import com.etp.ticketservice.domain.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;
    private final TicketService ticketService;

    // Multipart, not JSON -- "event" carries everything this endpoint used to accept as
    // a plain @RequestBody (including the images list's metadata), and "newImages" holds
    // the actual bytes for every images[].newImageIndex reference in it, since JSON can't
    // carry binary data inline. required = false on newImages so an event with zero
    // images doesn't need an empty file part.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateEventResponseDto> createEvent(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestPart("event") CreateEventRequestDto createEventRequestDto,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) {
        log.info("EventController --> createEvent");
        CreateEventRequest createEventRequest = eventService.convertFromDto(createEventRequestDto);

        UUID userId = parseUserId(jwt);

        Event createdEvent = eventService.createEvent(userId, createEventRequest, resolveNewImages(newImages));

        CreateEventResponseDto createEventResponseDto = eventService.convertToCreateEventResponseDto(createdEvent);

        return new ResponseEntity<>(createEventResponseDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ListEventResponseDto>> listEvents(@AuthenticationPrincipal Jwt jwt, Pageable pageable
    ) {
        log.info("EventController --> listEvents");
        UUID userId = parseUserId(jwt);
        Page<Event> events = eventService.listEventsForOrganizer(userId, pageable);
        return ResponseEntity.ok(
                events.map(eventService::convertToListEventResponseDto)
        );
    }

    @GetMapping(path = "/{eventId}")
    public ResponseEntity<GetEventDetailsResponseDto> getEvent(@AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId) {
        log.info("EventController --> getEvent --> id: {}", eventId);
        UUID userId = parseUserId(jwt);

        return eventService.getEventForOrganizer(userId, eventId)
                .map(eventService::convertToGetEventDetailsResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateEventResponseDto> updateEvent(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId,
            @Valid @RequestPart("event") UpdateEventRequestDto updateEventRequestDto,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) {
        log.info("EventController --> updateEvent --> id: {}", eventId);
        UpdateEventRequest updateEventRequest = eventService.convertFromDto(updateEventRequestDto);
        UUID userId = parseUserId(jwt);

        Event updatedEvent = eventService.updateEventForOrganizer(
                userId, eventId, updateEventRequest, resolveNewImages(newImages)
        );

        UpdateEventResponseDto updateEventResponseDto = eventService.convertToUpdateEventResponseDto(updatedEvent);

        return ResponseEntity.ok(updateEventResponseDto);
    }

    // required = false above means a null list is the normal "no new images this call"
    // case, not a missing/malformed request -- normalized to an empty list here so
    // EventServiceImpl never has to null-check it.
    private List<MultipartFile> resolveNewImages(List<MultipartFile> newImages) {
        return null != newImages ? newImages : Collections.emptyList();
    }

    @DeleteMapping(path = "/{eventId}")
    public ResponseEntity<Void> deleteEvent(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        log.info("EventController --> deleteEvent --> id: {}", eventId);
        UUID userId = parseUserId(jwt);
        eventService.deleteEventForOrganizer(userId, eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{eventId}/publish")
    public ResponseEntity<Void> publishEvent(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        log.info("EventController --> publishEvent --> id: {}", eventId);
        eventService.publishEvent(parseUserId(jwt), eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{eventId}/cancel")
    public ResponseEntity<Void> cancelEvent(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        log.info("EventController --> cancelEvent --> id: {}", eventId);
        eventService.cancelEvent(parseUserId(jwt), eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{eventId}/complete")
    public ResponseEntity<Void> completeEvent(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        log.info("EventController --> completeEvent --> id: {}", eventId);
        eventService.completeEvent(parseUserId(jwt), eventId);
        return ResponseEntity.noContent().build();
    }

    // Cross-event ticket-sales view, across every event this organizer owns. Placed
    // ahead of getEvent's /{eventId} in this file for readability -- Spring's request
    // matching already prioritizes this literal path over the {eventId} variable one
    // regardless of declaration order (same reason /api/v1/published-events/cities
    // didn't collide with /{eventId} there).
    @GetMapping(path = "/tickets")
    public ResponseEntity<Page<TicketSaleResponseDto>> listTicketsForOrganizer(
            @AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        log.info("EventController --> listTicketsForOrganizer");
        UUID userId = parseUserId(jwt);
        Page<Ticket> tickets = ticketService.listTicketsForOrganizer(userId, pageable);
        return ResponseEntity.ok(tickets.map(ticketService::convertToTicketSaleResponseDto));
    }

    @GetMapping(path = "/{eventId}/tickets")
    public ResponseEntity<Page<TicketSaleResponseDto>> listTicketsForEvent(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId, Pageable pageable) {
        log.info("EventController --> listTicketsForEvent --> eventId: {}", eventId);
        UUID userId = parseUserId(jwt);
        Page<Ticket> tickets = ticketService.listTicketsForEvent(userId, eventId, pageable);
        return ResponseEntity.ok(tickets.map(ticketService::convertToTicketSaleResponseDto));
    }

    @PostMapping(path = "/{eventId}/tickets/{ticketId}/cancel")
    public ResponseEntity<CancelTicketResponseDto> cancelTicketForOrganizer(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId,
            @PathVariable UUID ticketId,
            @RequestBody(required = false) CancelTicketRequestDto cancelTicketRequestDto) {
        log.info("EventController --> cancelTicketForOrganizer --> eventId: {}, ticketId: {}", eventId, ticketId);
        UUID userId = parseUserId(jwt);
        String note = null != cancelTicketRequestDto ? cancelTicketRequestDto.getNote() : null;
        Ticket cancelledTicket = ticketService.cancelTicketForOrganizer(userId, eventId, ticketId, note);
        return ResponseEntity.ok(ticketService.convertToCancelTicketResponseDto(cancelledTicket));
    }

    // Organizer-facing raw image bytes -- works for a still-DRAFT event, unlike
    // PublishedEventController's equivalent, which only ever resolves an image
    // belonging to a PUBLISHED event. Needed so the edit page can show its own gallery's
    // thumbnails before the event (and its images) are ever publicly visible.
    @GetMapping(path = "/{eventId}/images/{imageId}")
    public ResponseEntity<byte[]> getEventImage(@AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId, @PathVariable UUID imageId) {
        log.info("EventController --> getEventImage --> eventId: {}, imageId: {}", eventId, imageId);
        UUID userId = parseUserId(jwt);
        return eventService.getEventImageForOrganizer(userId, eventId, imageId)
                .map(bytes -> ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes))
                .orElse(ResponseEntity.notFound().build());
    }

    private UUID parseUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
