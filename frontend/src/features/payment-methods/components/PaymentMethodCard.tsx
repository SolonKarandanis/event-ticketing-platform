// One saved card's tile in the Wallet Grid -- split out of payment-methods.tsx so each
// card's own remove/set-default mutation state only disables *that* card's buttons, not
// every card's. Same reasoning as TicketTypeRow being split out of EventForm.
import { CreditCard, Star, Trash2 } from 'lucide-react'
import { useRemovePaymentMethod, useSetDefaultPaymentMethod } from '#/features/payment-methods/hooks'
import type { PaymentMethodResponse } from '#/features/payment-methods/types'

// Stripe's raw brand slugs aren't display-ready as-is, and a bare capitalize() mis-renders
// 'jcb' -> 'Jcb' and 'unionpay' -> 'Unionpay'. Known brands get an exact label; anything
// else (a brand Stripe adds later) falls back to capitalizing each underscore-separated
// word.
const CARD_BRAND_LABELS: Record<string, string> = {
  visa: 'Visa',
  mastercard: 'Mastercard',
  amex: 'Amex',
  discover: 'Discover',
  diners: 'Diners Club',
  jcb: 'JCB',
  unionpay: 'UnionPay',
  unknown: 'Card',
}

function formatCardBrand(brand: string): string {
  return (
    CARD_BRAND_LABELS[brand] ??
    brand
      .split('_')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ')
  )
}

function expiry(card: PaymentMethodResponse) {
  return `${String(card.expMonth).padStart(2, '0')}/${card.expYear}`
}

interface PaymentMethodCardProps {
  card: PaymentMethodResponse
}

export function PaymentMethodCard({ card }: PaymentMethodCardProps) {
  const setDefault = useSetDefaultPaymentMethod()
  const remove = useRemovePaymentMethod()

  return (
    <div className="island-shell relative aspect-[1.6/1] overflow-hidden rounded-2xl p-5">
      {card.isDefault && (
        <div className="absolute inset-x-0 top-0 h-1 bg-(--lagoon-deep)" />
      )}
      <div className="flex items-start justify-between">
        <CreditCard className="h-6 w-6 text-(--sea-ink-soft)" />
        <button
          type="button"
          aria-label={card.isDefault ? 'Default card' : 'Make default'}
          disabled={card.isDefault || setDefault.isPending}
          onClick={() => setDefault.mutate(card.id)}
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
          <p className="text-sm font-semibold text-(--sea-ink)">{formatCardBrand(card.brand)}</p>
          <button
            type="button"
            aria-label="Remove card"
            disabled={remove.isPending}
            onClick={() => remove.mutate(card.id)}
            className="text-(--sea-ink-soft)"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  )
}
