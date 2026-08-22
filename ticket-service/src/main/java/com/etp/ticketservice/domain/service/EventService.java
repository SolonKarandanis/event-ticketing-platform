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
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.model.CreateEventRequest;
import com.etp.ticketservice.domain.model.CreateTicketTypeRequest;
import com.etp.ticketservice.domain.model.UpdateEventRequest;
import com.etp.ticketservice.domain.model.UpdateTicketTypeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface EventService {
    Event createEvent(UUID organizerId, CreateEventRequest event);

    Page<Event> listEventsForOrganizer(UUID organizerId, Pageable pageable);

    Optional<Event> getEventForOrganizer(UUID organizerId, UUID id);

    Event updateEventForOrganizer(UUID organizerId, UUID id, UpdateEventRequest event);

    void deleteEventForOrganizer(UUID organizerId, UUID id);

    Event publishEvent(UUID organizerId, UUID id);

    Event cancelEvent(UUID organizerId, UUID id);

    Event completeEvent(UUID organizerId, UUID id);

    Page<Event> findPublishedEvents(String searchTerm, LocalDateTime from, LocalDateTime to,
            Double minPrice, Double maxPrice, String city, String sortBy, Pageable pageable);

    Optional<Event> getPublishedEvent(UUID id);

    CreateEventRequest convertFromDto(CreateEventRequestDto dto);

    CreateTicketTypeRequest convertFromDto(CreateTicketTypeRequestDto dto);

    UpdateTicketTypeRequest convertFromDto(UpdateTicketTypeRequestDto dto);

    List<UpdateTicketTypeRequest> convertFromDtoList(List<UpdateTicketTypeRequestDto> dtoList);

    UpdateEventRequest convertFromDto(UpdateEventRequestDto dto);

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
}
