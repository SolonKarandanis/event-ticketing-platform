package com.etp.ticketservice.events;

import com.etp.ticketservice.events.dto.CreateEventRequestDto;
import com.etp.ticketservice.tickettypes.dto.CreateTicketTypeRequestDto;
import com.etp.ticketservice.events.images.dto.EventImageRequestDto;
import com.etp.ticketservice.events.dto.UpdateEventRequestDto;
import com.etp.ticketservice.tickettypes.dto.UpdateTicketTypeRequestDto;
import com.etp.ticketservice.events.dto.CreateEventResponseDto;
import com.etp.ticketservice.tickettypes.dto.CreateTicketTypeResponseDto;
import com.etp.ticketservice.events.images.dto.EventImageResponseDto;
import com.etp.ticketservice.events.dto.GetEventDetailsResponseDto;
import com.etp.ticketservice.events.dto.GetEventDetailsTicketTypesResponseDto;
import com.etp.ticketservice.events.dto.GetPublishedEventDetailsResponseDto;
import com.etp.ticketservice.events.dto.GetPublishedEventDetailsTicketTypesResponseDto;
import com.etp.ticketservice.events.dto.ListEventResponseDto;
import com.etp.ticketservice.events.dto.ListEventTicketTypeResponseDto;
import com.etp.ticketservice.events.dto.ListPublishedEventResponseDto;
import com.etp.ticketservice.events.dto.UpdateEventResponseDto;
import com.etp.ticketservice.tickettypes.dto.UpdateTicketTypeResponseDto;
import com.etp.ticketservice.venues.dto.VenueResponseDto;
import com.etp.ticketservice.events.images.EventImage;
import com.etp.ticketservice.tickettypes.TicketType;
import com.etp.ticketservice.venues.Venue;
import com.etp.ticketservice.events.model.CreateEventRequest;
import com.etp.ticketservice.tickettypes.model.CreateTicketTypeRequest;
import com.etp.ticketservice.events.images.model.EventImageRequest;
import com.etp.ticketservice.events.model.UpdateEventRequest;
import com.etp.ticketservice.tickettypes.model.UpdateTicketTypeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface EventService {
    // newImages holds the actual file bytes for every EventImageRequest entry in
    // event.getImages() whose newImageIndex is set -- JSON can't carry binary data
    // inline, so this rides alongside the JSON-mapped request as its own parameter
    // rather than being a field on CreateEventRequest itself.
    Event createEvent(UUID organizerId, CreateEventRequest event, List<MultipartFile> newImages);

    Page<Event> listEventsForOrganizer(UUID organizerId, Pageable pageable);

    Optional<Event> getEventForOrganizer(UUID organizerId, UUID id);

    Event updateEventForOrganizer(UUID organizerId, UUID id, UpdateEventRequest event, List<MultipartFile> newImages);

    void deleteEventForOrganizer(UUID organizerId, UUID id);

    // Organizer-facing raw image bytes -- works for a still-DRAFT event, unlike
    // getPublishedEventImage below, since the caller is confirmed to own the event.
    Optional<byte[]> getEventImageForOrganizer(UUID organizerId, UUID eventId, UUID imageId);

    // Public raw image bytes -- only ever resolves an image belonging to a PUBLISHED
    // event.
    Optional<byte[]> getPublishedEventImage(UUID eventId, UUID imageId);

    Event publishEvent(UUID organizerId, UUID id);

    Event cancelEvent(UUID organizerId, UUID id);

    Event completeEvent(UUID organizerId, UUID id);

    Page<Event> findPublishedEvents(String searchTerm, LocalDateTime from, LocalDateTime to,
            Double minPrice, Double maxPrice, String city,
            Double latitude, Double longitude, Double radiusMeters,
            PublishedEventsSortBy sortBy, Pageable pageable);

    Optional<Event> getPublishedEvent(UUID id);

    List<String> findPublishedEventCities();

    CreateEventRequest convertFromDto(CreateEventRequestDto dto);

    CreateTicketTypeRequest convertFromDto(CreateTicketTypeRequestDto dto);

    UpdateTicketTypeRequest convertFromDto(UpdateTicketTypeRequestDto dto);

    List<UpdateTicketTypeRequest> convertFromDtoList(List<UpdateTicketTypeRequestDto> dtoList);

    UpdateEventRequest convertFromDto(UpdateEventRequestDto dto);

    EventImageRequest convertFromDto(EventImageRequestDto dto);

    List<EventImageRequest> convertFromEventImageDtoList(List<EventImageRequestDto> dtoList);

    VenueResponseDto convertToVenueResponseDto(Venue venue);

    CreateTicketTypeResponseDto convertToCreateTicketTypeResponseDto(TicketType ticketType);

    List<CreateTicketTypeResponseDto> convertToCreateTicketTypeResponseDtoList(Set<TicketType> ticketTypeList);

    CreateEventResponseDto convertToCreateEventResponseDto(Event event);

    ListEventTicketTypeResponseDto convertToListEventTicketTypeResponseDto(TicketType ticketType);

    List<ListEventTicketTypeResponseDto> convertToListEventTicketTypeResponseDtoList(Set<TicketType> ticketTypeList);

    ListEventResponseDto convertToListEventResponseDto(Event event);

    GetEventDetailsTicketTypesResponseDto convertToGetEventDetailsTicketTypesResponseDto(TicketType ticketType);

    List<GetEventDetailsTicketTypesResponseDto> convertToGetEventDetailsTicketTypesResponseDtoList(Set<TicketType> ticketTypeList);

    GetEventDetailsResponseDto convertToGetEventDetailsResponseDto(Event event);

    UpdateTicketTypeResponseDto convertToUpdateTicketTypeResponseDto(TicketType ticketType);

    List<UpdateTicketTypeResponseDto> convertToUpdateTicketTypeResponseDtoList(Set<TicketType> ticketTypeList);

    UpdateEventResponseDto convertToUpdateEventResponseDto(Event event);

    ListPublishedEventResponseDto convertToListPublishedEventResponseDto(Event event);

    GetPublishedEventDetailsTicketTypesResponseDto convertToGetPublishedEventDetailsTicketTypesResponseDto(TicketType ticketType);

    List<GetPublishedEventDetailsTicketTypesResponseDto> convertToGetPublishedEventDetailsTicketTypesResponseDtoList(Set<TicketType> ticketTypeList);

    GetPublishedEventDetailsResponseDto convertToGetPublishedEventDetailsResponseDto(Event event);

    EventImageResponseDto convertToEventImageResponseDto(EventImage image);

    List<EventImageResponseDto> convertToEventImageResponseDtoList(Set<EventImage> images);
}
