// The Wallet Grid's "Add Card" flow -- see issue #18 (#19). <Elements> needs a
// clientSecret up front (its options can't change after mount), so a SetupIntent is
// created fresh every time this dialog opens -- not eagerly on mount, and not reused
// across opens: createSetupIntent.reset() on close means the next open always starts
// clean rather than risking a stale/already-used clientSecret.
import { useState } from 'react'
import type { FormEvent } from 'react'
import { Elements, PaymentElement, useElements, useStripe } from '@stripe/react-stripe-js'
import { useQueryClient } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '#/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '#/components/ui/dialog'
import { paymentMethodsKey, useCreateSetupIntent } from '#/features/payment-methods/hooks'
import { getStripe } from '#/lib/stripe'

interface AddCardFormProps {
  onSuccess: () => void
  onCancel: () => void
}

// Split from AddCardDialog because useStripe()/useElements() only resolve to a real value
// inside an <Elements> provider -- this only ever renders as *its* child, once a
// clientSecret already exists.
function AddCardForm({ onSuccess, onCancel }: AddCardFormProps) {
  const stripe = useStripe()
  const elements = useElements()
  const [isConfirming, setIsConfirming] = useState(false)
  // Stripe's own card-validation errors (bad expiry, incomplete number) render inline
  // inside <PaymentElement> itself -- this is only for the separate class of error
  // confirmSetup() itself returns (a decline, a processing failure) once an otherwise
  // valid card has already been submitted to Stripe.
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    // Belt-and-suspenders: the submit button below is already disabled until both of
    // these resolve, so in practice this only guards a submit fired before Stripe.js has
    // finished loading in the current tab.
    if (!stripe || !elements) {
      return
    }

    setErrorMessage(null)
    setIsConfirming(true)

    const { error, setupIntent } = await stripe.confirmSetup({
      elements,
      redirect: 'if_required',
      // Not required by the type when redirect is 'if_required' -- only consulted in the
      // rare case a specific card issuer's authentication step needs a real top-level
      // redirect. There's no dedicated return page for that (unlike checkout's
      // success.tsx), so this just points back at the same page; if that path is ever
      // hit, the browser navigates away and this function never resumes. The user lands
      // back here with the card already attached (Stripe completes the SetupIntent
      // server-side before redirecting back), so a normal page load/refetch covers it --
      // no query-param handling added here for it.
      confirmParams: {
        return_url: `${window.location.origin}/payment-methods`,
      },
    })

    setIsConfirming(false)

    if (error) {
      setErrorMessage(error.message ?? "Couldn't save card. Try again.")
      return
    }

    if (setupIntent.status === 'succeeded') {
      onSuccess()
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <PaymentElement />
      {errorMessage && <p className="text-sm text-destructive">{errorMessage}</p>}
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onCancel} disabled={isConfirming}>
          Cancel
        </Button>
        <Button type="submit" disabled={!stripe || !elements || isConfirming}>
          {isConfirming ? 'Adding...' : 'Add Card'}
        </Button>
      </DialogFooter>
    </form>
  )
}

export function AddCardDialog() {
  const [open, setOpen] = useState(false)
  const queryClient = useQueryClient()
  const createSetupIntent = useCreateSetupIntent()

  function handleOpenChange(nextOpen: boolean) {
    setOpen(nextOpen)
    if (nextOpen) {
      // Fired directly from this handler, not a useEffect keyed on `open` -- an effect
      // would double-fire under React's dev StrictMode double-invoke and mint two
      // SetupIntents for one open.
      createSetupIntent.mutate()
    } else {
      createSetupIntent.reset()
    }
  }

  function handleSuccess() {
    setOpen(false)
    createSetupIntent.reset()
    queryClient.invalidateQueries({ queryKey: paymentMethodsKey })
    toast.success('Card added')
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <button
          type="button"
          className="flex aspect-[1.6/1] flex-col items-center justify-center gap-2 rounded-2xl border-2 border-dashed border-(--line) text-(--sea-ink-soft) hover:border-(--lagoon-deep) hover:text-(--lagoon-deep)"
        >
          <Plus className="h-6 w-6" />
          <span className="text-sm font-medium">Add Card</span>
        </button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add a card</DialogTitle>
          <DialogDescription>
            Your card details go directly to Stripe and never pass through our servers.
          </DialogDescription>
        </DialogHeader>

        {createSetupIntent.isPending && (
          <p className="text-sm text-(--sea-ink-soft)">Preparing secure payment form…</p>
        )}

        {createSetupIntent.isError && (
          <div className="space-y-3">
            <p className="text-sm text-destructive">Couldn't start adding a card.</p>
            <Button variant="outline" onClick={() => createSetupIntent.mutate()}>
              Retry
            </Button>
          </div>
        )}

        {createSetupIntent.data && (
          <Elements
            stripe={getStripe()}
            options={{ clientSecret: createSetupIntent.data.clientSecret }}
          >
            <AddCardForm onSuccess={handleSuccess} onCancel={() => handleOpenChange(false)} />
          </Elements>
        )}
      </DialogContent>
    </Dialog>
  )
}
