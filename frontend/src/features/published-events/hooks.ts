// Named React Query hooks wrapping api.ts -- see issues #4, #5, #10. Both are plain
// read-only queries (no toast on error) -- the browse page/event detail read
// isPending/isError directly, same as every other list/get hook in this app; toasts
// are reserved for mutations.
import { useQuery } from '@tanstack/react-query'
import { getPublishedEvent, listPublishedEventCities, listPublishedEvents } from './api'
import type { ListPublishedEventsParams } from './api'

const publishedEventsKey = ['published-events'] as const

export function usePublishedEvents(params: ListPublishedEventsParams) {
  return useQuery({
    queryKey: [...publishedEventsKey, params],
    queryFn: () => listPublishedEvents(params),
  })
}

export function usePublishedEvent(eventId: string) {
  return useQuery({
    queryKey: [...publishedEventsKey, eventId],
    queryFn: () => getPublishedEvent(eventId),
    enabled: Boolean(eventId),
  })
}

export function usePublishedEventCities() {
  return useQuery({
    queryKey: [...publishedEventsKey, 'cities'],
    queryFn: listPublishedEventCities,
  })
}
