// Mirrors TicketSaleResponseDto/CancelTicketResponseDto verbatim -- the organizer-facing
// counterpart to features/tickets/, which is attendee-scoped (their own tickets only).
// Kept as its own feature folder rather than folded into tickets/, same reasoning as
// published-events being separate from events: different resource shape, different
// backend endpoints (/api/v1/events/**, organizer-only), different auth.
import type { TicketCancelReason, TicketStatus } from '#/features/tickets/types'

export interface TicketSaleTicketTypeResponse {
  id: string
  name: string
  price: number
}

export interface TicketSaleResponse {
  id: string
  referenceCode: string
  status: TicketStatus
  ticketType: TicketSaleTicketTypeResponse
  purchaserName: string
  purchaserEmail: string
  eventId: string
  eventName: string
  createdAt: string
}

export interface CancelTicketForOrganizerRequest {
  note?: string
}

export interface CancelTicketForOrganizerResponse {
  id: string
  status: TicketStatus
  cancelledAt: string
  cancelReason: TicketCancelReason
  cancelNote: string | null
}
