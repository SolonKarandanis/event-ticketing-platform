import { loadStripe } from '@stripe/stripe-js'
import type { Stripe } from '@stripe/stripe-js'

let stripePromise: Promise<Stripe | null> | undefined

// Lazily constructed, client-only -- same shape as oidc.ts's getUserManager(). Memoizing
// the promise at module scope is load-bearing, not just stylistic consistency:
// react-stripe-js's own <Elements stripe> prop docs say "Once this prop has been set, it
// can not be changed" -- passing a fresh loadStripe() result on every render would
// violate that and tear down/remount the Elements tree.
export function getStripe(): Promise<Stripe | null> {
  if (typeof window === 'undefined') {
    throw new Error('getStripe() must only be called on the client')
  }

  if (!stripePromise) {
    stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY)
  }

  return stripePromise
}
