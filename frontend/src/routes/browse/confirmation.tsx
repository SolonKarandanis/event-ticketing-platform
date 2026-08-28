import { Link, createFileRoute } from '@tanstack/react-router'
import { z } from 'zod'
import { Button } from '#/components/ui/button'
import { usePublishedEvent } from '#/features/published-events/hooks'

// Search-param driven (not a path param under $eventId) so this stays a plain sibling
// route -- $eventId.tsx has no children today, and nesting a route under it would turn
// it into a layout route for no other reason than this one page. See issue #5: the
// purchase loop on $eventId.tsx already knows requested/purchased/errorMessage by the
// time it's done, so this page doesn't need to re-derive or persist any of that
// server-side -- it's just handed through the URL.
const confirmationSearchSchema = z.object({
  eventId: z.string(),
  ticketTypeId: z.string(),
  requested: z.coerce.number().int().min(1),
  purchased: z.coerce.number().int().min(0),
  errorMessage: z.string().optional().catch(undefined),
})

export const Route = createFileRoute('/browse/confirmation')({
  validateSearch: confirmationSearchSchema.parse,
  component: PurchaseConfirmation,
})

function PurchaseConfirmation() {
  const { eventId, ticketTypeId, requested, purchased, errorMessage } =
    Route.useSearch()
  const { data: event } = usePublishedEvent(eventId)
  const ticketType = event?.ticketTypes.find(
    (candidate) => candidate.id === ticketTypeId,
  )
  const isFullSuccess = purchased === requested

  return (
    <main className="page-wrap px-4 py-12">
      <div className="island-shell mx-auto max-w-lg rounded-xl p-8 text-center">
        <p className="island-kicker mb-2">
          {isFullSuccess ? 'Success' : 'Partial Purchase'}
        </p>
        <h1 className="display-title mb-4 text-2xl font-bold text-(--sea-ink)">
          {isFullSuccess
            ? `${purchased} ${ticketType?.name ?? 'ticket'}${purchased === 1 ? '' : 's'} purchased`
            : purchased > 0
              ? `${purchased} of ${requested} tickets purchased`
              : "We couldn't complete your purchase"}
        </h1>
        {event ? (
          <p className="mb-2 text-sm text-(--sea-ink-soft)">
            {event.name}
            {ticketType ? ` — ${ticketType.name}` : ''}
          </p>
        ) : null}
        {!isFullSuccess && errorMessage ? (
          <p className="mb-6 text-sm text-destructive">{errorMessage}</p>
        ) : (
          <div className="mb-6" />
        )}
        <div className="flex flex-wrap justify-center gap-3">
          {purchased > 0 ? (
            <Button asChild>
              <Link to="/tickets">View My Tickets</Link>
            </Button>
          ) : null}
          <Button asChild variant="outline">
            <Link to="/browse/$eventId" params={{ eventId }}>
              Back to Event
            </Link>
          </Button>
        </div>
      </div>
    </main>
  )
}
