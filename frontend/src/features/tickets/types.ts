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

export type TicketValidationMethod = 'QR_SCAN' | 'MANUAL'
export type TicketValidationStatus = 'VALID' | 'INVALID' | 'EXPIRED'

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
