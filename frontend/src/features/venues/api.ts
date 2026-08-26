// Typed fetch functions for the venues resource -- table list + create/edit form (issue #7).
import { apiFetch, parseJsonOrThrow } from '#/lib/api-client'
import type { Page } from '#/lib/api-client'
import type { PaginationParams } from '#/lib/pagination'
import type { CreateVenueRequest, UpdateVenueRequest, Venue } from './types'

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/venues`

export interface ListVenuesParams extends PaginationParams {
  // Venue-picker search-as-you-type -- not part of the generic PaginationParams shape,
  // since events/tickets have no search param yet.
  q?: string
}

export async function listVenues({ page, size, q }: ListVenuesParams): Promise<Page<Venue>> {
  const searchParam = q ? `&q=${encodeURIComponent(q)}` : ''
  const response = await apiFetch(`${BASE_URL}?page=${page}&size=${size}${searchParam}`)
  return parseJsonOrThrow<Page<Venue>>(response)
}

export async function getVenue(venueId: string): Promise<Venue> {
  const response = await apiFetch(`${BASE_URL}/${venueId}`)
  return parseJsonOrThrow<Venue>(response)
}

export async function createVenue(request: CreateVenueRequest): Promise<Venue> {
  const response = await apiFetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  return parseJsonOrThrow<Venue>(response)
}

export async function updateVenue(venueId: string, request: UpdateVenueRequest): Promise<Venue> {
  const response = await apiFetch(`${BASE_URL}/${venueId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  return parseJsonOrThrow<Venue>(response)
}
