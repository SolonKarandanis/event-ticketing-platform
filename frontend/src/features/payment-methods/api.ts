// Typed fetch functions for the payment-methods resource -- see issue #18 (#19).
import { apiFetch, parseJsonOrThrow, throwIfNotOk } from '#/lib/api-client'
import type { PaymentMethodResponse, SetupIntentResponse } from './types'

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/payment-methods`

export async function createSetupIntent(): Promise<SetupIntentResponse> {
  const response = await apiFetch(`${BASE_URL}/setup-intents`, { method: 'POST' })
  return parseJsonOrThrow<SetupIntentResponse>(response)
}

export async function listPaymentMethods(): Promise<PaymentMethodResponse[]> {
  const response = await apiFetch(BASE_URL)
  return parseJsonOrThrow<PaymentMethodResponse[]>(response)
}

export async function removePaymentMethod(paymentMethodId: string): Promise<void> {
  const response = await apiFetch(`${BASE_URL}/${paymentMethodId}`, { method: 'DELETE' })
  return throwIfNotOk(response)
}

export async function setDefaultPaymentMethod(paymentMethodId: string): Promise<void> {
  const response = await apiFetch(`${BASE_URL}/${paymentMethodId}/default`, { method: 'PUT' })
  return throwIfNotOk(response)
}
