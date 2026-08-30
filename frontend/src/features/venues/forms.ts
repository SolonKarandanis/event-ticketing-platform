import { z } from 'zod'
import { isDecimalOrEmpty, isIntegerOrEmpty } from '#/lib/validation'
import type { CreateVenueRequest, Venue } from './types'

// Numeric fields stay strings in form state (native inputs hand back strings) and get
// converted to numbers only when building the wire request in onSubmit -- simpler than
// fighting zod's number coercion around an empty optional input.
export const venueFormSchema = z.object({
  name: z.string().trim().min(1, 'Name is required'),
  addressLine1: z.string().trim().min(1, 'Address line 1 is required'),
  addressLine2: z.string().trim(),
  city: z.string().trim().min(1, 'City is required'),
  postalCode: z.string().trim().min(1, 'Postal code is required'),
  country: z.string().trim().min(1, 'Country is required'),
  capacity: z
    .string()
    .trim()
    .refine(isIntegerOrEmpty, 'Must be a whole number'),
  latitude: z.string().trim().refine(isDecimalOrEmpty, 'Must be a number'),
  longitude: z.string().trim().refine(isDecimalOrEmpty, 'Must be a number'),
  accessibilityInfo: z.string().trim(),
})

export type VenueFormValues = z.infer<typeof venueFormSchema>

export const emptyValues: VenueFormValues = {
  name: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  postalCode: '',
  country: '',
  capacity: '',
  latitude: '',
  longitude: '',
  accessibilityInfo: '',
}

export function venueToFormValues(venue: Venue): VenueFormValues {
  return {
    name: venue.name,
    addressLine1: venue.addressLine1,
    addressLine2: venue.addressLine2 ?? '',
    city: venue.city,
    postalCode: venue.postalCode,
    country: venue.country,
    capacity: venue.capacity?.toString() ?? '',
    latitude: venue.latitude?.toString() ?? '',
    longitude: venue.longitude?.toString() ?? '',
    accessibilityInfo: venue.accessibilityInfo ?? '',
  }
}

export function formValuesToRequest(
  values: VenueFormValues,
): CreateVenueRequest {
  return {
    name: values.name,
    addressLine1: values.addressLine1,
    addressLine2: values.addressLine2 || undefined,
    city: values.city,
    postalCode: values.postalCode,
    country: values.country,
    capacity: values.capacity ? Number(values.capacity) : undefined,
    latitude: values.latitude ? Number(values.latitude) : undefined,
    longitude: values.longitude ? Number(values.longitude) : undefined,
    accessibilityInfo: values.accessibilityInfo || undefined,
  }
}

// Guards against NaN while a latitude/longitude field is mid-typed (e.g. "-") or
// invalid -- LocationPicker treats "no position yet" as undefined, not NaN, so this
// keeps a not-yet-valid string from ever reaching it as a number.
export function toNumberOrUndefined(value: string): number | undefined {
  const parsed = Number(value)
  return value.trim() !== '' && !Number.isNaN(parsed) ? parsed : undefined
}
