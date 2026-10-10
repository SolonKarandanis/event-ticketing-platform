// Request/response types for the payment-methods resource, mirroring ticket-service's
// SetupIntentResponseDto/PaymentMethodResponseDto verbatim. See issue #18 (#19), which
// decided saved payment methods into full scope.
//
// stripe.confirmSetup() itself (collecting card details and completing a SetupIntent)
// happens entirely client-side against Stripe directly via @stripe/react-stripe-js --
// these types, and api.ts, only ever describe what ticket-service itself returns.

export interface SetupIntentResponse {
  clientSecret: string
}

// brand is Stripe's own raw lowercase slug ('visa', 'mastercard', 'amex', ...), not the
// capitalized 'Visa'|'Mastercard'|'Amex' union the old mock used -- kept as a plain
// string rather than a union of known values here (a DTO-mirroring file, not a display
// concern) since the real set of possible values is Stripe's, not ours. Display-casing
// is handled in components/PaymentMethodCard.tsx instead.
export interface PaymentMethodResponse {
  id: string
  brand: string
  last4: string
  expMonth: number
  expYear: number
  isDefault: boolean
}
