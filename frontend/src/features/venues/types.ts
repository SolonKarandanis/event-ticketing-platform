// Request/response types for the venues resource, mirroring ticket-service's DTOs
// verbatim (CreateVenueRequestDto / UpdateVenueRequestDto / VenueResponseDto). See issue #7.
export interface Venue {
  id: string
  name: string
  addressLine1: string
  addressLine2: string | null
  city: string
  postalCode: string
  country: string
  latitude: number | null
  longitude: number | null
  capacity: number | null
  accessibilityInfo: string | null
}

export interface CreateVenueRequest {
  name: string
  addressLine1: string
  addressLine2?: string
  city: string
  postalCode: string
  country: string
  latitude?: number
  longitude?: number
  capacity?: number
  accessibilityInfo?: string
}

export interface UpdateVenueRequest extends CreateVenueRequest {
  id: string
}
