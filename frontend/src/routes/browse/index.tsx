import { useEffect, useState } from 'react'
import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { toast } from 'sonner'
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
import {
  usePublishedEventCities,
  usePublishedEvents,
} from '#/features/published-events/hooks'
import { useDebouncedValue } from '#/hooks/use-debounced-value'
import { useMinimumDuration } from '#/hooks/use-minimum-duration'
import { browseSearchSchema, dateRangeFor, priceRangeFor } from './-forms'
import type {
  BrowseSearch,
  DatePreset,
  PricePreset,
  RadiusPreset,
  SORT_OPTIONS,
} from './-forms'

export const Route = createFileRoute('/browse/')({
  validateSearch: browseSearchSchema.parse,
  component: BrowseEvents,
})

function BrowseEvents() {
  const search = Route.useSearch()
  const navigate = useNavigate({ from: Route.fullPath })

  const page = search.page ?? 1
  const [queryInput, setQueryInput] = useState(search.q ?? '')
  const debouncedQuery = useDebouncedValue(queryInput, 400)
  const [isLocating, setIsLocating] = useState(false)
  const hasLocation = search.lat !== undefined && search.lng !== undefined

  // Commits the debounced search box value into the URL -- every other filter
  // navigates immediately on change (a Select's onValueChange isn't fired per
  // keystroke, so it doesn't need debouncing the same way).
  useEffect(() => {
    const current = search.q ?? ''
    if (debouncedQuery !== current) {
      void navigate({
        search: (prev) => ({
          ...prev,
          q: debouncedQuery || undefined,
          page: undefined,
        }),
      })
    }
    // Only re-run when the debounced value itself changes -- re-running on `search.q`
    // too would refire this the instant the navigate() call above updates it.
  }, [debouncedQuery])

  function updateFilter(patch: Partial<BrowseSearch>) {
    void navigate({
      search: (prev) => ({ ...prev, ...patch, page: undefined }),
    })
  }

  function clearFilters() {
    setQueryInput('')
    void navigate({ search: {} })
  }

  function handleUseMyLocation() {
    setIsLocating(true)
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setIsLocating(false)
        updateFilter({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
          radius: search.radius ?? '25',
        })
      },
      (error) => {
        setIsLocating(false)
        toast.error(
          error.code === error.PERMISSION_DENIED
            ? 'Location permission was denied.'
            : "Couldn't get your location. Try again.",
        )
      },
      { timeout: 10000, maximumAge: 300000 },
    )
  }

  function handleClearLocation() {
    void navigate({
      search: (prev) => ({
        ...prev,
        lat: undefined,
        lng: undefined,
        radius: undefined,
        // "Nearest" means nothing without an origin -- fall back to the default
        // sort rather than leaving a sort selected that can no longer do anything.
        sort: prev.sort === 'distance' ? undefined : prev.sort,
        page: undefined,
      }),
    })
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
    ...(hasLocation
      ? {
          latitude: search.lat,
          longitude: search.lng,
          radiusMeters: Number(search.radius ?? '25') * 1000,
        }
      : {}),
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
    (search.price && search.price !== 'any') ||
    hasLocation,
  )

  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker mb-2">Browse</p>
      <h1 className="display-title mb-6 text-3xl font-bold text-(--sea-ink)">
        Events
      </h1>

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
          onValueChange={(value) =>
            updateFilter({ price: value as PricePreset })
          }
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
          onValueChange={(value) =>
            updateFilter({ city: value === 'any' ? undefined : value })
          }
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
          onValueChange={(value) =>
            updateFilter({ sort: value as (typeof SORT_OPTIONS)[number] })
          }
        >
          <SelectTrigger className="w-42.5">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="soonest">Soonest</SelectItem>
            <SelectItem value="priceAsc">Price: Low to High</SelectItem>
            <SelectItem value="priceDesc">Price: High to Low</SelectItem>
            {/* Only offered once there's an origin to measure from -- selecting
                            it otherwise would silently fall back to Soonest server-side. */}
            {hasLocation && <SelectItem value="distance">Nearest</SelectItem>}
          </SelectContent>
        </Select>

        <Button
          type="button"
          variant="outline"
          size="sm"
          disabled={isLocating}
          onClick={hasLocation ? handleClearLocation : handleUseMyLocation}
        >
          {isLocating
            ? 'Locating...'
            : hasLocation
              ? 'Clear location'
              : 'Use my location'}
        </Button>

        {hasLocation && (
          <Select
            value={search.radius ?? '25'}
            onValueChange={(value) =>
              updateFilter({ radius: value as RadiusPreset })
            }
          >
            <SelectTrigger className="w-32">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="10">Within 10km</SelectItem>
              <SelectItem value="25">Within 25km</SelectItem>
              <SelectItem value="50">Within 50km</SelectItem>
              <SelectItem value="100">Within 100km</SelectItem>
            </SelectContent>
          </Select>
        )}
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
        <p className="text-sm text-destructive">
          Couldn't load events. Try refreshing.
        </p>
      )}

      {!showSkeleton && !isError && events.length === 0 && (
        <div className="text-sm text-(--sea-ink-soft)">
          {hasActiveFilters ? (
            <>
              <p className="mb-3">No events match your search.</p>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={clearFilters}
              >
                Clear filters
              </Button>
            </>
          ) : (
            <p>No events published yet.</p>
          )}
        </div>
      )}
      {!showSkeleton && !isError && events.length > 0 && (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {events.map((event) => (
              <EventCard key={event.id} event={event} />
            ))}
          </div>

          <div className="mt-8">
            <NumberedPagination
              page={page}
              totalPages={data?.totalPages ?? 0}
            />
          </div>
        </>
      )}
    </main>
  )
}
