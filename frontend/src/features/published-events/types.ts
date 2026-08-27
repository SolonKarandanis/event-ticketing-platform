// Response types for the public published-events resource, mirroring
// ticket-service's ListPublishedEventResponseDto/GetPublishedEventDetailsResponseDto
// (+ nested GetPublishedEventDetailsTicketTypesResponseDto) verbatim. This is a
// distinct, unauthenticated resource from features/events (organizer-only, requires
// ROLE_ORGANIZER) -- not the same DTOs reused with different auth. See issue #10.
import type { Venue } from '#/features/venues/types'

// Matches EventServiceImpl.findPublishedEvents' sortBy switch exactly -- any other
// value (including omitted) falls through to "soonest" server-side.
export type PublishedEventsSort = 'soonest' | 'priceAsc' | 'priceDesc'

export interface ListPublishedEventResponse {
  id: string
  name: string
  start: string | null
  end: string | null
  venue: Venue
}

export interface GetPublishedEventDetailsTicketTypesResponse {
  id: string
  name: string
  price: number
  description: string | null
}

export interface GetPublishedEventDetailsResponse {
  id: string
  name: string
  start: string | null
  end: string | null
  venue: Venue
  ticketTypes: GetPublishedEventDetailsTicketTypesResponse[]
}
