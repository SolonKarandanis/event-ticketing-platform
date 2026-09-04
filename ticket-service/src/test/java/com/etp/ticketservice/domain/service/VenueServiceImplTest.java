package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.exception.VenueNotFoundException;
import com.etp.ticketservice.domain.exception.VenueUpdateException;
import com.etp.ticketservice.domain.model.CreateVenueRequest;
import com.etp.ticketservice.domain.model.UpdateVenueRequest;
import com.etp.ticketservice.domain.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Pure Mockito unit tests -- see EventServiceImplTest's class comment for why this layer
// is worth testing directly. Most of VenueServiceImpl is straight-line DTO<->entity
// mapping (not worth a dedicated test each); the guard clauses in updateVenue are the
// one part with real branching logic.
@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueServiceImpl venueService;

    private static final UUID VENUE_ID = UUID.randomUUID();

    @Test
    void updateVenue_nullRequestId_throws() {
        UpdateVenueRequest request = validUpdateRequest(null);

        assertThatThrownBy(() -> venueService.updateVenue(VENUE_ID, request))
                .isInstanceOf(VenueUpdateException.class);
    }

    @Test
    void updateVenue_idMismatch_throws() {
        UpdateVenueRequest request = validUpdateRequest(UUID.randomUUID());

        assertThatThrownBy(() -> venueService.updateVenue(VENUE_ID, request))
                .isInstanceOf(VenueUpdateException.class);
    }

    @Test
    void updateVenue_notFound_throws() {
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.updateVenue(VENUE_ID, validUpdateRequest(VENUE_ID)))
                .isInstanceOf(VenueNotFoundException.class);
    }

    @Test
    void updateVenue_happyPath_updatesFieldsAndCoordinatesTogether() {
        Venue existing = new Venue();
        existing.setDomainId(VENUE_ID);
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.of(existing));
        when(venueRepository.save(existing)).thenReturn(existing);

        UpdateVenueRequest request = validUpdateRequest(VENUE_ID);
        request.setName("Renamed Hall");
        request.setLatitude(37.9838);
        request.setLongitude(23.7275);

        Venue updated = venueService.updateVenue(VENUE_ID, request);

        assertThat(updated.getName()).isEqualTo("Renamed Hall");
        assertThat(updated.getLatitude()).isEqualTo(37.9838);
        assertThat(updated.getLongitude()).isEqualTo(23.7275);
        // setCoordinates (see Venue's own comment) derives a JTS Point from lat/long --
        // both being set is what should populate it, not either field alone.
        assertThat((Object) updated.getLocation()).isNotNull();
    }

    @Test
    void createVenue_happyPath_generatesDomainIdAndMapsAllFields() {
        when(venueRepository.save(any(Venue.class))).thenAnswer(invocation -> invocation.getArgument(0, Venue.class));

        CreateVenueRequest request = new CreateVenueRequest();
        request.setName("Main Hall");
        request.setAddressLine1("1 Main St");
        request.setCity("Athens");
        request.setPostalCode("12345");
        request.setCountry("Greece");

        Venue created = venueService.createVenue(request);

        assertThat(created.getDomainId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Main Hall");
        assertThat(created.getCity()).isEqualTo("Athens");
    }

    private UpdateVenueRequest validUpdateRequest(UUID id) {
        UpdateVenueRequest request = new UpdateVenueRequest();
        request.setId(id);
        request.setName("Main Hall");
        request.setAddressLine1("1 Main St");
        request.setCity("Athens");
        request.setPostalCode("12345");
        request.setCountry("Greece");
        return request;
    }
}
