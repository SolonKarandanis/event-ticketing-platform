// Typed fetch functions for the analytics-service reporting endpoint -- populated
// when building the Reports dashboard page (issue #8), against analytics-service's
// /analytics/events/:eventId/summary (organizer-scoped per issue #15).
//
// Note VITE_ANALYTICS_SERVICE_URL has no context-path prefix, unlike
// VITE_TICKET_SERVICE_URL's baked-in /ticket-service.
import { apiFetch, parseJsonOrThrow } from '#/lib/api-client'
import type { EventAnalyticsSummary } from './types'

const BASE_URL = `${import.meta.env.VITE_ANALYTICS_SERVICE_URL}/analytics/events`

export async function getEventAnalyticsSummary(eventId: string): Promise<EventAnalyticsSummary> {
  const response = await apiFetch(`${BASE_URL}/${eventId}/summary`)
  return parseJsonOrThrow<EventAnalyticsSummary>(response)
}
