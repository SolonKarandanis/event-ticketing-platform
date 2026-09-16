// Typed fetch functions for the analytics-service reporting endpoint -- populated
// when building the Reports dashboard page (issue #8), against analytics-service's
// /analytics/events/:eventId/summary (organizer-scoped per issue #15).
//
// Note VITE_ANALYTICS_SERVICE_URL has no context-path prefix, unlike
// VITE_TICKET_SERVICE_URL's baked-in /ticket-service.
import { apiFetch, parseJsonOrThrow } from '#/lib/api-client'
import type {
  EventAnalyticsSummary,
  OrganizerAnalyticsSummary,
  SalesOverTimePoint,
} from './types'

const ANALYTICS_URL = import.meta.env.VITE_ANALYTICS_SERVICE_URL
const BASE_URL = `${ANALYTICS_URL}/analytics/events`

export async function getEventAnalyticsSummary(
  eventId: string,
): Promise<EventAnalyticsSummary> {
  const response = await apiFetch(`${BASE_URL}/${eventId}/summary`)
  return parseJsonOrThrow<EventAnalyticsSummary>(response)
}

// Organizer-wide rollup, from a separate controller (OrganizerAnalyticsController) --
// no eventId in the path, so it doesn't live under BASE_URL's /analytics/events prefix.
export async function getOrganizerAnalyticsSummary(): Promise<OrganizerAnalyticsSummary> {
  const response = await apiFetch(
    `${ANALYTICS_URL}/analytics/organizer/summary`,
  )
  return parseJsonOrThrow<OrganizerAnalyticsSummary>(response)
}

// Fixed 30-day window -- no range param sent, matches the endpoint's own fixed default.
export async function getSalesOverTime(): Promise<SalesOverTimePoint[]> {
  const response = await apiFetch(
    `${ANALYTICS_URL}/analytics/organizer/sales-over-time`,
  )
  return parseJsonOrThrow<SalesOverTimePoint[]>(response)
}
