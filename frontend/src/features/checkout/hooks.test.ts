// Same pattern as features/tickets/hooks.test.ts.
import { describe, expect, it } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { server } from '#/test/msw/server'
import { createQueryWrapper } from '#/test/test-utils'
import { useCreateCheckout } from './hooks'

const EVENT_ID = 'event-1'
const CHECKOUT_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/events/${EVENT_ID}/checkout`

describe('useCreateCheckout', () => {
  it('returns the Stripe checkout session from POST /api/v1/events/:eventId/checkout', async () => {
    server.use(
      http.post(CHECKOUT_URL, () =>
        HttpResponse.json(
          {
            orderId: 'order-1',
            checkoutUrl: 'https://checkout.stripe.com/c/pay/cs_test_123',
            expiresAt: '2026-01-01T00:30:00',
          },
          { status: 201 },
        ),
      ),
    )

    const { result } = renderHook(() => useCreateCheckout(), {
      wrapper: createQueryWrapper(),
    })

    result.current.mutate({
      eventId: EVENT_ID,
      request: { items: [{ ticketTypeId: 'tt-1', quantity: 2 }] },
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.checkoutUrl).toBe(
      'https://checkout.stripe.com/c/pay/cs_test_123',
    )
  })

  it('surfaces the backend validation message on a sold-out/invalid cart', async () => {
    server.use(
      http.post(CHECKOUT_URL, () =>
        HttpResponse.json({ error: 'Not enough tickets available' }, { status: 409 }),
      ),
    )

    const { result } = renderHook(() => useCreateCheckout(), {
      wrapper: createQueryWrapper(),
    })

    result.current.mutate({
      eventId: EVENT_ID,
      request: { items: [{ ticketTypeId: 'tt-1', quantity: 2 }] },
    })

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect(result.current.error?.message).toBe('Not enough tickets available')
  })
})
