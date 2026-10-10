// Named React Query hooks wrapping api.ts -- see issue #18 (#19).
import { queryOptions, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { toastErrorMessage } from '#/lib/api-client'
import {
  createSetupIntent,
  listPaymentMethods,
  removePaymentMethod,
  setDefaultPaymentMethod,
} from './api'

// Exported (unlike every other feature's query key) because AddCardDialog needs to
// invalidate this exact cache entry after a successful client-side stripe.confirmSetup()
// call -- a side effect this feature's own hooks never see, since it happens directly
// against Stripe, not through any mutation below.
export const paymentMethodsKey = ['payment-methods'] as const

export function paymentMethodsQueryOptions() {
  return queryOptions({
    queryKey: paymentMethodsKey,
    queryFn: listPaymentMethods,
  })
}

export function usePaymentMethods() {
  return useQuery(paymentMethodsQueryOptions())
}

// No onSuccess beyond returning data -- AddCardDialog is the only caller, and it needs
// the raw clientSecret back to mount <Elements>, not a cache side effect. Creating a
// SetupIntent doesn't change the saved-card list; only a later successful
// confirmSetup() does, and that invalidation lives in AddCardDialog itself.
export function useCreateSetupIntent() {
  return useMutation({
    mutationFn: createSetupIntent,
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't start adding a card"))
    },
  })
}

export function useRemovePaymentMethod() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: removePaymentMethod,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: paymentMethodsKey })
      toast.success('Card removed')
    },
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't remove card"))
    },
  })
}

export function useSetDefaultPaymentMethod() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: setDefaultPaymentMethod,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: paymentMethodsKey })
      toast.success('Default payment method updated')
    },
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't update default payment method"))
    },
  })
}
