package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.entity.EventImage;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.entity.TicketValidation;
import com.etp.ticketservice.domain.entity.User;
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import com.etp.ticketservice.domain.enums.PublishedEventsSortBy;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import com.etp.ticketservice.domain.enums.TicketValidationStatusEnum;
import com.etp.ticketservice.domain.exception.EventImageNotFoundException;
import com.etp.ticketservice.domain.exception.EventNotFoundException;
import com.etp.ticketservice.domain.exception.EventNotPublishableException;
import com.etp.ticketservice.domain.exception.EventTicketException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pure Mockito unit tests -- no Spring context, no database, every collaborator (the
// repositories, EventImageService, TicketEventPublisher) is mocked. This is deliberately
// the layer EventControllerTest and EventRepositoryTest don't cover: the controller
// slice mocks EventService itself out entirely, and the repository slice only exercises
// the query methods, not the branching logic (status-transition guards, the ticket-type
// and image create/keep/delete diffing, date validation) that lives here in
// EventServiceImpl. eventRepository.save(...) is stubbed to return whatever it was
// given, matching real JPA save() semantics for an already-managed/about-to-be-managed
// entity closely enough for these tests' purposes.
@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VenueRepository venueRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private EventImageRepository eventImageRepository;
    @Mock
    private TicketEventPublisher ticketEventPublisher;
    @Mock
    private EventImageService eventImageService;

    @InjectMocks
    private EventServiceImpl eventService;

    private static final UUID ORGANIZER_ID = UUID.randomUUID();
    private static final UUID VENUE_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();

    private User organizer;
    private Venue venue;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setDomainId(ORGANIZER_ID);

        venue = new Venue();
        venue.setDomainId(VENUE_ID);
        venue.setName("Main Hall");
    }

    // ---- createEvent ----

    @Test
    void createEvent_organizerNotFound_throws() {
        when(userRepository.findByDomainId(ORGANIZER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEvent(ORGANIZER_ID, validCreateEventRequest(), List.of()))
                .isInstanceOf(UserNotFoundException.class);

        verify(venueRepository, never()).findByDomainId(any());
    }

    @Test
    void createEvent_venueNotFound_throws() {
        when(userRepository.findByDomainId(ORGANIZER_ID)).thenReturn(Optional.of(organizer));
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEvent(ORGANIZER_ID, validCreateEventRequest(), List.of()))
                .isInstanceOf(VenueNotFoundException.class);
    }

    @Test
    void createEvent_endBeforeStart_throwsInvalidDates() {
        when(userRepository.findByDomainId(ORGANIZER_ID)).thenReturn(Optional.of(organizer));
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.of(venue));

        CreateEventRequest request = validCreateEventRequest();
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        request.setStart(start);
        request.setEnd(start.minusHours(1));

        assertThatThrownBy(() -> eventService.createEvent(ORGANIZER_ID, request, List.of()))
                .isInstanceOf(InvalidEventDatesException.class);
    }

    @Test
    void createEvent_moreThanEightImages_throwsTooManyImages() {
        when(userRepository.findByDomainId(ORGANIZER_ID)).thenReturn(Optional.of(organizer));
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.of(venue));

        CreateEventRequest request = validCreateEventRequest();
        request.setImages(List.of(
                imageRequest(null, 0), imageRequest(null, 1), imageRequest(null, 2), imageRequest(null, 3),
                imageRequest(null, 4), imageRequest(null, 5), imageRequest(null, 6), imageRequest(null, 7),
                imageRequest(null, 8)));

        assertThatThrownBy(() -> eventService.createEvent(ORGANIZER_ID, request,
                List.of(new MockMultipartFile("f", new byte[0]))))
                .isInstanceOf(TooManyEventImagesException.class);
    }

    @Test
    void createEvent_newImageIndexWithNoMatchingFile_throwsInvalidImage() {
        when(userRepository.findByDomainId(ORGANIZER_ID)).thenReturn(Optional.of(organizer));
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.of(venue));

        CreateEventRequest request = validCreateEventRequest();
        request.setImages(List.of(imageRequest(null, 0)));

        assertThatThrownBy(() -> eventService.createEvent(ORGANIZER_ID, request, List.of()))
                .isInstanceOf(InvalidEventImageException.class);
    }

    @Test
    void createEvent_happyPath_startsAsDraftWithTicketTypesAndImages() {
        when(userRepository.findByDomainId(ORGANIZER_ID)).thenReturn(Optional.of(organizer));
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateEventRequest request = validCreateEventRequest();
        request.setImages(List.of(imageRequest(null, 0)));
        MockMultipartFile imageFile = new MockMultipartFile("newImages", "cover.jpg", "image/jpeg", "bytes".getBytes());

        Event created = eventService.createEvent(ORGANIZER_ID, request, List.of(imageFile));

        assertThat(created.getStatus()).isEqualTo(EventStatusEnum.DRAFT);
        assertThat(created.getVenue()).isEqualTo(venue);
        assertThat(created.getOrganizer()).isEqualTo(organizer);
        assertThat(created.getTicketTypes()).hasSize(1);
        assertThat(created.getTicketTypes().iterator().next().getName()).isEqualTo("General");
        assertThat(created.getImages()).hasSize(1);
        verify(eventImageService).storeImage(eq(imageFile), any(UUID.class));
    }

    // ---- updateEventForOrganizer ----

    @Test
    void updateEventForOrganizer_nullRequestId_throws() {
        UpdateEventRequest request = validUpdateEventRequest(EVENT_ID);
        request.setId(null);

        assertThatThrownBy(() -> eventService.updateEventForOrganizer(ORGANIZER_ID, EVENT_ID, request, List.of()))
                .isInstanceOf(EventUpdateException.class);
    }

    @Test
    void updateEventForOrganizer_idMismatch_throws() {
        UpdateEventRequest request = validUpdateEventRequest(UUID.randomUUID());

        assertThatThrownBy(() -> eventService.updateEventForOrganizer(ORGANIZER_ID, EVENT_ID, request, List.of()))
                .isInstanceOf(EventUpdateException.class);
    }

    @Test
    void updateEventForOrganizer_notFound_throws() {
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEventForOrganizer(
                ORGANIZER_ID, EVENT_ID, validUpdateEventRequest(EVENT_ID), List.of()))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void updateEventForOrganizer_terminalStatus_throwsInvalidTransition() {
        Event existingEvent = draftEvent();
        existingEvent.setStatus(EventStatusEnum.COMPLETED);
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(existingEvent));

        assertThatThrownBy(() -> eventService.updateEventForOrganizer(
                ORGANIZER_ID, EVENT_ID, validUpdateEventRequest(EVENT_ID), List.of()))
                .isInstanceOf(InvalidEventStatusTransitionException.class);

        verify(venueRepository, never()).findByDomainId(any());
    }

    @Test
    void updateEventForOrganizer_removingTicketTypeWithSoldTickets_throws() {
        Event existingEvent = draftEvent();
        TicketType soldTicketType = ticketType(existingEvent, 100L, "General", 10.0);
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(existingEvent));
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.of(venue));
        when(ticketRepository.countByTicketTypeId(100L)).thenReturn(1);

        // The request omits soldTicketType entirely -- an implicit removal.
        UpdateEventRequest request = validUpdateEventRequest(EVENT_ID);
        request.setTicketTypes(List.of());

        assertThatThrownBy(() -> eventService.updateEventForOrganizer(ORGANIZER_ID, EVENT_ID, request, List.of()))
                .isInstanceOf(TicketTypeHasSoldTicketsException.class);

        assertThat(existingEvent.getTicketTypes()).contains(soldTicketType);
    }

    @Test
    void updateEventForOrganizer_referencesUnknownTicketTypeId_throws() {
        Event existingEvent = draftEvent();
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(existingEvent));
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.of(venue));

        UpdateEventRequest request = validUpdateEventRequest(EVENT_ID);
        UpdateTicketTypeRequest unknownTicketType = new UpdateTicketTypeRequest();
        unknownTicketType.setId(UUID.randomUUID());
        unknownTicketType.setName("Ghost");
        unknownTicketType.setPrice(5.0);
        request.setTicketTypes(List.of(unknownTicketType));

        assertThatThrownBy(() -> eventService.updateEventForOrganizer(ORGANIZER_ID, EVENT_ID, request, List.of()))
                .isInstanceOf(TicketTypeNotFoundException.class);
    }

    @Test
    void updateEventForOrganizer_createsUpdatesAndRemovesTicketTypesInOnePass() {
        Event existingEvent = draftEvent();
        TicketType toKeep = ticketType(existingEvent, 100L, "General", 10.0);
        TicketType toRemove = ticketType(existingEvent, 101L, "VIP", 50.0);
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(existingEvent));
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.of(venue));
        when(ticketRepository.countByTicketTypeId(101L)).thenReturn(0);
        when(eventRepository.save(existingEvent)).thenReturn(existingEvent);

        UpdateEventRequest request = validUpdateEventRequest(EVENT_ID);
        UpdateTicketTypeRequest keepRequest = new UpdateTicketTypeRequest();
        keepRequest.setId(toKeep.getDomainId());
        keepRequest.setName("General (renamed)");
        keepRequest.setPrice(12.0);
        UpdateTicketTypeRequest createRequest = new UpdateTicketTypeRequest();
        createRequest.setName("Early Bird");
        createRequest.setPrice(8.0);
        request.setTicketTypes(List.of(keepRequest, createRequest));

        Event updated = eventService.updateEventForOrganizer(ORGANIZER_ID, EVENT_ID, request, List.of());

        assertThat(updated.getTicketTypes()).hasSize(2);
        assertThat(updated.getTicketTypes()).extracting(TicketType::getName)
                .containsExactlyInAnyOrder("General (renamed)", "Early Bird");
        assertThat(updated.getTicketTypes()).doesNotContain(toRemove);
        assertThat(toKeep.getPrice()).isEqualTo(12.0);
    }

    @Test
    void updateEventForOrganizer_imageIdNotOnEvent_throwsImageNotFound() {
        Event existingEvent = draftEvent();
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(existingEvent));
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.of(venue));

        UpdateEventRequest request = validUpdateEventRequest(EVENT_ID);
        request.setTicketTypes(List.of());
        request.setImages(List.of(imageRequest(UUID.randomUUID(), null)));

        assertThatThrownBy(() -> eventService.updateEventForOrganizer(ORGANIZER_ID, EVENT_ID, request, List.of()))
                .isInstanceOf(EventImageNotFoundException.class);
    }

    @Test
    void updateEventForOrganizer_dropsImageNotInRequest_andDeletesItsFile() {
        Event existingEvent = draftEvent();
        EventImage existingImage = new EventImage();
        existingImage.setDomainId(UUID.randomUUID());
        existingImage.setPosition(0);
        existingEvent.addImage(existingImage);
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(existingEvent));
        when(venueRepository.findByDomainId(VENUE_ID)).thenReturn(Optional.of(venue));
        when(eventRepository.save(existingEvent)).thenReturn(existingEvent);

        UpdateEventRequest request = validUpdateEventRequest(EVENT_ID);
        request.setTicketTypes(List.of());
        request.setImages(List.of());

        Event updated = eventService.updateEventForOrganizer(ORGANIZER_ID, EVENT_ID, request, List.of());

        assertThat(updated.getImages()).isEmpty();
        verify(eventImageService).deleteImage(existingImage.getDomainId());
    }

    // ---- publishEvent ----

    @Test
    void publishEvent_notDraft_throws() {
        Event event = draftEvent();
        event.setStatus(EventStatusEnum.PUBLISHED);
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.publishEvent(ORGANIZER_ID, EVENT_ID))
                .isInstanceOf(InvalidEventStatusTransitionException.class);
    }

    @Test
    void publishEvent_noTicketTypes_throwsNotPublishable() {
        Event event = draftEvent();
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.publishEvent(ORGANIZER_ID, EVENT_ID))
                .isInstanceOf(EventNotPublishableException.class);
    }

    @Test
    void publishEvent_withTicketTypes_transitionsToPublished() {
        Event event = draftEvent();
        ticketType(event, 100L, "General", 10.0);
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);

        Event published = eventService.publishEvent(ORGANIZER_ID, EVENT_ID);

        assertThat(published.getStatus()).isEqualTo(EventStatusEnum.PUBLISHED);
    }

    // ---- cancelEvent ----

    @Test
    void cancelEvent_notPublished_throws() {
        Event event = draftEvent();
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.cancelEvent(ORGANIZER_ID, EVENT_ID))
                .isInstanceOf(InvalidEventStatusTransitionException.class);
    }

    @Test
    void cancelEvent_cascadesToUnvalidatedTickets_andSkipsAlreadyValidatedOnes() {
        Event event = draftEvent();
        event.setStatus(EventStatusEnum.PUBLISHED);
        event.setId(500L);
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);

        Ticket unvalidatedTicket = new Ticket();
        unvalidatedTicket.setDomainId(UUID.randomUUID());
        unvalidatedTicket.setStatus(TicketStatusEnum.PURCHASED);

        Ticket validatedTicket = new Ticket();
        validatedTicket.setDomainId(UUID.randomUUID());
        validatedTicket.setStatus(TicketStatusEnum.PURCHASED);
        TicketValidation validation = new TicketValidation();
        validation.setStatus(TicketValidationStatusEnum.VALID);
        validatedTicket.addValidation(validation);

        when(ticketRepository.findByEventIdAndStatusNotWithValidations(500L, TicketStatusEnum.CANCELLED))
                .thenReturn(List.of(unvalidatedTicket, validatedTicket));
        when(ticketRepository.save(unvalidatedTicket)).thenReturn(unvalidatedTicket);

        eventService.cancelEvent(ORGANIZER_ID, EVENT_ID);

        assertThat(event.getStatus()).isEqualTo(EventStatusEnum.CANCELLED);
        assertThat(unvalidatedTicket.getStatus()).isEqualTo(TicketStatusEnum.CANCELLED);
        assertThat(validatedTicket.getStatus()).isEqualTo(TicketStatusEnum.PURCHASED);
        verify(ticketRepository, never()).save(validatedTicket);
        verify(ticketEventPublisher).publishTicketCancelled(unvalidatedTicket);
        verify(ticketEventPublisher, never()).publishTicketCancelled(validatedTicket);
    }

    // ---- completeEvent ----

    @Test
    void completeEvent_notPublished_throws() {
        Event event = draftEvent();
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.completeEvent(ORGANIZER_ID, EVENT_ID))
                .isInstanceOf(InvalidEventStatusTransitionException.class);
    }

    @Test
    void completeEvent_published_transitionsToCompleted() {
        Event event = draftEvent();
        event.setStatus(EventStatusEnum.PUBLISHED);
        when(eventRepository.findByDomainIdAndOrganizerDomainId(EVENT_ID, ORGANIZER_ID)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);

        Event completed = eventService.completeEvent(ORGANIZER_ID, EVENT_ID);

        assertThat(completed.getStatus()).isEqualTo(EventStatusEnum.COMPLETED);
    }

    // ---- findPublishedEvents ----

    @Test
    void findPublishedEvents_distanceSortWithNoOrigin_fallsBackToSoonest() {
        when(eventRepository.findPublishedEventsSortedBySoonest(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        eventService.findPublishedEvents(null, null, null, null, null, null, null, null, null,
                PublishedEventsSortBy.DISTANCE, PageRequest.of(0, 10));

        verify(eventRepository).findPublishedEventsSortedBySoonest(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(eventRepository, never()).findPublishedEventsSortedByDistance(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void findPublishedEvents_priceAscSort_dispatchesToPriceAscQuery() {
        when(eventRepository.findPublishedEventsSortedByPriceAsc(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        eventService.findPublishedEvents(null, null, null, null, null, null, null, null, null,
                PublishedEventsSortBy.PRICE_ASC, PageRequest.of(0, 10));

        verify(eventRepository).findPublishedEventsSortedByPriceAsc(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void findPublishedEvents_withNoExplicitDateRange_defaultsFromToNow() {
        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(eventRepository.findPublishedEventsSortedBySoonest(
                any(), any(), fromCaptor.capture(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        LocalDateTime before = LocalDateTime.now();
        eventService.findPublishedEvents(null, null, null, null, null, null, null, null, null,
                PublishedEventsSortBy.SOONEST, PageRequest.of(0, 10));
        LocalDateTime after = LocalDateTime.now();

        assertThat(fromCaptor.getValue()).isBetween(before, after);
    }

    // ---- DTO conversion behavior worth its own test ----

    @Test
    void convertToEventImageResponseDtoList_sortsByPositionNotInsertionOrder() {
        EventImage second = new EventImage();
        second.setDomainId(UUID.randomUUID());
        second.setPosition(1);
        second.setAltText("Second");
        EventImage first = new EventImage();
        first.setDomainId(UUID.randomUUID());
        first.setPosition(0);
        first.setAltText("First");

        // Inserted out of position order -- LinkedHashSet iteration order would put
        // "second" first if the conversion trusted insertion order instead of sorting.
        var result = eventService.convertToEventImageResponseDtoList(new java.util.LinkedHashSet<>(Set.of(second, first)));

        assertThat(result).extracting(dto -> dto.getAltText()).containsExactly("First", "Second");
    }

    @Test
    void convertToListPublishedEventResponseDto_usesLowestPositionImageAsCover() {
        Event event = draftEvent();
        EventImage cover = new EventImage();
        cover.setDomainId(UUID.randomUUID());
        cover.setPosition(0);
        EventImage other = new EventImage();
        other.setDomainId(UUID.randomUUID());
        other.setPosition(1);
        event.addImage(other);
        event.addImage(cover);

        var dto = eventService.convertToListPublishedEventResponseDto(event);

        assertThat(dto.getCoverImageId()).isEqualTo(cover.getDomainId());
    }

    // ---- fixtures ----

    private Event draftEvent() {
        Event event = new Event();
        event.setDomainId(EVENT_ID);
        event.setName("Existing Event");
        event.setStatus(EventStatusEnum.DRAFT);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        return event;
    }

    private TicketType ticketType(Event event, long id, String name, double price) {
        TicketType ticketType = new TicketType();
        ticketType.setId(id);
        ticketType.setDomainId(UUID.randomUUID());
        ticketType.setName(name);
        ticketType.setPrice(price);
        event.addTicketType(ticketType);
        return ticketType;
    }

    private CreateEventRequest validCreateEventRequest() {
        CreateTicketTypeRequest ticketType = new CreateTicketTypeRequest();
        ticketType.setName("General");
        ticketType.setPrice(10.0);

        CreateEventRequest request = new CreateEventRequest();
        request.setName("New Event");
        request.setVenueId(VENUE_ID);
        request.setTicketTypes(List.of(ticketType));
        return request;
    }

    private UpdateEventRequest validUpdateEventRequest(UUID id) {
        UpdateEventRequest request = new UpdateEventRequest();
        request.setId(id);
        request.setName("Updated Event");
        request.setVenueId(VENUE_ID);
        request.setTicketTypes(List.of());
        return request;
    }

    private EventImageRequest imageRequest(UUID id, Integer newImageIndex) {
        EventImageRequest request = new EventImageRequest();
        request.setId(id);
        request.setNewImageIndex(newImageIndex);
        return request;
    }
}
