package com.etp.ticketservice.venues;

import com.etp.ticketservice.venues.dto.CreateVenueRequestDto;
import com.etp.ticketservice.venues.dto.UpdateVenueRequestDto;
import com.etp.ticketservice.venues.dto.VenueResponseDto;
import com.etp.ticketservice.venues.model.CreateVenueRequest;
import com.etp.ticketservice.venues.model.UpdateVenueRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VenueService {
    Venue createVenue(CreateVenueRequest request);

    Venue updateVenue(UUID id, UpdateVenueRequest request);

    // searchTerm is null for a plain "list everything" call, set for the venue picker's
    // search-as-you-type.
    Page<Venue> listVenues(String searchTerm, Pageable pageable);

    Optional<Venue> getVenue(UUID id);

    CreateVenueRequest convertFromDto(CreateVenueRequestDto dto);

    UpdateVenueRequest convertFromDto(UpdateVenueRequestDto dto);

    VenueResponseDto convertToVenueResponseDto(Venue venue);
}
