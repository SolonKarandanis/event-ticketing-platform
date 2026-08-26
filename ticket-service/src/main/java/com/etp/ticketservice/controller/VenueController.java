package com.etp.ticketservice.controller;

import com.etp.ticketservice.domain.dto.request.CreateVenueRequestDto;
import com.etp.ticketservice.domain.dto.request.UpdateVenueRequestDto;
import com.etp.ticketservice.domain.dto.response.VenueResponseDto;
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.model.CreateVenueRequest;
import com.etp.ticketservice.domain.model.UpdateVenueRequest;
import com.etp.ticketservice.domain.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {

    private static final Logger log = LoggerFactory.getLogger(VenueController.class);

    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<VenueResponseDto> createVenue(
            @Valid @RequestBody CreateVenueRequestDto createVenueRequestDto) {
        log.info("VenueController --> createVenue");
        CreateVenueRequest createVenueRequest = venueService.convertFromDto(createVenueRequestDto);
        Venue createdVenue = venueService.createVenue(createVenueRequest);
        VenueResponseDto venueResponseDto = venueService.convertToVenueResponseDto(createdVenue);
        return new ResponseEntity<>(venueResponseDto, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{venueId}")
    public ResponseEntity<VenueResponseDto> updateVenue(@PathVariable UUID venueId,
            @Valid @RequestBody UpdateVenueRequestDto updateVenueRequestDto) {
        log.info("VenueController --> updateVenue");
        UpdateVenueRequest updateVenueRequest = venueService.convertFromDto(updateVenueRequestDto);
        Venue updatedVenue = venueService.updateVenue(venueId, updateVenueRequest);
        VenueResponseDto venueResponseDto = venueService.convertToVenueResponseDto(updatedVenue);
        return ResponseEntity.ok(venueResponseDto);
    }

    @GetMapping
    public ResponseEntity<Page<VenueResponseDto>> listVenues(
            @RequestParam(required = false) String q, Pageable pageable) {
        log.info("VenueController --> listVenues");
        // A blank/whitespace-only q means "browse all", same as omitting it -- matches
        // PublishedEventController's normalization for the same reason.
        String searchTerm = (null != q && !q.trim().isEmpty()) ? q : null;
        Page<Venue> venues = venueService.listVenues(searchTerm, pageable);
        return ResponseEntity.ok(venues.map(venueService::convertToVenueResponseDto));
    }

    @GetMapping(path = "/{venueId}")
    public ResponseEntity<VenueResponseDto> getVenue(@PathVariable UUID venueId) {
        log.info("VenueController --> getVenue --> id: {}", venueId);
        return venueService.getVenue(venueId)
                .map(venueService::convertToVenueResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
