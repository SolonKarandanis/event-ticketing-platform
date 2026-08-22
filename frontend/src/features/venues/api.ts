// Typed fetch functions for the venues resource -- table list + create/edit form (issue #7).
import { apiFetch, parseJsonOrThrow } from '#/lib/api-client'
import type { CreateVenueRequest, UpdateVenueRequest, Venue } from './types'

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/venues`

interface VenuePage {
  content: Venue[]
}

// No pagination UI per issue #7's design (a plain table, no page controls) -- a generous
// page size stands in for "list everything" until the venue count actually needs paging.
export async function listVenues(): Promise<Venue[]> {
  const response = await apiFetch(`${BASE_URL}?size=100`)
  const page = await parseJsonOrThrow<VenuePage>(response)
  return page.content
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
