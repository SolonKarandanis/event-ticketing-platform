// Named React Query hooks wrapping api.ts -- see issues #4, #5, #10. Both are plain
// read-only queries (no toast on error) -- the browse page/event detail read
// isPending/isError directly, same as every other list/get hook in this app; toasts
// are reserved for mutations.
import { queryOptions, useQuery } from '@tanstack/react-query'
import { getPublishedEvent, listPublishedEventCities, listPublishedEvents } from './api'
import type { ListPublishedEventsParams } from './api'

const publishedEventsKey = ['published-events'] as const

// Exported so route loaders can ensureQueryData against the exact same cache entry
// these hooks read -- see features/events/hooks.ts for the pattern this follows.
export function publishedEventsQueryOptions(params: ListPublishedEventsParams) {
  return queryOptions({
    queryKey: [...publishedEventsKey, params],
    queryFn: () => listPublishedEvents(params),
  })
}

export function publishedEventQueryOptions(eventId: string) {
  return queryOptions({
    queryKey: [...publishedEventsKey, eventId],
    queryFn: () => getPublishedEvent(eventId),
    enabled: Boolean(eventId),
  })
}

export function publishedEventCitiesQueryOptions() {
  return queryOptions({
    queryKey: [...publishedEventsKey, 'cities'],
    queryFn: listPublishedEventCities,
  })
}

export function usePublishedEvents(params: ListPublishedEventsParams) {
  return useQuery(publishedEventsQueryOptions(params))
}

export function usePublishedEvent(eventId: string) {
  return useQuery(publishedEventQueryOptions(eventId))
}

export function usePublishedEventCities() {
  return useQuery(publishedEventCitiesQueryOptions())
}
