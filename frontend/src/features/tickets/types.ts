// Request/response types for the tickets resource, mirroring ticket-service's DTOs
// verbatim (including the referenceCode field from issue #16) -- see issue #9.
//
// GET /tickets/{ticketId}/qr-codes returns a raw PNG, not JSON -- there's no DTO to
// mirror for it; api.ts treats that response as a Blob.

export type TicketStatus = 'PURCHASED' | 'CANCELLED'
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
