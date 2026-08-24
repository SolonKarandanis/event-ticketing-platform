// Named React Query hooks wrapping api.ts -- query keys, invalidation, and
// success/failure toasts all live here once, not duplicated at call sites. See issue #7.
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { toastErrorMessage } from '#/lib/api-client'
import { createVenue, getVenue, listVenues, updateVenue } from './api'
import type { PaginationParams } from '#/lib/pagination'
import type { CreateVenueRequest, UpdateVenueRequest } from './types'

const venuesKey = ['venues'] as const

export function useVenues(params: PaginationParams) {
  return useQuery({
    queryKey: [...venuesKey, params],
    queryFn: () => listVenues(params),
  })
}

export function useVenue(venueId: string) {
  return useQuery({
    queryKey: [...venuesKey, venueId],
    queryFn: () => getVenue(venueId),
    enabled: Boolean(venueId),
  })
}

export function useCreateVenue() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CreateVenueRequest) => createVenue(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: venuesKey })
      toast.success('Venue created')
    },
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't create venue"))
    },
  })
}

export function useUpdateVenue(venueId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: UpdateVenueRequest) => updateVenue(venueId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: venuesKey })
      toast.success('Venue updated')
    },
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't update venue"))
    },
  })
}
