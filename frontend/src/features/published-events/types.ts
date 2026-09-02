// Response types for the public published-events resource, mirroring
// ticket-service's ListPublishedEventResponseDto/GetPublishedEventDetailsResponseDto
// (+ nested GetPublishedEventDetailsTicketTypesResponseDto) verbatim. This is a
// distinct, unauthenticated resource from features/events (organizer-only, requires
// ROLE_ORGANIZER) -- not the same DTOs reused with different auth. See issue #10.
import type { Venue } from '#/features/venues/types'

// Matches PublishedEventsSortBy's four constants exactly -- any other value (including
// omitted) falls through to SOONEST server-side. "distance" only does anything when a
// latitude/longitude/radiusMeters origin is also sent; without one the backend falls
// back to "soonest" the same way an unrecognized value would.
export type PublishedEventsSort =
  'soonest' | 'priceAsc' | 'priceDesc' | 'distance'

export interface ListPublishedEventResponse {
  id: string
  name: string
  start: string | null
  end: string | null
  venue: Venue
  // The image at gallery position 0, or null if the event has none -- a browse card
  // shows one image, not the whole gallery. Build its fetch URL with
  // publishedEventImageUrl(event.id, event.coverImageId) from api.ts.
  coverImageId: string | null
}

export interface GetPublishedEventDetailsTicketTypesResponse {
  id: string
  name: string
  price: number
  description: string | null
}

// No url field -- same as the backend's EventImageResponseDto and features/events'
// own EventImageResponse. Build the fetch URL from (eventId, id) via
// publishedEventImageUrl in api.ts.
export interface PublishedEventImageResponse {
  id: string
  altText: string | null
}

export interface GetPublishedEventDetailsResponse {
  id: string
  name: string
  start: string | null
  end: string | null
  venue: Venue
  ticketTypes: GetPublishedEventDetailsTicketTypesResponse[]
  // Already ordered by gallery position server-side -- index 0 is the same image
  // ListPublishedEventResponse.coverImageId points at.
  images: PublishedEventImageResponse[]
}
