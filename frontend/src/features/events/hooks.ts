// Named React Query hooks (useEvents, useCreateEvent, ...) wrapping api.ts -- query
// keys and invalidation live here once, not duplicated at call sites. See issues #6, #10.
import type { PaginationParams } from '#/lib/pagination.ts'
import {
  queryOptions,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import {
  cancelEvent,
  completeEvent,
  createEvent,
  deleteEvent,
  getEvent,
  getEventImage,
  listEvents,
  publishEvent,
  updateEvent,
} from '#/features/events/api.ts'
import type {
  CreateEventRequest,
  UpdateEventRequest,
} from '#/features/events/types.ts'
import { toast } from 'sonner'
import { toastErrorMessage } from '#/lib/api-client.ts'

const eventsKey = ['events'] as const

// Shared query-key/queryFn definitions -- exported so route loaders can
// `ensureQueryData(eventsQueryOptions(...))` against the exact same cache entry these
// hooks read, instead of the loader and the hook drifting out of sync on key shape.
export function eventsQueryOptions(params: PaginationParams) {
  return queryOptions({
    queryKey: [...eventsKey, params],
    queryFn: () => listEvents(params),
  })
}

export function eventQueryOptions(eventId: string) {
  return queryOptions({
    queryKey: [...eventsKey, eventId],
    queryFn: () => getEvent(eventId),
    enabled: Boolean(eventId),
  })
}

export function useEvents(params: PaginationParams) {
  return useQuery(eventsQueryOptions(params))
}

export function useEvent(eventId: string) {
  return useQuery(eventQueryOptions(eventId))
}

// Organizer-facing image bytes -- enabled only once both ids are known, same guard
// shape eventQueryOptions/ticketQrCodeQueryOptions already use. Query-keyed per image,
// not per event, since each image is fetched/cached independently.
export function eventImageQueryOptions(eventId: string, imageId: string) {
  return queryOptions({
    queryKey: [...eventsKey, eventId, 'images', imageId],
    queryFn: () => getEventImage(eventId, imageId),
    enabled: Boolean(eventId) && Boolean(imageId),
  })
}

export function useEventImage(eventId: string, imageId: string) {
  return useQuery(eventImageQueryOptions(eventId, imageId))
}

// newImageFiles rides alongside the JSON-shaped request as its own mutation variable,
// not a field on CreateEventRequest itself -- see api.ts's createEvent for why (JSON
// can't carry the file bytes inline).
interface CreateEventVariables {
  request: CreateEventRequest
  newImageFiles: File[]
}

export function useCreateEvent() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ request, newImageFiles }: CreateEventVariables) =>
      createEvent(request, newImageFiles),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: eventsKey })
      toast.success('Event created')
    },
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't create event"))
    },
  })
}

interface UpdateEventVariables {
  request: UpdateEventRequest
  newImageFiles: File[]
}

export function useUpdateEvent(eventId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ request, newImageFiles }: UpdateEventVariables) =>
      updateEvent(eventId, request, newImageFiles),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: eventsKey })
      toast.success('Event updated')
    },
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't update event"))
    },
  })
}

export function useDeleteEvent(eventId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => deleteEvent(eventId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: eventsKey })
      toast.success('Event deleted')
    },
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't delete event"))
    },
  })
}

export function usePublishEvent(eventId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => publishEvent(eventId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: eventsKey })
      toast.success('Event published')
    },
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't publish event"))
    },
  })
}

export function useCancelEvent(eventId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => cancelEvent(eventId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: eventsKey })
      toast.success('Event canceled')
    },
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't cancel event"))
    },
  })
}

export function useCompleteEvent(eventId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => completeEvent(eventId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: eventsKey })
      toast.success('Event completed')
    },
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't complete event"))
    },
  })
}
