// Typed fetch functions for the venues resource -- table list + create/edit form (issue #7).
import { apiFetch, parseJsonOrThrow } from '#/lib/api-client'
import type { Page } from '#/lib/api-client'
import type { PaginationParams } from '#/lib/pagination'
import type { CreateVenueRequest, UpdateVenueRequest, Venue } from './types'

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/venues`

export async function listVenues({ page, size }: PaginationParams): Promise<Page<Venue>> {
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
