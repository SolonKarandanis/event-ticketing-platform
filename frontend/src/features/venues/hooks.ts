// Named React Query hooks wrapping api.ts -- query keys, invalidation, and
// success/failure toasts all live here once, not duplicated at call sites. See issue #7.
import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { toastErrorMessage } from '#/lib/api-client'
import { createVenue, getVenue, listVenues, updateVenue } from './api'
import type { ListVenuesParams } from './api'
import type { CreateVenueRequest, UpdateVenueRequest } from './types'

const VENUE_PICKER_PAGE_SIZE = 20

const venuesKey = ['venues'] as const

export function useVenues(params: ListVenuesParams) {
  return useQuery({
    queryKey: [...venuesKey, params],
    queryFn: () => listVenues(params),
  })
}

// Backs the venue picker combobox: search-as-you-type (searchTerm becomes part of the
// query key, so a new search starts over at page 0 rather than appending to the old
// unfiltered results) plus infinite scroll through whatever matches.
export function useInfiniteVenues(searchTerm: string) {
  return useInfiniteQuery({
    queryKey: [...venuesKey, 'search', searchTerm],
    queryFn: ({ pageParam }) =>
      listVenues({ page: pageParam, size: VENUE_PICKER_PAGE_SIZE, q: searchTerm || undefined }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) =>
      lastPage.number + 1 < lastPage.totalPages ? lastPage.number + 1 : undefined,
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
