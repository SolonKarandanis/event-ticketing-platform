import { useEffect, useState } from 'react'
import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { z } from 'zod'
import { Button } from '#/components/ui/button'
import { Input } from '#/components/ui/input'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '#/components/ui/select'
import { Skeleton } from '#/components/ui/skeleton'
import { EventCard } from '#/features/published-events/components/EventCard'
import { NumberedPagination } from '#/features/published-events/components/NumberedPagination'
import { usePublishedEventCities, usePublishedEvents } from '#/features/published-events/hooks'
import { useDebouncedValue } from '#/hooks/use-debounced-value'
import { useMinimumDuration } from '#/hooks/use-minimum-duration'

const DATE_PRESETS = ['any', 'today', 'week', 'month'] as const
type DatePreset = (typeof DATE_PRESETS)[number]

const PRICE_PRESETS = ['any', 'free', 'under25', '25to50', 'over50'] as const
type PricePreset = (typeof PRICE_PRESETS)[number]

const SORT_OPTIONS = ['soonest', 'priceAsc', 'priceDesc'] as const

// All optional/absent-friendly, same reasoning as #/lib/pagination's schema: a Link to
// /browse from elsewhere shouldn't have to supply every filter, and .catch(undefined)
// recovers a malformed value instead of leaving validateSearch throwing.
const browseSearchSchema = z.object({
    q: z.string().optional().catch(undefined),
    page: z.coerce.number().int().min(1).optional().catch(undefined),
    city: z.string().optional().catch(undefined),
    date: z.enum(DATE_PRESETS).optional().catch(undefined),
    price: z.enum(PRICE_PRESETS).optional().catch(undefined),
    sort: z.enum(SORT_OPTIONS).optional().catch(undefined),
})

type BrowseSearch = z.infer<typeof browseSearchSchema>

export const Route = createFileRoute('/browse/')({
    validateSearch: browseSearchSchema.parse,
    component: BrowseEvents,
})

