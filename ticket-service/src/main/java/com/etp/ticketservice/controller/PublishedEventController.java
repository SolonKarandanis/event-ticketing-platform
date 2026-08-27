package com.etp.ticketservice.controller;

import com.etp.ticketservice.domain.dto.response.GetPublishedEventDetailsResponseDto;
import com.etp.ticketservice.domain.dto.response.ListPublishedEventResponseDto;
import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/published-events")
@RequiredArgsConstructor
public class PublishedEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<ListPublishedEventResponseDto>> listPublishedEvents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, defaultValue = "soonest") String sortBy,
            Pageable pageable) {
        // A blank/whitespace-only q means "browse all", same as omitting it -- normalize
        // here so the repository's ":searchTerm IS NULL" check treats both the same way.
        String searchTerm = (null != q && !q.trim().isEmpty()) ? q : null;
        Page<Event> events = eventService.findPublishedEvents(searchTerm, from, to, minPrice, maxPrice, city, sortBy, pageable);
        return ResponseEntity.ok(
                events.map(eventService::convertToListPublishedEventResponseDto)
        );
    }

    // Backs the browse page's City filter -- distinct cities with at least one published
    // event, not every venue ever created. Public, same as the rest of this controller
    // (GET /api/v1/published-events/** is permitAll in SecurityConfig) -- unlike
    // /api/v1/venues, which is organizer-only and shouldn't be called from a page an
    // anonymous visitor can reach.
    @GetMapping(path = "/cities")
    public ResponseEntity<List<String>> listPublishedEventCities() {
        return ResponseEntity.ok(eventService.findPublishedEventCities());
    }

    @GetMapping(path = "/{eventId}")
    public ResponseEntity<GetPublishedEventDetailsResponseDto> getPublishedEventDetails(@PathVariable UUID eventId) {
        return eventService.getPublishedEvent(eventId)
                .map(eventService::convertToGetPublishedEventDetailsResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
