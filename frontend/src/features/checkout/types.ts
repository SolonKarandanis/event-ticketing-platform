// Request/response types for the checkout resource, mirroring ticket-service's
// CreateCheckoutRequestDto/CheckoutLineItemRequestDto (request) and
// CreateCheckoutResponseDto (response) verbatim. See issue #18 (#20).
export interface CheckoutLineItemRequest {
  ticketTypeId: string
  quantity: number
}

export interface CreateCheckoutRequest {
  items: CheckoutLineItemRequest[]
}

// checkoutUrl is a Stripe-hosted Checkout Session URL -- an external domain, so callers
// must redirect with a real top-level navigation (window.location.href), not
// @tanstack/react-router's client-side navigate().
export interface CreateCheckoutResponse {
  orderId: string
  checkoutUrl: string
  expiresAt: string
}
