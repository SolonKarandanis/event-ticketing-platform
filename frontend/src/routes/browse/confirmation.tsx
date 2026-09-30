import { Link, createFileRoute } from '@tanstack/react-router'
import { z } from 'zod'
import { Button } from '#/components/ui/button'
import { publishedEventQueryOptions, usePublishedEvent } from '#/features/published-events/hooks'

// Search-param driven (not a path param under $eventId) so this stays a plain sibling
// route -- $eventId.tsx has no children today, and nesting a route under it would turn
// it into a layout route for no other reason than this one page.
//
// requested/purchased are cart-wide totals now (issue #26/#20's multi-ticket-type
// cart), not a single ticket type's counters. TODO(#18): once the real Stripe
// Checkout Session + webhook flow replaces $eventId.tsx's bridge loop, a cart-level
// reservation makes checkout atomic -- this collapses to Success/Failed only, and
// requested/purchased (a leftover from the pre-reservation sequential-loop model)
// can go away entirely.
const confirmationSearchSchema = z.object({
  eventId: z.string(),
  requested: z.coerce.number().int().min(1),
  purchased: z.coerce.number().int().min(0),
})

export const Route = createFileRoute('/browse/confirmation')({
  validateSearch: confirmationSearchSchema.parse,
  // eventId lives in search here, not a path param -- loaderDeps narrows the loader to
  // re-run only when it changes, not on requested/purchased too. Usually already warm
  // (the checkout flow on $eventId.tsx reads the same cache entry right before
  // navigating here), but this page is also reachable cold -- a refresh, a
  // shared/bookmarked confirmation link -- where nothing has fetched it yet.
  //
  // Client-only, same reason as every other /browse/** loader: this route isn't
  // ssr:false, so this also runs server-side, where apiFetch() throws via
  // getUserManager() (client-only). Skipping server-side entirely, not just catching
  // the throw, is what matters -- ensureQueryData still records a caught failure as an
  // *errored* query, and that dehydrates into the SSR'd HTML as a false error state on
  // a cold hit instead of the real pending state. See browse/index.tsx's loader for the
  // full story.
  loaderDeps: ({ search }) => ({ eventId: search.eventId }),
  loader: async ({ context, deps }) => {
    if (typeof window === 'undefined') {
      return
    }
    await context.queryClient.ensureQueryData(publishedEventQueryOptions(deps.eventId)).catch(() => {
      // Handled by usePublishedEvent()'s data staying undefined below.
    })
  },
  component: PurchaseConfirmation,
})

function PurchaseConfirmation() {
  const { eventId, requested, purchased } = Route.useSearch()
  const { data: event } = usePublishedEvent(eventId)
  const isFullSuccess = purchased === requested

  return (
    <main className="page-wrap px-4 py-12">
      <div className="island-shell mx-auto max-w-lg rounded-xl p-8 text-center">
        <p className="island-kicker mb-2">
          {isFullSuccess ? 'Success' : 'Partial Purchase'}
        </p>
        <h1 className="display-title mb-4 text-2xl font-bold text-(--sea-ink)">
          {isFullSuccess
            ? `${purchased} ticket${purchased === 1 ? '' : 's'} purchased`
            : purchased > 0
              ? `${purchased} of ${requested} tickets purchased`
              : "We couldn't complete your purchase"}
        </h1>
        {event ? (
          <p className="mb-6 text-sm text-(--sea-ink-soft)">{event.name}</p>
        ) : (
          <div className="mb-6" />
        )}
        <div className="flex flex-wrap justify-center gap-3">
          {purchased > 0 ? (
            <Button asChild>
              <Link to="/tickets">View My Tickets</Link>
            </Button>
          ) : null}
          <Button asChild variant={purchased > 0 ? 'outline' : 'default'}>
            <Link to="/browse/$eventId" params={{ eventId }}>
              Back to Event
            </Link>
          </Button>
        </div>
      </div>
    </main>
  )
}
