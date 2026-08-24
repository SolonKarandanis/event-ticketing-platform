// Response type for analytics-service's reporting endpoint, mirroring
// EventAnalyticsController's GET /analytics/events/:eventId/summary verbatim -- see
// issue #8. Organizer scoping (issue #15) happens server-side off the JWT; it's not a
// request param or a field in this response.
export interface EventAnalyticsSummary {
  eventId: string
  ticketsSold: number
  revenue: number
}
