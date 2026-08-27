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
  sortBy?: PublishedEventsSort
}

// Typed directly against ListPublishedEventsParams rather than a generic
// Record<string, ...> -- an interface without its own index signature (this one)
// isn't assignable to a Record type, even when every property's type would fit.
function buildQuery(params: ListPublishedEventsParams): string {
  const query = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== '') {
      query.set(key, String(value))
    }
  }
  return query.toString()
}

export async function listPublishedEvents(
  params: ListPublishedEventsParams,
): Promise<Page<ListPublishedEventResponse>> {
  const query = buildQuery(params)
  const response = await apiFetch(`${BASE_URL}?${query}`)
  return parseJsonOrThrow<Page<ListPublishedEventResponse>>(response)
}

export async function getPublishedEvent(eventId: string): Promise<GetPublishedEventDetailsResponse> {
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
