package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.dto.request.CreateEventRequestDto;
import com.etp.ticketservice.domain.dto.request.CreateTicketTypeRequestDto;
import com.etp.ticketservice.domain.dto.request.EventImageRequestDto;
import com.etp.ticketservice.domain.dto.request.UpdateEventRequestDto;
import com.etp.ticketservice.domain.dto.request.UpdateTicketTypeRequestDto;
import com.etp.ticketservice.domain.dto.response.CreateEventResponseDto;
import com.etp.ticketservice.domain.dto.response.CreateTicketTypeResponseDto;
import com.etp.ticketservice.domain.dto.response.EventImageResponseDto;
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
import com.etp.ticketservice.domain.entity.EventImage;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.entity.User;
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import com.etp.ticketservice.domain.enums.PublishedEventsSortBy;
import com.etp.ticketservice.domain.enums.TicketCancelReasonEnum;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import com.etp.ticketservice.domain.enums.TicketValidationStatusEnum;
import com.etp.ticketservice.domain.exception.ErrorCode;
import com.etp.ticketservice.domain.exception.EventImageNotFoundException;
import com.etp.ticketservice.domain.exception.EventNotFoundException;
import com.etp.ticketservice.domain.exception.EventNotPublishableException;
import com.etp.ticketservice.domain.exception.EventUpdateException;
import com.etp.ticketservice.domain.exception.InvalidEventDatesException;
import com.etp.ticketservice.domain.exception.InvalidEventImageException;
import com.etp.ticketservice.domain.exception.InvalidEventStatusTransitionException;
import com.etp.ticketservice.domain.exception.TicketTypeHasSoldTicketsException;
import com.etp.ticketservice.domain.exception.TicketTypeNotFoundException;
import com.etp.ticketservice.domain.exception.TooManyEventImagesException;
import com.etp.ticketservice.domain.exception.UserNotFoundException;
import com.etp.ticketservice.domain.exception.VenueNotFoundException;
import com.etp.ticketservice.domain.model.CreateEventRequest;
import com.etp.ticketservice.domain.model.CreateTicketTypeRequest;
import com.etp.ticketservice.domain.model.EventImageRequest;
import com.etp.ticketservice.domain.model.UpdateEventRequest;
import com.etp.ticketservice.domain.model.UpdateTicketTypeRequest;
import com.etp.ticketservice.domain.repository.EventImageRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    // An event with more than this many images is rejected -- 409, not a Bean Validation
    // 400, since it's a state/quota conflict rather than a malformed request.
    private static final int MAX_EVENT_IMAGES = 8;

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final EventImageRepository eventImageRepository;
    private final TicketEventPublisher ticketEventPublisher;
    private final EventImageService eventImageService;

    @Override
    @Transactional
    public Event createEvent(UUID organizerId, CreateEventRequest event, List<MultipartFile> newImages) {
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

        validateImageCount(event.getImages().size());

        // Every entry here is necessarily "new" -- nothing exists yet to keep -- so
        // every id is null and every newImageIndex is expected to be set. Array order in
        // event.getImages() becomes gallery position; there's no separate reorder step.
        int position = 0;
        for (EventImageRequest imageRequest : event.getImages()) {
            MultipartFile file = resolveNewImageFile(newImages, imageRequest.getNewImageIndex());
            EventImage imageToCreate = new EventImage();
            imageToCreate.setDomainId(UUID.randomUUID());
            imageToCreate.setPosition(position++);
            imageToCreate.setAltText(imageRequest.getAltText());
            eventImageService.storeImage(file, imageToCreate.getDomainId());
            eventToCreate.addImage(imageToCreate);
        }

        return eventRepository.save(eventToCreate);
    }

    // A cap enforced here, once, rather than duplicated across createEvent and
    // updateEventForOrganizer's own loops.
    private void validateImageCount(int count) {
        if (count > MAX_EVENT_IMAGES) {
            throw new TooManyEventImagesException(ErrorCode.EVENT_TOO_MANY_IMAGES, count);
        }
    }

    // JSON can't carry the actual file bytes -- an EventImageRequest with a null id
    // points at its file via newImageIndex, an index into the separate "newImages"
    // multipart parts sent alongside the JSON body. An out-of-range or missing index
    // means the request itself is inconsistent (a new-image entry with nothing to
    // upload), not a normal validation failure on a single field.
    private MultipartFile resolveNewImageFile(List<MultipartFile> newImages, Integer newImageIndex) {
        if (null == newImageIndex || null == newImages || newImageIndex < 0 || newImageIndex >= newImages.size()) {
            throw new InvalidEventImageException(ErrorCode.EVENT_IMAGE_INVALID_FILE, "newImageIndex " + newImageIndex + " has no matching file");
        }
        return newImages.get(newImageIndex);
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
    public Event updateEventForOrganizer(UUID organizerId, UUID id, UpdateEventRequest event, List<MultipartFile> newImages) {
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

        applyImageChanges(existingEvent, event.getImages(), newImages);

        return eventRepository.save(existingEvent);
    }

    // Same create/keep/delete-by-id shape as the ticket-type diffing above, plus a
    // position assignment ticketTypes doesn't need: every image in the submitted list is
    // kept (or created) at its array index, and array order IS gallery order -- there's
    // no separate reorder endpoint, resubmitting the list in a new order is a reorder.
    private void applyImageChanges(Event existingEvent, List<EventImageRequest> requestedImages, List<MultipartFile> newImages) {
        validateImageCount(requestedImages.size());

        Set<UUID> requestImageDomainIds = requestedImages.stream()
                .map(EventImageRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<EventImage> imagesToRemove = existingEvent.getImages().stream()
                .filter(existingImage -> !requestImageDomainIds.contains(existingImage.getDomainId()))
                .collect(Collectors.toSet());
        for (EventImage imageToRemove : imagesToRemove) {
            eventImageService.deleteImage(imageToRemove.getDomainId());
            existingEvent.removeImage(imageToRemove);
        }

        Map<UUID, EventImage> existingImagesIndex = existingEvent.getImages().stream()
                .collect(Collectors.toMap(EventImage::getDomainId, Function.identity()));

        int position = 0;
        for (EventImageRequest imageRequest : requestedImages) {
            if (null == imageRequest.getId()) {
                // Create
                MultipartFile file = resolveNewImageFile(newImages, imageRequest.getNewImageIndex());
                EventImage imageToCreate = new EventImage();
                imageToCreate.setDomainId(UUID.randomUUID());
                imageToCreate.setPosition(position);
                imageToCreate.setAltText(imageRequest.getAltText());
                eventImageService.storeImage(file, imageToCreate.getDomainId());
                existingEvent.addImage(imageToCreate);
            } else if (existingImagesIndex.containsKey(imageRequest.getId())) {
                // Keep -- possibly at a new position and/or with new alt text
                EventImage existingImage = existingImagesIndex.get(imageRequest.getId());
                existingImage.setPosition(position);
                existingImage.setAltText(imageRequest.getAltText());
            } else {
                throw new EventImageNotFoundException(ErrorCode.EVENT_IMAGE_NOT_FOUND, imageRequest.getId());
            }
            position++;
        }
    }

    @Override
    @Transactional
    public void deleteEventForOrganizer(UUID organizerId, UUID id) {
        // Cascade handles the EventImage rows; it doesn't touch the filesystem, so each
        // image's file is deleted explicitly here before the event itself goes.
        getEventForOrganizer(organizerId, id).ifPresent(event -> {
            event.getImages().forEach(image -> eventImageService.deleteImage(image.getDomainId()));
            eventRepository.delete(event);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<byte[]> getEventImageForOrganizer(UUID organizerId, UUID eventId, UUID imageId) {
        return eventImageRepository.findByDomainIdAndEventDomainIdAndEventOrganizerDomainId(imageId, eventId, organizerId)
                .map(image -> eventImageService.readImage(image.getDomainId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<byte[]> getPublishedEventImage(UUID eventId, UUID imageId) {
        return eventImageRepository.findByDomainIdAndEventDomainIdAndEventStatus(imageId, eventId, EventStatusEnum.PUBLISHED)
                .map(image -> eventImageService.readImage(image.getDomainId()));
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
        Event cancelledEvent = eventRepository.save(event);

        cancelTicketsForCancelledEvent(cancelledEvent);

        return cancelledEvent;
    }

    // Cascades the event's own cancellation onto every ticket sold for it -- otherwise
    // every attendee's ticket would silently sit PURCHASED for an event that no longer
    // exists. An already-validated ticket (someone was already admitted) is left alone
    // rather than erroring the whole cascade over it -- same "can't cancel after entry"
    // rule TicketServiceImpl#guardCancellable enforces for an individually-cancelled
    // ticket, just applied per-ticket here instead of failing the whole operation.
    private void cancelTicketsForCancelledEvent(Event event) {
        List<Ticket> cancellableTickets =
                ticketRepository.findByEventIdAndStatusNotWithValidations(event.getId(), TicketStatusEnum.CANCELLED);

        for (Ticket ticket : cancellableTickets) {
            boolean alreadyValidated = ticket.getValidations().stream()
                    .anyMatch(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()));
            if (alreadyValidated) {
                continue;
            }

            ticket.setStatus(TicketStatusEnum.CANCELLED);
            ticket.setCancelledAt(LocalDateTime.now());
            ticket.setCancelReason(TicketCancelReasonEnum.EVENT_CANCELLED);
            Ticket savedTicket = ticketRepository.save(ticket);
            ticketEventPublisher.publishTicketCancelled(savedTicket);
        }
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
            PublishedEventsSortBy sortBy, Pageable pageable) {
        // With no explicit date range, default to upcoming events only -- otherwise a
        // PUBLISHED event whose end has already passed but hasn't been manually marked
        // COMPLETED yet would still show up in browse results.
        LocalDateTime effectiveFrom = (null == from && null == to) ? LocalDateTime.now() : from;

        // DISTANCE only means something with an origin point -- a distance sort with no
        // coordinates falls back to SOONEST instead of sorting by distance to nowhere.
        boolean hasOrigin = null != latitude && null != longitude && null != radiusMeters;
        PublishedEventsSortBy effectiveSortBy =
                (PublishedEventsSortBy.DISTANCE == sortBy && !hasOrigin) ? PublishedEventsSortBy.SOONEST : sortBy;

        // Exhaustive over the enum's four constants -- no default branch, so the
        // compiler (not a runtime fallthrough) catches a future fifth sort option that
        // forgets to add its query variant here.
        Page<Event> page = switch (null == effectiveSortBy ? PublishedEventsSortBy.SOONEST : effectiveSortBy) {
            case PRICE_ASC -> eventRepository.findPublishedEventsSortedByPriceAsc(
                    searchTerm, city, effectiveFrom, to, minPrice, maxPrice, latitude, longitude, radiusMeters, pageable);
            case PRICE_DESC -> eventRepository.findPublishedEventsSortedByPriceDesc(
                    searchTerm, city, effectiveFrom, to, minPrice, maxPrice, latitude, longitude, radiusMeters, pageable);
            case DISTANCE -> eventRepository.findPublishedEventsSortedByDistance(
                    searchTerm, city, effectiveFrom, to, minPrice, maxPrice, latitude, longitude, radiusMeters, pageable);
            case SOONEST -> eventRepository.findPublishedEventsSortedBySoonest(
                    searchTerm, city, effectiveFrom, to, minPrice, maxPrice, latitude, longitude, radiusMeters, pageable);
        };

        // Native query -- JOIN FETCH isn't expressible here, so venue and images (the two
        // associations ListPublishedEventResponseDto needs, the latter for
        // coverImageId) are force-initialized while the session is still open, so
        // they're real objects/collections rather than lazy proxies by the time this
        // detaches and reaches the controller's DTO conversion.
        page.getContent().forEach(event -> {
            Hibernate.initialize(event.getVenue());
            Hibernate.initialize(event.getImages());
        });

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
        request.setImages(convertFromEventImageDtoList(dto.getImages()));
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
        request.setImages(convertFromEventImageDtoList(dto.getImages()));
        return request;
    }

    @Override
    public EventImageRequest convertFromDto(EventImageRequestDto dto) {
        EventImageRequest request = new EventImageRequest();
        request.setId(dto.getId());
        request.setNewImageIndex(dto.getNewImageIndex());
        request.setAltText(dto.getAltText());
        return request;
    }

    @Override
    public List<EventImageRequest> convertFromEventImageDtoList(List<EventImageRequestDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return Collections.emptyList();
        }
        return dtoList.stream()
                .map(this::convertFromDto)
                .toList();
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
        dto.setImages(convertToEventImageResponseDtoList(event.getImages()));
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
        // Active count, not the raw historical one -- a cancelled ticket freed its slot
        // back up, so "sold" here should mean the same thing it means for availability.
        dto.setTicketsSold(ticketRepository.countActiveByTicketTypeId(ticketType.getId(), TicketStatusEnum.CANCELLED));
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
        dto.setImages(convertToEventImageResponseDtoList(event.getImages()));
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
        dto.setImages(convertToEventImageResponseDtoList(event.getImages()));
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
        dto.setCoverImageId(findCoverImageId(event.getImages()));
        return dto;
    }

    private UUID findCoverImageId(Set<EventImage> images) {
        return images.stream()
                .min(Comparator.comparing(EventImage::getPosition))
                .map(EventImage::getDomainId)
                .orElse(null);
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
        dto.setImages(convertToEventImageResponseDtoList(event.getImages()));
        return dto;
    }

    @Override
    public EventImageResponseDto convertToEventImageResponseDto(EventImage image) {
        EventImageResponseDto dto = new EventImageResponseDto();
        dto.setId(image.getDomainId());
        dto.setAltText(image.getAltText());
        return dto;
    }

    @Override
    public List<EventImageResponseDto> convertToEventImageResponseDtoList(Set<EventImage> images) {
        if (CollectionUtils.isEmpty(images)) {
            return Collections.emptyList();
        }
        // images is a LinkedHashSet in insertion order, not position order -- an update
        // can change an existing image's position without changing when it was first
        // added to the set, so this sorts by position explicitly rather than trusting
        // iteration order to already match the gallery's intended display order.
        return images.stream()
                .sorted(Comparator.comparing(EventImage::getPosition))
                .map(this::convertToEventImageResponseDto)
                .toList();
    }
}
