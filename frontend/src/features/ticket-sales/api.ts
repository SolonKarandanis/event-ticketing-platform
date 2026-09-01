// Typed fetch functions for the organizer-facing ticket-sales resource -- both the
// per-event (/api/v1/events/{eventId}/tickets) and cross-event
// (/api/v1/events/tickets) listings, plus organizer-cancel.
import { apiFetch, parseJsonOrThrow } from '#/lib/api-client'
import type { Page } from '#/lib/api-client'
import type { PaginationParams } from '#/lib/pagination'
import type {
  CancelTicketForOrganizerRequest,
  CancelTicketForOrganizerResponse,
  TicketSaleResponse,
} from './types'

const EVENTS_BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/events`

export async function listTicketsForEvent(
  eventId: string,
  { page, size }: PaginationParams,
): Promise<Page<TicketSaleResponse>> {
  const response = await apiFetch(`${EVENTS_BASE_URL}/${eventId}/tickets?page=${page}&size=${size}`)
  return parseJsonOrThrow<Page<TicketSaleResponse>>(response)
}

export async function listTicketsForOrganizer(
  { page, size }: PaginationParams,
): Promise<Page<TicketSaleResponse>> {
  const response = await apiFetch(`${EVENTS_BASE_URL}/tickets?page=${page}&size=${size}`)
  return parseJsonOrThrow<Page<TicketSaleResponse>>(response)
}

export async function cancelTicketForOrganizer(
  eventId: string,
  ticketId: string,
  request: CancelTicketForOrganizerRequest,
): Promise<CancelTicketForOrganizerResponse> {
  const response = await apiFetch(`${EVENTS_BASE_URL}/${eventId}/tickets/${ticketId}/cancel`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  return parseJsonOrThrow<CancelTicketForOrganizerResponse>(response)
}
