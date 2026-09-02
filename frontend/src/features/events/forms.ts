import { z } from 'zod'
import { isIntegerOrEmpty, isNonNegativeDecimal } from '#/lib/validation'
import type { GetEventDetailsResponse, UpdateEventRequest } from './types'

// Two shapes in one, same as EventImageRequest server-side: `id` set means an existing,
// already-uploaded image (kept, possibly reordered/re-captioned); `file` set means a
// newly-picked local file that hasn't touched the server yet -- nothing about images
// hits the backend until the form's own submit button is clicked (Save as
// Draft/Publish/Save Changes), matching how the rest of this form already defers to
// one submit rather than saving field-by-field. No separate stable-key field here --
// useFieldArray's own generated field.id already survives reorders/removals, which is
// exactly the identity both React's key prop and dnd-kit's sortable id need; adding a
// second one would just be redundant.
export const eventImageFormSchema = z.object({
  id: z.string().optional(),
  file: z.instanceof(File).optional(),
  altText: z.string().trim().max(255, 'Must be 255 characters or fewer'),
})

export type EventImageFormValues = z.infer<typeof eventImageFormSchema>

// Matches EventServiceImpl.MAX_EVENT_IMAGES -- shared by the Zod schema below and
// EventImageGallery's own "disable Add once full" check, so the cap is written once.
export const MAX_EVENT_IMAGES = 8

// Mirrors CreateTicketTypeRequestDto/UpdateTicketTypeRequestDto -- id is absent for a
// ticket type being added, present (and matched against the existing row) for one
// being edited. `limitedQuantity` isn't a backend field at all: it's the "Limited
// quantity" toggle from issue #6, driving whether totalAvailable gets sent at all when
// building the request.
export const ticketTypeFormSchema = z
  .object({
    id: z.string().optional(),
    name: z.string().trim().min(1, 'Name is required'),
    price: z
      .string()
      .trim()
      .refine(isNonNegativeDecimal, 'Must be a non-negative number'),
    description: z.string().trim(),
    limitedQuantity: z.boolean(),
    totalAvailable: z.string().trim(),
  })
  .superRefine((ticketType, ctx) => {
    if (ticketType.limitedQuantity && ticketType.totalAvailable === '') {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['totalAvailable'],
        message: 'Required when quantity is limited',
      })
      return
    }
    if (!isIntegerOrEmpty(ticketType.totalAvailable)) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['totalAvailable'],
        message: 'Must be a whole number',
      })
    }
  })

// start/end/salesStart/salesEnd hold whatever an <input type="datetime-local"> hands
// back ("" or "YYYY-MM-DDTHH:mm") -- none are required on CreateEventRequestDto, so
// blank is valid for each individually; the cross-field rules below only fire once the
// relevant pair is actually present, mirroring EventServiceImpl.validateEventDates
// exactly (including that it's a >/<=  comparison, not >=/<, so equal timestamps are
// only rejected where the backend itself rejects them).
export const eventFormSchema = z
  .object({
    name: z.string().trim().min(1, 'Name is required'),
    start: z.string(),
    end: z.string(),
    venueId: z.string().min(1, 'Venue is required'),
    salesStart: z.string(),
    salesEnd: z.string(),
    ticketTypes: z
      .array(ticketTypeFormSchema)
      .min(1, 'At least one ticket type is required'),
    // Optional -- 0 is fine, unlike ticketTypes' .min(1). Matches the 8-image cap
    // EventServiceImpl enforces server-side (409, not this 400) -- validating it
    // here too just gives faster feedback than a round-trip to find out.
    images: z
      .array(eventImageFormSchema)
      .max(
        MAX_EVENT_IMAGES,
        `An event can have at most ${MAX_EVENT_IMAGES} images`,
      ),
  })
  .superRefine((event, ctx) => {
    const { start, end, salesStart, salesEnd } = event

    if (start && end && end <= start) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['end'],
        message: 'End must be after start',
      })
    }

    if (start && salesEnd && salesEnd > start) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['salesEnd'],
        message: 'Sales must end by the event start',
      })
    }

    if (salesStart && salesEnd && salesEnd <= salesStart) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['salesEnd'],
        message: 'Sales end must be after sales start',
      })
    }
  })

