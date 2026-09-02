import { useState } from 'react'
import { Link, createFileRoute, useNavigate } from '@tanstack/react-router'
import { useAuth } from 'react-oidc-context'
import { Button } from '#/components/ui/button'
import { Input } from '#/components/ui/input'
import { toastErrorMessage } from '#/lib/api-client'
import { publishedEventImageUrl } from '#/features/published-events/api'
import {
  publishedEventQueryOptions,
  usePublishedEvent,
} from '#/features/published-events/hooks'
import { usePurchaseTicket } from '#/features/ticket-types/hooks'

export const Route = createFileRoute('/browse/$eventId')({
  // Warms the cache usePublishedEvent() below reads, on navigation/intent-preload
  // (hovering an event card on /browse). Client-only -- this route isn't ssr:false (it's
  // public, meant to render without a login), so this loader also runs server-side,
  // where apiFetch() throws via getUserManager() (client-only, see lib/oidc.ts).
  // Skipping server-side entirely -- not just catching the throw -- matters: catching it
  // still leaves ensureQueryData's failed attempt as an *errored* query in the cache,
  // which then dehydrates into the SSR'd HTML and hydrates as a false "Couldn't find
  // this event" instead of the real pending state a fresh visit should show. See
  // browse/index.tsx's loader for the full story (found via the dehydrated payload).
  loader: async ({ context, params }) => {
    if (typeof window === 'undefined') {
      return
    }
    try {
      await context.queryClient.ensureQueryData(
        publishedEventQueryOptions(params.eventId),
      )
    } catch {
      // Handled by usePublishedEvent()'s isError below.
    }
  },
  component: EventDetails,
})

function formatDateTime(value: string | null): string {
  if (!value) {
    return 'TBA'
  }
  return new Date(value).toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

function EventDetails() {
  const { eventId } = Route.useParams()
  const { data: event, isPending, isError } = usePublishedEvent(eventId)
  const auth = useAuth()
  const navigate = useNavigate()
  const purchaseTicket = usePurchaseTicket()

  const [quantities, setQuantities] = useState<Record<string, number>>({})
  const [pendingTicketTypeId, setPendingTicketTypeId] = useState<string | null>(
    null,
  )
  const [progress, setProgress] = useState<{
    done: number
    total: number
  } | null>(null)

  // Sequential, not Promise.all -- issue #5 decided a single-ticket-per-call purchase
  // (the backend has no bulk-purchase endpoint) with live "Purchasing... (X of Y)"
  // progress, stopping at the first failure (e.g. sold out mid-loop) rather than firing
  // every request at once and sorting out a mixed-result pile after the fact.
  async function handlePurchase(ticketTypeId: string, quantity: number) {
    if (!auth.isAuthenticated) {
      void auth.signinRedirect()
      return
    }

    setPendingTicketTypeId(ticketTypeId)
    setProgress({ done: 0, total: quantity })

    let purchased = 0
    let errorMessage: string | undefined
    for (let i = 0; i < quantity; i++) {
      try {
        await purchaseTicket.mutateAsync({ eventId, ticketTypeId })
        purchased += 1
        setProgress({ done: purchased, total: quantity })
      } catch (error) {
        // usePurchaseTicket's own onError already toasts this -- captured again
        // here just so the confirmation page can keep showing it after the toast
        // has faded.
        errorMessage = toastErrorMessage(error, 'Something went wrong')
        break
      }
    }

    setPendingTicketTypeId(null)
    setProgress(null)
    void navigate({
      to: '/browse/confirmation',
      search: {
        eventId,
        ticketTypeId,
        requested: quantity,
        purchased,
        errorMessage,
      },
    })
  }

  return (
    <main className="page-wrap px-4 py-12">
      <Link to="/browse" className="nav-link mb-4 inline-block">
        &larr; Back to Events
      </Link>
      {isPending && (
        <p className="text-sm text-(--sea-ink-soft)">Loading event...</p>
      )}

      {!isPending && isError && (
        <p className="text-sm text-destructive">
          Couldn't find this event. It may no longer be published.
        </p>
      )}
      {!isPending && !isError && (
        <>
          <h1 className="display-title mb-2 text-3xl font-bold text-(--sea-ink)">
            {event.name}
          </h1>
          <p className="text-sm text-(--sea-ink-soft)">
            {formatDateTime(event.start)}
            {event.end ? ` - ${formatDateTime(event.end)}` : ''}
          </p>
          <p className="mb-8 text-sm text-(--sea-ink-soft)">
            {event.venue.name}, {event.venue.addressLine1}, {event.venue.city}
          </p>

          {event.images.length > 0 && (
            <div className="mb-8 grid grid-cols-2 gap-3 sm:grid-cols-3">
              {event.images.map((image) => (
                <img
                  key={image.id}
                  src={publishedEventImageUrl(event.id, image.id)}
                  alt={image.altText ?? ''}
                  className="aspect-square w-full rounded-lg object-cover"
                />
              ))}
            </div>
          )}

          <h2 className="mb-4 text-lg font-semibold text-(--sea-ink)">
            Ticket Types
          </h2>
          <div className="grid max-w-xl gap-3">
            {event.ticketTypes.map((ticketType) => {
              const quantity = quantities[ticketType.id] ?? 1
              const isPurchasingThis = pendingTicketTypeId === ticketType.id

              return (
                <div
                  key={ticketType.id}
                  className="island-shell rounded-xl p-4"
                >
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-(--sea-ink)">
                      {ticketType.name}
                    </span>
                    <span className="text-(--sea-ink)">
                      ${ticketType.price.toFixed(2)}
                    </span>
                  </div>
                  {ticketType.description ? (
                    <p className="mt-1 text-sm text-(--sea-ink-soft)">
                      {ticketType.description}
                    </p>
                  ) : null}
                  <div className="mt-3 flex items-center gap-2">
                    <Input
                      type="number"
                      min={1}
                      value={quantity}
                      disabled={pendingTicketTypeId !== null}
                      onChange={(changeEvent) => {
                        const value = Math.max(
                          1,
                          Math.floor(Number(changeEvent.target.value)) || 1,
                        )
                        setQuantities((prev) => ({
                          ...prev,
                          [ticketType.id]: value,
                        }))
                      }}
                      className="w-20"
                    />
                    <Button
                      type="button"
                      size="sm"
                      disabled={pendingTicketTypeId !== null}
                      onClick={() =>
                        void handlePurchase(ticketType.id, quantity)
                      }
                    >
                      {isPurchasingThis && progress
                        ? `Purchasing... (${progress.done} of ${progress.total})`
                        : 'Buy'}
                    </Button>
                  </div>
                </div>
              )
            })}
          </div>
        </>
      )}
    </main>
  )
}
