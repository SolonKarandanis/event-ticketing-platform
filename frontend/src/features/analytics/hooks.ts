// Named React Query hooks wrapping api.ts -- see issue #8.
import { useQuery } from '@tanstack/react-query'
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