export type EventFormValues = z.infer<typeof eventFormSchema>
export type TicketTypeFormValues = z.infer<typeof ticketTypeFormSchema>

// Seeds a freshly-added row (via useFieldArray().append) and the create form's initial
// single row -- ticketTypes needs at least one entry to satisfy the .min(1) above, so
// "no ticket types yet" has to be "one blank one", not an empty array.
export const emptyTicketType: TicketTypeFormValues = {
  id: undefined,
  name: '',
  price: '',
  description: '',
  limitedQuantity: false,
  totalAvailable: '',
}

export const emptyValues: EventFormValues = {
  name: '',
  start: '',
  end: '',
  venueId: '',
  salesStart: '',
  salesEnd: '',
  ticketTypes: [emptyTicketType],
  images: [],
}

// event.start/end/salesStart/salesEnd are already "YYYY-MM-DDTHH:mm[:ss]" LocalDateTime
// strings -- that's exactly what <input type="datetime-local"> accepts as a value, so
// no reformatting is needed, just the null -> '' fallback every optional field gets.
export function eventToFormValues(
  event: GetEventDetailsResponse,
): EventFormValues {
  return {
    name: event.name,
    start: event.start ?? '',
    end: event.end ?? '',
    venueId: event.venue.id,
    salesStart: event.salesStart ?? '',
    salesEnd: event.salesEnd ?? '',
    ticketTypes: event.ticketTypes.map((ticketType) => ({
      id: ticketType.id,
      name: ticketType.name,
      price: String(ticketType.price),
      description: ticketType.description ?? '',
      limitedQuantity: ticketType.totalAvailable !== null,
      totalAvailable:
        ticketType.totalAvailable !== null
          ? String(ticketType.totalAvailable)
          : '',
    })),
    // Already in gallery order from the backend (see GetEventDetailsResponse.images).
    images: event.images.map((image) => ({
      id: image.id,
      altText: image.altText ?? '',
    })),
  }
}

export interface EventFormSubmission {
  request: Omit<UpdateEventRequest, 'id'>
  // Every staged new image's actual File, in the same order its EventImageRequest
  // entry's newImageIndex references -- api.ts's createEvent/updateEvent send these
  // as separate multipart file parts, since JSON (the "event" part) can't carry
  // binary data inline.
  newImageFiles: File[]
}

// Shaped like UpdateEventRequest minus `id` (which the route adds on top, the same way
// VenueForm's callers do) rather than CreateEventRequest -- CreateTicketTypeRequest has
// no `id` field at all, so a ticket type row carrying one (from an existing row being
// edited) wouldn't satisfy it, while UpdateTicketTypeRequest's optional `id` covers both
// "new row" (undefined) and "existing row" (set) in one shape. A plain CreateEventRequest
// is still assignable from this when creating, since a variable with an extra optional
// property is fine where the narrower type is expected -- it's only a fresh object
// literal that would trip the excess-property check. Same reasoning extends to `images`:
// EventImageRequest's shape is identical either way.
export function formValuesToRequest(
  values: EventFormValues,
): EventFormSubmission {
  const newImageFiles: File[] = []

  // Array order here becomes gallery order server-side (position 0 = cover) --
  // there's no separate field for it, resubmitting the list in a new order (via
  // drag-and-drop in EventImageGallery) already is the reorder.
  const images = values.images.map((image) => {
    if (image.file) {
      const newImageIndex = newImageFiles.length
      newImageFiles.push(image.file)
      return { newImageIndex, altText: image.altText || undefined }
    }
    return { id: image.id, altText: image.altText || undefined }
  })

  return {
    request: {
      name: values.name,
      start: values.start || undefined,
      end: values.end || undefined,
      venueId: values.venueId,
      salesStart: values.salesStart || undefined,
      salesEnd: values.salesEnd || undefined,
      ticketTypes: values.ticketTypes.map((ticketType) => ({
        id: ticketType.id,
        name: ticketType.name,
        price: Number(ticketType.price),
        description: ticketType.description || undefined,
        totalAvailable: ticketType.limitedQuantity
          ? Number(ticketType.totalAvailable)
          : undefined,
      })),
      images,
    },
    newImageFiles,
  }
}
