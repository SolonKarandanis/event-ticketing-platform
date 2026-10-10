// Typed fetch functions for the checkout resource -- the real Stripe Checkout Session
// flow issue #18 (#20) wires in, replacing $eventId.tsx's old sequential
// ticket-types/purchase loop. One endpoint: create a Checkout Session for the whole
// cart (every ticket type + quantity in it) in a single call. Returns 201 Created, not
// 200 -- parseJsonOrThrow doesn't care (any 2xx is `response.ok`).
import { apiFetch, parseJsonOrThrow } from '#/lib/api-client'
import type { CreateCheckoutRequest, CreateCheckoutResponse } from './types'

export async function createCheckout(
  eventId: string,
  request: CreateCheckoutRequest,
): Promise<CreateCheckoutResponse> {
  const response = await apiFetch(
    `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/events/${eventId}/checkout`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    },
  )
  return parseJsonOrThrow<CreateCheckoutResponse>(response)
}
