// Named React Query hooks wrapping api.ts -- see features/events/hooks.ts for the
// queryOptions-export pattern this follows, so route loaders can ensureQueryData
// against the exact same cache entries these hooks read.
import { queryOptions, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { toastErrorMessage } from '#/lib/api-client'
import type { PaginationParams } from '#/lib/pagination'
import { cancelTicketForOrganizer, listTicketsForEvent, listTicketsForOrganizer } from './api'
import type { CancelTicketForOrganizerRequest } from './types'

const ticketSalesKey = ['ticket-sales'] as const

export function eventTicketSalesQueryOptions(eventId: string, params: PaginationParams) {
  return queryOptions({
    queryKey: [...ticketSalesKey, 'event', eventId, params],
    queryFn: () => listTicketsForEvent(eventId, params),
    enabled: Boolean(eventId),
  })
}

export function organizerTicketSalesQueryOptions(params: PaginationParams) {
  return queryOptions({
    queryKey: [...ticketSalesKey, 'organizer', params],
    queryFn: () => listTicketsForOrganizer(params),
  })
}

export function useEventTicketSales(eventId: string, params: PaginationParams) {
  return useQuery(eventTicketSalesQueryOptions(eventId, params))
}

export function useOrganizerTicketSales(params: PaginationParams) {
  return useQuery(organizerTicketSalesQueryOptions(params))
}

export function useCancelTicketForOrganizer(eventId: string, ticketId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CancelTicketForOrganizerRequest) =>
      cancelTicketForOrganizer(eventId, ticketId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ticketSalesKey })
      toast.success('Ticket cancelled')
    },
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't cancel ticket"))
    },
  })
}
