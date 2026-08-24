// Request/response types for the ticket-types resource, mirroring ticket-service's DTOs
// verbatim -- see issues #4, #5, #6.
//
// There's nothing to export here: POST /events/{eventId}/ticket-types/{ticketTypeId}/tickets
// (the purchase endpoint) takes no request body -- eventId/ticketTypeId are path params --
// and returns 204 No Content, so there's no DTO on either side of that call to mirror.
// A ticket type's own shape (name/price/description/totalAvailable, plus ticketsSold on
// the detail view) only ever appears nested inside an event response -- see
// CreateTicketTypeRequest/Response, UpdateTicketTypeRequest/Response, and
// GetEventDetailsTicketTypesResponse in features/events/types.ts.
