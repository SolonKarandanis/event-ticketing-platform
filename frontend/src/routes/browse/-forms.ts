import { z } from 'zod'
import type { ListPublishedEventsParams } from '#/features/published-events/api'

const DATE_PRESETS = ['any', 'today', 'week', 'month'] as const
export type DatePreset = (typeof DATE_PRESETS)[number]

const PRICE_PRESETS = ['any', 'free', 'under25', '25to50', 'over50'] as const
export type PricePreset = (typeof PRICE_PRESETS)[number]

// Kilometers, as strings -- a preset dropdown like every other filter on this page,
// not a raw number input. Converted to meters (what the backend's radiusMeters param
// actually expects) at the usePublishedEvents call site.
const RADIUS_PRESETS = ['10', '25', '50', '100'] as const
export type RadiusPreset = (typeof RADIUS_PRESETS)[number]

export const SORT_OPTIONS = [
  'soonest',
  'priceAsc',
  'priceDesc',
  'distance',
] as const

// All optional/absent-friendly, same reasoning as #/lib/pagination's schema: a Link to
// /browse from elsewhere shouldn't have to supply every filter, and .catch(undefined)
// recovers a malformed value instead of leaving validateSearch throwing.
export const browseSearchSchema = z.object({
  q: z.string().optional().catch(undefined),
  page: z.coerce.number().int().min(1).optional().catch(undefined),
  city: z.string().optional().catch(undefined),
  date: z.enum(DATE_PRESETS).optional().catch(undefined),
  price: z.enum(PRICE_PRESETS).optional().catch(undefined),
  sort: z.enum(SORT_OPTIONS).optional().catch(undefined),
  // Set together, from the browser's Geolocation API -- see handleUseMyLocation.
  // Kept in the URL like every other filter here, so a "near me" search is still
  // shareable/bookmarkable rather than living only in transient component state.
  lat: z.coerce.number().optional().catch(undefined),
  lng: z.coerce.number().optional().catch(undefined),
  radius: z.enum(RADIUS_PRESETS).optional().catch(undefined),
})

export type BrowseSearch = z.infer<typeof browseSearchSchema>

// LocalDateTime, not an ISO instant -- Date#toISOString() produces a "Z"-suffixed UTC
// string, which Spring's default LocalDateTime binder doesn't accept. This formats the
// same wall-clock components a <input type="datetime-local"> would produce.
function toLocalDateTimeString(date: Date): string {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

export function dateRangeFor(preset: DatePreset | undefined): {
  from?: string
  to?: string
} {
  if (!preset || preset === 'any') {
    // Omitting both defaults to upcoming-only server-side (EventServiceImpl.findPublishedEvents).
    return {}
  }

  const now = new Date()
  const to = new Date(now)
  if (preset === 'today') {
    to.setHours(23, 59, 59, 0)
  } else if (preset === 'week') {
    to.setDate(to.getDate() + 7)
  } else {
    to.setMonth(to.getMonth() + 1)
  }

  return { from: toLocalDateTimeString(now), to: toLocalDateTimeString(to) }
}

// minPrice/maxPrice match an event's cheapest ticket type ("starting from" price), per
// VenueRepository -- sorry, EventRepository's own comment on the published-events query.
export function priceRangeFor(preset: PricePreset | undefined): {
  minPrice?: number
  maxPrice?: number
} {
  switch (preset) {
    case 'free':
      return { maxPrice: 0 }
    case 'under25':
      return { maxPrice: 25 }
    case '25to50':
      return { minPrice: 25, maxPrice: 50 }
    case 'over50':
      return { minPrice: 50 }
    default:
      return {}
  }
}

// Fixed page size for the browse grid -- not part of the shared pagination lib, since
// this page's numbered pagination (issue #10's resolved design) is its own thing,
// distinct from the organizer tables' Previous/Next + page-size-select pattern.
const PAGE_SIZE = 12

// Shared by the route's loader (to ensureQueryData against the exact cache entry
// usePublishedEvents reads) and the component itself -- computing this independently
// in two places risks two objects that look the same but drift (a stray field, a
// different PAGE_SIZE) and stop matching as a query key. Note dateRangeFor reads
// `new Date()` internally, so the loader and the component's own render call this
// microseconds apart; at a second-boundary that could, in the rarest case, produce a
// one-second-different `to`/`from` and a cache miss on an otherwise-warm prefetch --
// not a correctness issue (usePublishedEvents just fetches again), only a missed
// optimization.
export function buildPublishedEventsParams(search: BrowseSearch): ListPublishedEventsParams {
  const page = search.page ?? 1
  const hasLocation = search.lat !== undefined && search.lng !== undefined

  return {
    page: page - 1,
    size: PAGE_SIZE,
    q: search.q,
    sortBy: search.sort ?? 'soonest',
    ...dateRangeFor(search.date),
    ...priceRangeFor(search.price),
    city: search.city,
    ...(hasLocation
      ? {
          latitude: search.lat,
          longitude: search.lng,
          radiusMeters: Number(search.radius ?? '25') * 1000,
        }
      : {}),
  }
}
