// Typed fetch functions for the ticket-types resource -- populated when building the
// event form's dynamic ticket-type rows (issue #6) and the purchase flow (issues #4, #5).
//
// Only one real endpoint lives here: purchasing a ticket. It takes no request body
// (eventId/ticketTypeId are path params) and returns 204 No Content -- see types.ts for
// why there's no request/response DTO to mirror.
import { apiFetch, throwIfNotOk } from '#/lib/api-client'

export async function purchaseTicket(eventId: string, ticketTypeId: string): Promise<void> {
  const response = await apiFetch(
    `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/events/${eventId}/ticket-types/${ticketTypeId}/tickets`,
    { method: 'POST' },
  )
  return throwIfNotOk(response)
}
