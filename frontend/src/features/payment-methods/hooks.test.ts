// Same pattern as features/checkout/hooks.test.ts and features/tickets/hooks.test.ts.
//
// stripe.confirmSetup() itself is a direct call against @stripe/react-stripe-js's
// useStripe()/useElements() hooks inside AddCardDialog, not something this feature's
// api.ts/hooks.ts touches -- MSW mocks network calls, not the Stripe.js SDK, so there's
// nothing here to intercept for that half of the flow. Same accepted gap this repo
// already leaves for checkout's real window.location.href redirect to Stripe Checkout.
import { describe, expect, it } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { server } from '#/test/msw/server'
import { createQueryWrapper, createTestQueryClient } from '#/test/test-utils'
import {
  useCreateSetupIntent,
  usePaymentMethods,
  useRemovePaymentMethod,
  useSetDefaultPaymentMethod,
} from './hooks'
import type { PaymentMethodResponse } from './types'

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/payment-methods`

function cardFixture(overrides: Partial<PaymentMethodResponse> = {}): PaymentMethodResponse {
  return {
    id: 'pm_123',
    brand: 'visa',
    last4: '4242',
    expMonth: 8,
    expYear: 2028,
    isDefault: true,
    ...overrides,
  }
}

describe('usePaymentMethods', () => {
  it('returns the saved cards from GET /api/v1/payment-methods', async () => {
    server.use(http.get(BASE_URL, () => HttpResponse.json([cardFixture()])))

    const { result } = renderHook(() => usePaymentMethods(), { wrapper: createQueryWrapper() })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toHaveLength(1)
    expect(result.current.data?.[0].brand).toBe('visa')
  })

  it('resolves to an empty list for a user with no saved cards yet', async () => {
    server.use(http.get(BASE_URL, () => HttpResponse.json([])))

    const { result } = renderHook(() => usePaymentMethods(), { wrapper: createQueryWrapper() })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual([])
  })
})

describe('useCreateSetupIntent', () => {
  it('returns the clientSecret from POST /api/v1/payment-methods/setup-intents', async () => {
    server.use(
      http.post(`${BASE_URL}/setup-intents`, () =>
        HttpResponse.json({ clientSecret: 'seti_123_secret_abc' }, { status: 201 }),
      ),
    )

    const { result } = renderHook(() => useCreateSetupIntent(), { wrapper: createQueryWrapper() })

    result.current.mutate()

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.clientSecret).toBe('seti_123_secret_abc')
  })
})

describe('useRemovePaymentMethod', () => {
  it('invalidates the payment-methods cache on success, refetching any active list query', async () => {
    let listRequestCount = 0
    server.use(
      http.get(BASE_URL, () => {
        listRequestCount += 1
        return HttpResponse.json([cardFixture()])
      }),
      http.delete(`${BASE_URL}/:paymentMethodId`, () => new HttpResponse(null, { status: 204 })),
    )

    const queryClient = createTestQueryClient()
    const wrapper = createQueryWrapper(queryClient)

    const list = renderHook(() => usePaymentMethods(), { wrapper })
    await waitFor(() => expect(list.result.current.isSuccess).toBe(true))
    expect(listRequestCount).toBe(1)

    const remove = renderHook(() => useRemovePaymentMethod(), { wrapper })
    remove.result.current.mutate('pm_123')
    await waitFor(() => expect(remove.result.current.isSuccess).toBe(true))

    await waitFor(() => expect(listRequestCount).toBe(2))
  })

  it("surfaces the backend's validation message if the card doesn't belong to this user", async () => {
    server.use(
      http.delete(`${BASE_URL}/:paymentMethodId`, () =>
        HttpResponse.json({ error: 'Payment method not found' }, { status: 400 }),
      ),
    )

    const { result } = renderHook(() => useRemovePaymentMethod(), { wrapper: createQueryWrapper() })

    result.current.mutate('pm_not_mine')

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect(result.current.error?.message).toBe('Payment method not found')
  })
})

describe('useSetDefaultPaymentMethod', () => {
  it('invalidates the payment-methods cache on success, refetching any active list query', async () => {
    let listRequestCount = 0
    server.use(
      http.get(BASE_URL, () => {
        listRequestCount += 1
        return HttpResponse.json([cardFixture()])
      }),
      http.put(`${BASE_URL}/:paymentMethodId/default`, () => new HttpResponse(null, { status: 204 })),
    )

    const queryClient = createTestQueryClient()
    const wrapper = createQueryWrapper(queryClient)

    const list = renderHook(() => usePaymentMethods(), { wrapper })
    await waitFor(() => expect(list.result.current.isSuccess).toBe(true))
    expect(listRequestCount).toBe(1)

    const setDefault = renderHook(() => useSetDefaultPaymentMethod(), { wrapper })
    setDefault.result.current.mutate('pm_123')
    await waitFor(() => expect(setDefault.result.current.isSuccess).toBe(true))

    await waitFor(() => expect(listRequestCount).toBe(2))
  })
})
