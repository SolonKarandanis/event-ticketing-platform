// Design settled in issue #28 ("Wallet Grid", prototyped against two alternatives --
// a plain list and a compact settings-section treatment -- see the closed ticket for
// what didn't get picked and why).
//
// TODO(#18): this page is UI-only. #19 decided saved payment methods are full scope
// (a providerCustomerId per User, a SetupIntent-backed add flow, real list/remove
// endpoints), but none of that backend exists yet -- there's no `features/payment-
// methods/` api.ts to wire this to. Cards below are local component state, not
// persisted anywhere; a refresh resets to the two seeded examples. Replace
// useMockCards with real React Query hooks once the backend lands, following the
// same api.ts/hooks.ts shape every other feature (venues, events, tickets) already
// uses -- nothing about this component's rendering should need to change when that
// happens, only where its data comes from.
import { useState } from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { CreditCard, Plus, Star, Trash2 } from 'lucide-react'

export const Route = createFileRoute('/_attendee/payment-methods')({
  component: PaymentMethods,
})

interface SavedCard {
  id: string
  brand: 'Visa' | 'Mastercard' | 'Amex'
  last4: string
  expMonth: number
  expYear: number
  isDefault: boolean
}

function useMockCards() {
  const [cards, setCards] = useState<SavedCard[]>([
    { id: '1', brand: 'Visa', last4: '4242', expMonth: 8, expYear: 2028, isDefault: true },
    { id: '2', brand: 'Mastercard', last4: '5454', expMonth: 3, expYear: 2027, isDefault: false },
  ])

  function remove(id: string) {
    setCards((prev) => prev.filter((c) => c.id !== id))
  }

  function makeDefault(id: string) {
    setCards((prev) => prev.map((c) => ({ ...c, isDefault: c.id === id })))
  }

  // Stub -- a real "add" opens Stripe's SetupIntent-backed card element and only adds
  // the card on success.
  function addStub() {
    const id = String(Date.now())
    setCards((prev) => [
      ...prev,
      { id, brand: 'Visa', last4: String(1000 + prev.length).slice(-4), expMonth: 11, expYear: 2029, isDefault: prev.length === 0 },
    ])
  }

  return { cards, remove, makeDefault, addStub }
}

function expiry(card: SavedCard) {
  return `${String(card.expMonth).padStart(2, '0')}/${card.expYear}`
}

function PaymentMethods() {
  const { cards, remove, makeDefault, addStub } = useMockCards()

  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker mb-2">Attendee</p>
      <h1 className="display-title mb-6 text-3xl font-bold text-(--sea-ink)">
        Payment Methods
      </h1>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {cards.map((card) => (
          <div
            key={card.id}
            className="island-shell relative aspect-[1.6/1] overflow-hidden rounded-2xl p-5"
          >
            {card.isDefault && (
              <div className="absolute inset-x-0 top-0 h-1 bg-(--lagoon-deep)" />
            )}
            <div className="flex items-start justify-between">
              <CreditCard className="h-6 w-6 text-(--sea-ink-soft)" />
              <button
                type="button"
                aria-label={card.isDefault ? 'Default card' : 'Make default'}
                onClick={() => makeDefault(card.id)}
              >
                <Star
                  className={`h-5 w-5 ${card.isDefault ? 'fill-(--lagoon-deep) text-(--lagoon-deep)' : 'text-(--sea-ink-soft)'}`}
                />
              </button>
            </div>
            <p className="mt-6 text-lg tracking-widest text-(--sea-ink)">
              •••• •••• •••• {card.last4}
            </p>
            <div className="mt-3 flex items-end justify-between">
              <div>
                <p className="text-[10px] uppercase text-(--sea-ink-soft)">Expires</p>
                <p className="text-sm text-(--sea-ink)">{expiry(card)}</p>
              </div>
              <div className="flex items-center gap-2">
                <p className="text-sm font-semibold text-(--sea-ink)">{card.brand}</p>
                <button
                  type="button"
                  aria-label="Remove card"
                  onClick={() => remove(card.id)}
                  className="text-(--sea-ink-soft)"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>
        ))}

        <button
          type="button"
          onClick={addStub}
          className="flex aspect-[1.6/1] flex-col items-center justify-center gap-2 rounded-2xl border-2 border-dashed border-(--line) text-(--sea-ink-soft) hover:border-(--lagoon-deep) hover:text-(--lagoon-deep)"
        >
          <Plus className="h-6 w-6" />
          <span className="text-sm font-medium">Add Card</span>
        </button>
      </div>
    </main>
  )
}
