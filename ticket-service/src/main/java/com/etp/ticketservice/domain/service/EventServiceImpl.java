package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.dto.request.CreateEventRequestDto;
import com.etp.ticketservice.domain.dto.request.CreateTicketTypeRequestDto;
import com.etp.ticketservice.domain.dto.request.UpdateEventRequestDto;
import com.etp.ticketservice.domain.dto.request.UpdateTicketTypeRequestDto;
import com.etp.ticketservice.domain.dto.response.CreateEventResponseDto;
import com.etp.ticketservice.domain.dto.response.CreateTicketTypeResponseDto;
import com.etp.ticketservice.domain.dto.response.GetEventDetailsResponseDto;
import com.etp.ticketservice.domain.dto.response.GetEventDetailsTicketTypesResponseDto;
import com.etp.ticketservice.domain.dto.response.GetPublishedEventDetailsResponseDto;
import com.etp.ticketservice.domain.dto.response.GetPublishedEventDetailsTicketTypesResponseDto;
import com.etp.ticketservice.domain.dto.response.ListEventResponseDto;
import com.etp.ticketservice.domain.dto.response.ListEventTicketTypeResponseDto;
import com.etp.ticketservice.domain.dto.response.ListPublishedEventResponseDto;
import com.etp.ticketservice.domain.dto.response.UpdateEventResponseDto;
import com.etp.ticketservice.domain.dto.response.UpdateTicketTypeResponseDto;
import com.etp.ticketservice.domain.dto.response.VenueResponseDto;
import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.entity.User;
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import com.etp.ticketservice.domain.exception.ErrorCode;
import com.etp.ticketservice.domain.exception.EventNotFoundException;
import com.etp.ticketservice.domain.exception.EventNotPublishableException;
import com.etp.ticketservice.domain.exception.EventUpdateException;
import com.etp.ticketservice.domain.exception.InvalidEventDatesException;
import com.etp.ticketservice.domain.exception.InvalidEventStatusTransitionException;
import com.etp.ticketservice.domain.exception.TicketTypeHasSoldTicketsException;
import com.etp.ticketservice.domain.exception.TicketTypeNotFoundException;
import com.etp.ticketservice.domain.exception.UserNotFoundException;
import com.etp.ticketservice.domain.exception.VenueNotFoundException;
import com.etp.ticketservice.domain.model.CreateEventRequest;
import com.etp.ticketservice.domain.model.CreateTicketTypeRequest;
import com.etp.ticketservice.domain.model.UpdateEventRequest;
import com.etp.ticketservice.domain.model.UpdateTicketTypeRequest;
import com.etp.ticketservice.domain.repository.EventRepository;
import com.etp.ticketservice.domain.repository.TicketRepository;
import com.etp.ticketservice.domain.repository.UserRepository;
import com.etp.ticketservice.domain.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional
    public Event createEvent(UUID organizerId, CreateEventRequest event) {
        User organizer = userRepository.findByDomainId(organizerId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND, organizerId));

        Venue venue = venueRepository.findByDomainId(event.getVenueId())
                .orElseThrow(() -> new VenueNotFoundException(ErrorCode.VENUE_NOT_FOUND, event.getVenueId()));

        validateEventDates(event.getStart(), event.getEnd(), event.getSalesStart(), event.getSalesEnd());

        Event eventToCreate = new Event();
        eventToCreate.setDomainId(UUID.randomUUID());
        eventToCreate.setName(event.getName());
        eventToCreate.setStart(event.getStart());
        eventToCreate.setEnd(event.getEnd());
        eventToCreate.setSalesStart(event.getSalesStart());
        eventToCreate.setSalesEnd(event.getSalesEnd());
        // New events always start as DRAFT -- publishing is a separate, explicit action
        // (see publishEvent) so a client can never create an event in a CANCELLED/COMPLETED
        // state, or skip straight to PUBLISHED without the ticket-types-required check.
        eventToCreate.setStatus(EventStatusEnum.DRAFT);

        venue.addEvent(eventToCreate);
        organizer.addEventOrganized(eventToCreate);

        event.getTicketTypes().forEach(ticketType -> {
            TicketType ticketTypeToCreate = new TicketType();
            ticketTypeToCreate.setDomainId(UUID.randomUUID());
            ticketTypeToCreate.setName(ticketType.getName());
            ticketTypeToCreate.setPrice(ticketType.getPrice());
            ticketTypeToCreate.setDescription(ticketType.getDescription());
            ticketTypeToCreate.setTotalAvailable(ticketType.getTotalAvailable());
            eventToCreate.addTicketType(ticketTypeToCreate);
        });

        return eventRepository.save(eventToCreate);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Event> listEventsForOrganizer(UUID organizerId, Pageable pageable) {
        Page<Event> page = eventRepository.findByOrganizerDomainId(organizerId, pageable);

        // ticketTypes is a @OneToMany -- fetch-joining it together with Pageable would force
        // Hibernate to paginate in memory, so it's hydrated in a second, unpaged, bounded
        // query instead, onto the same managed Event instances already in this page.
        List<Long> eventIds = page.getContent().stream().map(Event::getId).toList();
        if (!eventIds.isEmpty()) {
            eventRepository.findByIdInWithTicketTypes(eventIds);
        }

        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> getEventForOrganizer(UUID organizerId, UUID id) {
        return eventRepository.findByDomainIdAndOrganizerDomainId(id, organizerId);
    }

    @Override
    @Transactional
    public Event updateEventForOrganizer(UUID organizerId, UUID id, UpdateEventRequest event) {
        if (null == event.getId()) {
            throw new EventUpdateException(ErrorCode.EVENT_ID_REQUIRED);
        }

        if (!id.equals(event.getId())) {
            throw new EventUpdateException(ErrorCode.EVENT_ID_MISMATCH, id);
        }

        Event existingEvent = eventRepository
                .findByDomainIdAndOrganizerDomainId(id, organizerId)
                .orElseThrow(() -> new EventNotFoundException(ErrorCode.EVENT_NOT_FOUND, id));

        // CANCELLED/COMPLETED are terminal -- the event becomes a stable historical record,
        // never editable again, not just non-transitionable (see publishEvent/cancelEvent/completeEvent).
        if (EventStatusEnum.CANCELLED.equals(existingEvent.getStatus())
                || EventStatusEnum.COMPLETED.equals(existingEvent.getStatus())) {
            throw new InvalidEventStatusTransitionException(ErrorCode.EVENT_INVALID_STATUS_TRANSITION, existingEvent.getStatus());
        }

        Venue venue = venueRepository.findByDomainId(event.getVenueId())
                .orElseThrow(() -> new VenueNotFoundException(ErrorCode.VENUE_NOT_FOUND, event.getVenueId()));

        validateEventDates(event.getStart(), event.getEnd(), event.getSalesStart(), event.getSalesEnd());

        existingEvent.setName(event.getName());
        existingEvent.setStart(event.getStart());
        existingEvent.setEnd(event.getEnd());
        venue.addEvent(existingEvent);
        existingEvent.setSalesStart(event.getSalesStart());
        existingEvent.setSalesEnd(event.getSalesEnd());

        // UpdateTicketTypeRequest.id is the ticket type's domainId, not its internal id
        Set<UUID> requestTicketTypeDomainIds = event.getTicketTypes()
                .stream()
                .map(UpdateTicketTypeRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<TicketType> ticketTypesToRemove = existingEvent.getTicketTypes().stream()
                .filter(existingTicketType -> !requestTicketTypeDomainIds.contains(existingTicketType.getDomainId()))
                .collect(Collectors.toSet());

        // A ticket type that already has sold tickets can't be silently orphan-deleted --
        // removing it from the request would otherwise cascade-delete rows that back real
        // purchases, with no warning to the organizer.
        for (TicketType ticketTypeToRemove : ticketTypesToRemove) {
            if (ticketRepository.countByTicketTypeId(ticketTypeToRemove.getId()) > 0) {
                throw new TicketTypeHasSoldTicketsException(ErrorCode.TICKET_TYPE_HAS_SOLD_TICKETS, ticketTypeToRemove.getDomainId());
            }
        }
        ticketTypesToRemove.forEach(existingEvent::removeTicketType);

        Map<UUID, TicketType> existingTicketTypesIndex = existingEvent.getTicketTypes().stream()
                .collect(Collectors.toMap(TicketType::getDomainId, Function.identity()));

        for (UpdateTicketTypeRequest ticketType : event.getTicketTypes()) {
            if (null == ticketType.getId()) {
                // Create
                TicketType ticketTypeToCreate = new TicketType();
                ticketTypeToCreate.setDomainId(UUID.randomUUID());
                ticketTypeToCreate.setName(ticketType.getName());
                ticketTypeToCreate.setPrice(ticketType.getPrice());
                ticketTypeToCreate.setDescription(ticketType.getDescription());
                ticketTypeToCreate.setTotalAvailable(ticketType.getTotalAvailable());
                existingEvent.addTicketType(ticketTypeToCreate);
            } else if (existingTicketTypesIndex.containsKey(ticketType.getId())) {
                // Update
                TicketType existingTicketType = existingTicketTypesIndex.get(ticketType.getId());
                existingTicketType.setName(ticketType.getName());
                existingTicketType.setPrice(ticketType.getPrice());
                existingTicketType.setDescription(ticketType.getDescription());
                existingTicketType.setTotalAvailable(ticketType.getTotalAvailable());
            } else {
                throw new TicketTypeNotFoundException(ErrorCode.TICKET_TYPE_NOT_FOUND, ticketType.getId());
            }
        }

        return eventRepository.save(existingEvent);
    }

    @Override
    @Transactional
    public void deleteEventForOrganizer(UUID organizerId, UUID id) {
        getEventForOrganizer(organizerId, id).ifPresent(eventRepository::delete);
    }

    @Override
    @Transactional
    public Event publishEvent(UUID organizerId, UUID id) {
        Event event = eventRepository.findByDomainIdAndOrganizerDomainId(id, organizerId)
                .orElseThrow(() -> new EventNotFoundException(ErrorCode.EVENT_NOT_FOUND, id));

        if (!EventStatusEnum.DRAFT.equals(event.getStatus())) {
            throw new InvalidEventStatusTransitionException(ErrorCode.EVENT_INVALID_STATUS_TRANSITION, event.getStatus());
        }

        if (event.getTicketTypes().isEmpty()) {
            throw new EventNotPublishableException(ErrorCode.EVENT_NOT_PUBLISHABLE, id);
        }

        event.setStatus(EventStatusEnum.PUBLISHED);
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event cancelEvent(UUID organizerId, UUID id) {
        Event event = eventRepository.findByDomainIdAndOrganizerDomainId(id, organizerId)
                .orElseThrow(() -> new EventNotFoundException(ErrorCode.EVENT_NOT_FOUND, id));

        if (!EventStatusEnum.PUBLISHED.equals(event.getStatus())) {
            throw new InvalidEventStatusTransitionException(ErrorCode.EVENT_INVALID_STATUS_TRANSITION, event.getStatus());
        }

        event.setStatus(EventStatusEnum.CANCELLED);
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event completeEvent(UUID organizerId, UUID id) {
        Event event = eventRepository.findByDomainIdAndOrganizerDomainId(id, organizerId)
                .orElseThrow(() -> new EventNotFoundException(ErrorCode.EVENT_NOT_FOUND, id));

        if (!EventStatusEnum.PUBLISHED.equals(event.getStatus())) {
            throw new InvalidEventStatusTransitionException(ErrorCode.EVENT_INVALID_STATUS_TRANSITION, event.getStatus());
        }

        event.setStatus(EventStatusEnum.COMPLETED);
        return eventRepository.save(event);
    }

    // None of start/end/salesStart/salesEnd are required, so each rule only fires when both
    // of its relevant fields are actually present.
    private void validateEventDates(LocalDateTime start, LocalDateTime end, LocalDateTime salesStart, LocalDateTime salesEnd) {
        if (null != start && null != end && !end.isAfter(start)) {
            throw new InvalidEventDatesException(ErrorCode.EVENT_INVALID_DATES, "end must be after start");
        }

        if (null != salesEnd && null != start && salesEnd.isAfter(start)) {
            throw new InvalidEventDatesException(ErrorCode.EVENT_INVALID_DATES, "salesEnd must not be after start");
        }

        if (null != salesStart && null != salesEnd && !salesEnd.isAfter(salesStart)) {
            throw new InvalidEventDatesException(ErrorCode.EVENT_INVALID_DATES, "salesStart must be before salesEnd");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Event> findPublishedEvents(String searchTerm, LocalDateTime from, LocalDateTime to,
            Double minPrice, Double maxPrice, String city,
            Double latitude, Double longitude, Double radiusMeters,
            String sortBy, Pageable pageable) {
        // With no explicit date range, default to upcoming events only -- otherwise a
        // PUBLISHED event whose end has already passed but hasn't been manually marked
        // COMPLETED yet would still show up in browse results.
        LocalDateTime effectiveFrom = (null == from && null == to) ? LocalDateTime.now() : from;

        // "distance" only means something with an origin point -- a distance sort with
        // no coordinates falls through to the default (soonest) instead of sorting by
        // distance to nowhere, the same way an unrecognized sortBy value already does.
        boolean hasOrigin = null != latitude && null != longitude && null != radiusMeters;
        String effectiveSortBy = ("distance".equals(sortBy) && !hasOrigin) ? "" : sortBy;

        Page<Event> page = switch (null == effectiveSortBy ? "" : effectiveSortBy) {
            case "priceAsc" -> eventRepository.findPublishedEventsSortedByPriceAsc(
                    searchTerm, city, effectiveFrom, to, minPrice, maxPrice, latitude, longitude, radiusMeters, pageable);
            case "priceDesc" -> eventRepository.findPublishedEventsSortedByPriceDesc(
                    searchTerm, city, effectiveFrom, to, minPrice, maxPrice, latitude, longitude, radiusMeters, pageable);
            case "distance" -> eventRepository.findPublishedEventsSortedByDistance(
                    searchTerm, city, effectiveFrom, to, minPrice, maxPrice, latitude, longitude, radiusMeters, pageable);
            default -> eventRepository.findPublishedEventsSortedBySoonest(
                    searchTerm, city, effectiveFrom, to, minPrice, maxPrice, latitude, longitude, radiusMeters, pageable);
        };

        // Native query -- JOIN FETCH isn't expressible here, so venue (the only association
        // ListPublishedEventResponseDto needs) is force-initialized while the session is
        // still open, so it's a real object rather than a lazy proxy by the time this
        // detaches and reaches the controller's DTO conversion.
        page.getContent().forEach(event -> Hibernate.initialize(event.getVenue()));

        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> getPublishedEvent(UUID id) {
        return eventRepository.findByDomainIdAndStatus(id, EventStatusEnum.PUBLISHED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findPublishedEventCities() {
        return eventRepository.findDistinctPublishedEventCities();
    }

    @Override
    public CreateEventRequest convertFromDto(CreateEventRequestDto dto) {
        CreateEventRequest request = new CreateEventRequest();
        request.setName(dto.getName());
        request.setStart(dto.getStart());
        request.setEnd(dto.getEnd());
        request.setVenueId(dto.getVenueId());
        request.setSalesStart(dto.getSalesStart());
        request.setSalesEnd(dto.getSalesEnd());
        request.setTicketTypes(dto.getTicketTypes().stream()
                .map(this::convertFromDto)
                .toList());
        return request;
    }

    @Override
    public CreateTicketTypeRequest convertFromDto(CreateTicketTypeRequestDto dto) {
        CreateTicketTypeRequest request = new CreateTicketTypeRequest();
        request.setName(dto.getName());
        request.setPrice(dto.getPrice());
        request.setDescription(dto.getDescription());
        request.setTotalAvailable(dto.getTotalAvailable());
        return request;
    }

    @Override
    public UpdateTicketTypeRequest convertFromDto(UpdateTicketTypeRequestDto dto) {
        UpdateTicketTypeRequest request = new UpdateTicketTypeRequest();
        request.setId(dto.getId());
        request.setName(dto.getName());
        request.setPrice(dto.getPrice());
        request.setDescription(dto.getDescription());
        request.setTotalAvailable(dto.getTotalAvailable());
        return request;
    }

    @Override
    public List<UpdateTicketTypeRequest> convertFromDtoList(List<UpdateTicketTypeRequestDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return Collections.emptyList();
        }
        return dtoList.stream()
                .map(this::convertFromDto)
                .toList();
    }

    @Override
    public UpdateEventRequest convertFromDto(UpdateEventRequestDto dto) {
        UpdateEventRequest request = new UpdateEventRequest();
        request.setId(dto.getId());
        request.setName(dto.getName());
        request.setStart(dto.getStart());
        request.setEnd(dto.getEnd());
        request.setVenueId(dto.getVenueId());
        request.setSalesStart(dto.getSalesStart());
        request.setSalesEnd(dto.getSalesEnd());
        request.setTicketTypes(convertFromDtoList(dto.getTicketTypes()));
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

    @Override
    public CreateTicketTypeResponseDto convertToCreateTicketTypeResponseDto(TicketType ticketType) {
        CreateTicketTypeResponseDto dto = new CreateTicketTypeResponseDto();
        dto.setId(ticketType.getDomainId());
        dto.setName(ticketType.getName());
        dto.setPrice(ticketType.getPrice());
        dto.setDescription(ticketType.getDescription());
        dto.setTotalAvailable(ticketType.getTotalAvailable());
        return dto;
    }

    @Override
    public List<CreateTicketTypeResponseDto> convertToCreateTicketTypeResponseDtoList(Set<TicketType> ticketTypeList) {
        if (CollectionUtils.isEmpty(ticketTypeList)) {
            return Collections.emptyList();
        }
        return ticketTypeList.stream()
                .map(this::convertToCreateTicketTypeResponseDto)
                .toList();
    }

    @Override
    public CreateEventResponseDto convertToCreateEventResponseDto(Event event) {
        CreateEventResponseDto dto = new CreateEventResponseDto();
        dto.setId(event.getDomainId());
        dto.setName(event.getName());
        dto.setStart(event.getStart());
        dto.setEnd(event.getEnd());
        dto.setVenue(convertToVenueResponseDto(event.getVenue()));
        dto.setSalesStart(event.getSalesStart());
        dto.setSalesEnd(event.getSalesEnd());
        dto.setStatus(event.getStatus());
        dto.setTicketTypes(convertToCreateTicketTypeResponseDtoList(event.getTicketTypes()));
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        return dto;
    }

    @Override
    public ListEventTicketTypeResponseDto convertToListEventTicketTypeResponseDto(TicketType ticketType) {
        ListEventTicketTypeResponseDto dto = new ListEventTicketTypeResponseDto();
        dto.setId(ticketType.getDomainId());
        dto.setName(ticketType.getName());
        dto.setPrice(ticketType.getPrice());
        dto.setDescription(ticketType.getDescription());
        dto.setTotalAvailable(ticketType.getTotalAvailable());
        return dto;
    }

    @Override
    public List<ListEventTicketTypeResponseDto> convertToListEventTicketTypeResponseDtoList(Set<TicketType> ticketTypeList) {
        if (CollectionUtils.isEmpty(ticketTypeList)) {
            return Collections.emptyList();
        }
        return ticketTypeList.stream()
                .map(this::convertToListEventTicketTypeResponseDto)
                .toList();
    }

    @Override
    public ListEventResponseDto convertToListEventResponseDto(Event event) {
        ListEventResponseDto dto = new ListEventResponseDto();
        dto.setId(event.getDomainId());
        dto.setName(event.getName());
        dto.setStart(event.getStart());
        dto.setEnd(event.getEnd());
        dto.setVenue(convertToVenueResponseDto(event.getVenue()));
        dto.setSalesStart(event.getSalesStart());
        dto.setSalesEnd(event.getSalesEnd());
        dto.setStatus(event.getStatus());
        dto.setTicketTypes(convertToListEventTicketTypeResponseDtoList(event.getTicketTypes()));
        return dto;
    }

    @Override
    public GetEventDetailsTicketTypesResponseDto convertToGetEventDetailsTicketTypesResponseDto(TicketType ticketType) {
        GetEventDetailsTicketTypesResponseDto dto = new GetEventDetailsTicketTypesResponseDto();
        dto.setId(ticketType.getDomainId());
        dto.setName(ticketType.getName());
        dto.setPrice(ticketType.getPrice());
        dto.setDescription(ticketType.getDescription());
        dto.setTotalAvailable(ticketType.getTotalAvailable());
        dto.setTicketsSold(ticketRepository.countByTicketTypeId(ticketType.getId()));
        dto.setCreatedAt(ticketType.getCreatedAt());
        dto.setUpdatedAt(ticketType.getUpdatedAt());
        return dto;
    }

    @Override
    public List<GetEventDetailsTicketTypesResponseDto> convertToGetEventDetailsTicketTypesResponseDtoList(Set<TicketType> ticketTypeList) {
        if (CollectionUtils.isEmpty(ticketTypeList)) {
            return Collections.emptyList();
        }
        return ticketTypeList.stream()
                .map(this::convertToGetEventDetailsTicketTypesResponseDto)
                .toList();
    }

    @Override
    public GetEventDetailsResponseDto convertToGetEventDetailsResponseDto(Event event) {
        GetEventDetailsResponseDto dto = new GetEventDetailsResponseDto();
        dto.setId(event.getDomainId());
        dto.setName(event.getName());
        dto.setStart(event.getStart());
        dto.setEnd(event.getEnd());
        dto.setVenue(convertToVenueResponseDto(event.getVenue()));
        dto.setSalesStart(event.getSalesStart());
        dto.setSalesEnd(event.getSalesEnd());
        dto.setStatus(event.getStatus());
        dto.setTicketTypes(convertToGetEventDetailsTicketTypesResponseDtoList(event.getTicketTypes()));
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        return dto;
    }

    @Override
    public UpdateTicketTypeResponseDto convertToUpdateTicketTypeResponseDto(TicketType ticketType) {
        UpdateTicketTypeResponseDto dto = new UpdateTicketTypeResponseDto();
        dto.setId(ticketType.getDomainId());
        dto.setName(ticketType.getName());
        dto.setPrice(ticketType.getPrice());
        dto.setDescription(ticketType.getDescription());
        dto.setTotalAvailable(ticketType.getTotalAvailable());
        dto.setCreatedAt(ticketType.getCreatedAt());
        dto.setUpdatedAt(ticketType.getUpdatedAt());
        return dto;
    }

    @Override
    public List<UpdateTicketTypeResponseDto> convertToUpdateTicketTypeResponseDtoList(Set<TicketType> ticketTypeList) {
        if (CollectionUtils.isEmpty(ticketTypeList)) {
            return Collections.emptyList();
        }
        return ticketTypeList.stream()
                .map(this::convertToUpdateTicketTypeResponseDto)
                .toList();
    }

    @Override
    public UpdateEventResponseDto convertToUpdateEventResponseDto(Event event) {
        UpdateEventResponseDto dto = new UpdateEventResponseDto();
        dto.setId(event.getDomainId());
        dto.setName(event.getName());
        dto.setStart(event.getStart());
        dto.setEnd(event.getEnd());
        dto.setVenue(convertToVenueResponseDto(event.getVenue()));
        dto.setSalesStart(event.getSalesStart());
        dto.setSalesEnd(event.getSalesEnd());
        dto.setStatus(event.getStatus());
        dto.setTicketTypes(convertToUpdateTicketTypeResponseDtoList(event.getTicketTypes()));
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        return dto;
    }

    @Override
    public ListPublishedEventResponseDto convertToListPublishedEventResponseDto(Event event) {
        ListPublishedEventResponseDto dto = new ListPublishedEventResponseDto();
        dto.setId(event.getDomainId());
        dto.setName(event.getName());
        dto.setStart(event.getStart());
        dto.setEnd(event.getEnd());
        dto.setVenue(convertToVenueResponseDto(event.getVenue()));
        return dto;
    }

    @Override
    public GetPublishedEventDetailsTicketTypesResponseDto convertToGetPublishedEventDetailsTicketTypesResponseDto(TicketType ticketType) {
        GetPublishedEventDetailsTicketTypesResponseDto dto = new GetPublishedEventDetailsTicketTypesResponseDto();
        dto.setId(ticketType.getDomainId());
        dto.setName(ticketType.getName());
        dto.setPrice(ticketType.getPrice());
        dto.setDescription(ticketType.getDescription());
        return dto;
    }

    @Override
    public List<GetPublishedEventDetailsTicketTypesResponseDto> convertToGetPublishedEventDetailsTicketTypesResponseDtoList(Set<TicketType> ticketTypeList) {
        if (CollectionUtils.isEmpty(ticketTypeList)) {
            return Collections.emptyList();
        }
        return ticketTypeList.stream()
                .map(this::convertToGetPublishedEventDetailsTicketTypesResponseDto)
                .toList();
    }

    @Override
    public GetPublishedEventDetailsResponseDto convertToGetPublishedEventDetailsResponseDto(Event event) {
        GetPublishedEventDetailsResponseDto dto = new GetPublishedEventDetailsResponseDto();
        dto.setId(event.getDomainId());
        dto.setName(event.getName());
        dto.setStart(event.getStart());
        dto.setEnd(event.getEnd());
        dto.setVenue(convertToVenueResponseDto(event.getVenue()));
        dto.setTicketTypes(convertToGetPublishedEventDetailsTicketTypesResponseDtoList(event.getTicketTypes()));
        return dto;
    }
}
