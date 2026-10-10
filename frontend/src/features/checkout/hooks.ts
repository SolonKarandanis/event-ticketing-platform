// Named React Query hooks wrapping api.ts -- see issue #18 (#20).
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { toastErrorMessage } from '#/lib/api-client'
import { createCheckout } from './api'
import type { CreateCheckoutRequest } from './types'

// No onSuccess here, deliberately: the only thing any caller ever does with a
// successful response is a one-off window.location.href redirect to Stripe -- that's
// $eventId.tsx's own side effect, not shared cache/navigation behavior worth
// centralizing the way onError's toast is. No cache invalidation either: nothing in
// this app queries order/checkout state yet.
export function useCreateCheckout() {
  return useMutation({
    mutationFn: ({ eventId, request }: { eventId: string; request: CreateCheckoutRequest }) =>
      createCheckout(eventId, request),
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't start checkout"))
    },
  })
}
