// Named React Query hooks wrapping api.ts -- query keys and invalidation live here
// once, not duplicated at call sites. See issues #4, #5, #6.
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { toastErrorMessage } from '#/lib/api-client'
import { purchaseTicket } from './api'

// No onSuccess toast here: issue #5 decided a sequential "Purchasing... (X of Y)"
// progress indicator plus a dedicated Success/Partial confirmation page for the overall
// purchase, not a toast per ticket -- a quantity > 1 purchase calls this mutation N
// times, so a per-call toast would fire N times in a row instead of once at the end.
// No cache invalidation either: the attendee-facing GetPublishedEventDetailsResponse
// doesn't expose totalAvailable/ticketsSold at all, so there's no query left stale here.
export function usePurchaseTicket() {
  return useMutation({
    mutationFn: ({ eventId, ticketTypeId }: { eventId: string; ticketTypeId: string }) =>
      purchaseTicket(eventId, ticketTypeId),
    onError: (error) => {
      toast.error(toastErrorMessage(error, "Couldn't purchase ticket"))
    },
  })
}
