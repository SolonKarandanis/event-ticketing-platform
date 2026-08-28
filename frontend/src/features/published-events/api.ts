// Typed fetch functions for the public published-events resource -- populated when
// building the attendee browse/search page (issue #10) and event detail + purchase
// flow (issues #4, #5), against ticket-service's /api/v1/published-events. Public
// (permitAll in SecurityConfig), but apiFetch works fine unauthenticated too -- no
// separate no-auth variant needed.
import { apiFetch, parseJsonOrThrow } from '#/lib/api-client'
import type { Page } from '#/lib/api-client'
import type { PaginationParams } from '#/lib/pagination'
import type {
  GetPublishedEventDetailsResponse,
  ListPublishedEventResponse,
  PublishedEventsSort,
} from './types'

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/published-events`

export interface ListPublishedEventsParams extends PaginationParams {
  q?: string
  // LocalDateTime strings (e.g. "2026-09-01T00:00:00"), not just a date -- matches
  // EventServiceImpl.findPublishedEvents' LocalDateTime from/to params. Omitting both
  // defaults to upcoming-only server-side.
  from?: string
  to?: string
  minPrice?: number
  maxPrice?: number
  city?: string
  // A "near me" origin + radius -- unused until the browse page grows a location
  // filter, but mirrored here now since the backend already accepts them (see
  // EventRepository's PUBLISHED_EVENTS_WHERE). All three or none: the backend treats a
  // partial trio as absent rather than erroring.
  latitude?: number
  longitude?: number
  radiusMeters?: number
  sortBy?: PublishedEventsSort
}

// POST /search, not GET -- nine independent optional filters stopped being a
// reasonable query string a while ago (see PublishedEventController's own comment on
// why). Paging and sort both ride inside the body now too, nested under `paging` --
// matches ListPublishedEventsRequestDto extending SearchRequestDTO server-side. The
// wire keys (page/limit/sortField) come from AbstractPaging's @JsonProperty mapping,
// not the Java field names (pagingStart/pagingSize/sortingColumn) -- sortField still
// carries the same "soonest"/"priceAsc"/"priceDesc"/"distance" values sortBy used to;
// sortOrder has no equivalent here and is omitted, since each of those is already a
// named, fixed-direction sort.
export async function listPublishedEvents(
  params: ListPublishedEventsParams,
): Promise<Page<ListPublishedEventResponse>> {
  const { page, size, sortBy, ...filters } = params
  const response = await apiFetch(`${BASE_URL}/search`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      ...filters,
      paging: { page, limit: size, sortField: sortBy },
    }),
  })
  return parseJsonOrThrow<Page<ListPublishedEventResponse>>(response)
}

export async function getPublishedEvent(
  eventId: string,
): Promise<GetPublishedEventDetailsResponse> {
  const response = await apiFetch(`${BASE_URL}/${eventId}`)
  return parseJsonOrThrow<GetPublishedEventDetailsResponse>(response)
}

// Distinct cities with at least one published event -- backs the browse page's City
// filter. Deliberately not /api/v1/venues: that's organizer-only, and calling it from
// a page an anonymous visitor can reach 401s and triggers apiFetch's auto-redirect to
// Keycloak login, which is exactly the bug this endpoint exists to avoid.
export async function listPublishedEventCities(): Promise<string[]> {
  const response = await apiFetch(`${BASE_URL}/cities`)
  return parseJsonOrThrow<string[]>(response)
}
