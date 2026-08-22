// Named React Query hooks wrapping api.ts -- query keys and invalidation live here once,
// not duplicated at call sites. See issue #7.
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createVenue, getVenue, listVenues, updateVenue } from './api'
import type { CreateVenueRequest, UpdateVenueRequest } from './types'

const venuesKey = ['venues'] as const

export function useVenues() {
  return useQuery({ queryKey: venuesKey, queryFn: listVenues })
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
    onSuccess: () => queryClient.invalidateQueries({ queryKey: venuesKey }),
  })
}

export function useUpdateVenue(venueId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: UpdateVenueRequest) => updateVenue(venueId, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: venuesKey }),
  })
}
