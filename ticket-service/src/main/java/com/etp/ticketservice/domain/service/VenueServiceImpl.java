package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.dto.request.CreateVenueRequestDto;
import com.etp.ticketservice.domain.dto.request.UpdateVenueRequestDto;
import com.etp.ticketservice.domain.dto.response.VenueResponseDto;
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.exception.VenueNotFoundException;
import com.etp.ticketservice.domain.exception.VenueUpdateException;
import com.etp.ticketservice.domain.model.CreateVenueRequest;
import com.etp.ticketservice.domain.model.UpdateVenueRequest;
import com.etp.ticketservice.domain.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    @Override
    @Transactional
    public Venue createVenue(CreateVenueRequest request) {
        Venue venueToCreate = new Venue();
        venueToCreate.setDomainId(UUID.randomUUID());
        venueToCreate.setName(request.getName());
        venueToCreate.setAddressLine1(request.getAddressLine1());
        venueToCreate.setAddressLine2(request.getAddressLine2());
        venueToCreate.setCity(request.getCity());
        venueToCreate.setPostalCode(request.getPostalCode());
        venueToCreate.setCountry(request.getCountry());
        venueToCreate.setLatitude(request.getLatitude());
        venueToCreate.setLongitude(request.getLongitude());
        venueToCreate.setCapacity(request.getCapacity());
        venueToCreate.setAccessibilityInfo(request.getAccessibilityInfo());

        return venueRepository.save(venueToCreate);
    }

    @Override
    @Transactional
    public Venue updateVenue(UUID id, UpdateVenueRequest request) {
        if (null == request.getId()) {
            throw new VenueUpdateException("Venue ID cannot be null");
        }

        if (!id.equals(request.getId())) {
            throw new VenueUpdateException("Cannot update the ID of a venue");
        }

        Venue existingVenue = venueRepository.findByDomainId(id)
                .orElseThrow(() -> new VenueNotFoundException(
                        String.format("Venue with ID '%s' not found", id))
                );

        existingVenue.setName(request.getName());
        existingVenue.setAddressLine1(request.getAddressLine1());
        existingVenue.setAddressLine2(request.getAddressLine2());
        existingVenue.setCity(request.getCity());
        existingVenue.setPostalCode(request.getPostalCode());
        existingVenue.setCountry(request.getCountry());
        existingVenue.setLatitude(request.getLatitude());
        existingVenue.setLongitude(request.getLongitude());
        existingVenue.setCapacity(request.getCapacity());
        existingVenue.setAccessibilityInfo(request.getAccessibilityInfo());

        return venueRepository.save(existingVenue);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Venue> listVenues(Pageable pageable) {
        return venueRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Venue> getVenue(UUID id) {
        return venueRepository.findByDomainId(id);
    }

    @Override
    public CreateVenueRequest convertFromDto(CreateVenueRequestDto dto) {
        CreateVenueRequest request = new CreateVenueRequest();
        request.setName(dto.getName());
        request.setAddressLine1(dto.getAddressLine1());
        request.setAddressLine2(dto.getAddressLine2());
        request.setCity(dto.getCity());
        request.setPostalCode(dto.getPostalCode());
        request.setCountry(dto.getCountry());
        request.setLatitude(dto.getLatitude());
        request.setLongitude(dto.getLongitude());
        request.setCapacity(dto.getCapacity());
        request.setAccessibilityInfo(dto.getAccessibilityInfo());
        return request;
    }

    @Override
    public UpdateVenueRequest convertFromDto(UpdateVenueRequestDto dto) {
        UpdateVenueRequest request = new UpdateVenueRequest();
        request.setId(dto.getId());
        request.setName(dto.getName());
        request.setAddressLine1(dto.getAddressLine1());
        request.setAddressLine2(dto.getAddressLine2());
        request.setCity(dto.getCity());
        request.setPostalCode(dto.getPostalCode());
        request.setCountry(dto.getCountry());
        request.setLatitude(dto.getLatitude());
        request.setLongitude(dto.getLongitude());
        request.setCapacity(dto.getCapacity());
        request.setAccessibilityInfo(dto.getAccessibilityInfo());
        return request;
    }

    @Override
    public VenueResponseDto convertToVenueResponseDto(Venue venue) {
        VenueResponseDto dto = new VenueResponseDto();
        dto.setId(venue.getDomainId());
        dto.setName(venue.getName());
        dto.setAddressLine1(venue.getAddressLine1());
        dto.setAddressLine2(venue.getAddressLine2());
        dto.setCity(venue.getCity());
        dto.setPostalCode(venue.getPostalCode());
        dto.setCountry(venue.getCountry());
        dto.setLatitude(venue.getLatitude());
        dto.setLongitude(venue.getLongitude());
        dto.setCapacity(venue.getCapacity());
        dto.setAccessibilityInfo(venue.getAccessibilityInfo());
        return dto;
    }
}
