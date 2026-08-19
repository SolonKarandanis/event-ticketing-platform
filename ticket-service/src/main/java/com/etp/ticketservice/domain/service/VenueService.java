package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.dto.request.CreateVenueRequestDto;
import com.etp.ticketservice.domain.dto.request.UpdateVenueRequestDto;
import com.etp.ticketservice.domain.dto.response.VenueResponseDto;
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.model.CreateVenueRequest;
import com.etp.ticketservice.domain.model.UpdateVenueRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface VenueService {
    Venue createVenue(CreateVenueRequest request);

    Venue updateVenue(UUID id, UpdateVenueRequest request);

    Page<Venue> listVenues(Pageable pageable);

    Optional<Venue> getVenue(UUID id);

    CreateVenueRequest convertFromDto(CreateVenueRequestDto dto);

    UpdateVenueRequest convertFromDto(UpdateVenueRequestDto dto);

    VenueResponseDto convertToVenueResponseDto(Venue venue);
}