// LocalDateTime, not an ISO instant -- Date#toISOString() produces a "Z"-suffixed UTC
// string, which Spring's default LocalDateTime binder doesn't accept. This formats the
// same wall-clock components a <input type="datetime-local"> would produce.
function toLocalDateTimeString(date: Date): string {
    const pad = (value: number) => String(value).padStart(2, '0')
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function dateRangeFor(preset: DatePreset | undefined): { from?: string; to?: string } {
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
function priceRangeFor(preset: PricePreset | undefined): { minPrice?: number; maxPrice?: number } {
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

function BrowseEvents() {
    const search = Route.useSearch()
    const navigate = useNavigate({ from: Route.fullPath })

    const page = search.page ?? 1
    const [queryInput, setQueryInput] = useState(search.q ?? '')
    const debouncedQuery = useDebouncedValue(queryInput, 400)

    // Commits the debounced search box value into the URL -- every other filter
    // navigates immediately on change (a Select's onValueChange isn't fired per
    // keystroke, so it doesn't need debouncing the same way).
    useEffect(() => {
        const current = search.q ?? ''
        if (debouncedQuery !== current) {
            void navigate({
                search: (prev) => ({ ...prev, q: debouncedQuery || undefined, page: undefined }),
            })
        }
        // Only re-run when the debounced value itself changes -- re-running on `search.q`
        // too would refire this the instant the navigate() call above updates it.
    }, [debouncedQuery])

    function updateFilter(patch: Partial<BrowseSearch>) {
        void navigate({ search: (prev) => ({ ...prev, ...patch, page: undefined }) })
    }

    function clearFilters() {
        setQueryInput('')
        void navigate({ search: {} })
    }

    // Not useVenues -- /api/v1/venues is organizer-only, and this page is public. Calling
    // an organizer-gated endpoint from here 401s for anyone not logged in as an
    // organizer, which apiFetch turns into an unwanted redirect to Keycloak login.
    const { data: cities = [] } = usePublishedEventCities()

    const { data, isPending, isError } = usePublishedEvents({
        page: page - 1,
        size: 12,
        q: search.q,
        sortBy: search.sort ?? 'soonest',
        ...dateRangeFor(search.date),
        ...priceRangeFor(search.price),
        city: search.city,
    })
    const showSkeleton = useMinimumDuration(isPending, 400)
    // showSkeleton isn't literally isPending (it stays true a bit longer on purpose --
    // see useMinimumDuration), so TS can't narrow `data` from it the way it could from
    // isPending directly. Falling back here once, rather than accessing data.content
    // inline, avoids repeating that non-narrowing gap at every access site.
    const events = data?.content ?? []

    const hasActiveFilters = Boolean(
        search.q ||
            search.city ||
            (search.date && search.date !== 'any') ||
            (search.price && search.price !== 'any'),
    )

    return (
        <main className="page-wrap px-4 py-12">
            <p className="island-kicker mb-2">Browse</p>
            <h1 className="display-title mb-6 text-3xl font-bold text-(--sea-ink)">Events</h1>

            <div className="mb-4">
                <Input
                    placeholder="Search events..."
                    value={queryInput}
                    onChange={(event) => setQueryInput(event.target.value)}
                    className="max-w-md"
                />
            </div>

            <div className="mb-8 flex flex-wrap gap-3">
                <Select
                    value={search.date ?? 'any'}
                    onValueChange={(value) => updateFilter({ date: value as DatePreset })}
                >
                    <SelectTrigger className="w-35">
                        <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="any">Any date</SelectItem>
                        <SelectItem value="today">Today</SelectItem>
                        <SelectItem value="week">This week</SelectItem>
                        <SelectItem value="month">This month</SelectItem>
                    </SelectContent>
                </Select>

                <Select
                    value={search.price ?? 'any'}
                    onValueChange={(value) => updateFilter({ price: value as PricePreset })}
                >
                    <SelectTrigger className="w-35">
                        <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="any">Any price</SelectItem>
                        <SelectItem value="free">Free</SelectItem>
                        <SelectItem value="under25">Under $25</SelectItem>
                        <SelectItem value="25to50">$25 - $50</SelectItem>
                        <SelectItem value="over50">$50+</SelectItem>
                    </SelectContent>
                </Select>

                <Select
                    value={search.city ?? 'any'}
                    onValueChange={(value) => updateFilter({ city: value === 'any' ? undefined : value })}
                >
                    <SelectTrigger className="w-35">
                        <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="any">Any city</SelectItem>
                        {cities.map((city) => (
                            <SelectItem key={city} value={city}>
                                {city}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>

                <Select
                    value={search.sort ?? 'soonest'}
                    onValueChange={(value) => updateFilter({ sort: value as (typeof SORT_OPTIONS)[number] })}
                >
                    <SelectTrigger className="w-42.5">
                        <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="soonest">Soonest</SelectItem>
                        <SelectItem value="priceAsc">Price: Low to High</SelectItem>
                        <SelectItem value="priceDesc">Price: High to Low</SelectItem>
                    </SelectContent>
                </Select>
            </div>
            {showSkeleton && (
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {Array.from({ length: 6 }).map((_, index) => (
                        // Index as key is fine: a fixed-count placeholder grid, never reordered.
                        <div key={index} className="island-shell rounded-xl p-5">
                            <Skeleton className="h-5 w-3/4" />
                            <Skeleton className="mt-3 h-4 w-1/2" />
                            <Skeleton className="mt-2 h-4 w-2/3" />
                        </div>
                    ))}
                </div>
            )}
            {!showSkeleton && isError && (
                <p className="text-sm text-destructive">Couldn't load events. Try refreshing.</p>
            )}

            {!showSkeleton && !isError && events.length === 0 && (
                <div className="text-sm text-(--sea-ink-soft)">
                    {hasActiveFilters ? (
                        <>
                            <p className="mb-3">No events match your search.</p>
                            <Button type="button" variant="outline" size="sm" onClick={clearFilters}>
                                Clear filters
                            </Button>
                        </>
                    ) : (
                        <p>No events published yet.</p>
                    )}
                </div>
            )}
            {!showSkeleton && !isError && events.length > 0 &&(
                <>
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                        {events.map((event) => (
                            <EventCard key={event.id} event={event} />
                        ))}
                    </div>

                    <div className="mt-8">
                        <NumberedPagination page={page} totalPages={data?.totalPages ?? 0} />
                    </div>
                </>
            )}
        </main>
    )
}
