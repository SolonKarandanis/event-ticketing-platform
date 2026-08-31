import { createFileRoute } from '@tanstack/react-router'
import { Skeleton } from '#/components/ui/skeleton'
import { eventAnalyticsSummaryQueryOptions, useEventAnalyticsSummaries } from '#/features/analytics/hooks'
import { eventsQueryOptions, useEvents } from '#/features/events/hooks'
import { useMinimumDuration } from '#/hooks/use-minimum-duration'

// The largest single page this app's shared pagination allows (see lib/pagination's
// PAGE_SIZES) -- analytics-service has no organizer-wide rollup endpoint (issue #8's
// Notes), so this is a client-side chart over "as many events as one page can hold",
// not a true unpaginated "all events" fetch.
const EVENTS_PAGE_SIZE = 100

export const Route = createFileRoute('/_organizer/dashboard')({
  // Two-step warm-cache: fetch the events page first (mirroring the component's own
  // useEvents({ page: 0, size: EVENTS_PAGE_SIZE }) call), then fan out one summary
  // prefetch per event -- same shape as useEventAnalyticsSummaries below, just run
  // ahead of the component mounting instead of from it. Every await is individually
  // caught: no errorComponent on this route, so an uncaught rejection here would bypass
  // the "Couldn't load revenue data" message the component already renders on isError,
  // and one event's summary failing shouldn't stop the others from prefetching.
  loader: async ({ context }) => {
    const eventsPage = await context.queryClient
      .ensureQueryData(eventsQueryOptions({ page: 0, size: EVENTS_PAGE_SIZE }))
      .catch(() => undefined)

    await Promise.all(
      (eventsPage?.content ?? []).map((event) =>
        context.queryClient
          .ensureQueryData(eventAnalyticsSummaryQueryOptions(event.id))
          .catch(() => {
            // Handled by useEventAnalyticsSummaries()'s per-query isError below.
          }),
      ),
    )
  },
  component: OrganizerDashboard,
})

interface EventRevenueRow {
  id: string
  name: string
  revenue: number
  ticketsSold: number
}

function OrganizerDashboard() {
  const {
    data: eventsPage,
    isPending: isEventsPending,
    isError: isEventsError,
  } = useEvents({ page: 0, size: EVENTS_PAGE_SIZE })
  const events = eventsPage?.content ?? []

  const summaryQueries = useEventAnalyticsSummaries(
    events.map((event) => event.id),
  )
  const isSummariesPending = summaryQueries.some((query) => query.isPending)
  const isSummariesError = summaryQueries.some((query) => query.isError)

  const showSkeleton = useMinimumDuration(
    isEventsPending || isSummariesPending,
    400,
  )
  const isError = isEventsError || isSummariesError

  const rows: EventRevenueRow[] = events
    .map((event, index) => ({
      id: event.id,
      name: event.name,
      revenue: summaryQueries[index]?.data?.revenue ?? 0,
      ticketsSold: summaryQueries[index]?.data?.ticketsSold ?? 0,
    }))
    .sort((a, b) => b.revenue - a.revenue)

  // Floors at 1, not 0 -- avoids a divide-by-zero when every event has zero revenue
  // (a brand-new organizer with only draft events), which would otherwise turn every
  // bar's width into NaN%.
  const maxRevenue = Math.max(1, ...rows.map((row) => row.revenue))

  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker mb-2">Organizer</p>
      <h1 className="display-title mb-6 text-3xl font-bold text-(--sea-ink)">
        Dashboard
      </h1>

      {showSkeleton && (
        <div className="island-shell space-y-4 rounded-xl p-6" role="status">
          <span className="sr-only">Loading revenue by event...</span>
          {Array.from({ length: 5 }).map((_, index) => (
            // Index as key is fine here: a fixed-count list of placeholder rows with
            // no real identity, never reordered.
            <div
              key={index}
              className="flex items-center gap-3"
              aria-hidden="true"
            >
              <Skeleton className="h-4 w-32 shrink-0" />
              <Skeleton className="h-5 flex-1" />
            </div>
          ))}
        </div>
      )}

      {!showSkeleton && isError && (
        <p className="text-sm text-destructive">
          Couldn't load revenue data. Try refreshing.
        </p>
      )}

      {!showSkeleton && !isError && rows.length === 0 && (
        <p className="text-sm text-(--sea-ink-soft)">
          No events yet. Revenue shows up here once you've created one.
        </p>
      )}

      {!showSkeleton && !isError && rows.length > 0 && (
        <div className="island-shell rounded-xl p-6">
          <h2 className="mb-6 text-sm font-semibold text-(--sea-ink)">
            Revenue by event
          </h2>
          <div className="space-y-3">
            {rows.map((row) => {
              const widthPercent = (row.revenue / maxRevenue) * 100
              return (
                <div key={row.id} className="group flex items-center gap-3">
                  <span
                    className="w-32 shrink-0 truncate text-sm text-(--sea-ink)"
                    title={row.name}
                  >
                    {row.name}
                  </span>
                  <div className="h-5 flex-1 rounded-sm bg-(--line)">
                    <div
                      className="h-5 rounded-r-lg bg-(--lagoon-deep) transition-[filter] group-hover:brightness-110"
                      style={{ width: `${widthPercent}%` }}
                    />
                  </div>
                  <span className="w-36 shrink-0 text-right text-sm tabular-nums text-(--sea-ink-soft)">
                    ${row.revenue.toFixed(2)} · {row.ticketsSold} sold
                  </span>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </main>
  )
}
