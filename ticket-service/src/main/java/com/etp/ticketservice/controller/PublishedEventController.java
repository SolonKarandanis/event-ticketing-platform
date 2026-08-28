package com.etp.ticketservice.controller;

import com.etp.ticketservice.domain.dto.request.ListPublishedEventsRequestDto;
import com.etp.ticketservice.domain.dto.request.Paging;
import com.etp.ticketservice.domain.dto.response.GetPublishedEventDetailsResponseDto;
import com.etp.ticketservice.domain.dto.response.ListPublishedEventResponseDto;
import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.enums.PublishedEventsSortBy;
import com.etp.ticketservice.domain.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/published-events")
@RequiredArgsConstructor
public class PublishedEventController {

    private final EventService eventService;

    // Paging now rides in the request body too (SearchRequestDTO.paging), not a
    // Pageable method param -- page/limit map onto Spring's zero-based PageRequest
    // directly. sortField (paging.getSortingColumn()) carries the same
    // "soonest"/"priceAsc"/"priceDesc"/"distance" values the old standalone sortBy
    // field did; sortOrder is unused here, since each of those is already a named,
    // fixed-direction sort rather than a generic column+direction pair. The raw wire
    // string is converted to PublishedEventsSortBy right here, at the API boundary --
    // EventService takes the enum, not a String, so an unrecognized value can't
    // propagate any deeper than this one conversion.
    @PostMapping(path = "/search")
    public ResponseEntity<Page<ListPublishedEventResponseDto>> listPublishedEvents(
            @RequestBody(required = false) ListPublishedEventsRequestDto request) {
        ListPublishedEventsRequestDto criteria = (null != request) ? request : new ListPublishedEventsRequestDto();
        Paging paging = criteria.getPaging();
        Pageable pageable = PageRequest.of(paging.getPagingStart(), paging.getPagingSize());
        PublishedEventsSortBy sortBy = PublishedEventsSortBy.fromWireValue(paging.getSortingColumn());

        // A blank/whitespace-only q means "browse all", same as omitting it -- normalize
        // here so the repository's ":searchTerm IS NULL" check treats both the same way.
        String q = criteria.getQ();
        String searchTerm = (null != q && !q.trim().isEmpty()) ? q : null;
        Page<Event> events = eventService.findPublishedEvents(
                searchTerm, criteria.getFrom(), criteria.getTo(), criteria.getMinPrice(), criteria.getMaxPrice(),
                criteria.getCity(), criteria.getLatitude(), criteria.getLongitude(), criteria.getRadiusMeters(),
                sortBy, pageable);
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
