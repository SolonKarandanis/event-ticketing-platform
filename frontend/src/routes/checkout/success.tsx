import { Link, createFileRoute } from '@tanstack/react-router'
import { Button } from '#/components/ui/button'

// Lands here after a real top-level browser redirect to Stripe Checkout and back --
// ticket-service's already-configured app.checkout.success-url
// (application.properties) points Stripe at exactly this path on a successful payment:
// /checkout/success?session_id={CHECKOUT_SESSION_ID}.
//
// Deliberately doesn't read `session_id` from the URL (no validateSearch here): Stripe's
// success_url templating only supports its own {CHECKOUT_SESSION_ID} placeholder, and
// CheckoutServiceImpl passes that exact same static success URL to every checkout
// regardless of event -- there's no eventId to show "which event" was purchased, and no
// backend endpoint to look an order up by Stripe session id either (out of scope here).
// session_id stays in the URL because Stripe put it there, not because this page does
// anything with it.
//
// Also doesn't try to prove tickets exist yet: ticket issuance is async and
// webhook-driven (issue #21) -- by the time the browser lands here, the paid order's
// Ticket rows may not be committed. This confirms the *payment*, not the *tickets*.
export const Route = createFileRoute('/checkout/success')({
  component: CheckoutSuccess,
})

function CheckoutSuccess() {
  return (
    <main className="page-wrap px-4 py-12">
      <div className="island-shell mx-auto max-w-lg rounded-xl p-8 text-center">
        <p className="island-kicker mb-2">Payment received</p>
        <h1 className="display-title mb-4 text-2xl font-bold text-(--sea-ink)">
          Thanks for your purchase
        </h1>
        <p className="mb-6 text-sm text-(--sea-ink-soft)">
          Your payment went through. Your tickets are being issued now and will show up
          under My Tickets shortly.
        </p>
        <div className="flex flex-wrap justify-center gap-3">
          <Button asChild>
            <Link to="/tickets">View My Tickets</Link>
          </Button>
          <Button asChild variant="outline">
            <Link to="/browse">Browse More Events</Link>
          </Button>
        </div>
      </div>
    </main>
  )
}
