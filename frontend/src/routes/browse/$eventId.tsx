import { useState } from 'react'
import { Link, createFileRoute, useNavigate } from '@tanstack/react-router'
import { useAuth } from 'react-oidc-context'
import { Button } from '#/components/ui/button'
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

function formatPrice(price: number): string {
  return `$${price.toFixed(2)}`
}

function EventDetails() {
  const { eventId } = Route.useParams()
  const { data: event, isPending, isError } = usePublishedEvent(eventId)
  const auth = useAuth()
  const navigate = useNavigate()
  const purchaseTicket = usePurchaseTicket()

  // Cart shape decided in issue #26 (Variant C, "Order Summary Card") and issue #18's
  // #20 (a real multi-ticket-type cart, not one-ticket-type-at-a-time) -- keyed by
  // ticketTypeId, 0 means "not in the cart", not "1 by default" the way the old
  // per-row shape worked.
  const [quantities, setQuantities] = useState<Record<string, number>>({})
  const [isCheckingOut, setIsCheckingOut] = useState(false)
  const [progress, setProgress] = useState<{ done: number; total: number } | null>(null)

  function setQuantity(ticketTypeId: string, quantity: number) {
    setQuantities((prev) => {
      if (quantity <= 0) {
        const next = { ...prev }
        delete next[ticketTypeId]
        return next
      }
      return { ...prev, [ticketTypeId]: quantity }
    })
  }

  const cartLines = Object.entries(quantities).filter(([, qty]) => qty > 0)
  const itemCount = cartLines.reduce((sum, [, qty]) => sum + qty, 0)
  const total =
    event === undefined
      ? 0
      : cartLines.reduce((sum, [ticketTypeId, qty]) => {
          const ticketType = event.ticketTypes.find((tt) => tt.id === ticketTypeId)
          return sum + (ticketType?.price ?? 0) * qty
        }, 0)

  // TODO(#18): this is a bridge, not the real checkout. The map (issue #18) decided
  // purchase becomes a single Stripe Checkout Session per cart -- one charge, one
  // webhook, atomic (every held line item is issued or none are). That needs a real
  // "create checkout session" endpoint and a hosted-redirect round trip, neither of
  // which exist yet. Until they do, this generalizes the pre-#18 sequential-purchase-
  // loop (issue #4/#5) across every line in the cart instead of just one ticket type,
  // so purchasing stays genuinely functional against the real backend in the meantime.
  // Replace this whole function with a single POST + redirect once #18's backend lands
  // -- at that point the outcome also collapses to Success/Failed only (no more
  // partial), since a reservation-backed checkout can't half-succeed the way this
  // unreserved loop still can.
  async function handleCheckout() {
    if (!auth.isAuthenticated) {
      void auth.signinRedirect()
      return
    }
    if (cartLines.length === 0) {
      return
    }

    setIsCheckingOut(true)
    setProgress({ done: 0, total: itemCount })

    let purchased = 0
    outer: for (const [ticketTypeId, quantity] of cartLines) {
      for (let i = 0; i < quantity; i++) {
        try {
          await purchaseTicket.mutateAsync({ eventId, ticketTypeId })
          purchased += 1
          setProgress({ done: purchased, total: itemCount })
        } catch {
          // usePurchaseTicket's own onError already toasts this.
          break outer
        }
      }
    }

    setIsCheckingOut(false)
    setProgress(null)
    void navigate({
      to: '/browse/confirmation',
      search: { eventId, requested: itemCount, purchased },
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

          {/* Variant C from issue #26's prototype ("Order Summary Card") -- ticket
              types are described on the left, and the whole cart -- selection,
              running total, checkout -- lives in one consolidated card, sticky on
              wide screens, rather than a per-row Buy button or a separate drawer. */}
          <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
            <div className="grid gap-2 text-sm text-(--sea-ink-soft)">
              <h2 className="mb-2 text-lg font-semibold text-(--sea-ink)">
                Ticket Types
              </h2>
              {event.ticketTypes.map((ticketType) => (
                <p key={ticketType.id}>
                  <span className="font-medium text-(--sea-ink)">{ticketType.name}</span>
                  {' — '}
                  {formatPrice(ticketType.price)}
                  {ticketType.description ? ` · ${ticketType.description}` : ''}
                </p>
              ))}
            </div>

            <div className="island-shell h-fit rounded-xl p-5 lg:sticky lg:top-6">
              <h3 className="mb-4 font-semibold text-(--sea-ink)">Build Your Order</h3>
              <div className="grid gap-3">
                {event.ticketTypes.map((ticketType) => {
                  const quantity = quantities[ticketType.id] ?? 0
                  return (
                    <div key={ticketType.id} className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-(--sea-ink)">{ticketType.name}</p>
                        <p className="text-xs text-(--sea-ink-soft)">
                          {formatPrice(ticketType.price)} each
                        </p>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <button
                          type="button"
                          aria-label={`Decrease ${ticketType.name} quantity`}
                          className="h-7 w-7 rounded-full border border-(--line) text-sm text-(--sea-ink) disabled:opacity-40"
                          disabled={quantity === 0 || isCheckingOut}
                          onClick={() => setQuantity(ticketType.id, quantity - 1)}
                        >
                          −
                        </button>
                        <span className="w-6 text-center text-sm text-(--sea-ink)">
                          {quantity}
                        </span>
                        <button
                          type="button"
                          aria-label={`Increase ${ticketType.name} quantity`}
                          className="h-7 w-7 rounded-full border border-(--line) text-sm text-(--sea-ink) disabled:opacity-40"
                          disabled={isCheckingOut}
                          onClick={() => setQuantity(ticketType.id, quantity + 1)}
                        >
                          +
                        </button>
                      </div>
                    </div>
                  )
                })}
              </div>
              <div className="mt-5 flex items-center justify-between border-t border-(--line) pt-4 font-semibold text-(--sea-ink)">
                <span>Total</span>
                <span>{formatPrice(total)}</span>
              </div>
              <Button
                className="mt-4 w-full"
                disabled={itemCount === 0 || isCheckingOut}
                onClick={() => void handleCheckout()}
              >
                {isCheckingOut && progress
                  ? `Purchasing... (${progress.done} of ${progress.total})`
                  : itemCount === 0
                    ? 'Select tickets'
                    : 'Checkout'}
              </Button>
            </div>
          </div>
        </>
      )}
    </main>
  )
}
