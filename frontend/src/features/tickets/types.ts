// Request/response types for the tickets resource, mirroring ticket-service's DTOs
// verbatim (including the referenceCode field from issue #16) -- see issue #9.
//
// GET /tickets/{ticketId}/qr-codes returns a raw PNG, not JSON -- there's no DTO to
// mirror for it; api.ts treats that response as a Blob.

// Same const-object-as-enum pattern as EventStatus in features/events/types.ts: the
// type and the value share the name, so TicketStatus works as both an annotation
// (TicketStatus) and a value/namespace (TicketStatus.CANCELLED).
export const TicketStatus = {
  PURCHASED: 'PURCHASED',
  CANCELLED: 'CANCELLED',
} as const

export type TicketStatus = (typeof TicketStatus)[keyof typeof TicketStatus]

export const TicketValidationMethod = {
  QR_SCAN: 'QR_SCAN',
  MANUAL: 'MANUAL',
} as const

export type TicketValidationMethod =
  (typeof TicketValidationMethod)[keyof typeof TicketValidationMethod]

// EXPIRED is part of the backend's enum but nothing ever sets it yet (flagged for
// future in issue #16) -- validateTicket only ever actually resolves VALID, INVALID, or
// (now that ticket cancellation exists) CANCELLED.
export const TicketValidationStatus = {
  VALID: 'VALID',
  INVALID: 'INVALID',
  EXPIRED: 'EXPIRED',
  CANCELLED: 'CANCELLED',
} as const

export type TicketValidationStatus =
  (typeof TicketValidationStatus)[keyof typeof TicketValidationStatus]

// Mirrors TicketCancelReasonEnum. Not client-supplied -- the backend infers this from
// which cancel endpoint was called -- but the frontend still needs the type to read
// cancelReason back off CancelTicketResponse/TicketSaleResponse.
export const TicketCancelReason = {
  ATTENDEE_REQUEST: 'ATTENDEE_REQUEST',
  ORGANIZER_ACTION: 'ORGANIZER_ACTION',
  EVENT_CANCELLED: 'EVENT_CANCELLED',
} as const

export type TicketCancelReason =
  (typeof TicketCancelReason)[keyof typeof TicketCancelReason]

export interface ListTicketTicketTypeResponse {
  id: string
  name: string
  price: number
}

export interface ListTicketResponse {
  id: string
  status: TicketStatus
  ticketType: ListTicketTicketTypeResponse
}

export interface GetTicketResponse {
  id: string
  referenceCode: string
  status: TicketStatus
  price: number
  description: string | null
  eventName: string
  eventVenueName: string
  eventStart: string | null
  eventEnd: string | null
}

export interface TicketValidationRequest {
  // A QR code's domainId (UUID, as a string) for QR_SCAN, or a ticket's referenceCode
  // for MANUAL -- which one applies depends on `method`.
  id: string
  method: TicketValidationMethod
}

export interface TicketValidationResponse {
  ticketId: string
  status: TicketValidationStatus
}

// note is the only client-supplied input -- cancelReason is inferred server-side from
// which endpoint was called (ATTENDEE_REQUEST here), not sent by the client.
export interface CancelTicketRequest {
  note?: string
}

export interface CancelTicketResponse {
  id: string
  status: TicketStatus
  cancelledAt: string
  cancelReason: TicketCancelReason
  cancelNote: string | null
}
