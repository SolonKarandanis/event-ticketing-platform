// Typed fetch functions for the events resource -- populated when building the
// screens from GitHub issue #6 (Event creation/edit form) and #10 (published events
// search & browse), against ticket-service's /api/v1/events and /api/v1/published-events.
import type {
  CreateEventRequest,
  CreateEventResponse,
  GetEventDetailsResponse,
  ListEventResponse,
  UpdateEventRequest,
  UpdateEventResponse,
} from '#/features/events/types.ts'
import type { PaginationParams } from '#/lib/pagination.ts'
import {
  apiFetch,
  parseBlobOrThrow,
  parseJsonOrThrow,
  throwIfNotOk,
} from '#/lib/api-client.ts'
import type { Page } from '#/lib/api-client.ts'

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/events`

export async function listEvents({
  page,
  size,
}: PaginationParams): Promise<Page<ListEventResponse>> {
  const response = await apiFetch(`${BASE_URL}?page=${page}&size=${size}`)
  return parseJsonOrThrow<Page<ListEventResponse>>(response)
}

export async function getEvent(
  eventId: string,
): Promise<GetEventDetailsResponse> {
  const response = await apiFetch(`${BASE_URL}/${eventId}`)
  return parseJsonOrThrow<GetEventDetailsResponse>(response)
}

// Multipart, not JSON -- matches ticket-service's EventController exactly. "event" is
// the same JSON body this used to send as a plain @RequestBody (images included, as
// {id}/{newImageIndex} entries -- see EventImageRequest), sent as a Blob with an
// explicit application/json type so the browser's own multipart boundary logic tags
// that part correctly; a raw string part would come through as text/plain instead,
// which Spring's @RequestPart can't deserialize into the DTO. newImageFiles holds the
// actual bytes for every images[].newImageIndex reference in the JSON part, in order --
// JSON can't carry binary data inline. Deliberately no Content-Type header set here:
// the browser computes the correct multipart/form-data boundary itself from the
// FormData body, and overriding it manually would break that.
function buildEventFormData(
  request: CreateEventRequest | UpdateEventRequest,
  newImageFiles: File[],
): FormData {
  const formData = new FormData()
  formData.append(
    'event',
    new Blob([JSON.stringify(request)], { type: 'application/json' }),
  )
  newImageFiles.forEach((file) => formData.append('newImages', file))
  return formData
}

export async function createEvent(
  request: CreateEventRequest,
  newImageFiles: File[],
): Promise<CreateEventResponse> {
  const response = await apiFetch(BASE_URL, {
    method: 'POST',
    body: buildEventFormData(request, newImageFiles),
  })
  return parseJsonOrThrow<CreateEventResponse>(response)
}

export async function updateEvent(
  eventId: string,
  request: UpdateEventRequest,
  newImageFiles: File[],
): Promise<UpdateEventResponse> {
  const response = await apiFetch(`${BASE_URL}/${eventId}`, {
    method: 'PUT',
    body: buildEventFormData(request, newImageFiles),
  })
  return parseJsonOrThrow<UpdateEventResponse>(response)
}

// Organizer-facing raw image bytes -- works for a still-DRAFT event, unlike a public
// <img src> straight at published-events' equivalent endpoint, since this one requires
// the caller's own Authorization header (apiFetch attaches it) and ownership check.
// Same Blob + useObjectUrl pattern getTicketQrCode already established for a ticket's
// QR image.
export async function getEventImage(
  eventId: string,
  imageId: string,
): Promise<Blob> {
  const response = await apiFetch(`${BASE_URL}/${eventId}/images/${imageId}`)
  return parseBlobOrThrow(response)
}

export async function deleteEvent(eventId: string): Promise<void> {
  const response = await apiFetch(`${BASE_URL}/${eventId}`, {
    method: 'DELETE',
  })
  return throwIfNotOk(response)
}

export async function publishEvent(eventId: string): Promise<void> {
  const response = await apiFetch(`${BASE_URL}/${eventId}/publish`, {
    method: 'POST',
  })
  return throwIfNotOk(response)
}

export async function cancelEvent(eventId: string): Promise<void> {
  const response = await apiFetch(`${BASE_URL}/${eventId}/cancel`, {
    method: 'POST',
  })
  return throwIfNotOk(response)
}

export async function completeEvent(eventId: string): Promise<void> {
  const response = await apiFetch(`${BASE_URL}/${eventId}/complete`, {
    method: 'POST',
  })
  return throwIfNotOk(response)
}
