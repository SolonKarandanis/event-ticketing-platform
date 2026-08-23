// Typed fetch functions for the venues resource -- table list + create/edit form (issue #7).
import { apiFetch, parseJsonOrThrow } from '#/lib/api-client'
import type { Page } from '#/lib/api-client'
import type { CreateVenueRequest, UpdateVenueRequest, Venue } from './types'

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/venues`

export interface ListVenuesParams {
  // 0-based, matching ticket-service's Pageable exactly -- any 1-based/0-based
  // translation for the UI happens in the route, not here.
  page: number
  size: number
}

export async function listVenues({ page, size }: ListVenuesParams): Promise<Page<Venue>> {
  const response = await apiFetch(`${BASE_URL}?page=${page}&size=${size}`)
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
