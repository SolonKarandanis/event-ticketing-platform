// Request/response types for the events resource, mirroring ticket-service's DTOs
// verbatim -- CreateEventRequestDto/UpdateEventRequestDto (+ their nested
// Create/UpdateTicketTypeRequestDto), and every response shape the API returns
// (Create/Update/GetDetails/List, each with its own nested ticket-type DTO, same as
// the backend -- no MapStruct-style collapsing into one shared shape). See issue #6.
//
// `status` is never a field on either request DTO -- the event lifecycle (issue #13)
// is driven entirely by the dedicated publish/cancel/complete endpoints, not by a
// value in the create/update body.
import type { Venue } from '#/features/venues/types'

// A const object rather than a real TS `enum` -- matches the rest of this frontend
// (nothing else here uses `enum`), while still giving named references
// (EventStatus.DRAFT) instead of magic strings scattered across status checks. The
// type and the value share the name on purpose: `EventStatus` works as both a type
// annotation and a value/namespace, the same way a real enum would.
export const EventStatus = {
    DRAFT: 'DRAFT',
    PUBLISHED: 'PUBLISHED',
    CANCELLED: 'CANCELLED',
    COMPLETED: 'COMPLETED',
} as const

export type EventStatus = (typeof EventStatus)[keyof typeof EventStatus]

// ---- Requests ----

export interface CreateTicketTypeRequest {
  name: string
  price: number
  description?: string
  totalAvailable?: number
}

export interface CreateEventRequest {
  name: string
  start?: string
  end?: string
  venueId: string
  salesStart?: string
  salesEnd?: string
  ticketTypes: CreateTicketTypeRequest[]
}

export interface UpdateTicketTypeRequest {
  // Absent for a ticket type being added as part of this update; present (and matched
  // against the existing ticket type's id) for one being edited.
  id?: string
  name: string
  price: number
  description?: string
  totalAvailable?: number
}

export interface UpdateEventRequest {
  id: string
  name: string
  start?: string
  end?: string
  venueId: string
  salesStart?: string
  salesEnd?: string
  ticketTypes: UpdateTicketTypeRequest[]
}

// ---- Responses ----

export interface CreateTicketTypeResponse {
  id: string
  name: string
  price: number
  description: string | null
  totalAvailable: number | null
}

export interface CreateEventResponse {
  id: string
  name: string
  start: string | null
  end: string | null
  venue: Venue
  salesStart: string | null
  salesEnd: string | null
  status: EventStatus
  ticketTypes: CreateTicketTypeResponse[]
  createdAt: string
  updatedAt: string
}

export interface UpdateTicketTypeResponse {
  id: string
  name: string
  price: number
  description: string | null
  totalAvailable: number | null
  createdAt: string
  updatedAt: string
}

export interface UpdateEventResponse {
  id: string
  name: string
  start: string | null
  end: string | null
  venue: Venue
  salesStart: string | null
  salesEnd: string | null
  status: EventStatus
  ticketTypes: UpdateTicketTypeResponse[]
  createdAt: string
  updatedAt: string
}

export interface GetEventDetailsTicketTypesResponse {
  id: string
  name: string
  price: number
  description: string | null
  totalAvailable: number | null
  // Always a real count (0 or more), never null -- backend computes it from
  // countByTicketTypeId, not a nullable stored field.
  ticketsSold: number
  createdAt: string
  updatedAt: string
}

export interface GetEventDetailsResponse {
  id: string
  name: string
  start: string | null
  end: string | null
  venue: Venue
  salesStart: string | null
  salesEnd: string | null
  status: EventStatus
  ticketTypes: GetEventDetailsTicketTypesResponse[]
  createdAt: string
  updatedAt: string
}

export interface ListEventTicketTypeResponse {
  id: string
  name: string
  price: number
  description: string | null
  totalAvailable: number | null
}

export interface ListEventResponse {
  id: string
  name: string
  start: string | null
  end: string | null
  venue: Venue
  salesStart: string | null
  salesEnd: string | null
  status: EventStatus
  ticketTypes: ListEventTicketTypeResponse[]
}
