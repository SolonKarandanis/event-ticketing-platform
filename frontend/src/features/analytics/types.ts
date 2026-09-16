// Response type for analytics-service's reporting endpoint, mirroring
// EventAnalyticsController's GET /analytics/events/:eventId/summary verbatim -- see
// issue #8. Organizer scoping (issue #15) happens server-side off the JWT; it's not a
// request param or a field in this response.
export interface EventAnalyticsSummary {
  eventId: string
  ticketsSold: number
  revenue: number
}

// GET /analytics/organizer/summary -- organizer-wide rollup across every event, not
// scoped to one. No eventId here: there's nothing to echo back, unlike the per-event
// shape above.
export interface OrganizerAnalyticsSummary {
  ticketsSold: number
  revenue: number
}

// GET /analytics/organizer/sales-over-time -- one point per day, oldest first, for a
// fixed window (30 days server-side; no range param yet). date is YYYY-MM-DD, already
// zero-filled server-side for days with no sales.
export interface SalesOverTimePoint {
  date: string
  ticketsSold: number
  revenue: number
}
