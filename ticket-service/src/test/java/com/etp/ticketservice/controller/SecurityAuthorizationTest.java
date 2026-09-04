package com.etp.ticketservice.controller;

import com.etp.ticketservice.config.SecurityConfig;
import com.etp.ticketservice.controller.support.UserProvisioningTestConfig;
import com.etp.ticketservice.domain.dto.request.TicketValidationRequestDto;
import com.etp.ticketservice.domain.dto.response.TicketValidationResponseDto;
import com.etp.ticketservice.domain.entity.TicketValidation;
import com.etp.ticketservice.domain.enums.TicketValidationMethod;
import com.etp.ticketservice.domain.enums.TicketValidationStatusEnum;
import com.etp.ticketservice.domain.service.EventService;
import com.etp.ticketservice.domain.service.QrCodeService;
import com.etp.ticketservice.domain.service.TicketService;
import com.etp.ticketservice.domain.service.TicketTypeService;
import com.etp.ticketservice.domain.service.TicketValidationService;
import com.etp.ticketservice.domain.service.VenueService;
import com.etp.ticketservice.domain.service.antivirus.AntivirusService;
import com.etp.ticketservice.security.JwtAuthenticationConverter;
import com.etp.ticketservice.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static com.etp.ticketservice.controller.support.TestJwts.withSubject;
import static com.etp.ticketservice.controller.support.TestJwts.withSubjectAndRole;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Unlike every other @WebMvcTest in this app, this one DOES import the real
// SecurityConfig -- its whole point is to verify authorizeHttpRequests' actual rules
// (hasRole(ORGANIZER)/hasRole(STAFF)/authenticated()/permitAll, and their matcher
// ordering), which every other controller slice deliberately skips (see each of their
// own class comments). @WebMvcTest with no controllers argument loads every
// @RestController in the app, since a single rule here (e.g. "/api/v1/events/**") can
// span endpoints that live on more than one controller. JwtAuthenticationConverter is
// imported alongside SecurityConfig since the filter chain bean wires it in by
// constructor parameter -- but note TestJwts.withSubjectAndRole sets authorities
// directly and never actually exercises this converter's own claim-parsing logic; see
// JwtAuthenticationConverterTest for that.
//
// Every service dependency across every controller is mocked, same as each individual
// controller slice -- but here they're stubbed only just enough to reach an unambiguous
// 2xx/3xx past the security filter chain. What each endpoint's business logic actually
// does with a request is already covered by that controller's own dedicated test class;
// this class only asserts on status codes, and only cares whether a request got past
// authorization, not what it received back.
@WebMvcTest
@Import({SecurityConfig.class, JwtAuthenticationConverter.class, UserProvisioningTestConfig.class})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EventService eventService;
    @MockitoBean
    private TicketService ticketService;
    @MockitoBean
    private AntivirusService antivirusService;
    @MockitoBean
    private QrCodeService qrCodeService;
    @MockitoBean
    private VenueService venueService;
    @MockitoBean
    private TicketTypeService ticketTypeService;
    @MockitoBean
    private TicketValidationService ticketValidationService;

    private static final UUID USER_ID = UUID.randomUUID();

    // ---- permitAll ----

    @Test
    void publishedEventsSearch_isPermitAll_reachesControllerWithoutAnyAuthentication() throws Exception {
        when(eventService.findPublishedEvents(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(post("/api/v1/published-events/search"))
                .andExpect(status().isOk());
    }

    @Test
    void publishedEventDetails_isPermitAll_reachesControllerWithoutAnyAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/published-events/{eventId}", UUID.randomUUID()))
                // Empty Optional -> 404, not 401/403 -- proof this reached the
                // controller at all rather than being rejected by the filter chain.
                .andExpect(status().isNotFound());
    }

    // ---- /api/v1/events/** -> hasRole(ORGANIZER) ----

    @Test
    void eventEndpoints_rejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eventEndpoints_rejectAuthenticatedNonOrganizer() throws Exception {
        mockMvc.perform(get("/api/v1/events").with(withSubjectAndRole(USER_ID, Role.ATTENDEE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void eventEndpoints_allowOrganizer() throws Exception {
        when(eventService.listEventsForOrganizer(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/events").with(withSubjectAndRole(USER_ID, Role.ORGANIZER)))
                .andExpect(status().isOk());
    }

    // ---- /api/v1/venues/** -> hasRole(ORGANIZER) ----

    @Test
    void venueEndpoints_rejectAuthenticatedNonOrganizer() throws Exception {
        mockMvc.perform(get("/api/v1/venues").with(withSubjectAndRole(USER_ID, Role.ATTENDEE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void venueEndpoints_allowOrganizer() throws Exception {
        when(venueService.listVenues(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/venues").with(withSubjectAndRole(USER_ID, Role.ORGANIZER)))
                .andExpect(status().isOk());
    }

    // ---- /api/v1/events/{id}/ticket-types/{id}/tickets -> authenticated() specifically,
    // carved out ahead of the broader ORGANIZER-only /api/v1/events/** rule below it in
    // SecurityConfig. The single most fragile rule in this file to a matcher-ordering
    // regression: ticket purchase is an attendee action living under the /api/v1/events/
    // prefix, and if this carve-out's requestMatcher ever moved below the ORGANIZER-only
    // one (or was removed), attendees would silently start getting 403s on checkout.

    @Test
    void ticketPurchase_rejectsAnonymousRequest() throws Exception {
        mockMvc.perform(post("/api/v1/events/{eventId}/ticket-types/{ticketTypeId}/tickets",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ticketPurchase_needsOnlyAuthentication_notOrganizerRole() throws Exception {
        mockMvc.perform(post("/api/v1/events/{eventId}/ticket-types/{ticketTypeId}/tickets",
                        UUID.randomUUID(), UUID.randomUUID())
                        .with(withSubjectAndRole(USER_ID, Role.ATTENDEE)))
                .andExpect(status().isNoContent());
    }

    // ---- /api/v1/ticket-validations -> hasRole(STAFF) ----

    @Test
    void ticketValidations_rejectsAuthenticatedNonStaff() throws Exception {
        mockMvc.perform(post("/api/v1/ticket-validations")
                        .with(withSubjectAndRole(USER_ID, Role.ORGANIZER))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(validationRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void ticketValidations_allowsStaff() throws Exception {
        TicketValidation validation = new TicketValidation();
        TicketValidationResponseDto responseDto = new TicketValidationResponseDto(
                UUID.randomUUID(), TicketValidationStatusEnum.VALID);
        when(ticketValidationService.validateTicketByReferenceCode(any())).thenReturn(validation);
        when(ticketValidationService.convertToTicketValidationResponseDto(validation)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/ticket-validations")
                        .with(withSubjectAndRole(USER_ID, Role.STAFF))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(validationRequest())))
                .andExpect(status().isOk());
    }

    // ---- anyRequest().authenticated() fallback (everything not matched above, e.g.
    // /api/v1/tickets/**) ----

    @Test
    void fallbackEndpoints_rejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fallbackEndpoints_needOnlyAuthentication_anyRoleAllowed() throws Exception {
        when(ticketService.listTicketsForUser(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/tickets").with(withSubject(USER_ID)))
                .andExpect(status().isOk());
    }

    private TicketValidationRequestDto validationRequest() {
        return new TicketValidationRequestDto("XY3P9KRT", TicketValidationMethod.MANUAL);
    }
}
