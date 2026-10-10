// Design settled in issue #28 ("Wallet Grid", prototyped against two alternatives --
// a plain list and a compact settings-section treatment -- see the closed ticket for
// what didn't get picked and why).
//
// Issue #18 (#19): wired to the real backend -- a SetupIntent-backed add flow via
// Stripe Elements, and real list/remove/set-default endpoints. Nothing about the
// rendering changed from the old mock, only where the data and mutations come from.
import { createFileRoute } from '@tanstack/react-router'
import { Skeleton } from '#/components/ui/skeleton'
import { AddCardDialog } from '#/features/payment-methods/components/AddCardDialog'
import { PaymentMethodCard } from '#/features/payment-methods/components/PaymentMethodCard'
import { paymentMethodsQueryOptions, usePaymentMethods } from '#/features/payment-methods/hooks'
import { useMinimumDuration } from '#/hooks/use-minimum-duration'

export const Route = createFileRoute('/_attendee/payment-methods')({
  // Warms the cache usePaymentMethods() below reads, on navigation/intent-preload.
  loader: async ({ context }) => {
    try {
      await context.queryClient.ensureQueryData(paymentMethodsQueryOptions())
    } catch {
      // Handled by usePaymentMethods()'s isError below.
    }
  },
  component: PaymentMethods,
})

function PaymentMethods() {
  const { data, isPending, isError } = usePaymentMethods()
  const showSkeleton = useMinimumDuration(isPending, 400)
  const cards = data ?? []

  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker mb-2">Attendee</p>
      <h1 className="display-title mb-6 text-3xl font-bold text-(--sea-ink)">
        Payment Methods
      </h1>

      {showSkeleton && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, index) => (
            // Index as key is fine: a fixed-count placeholder grid, never reordered.
            <div key={index} className="island-shell aspect-[1.6/1] rounded-2xl p-5">
              <Skeleton className="h-6 w-6" />
              <Skeleton className="mt-6 h-6 w-2/3" />
              <Skeleton className="mt-6 h-4 w-1/3" />
            </div>
          ))}
        </div>
      )}

      {!showSkeleton && isError && (
        <p className="text-sm text-destructive">
          Couldn't load payment methods. Try refreshing.
        </p>
      )}

      {!showSkeleton && !isError && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {cards.map((card) => (
            <PaymentMethodCard key={card.id} card={card} />
          ))}
          <AddCardDialog />
        </div>
      )}
    </main>
  )
}
