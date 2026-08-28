// Named React Query hooks wrapping api.ts -- see issue #8.
import { useQueries, useQuery } from '@tanstack/react-query'
import { getEventAnalyticsSummary } from './api'

const analyticsKey = ['analytics'] as const

// Read-only, like every other list/get query hook in this app -- no toast on error,
// the component reads isError/error itself (toasts are reserved for mutations).
export function useEventAnalyticsSummary(eventId: string) {
  return useQuery({
    queryKey: [...analyticsKey, 'events', eventId, 'summary'],
    queryFn: () => getEventAnalyticsSummary(eventId),
    enabled: Boolean(eventId),
  })
}

// Fans out one summary query per event, in the same order as eventIds -- the Reports
// dashboard (issue #8) needs a per-event bar chart, but analytics-service has no
// organizer-wide rollup endpoint (see the wayfinder map's Notes on issue #8) to fetch
// them all in a single call.
export function useEventAnalyticsSummaries(eventIds: string[]) {
  return useQueries({
    queries: eventIds.map((eventId) => ({
      queryKey: [...analyticsKey, 'events', eventId, 'summary'],
      queryFn: () => getEventAnalyticsSummary(eventId),
    })),
  })
}
