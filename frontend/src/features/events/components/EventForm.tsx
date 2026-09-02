import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useFieldArray, useForm } from 'react-hook-form'
import { Button } from '#/components/ui/button'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '#/components/ui/form'
import { Input } from '#/components/ui/input'
import { VenueCombobox } from '#/features/venues/components/VenueCombobox'
import { emptyTicketType, emptyValues, eventFormSchema } from '../forms'
import type { EventFormValues } from '../forms'
import { EventImageGallery } from './EventImageGallery'
import { TicketTypeRow } from './TicketTypeRow'

// A row of one or more submit-type actions -- the edit page has just one ("Save
// Changes"), the create page has two ("Save as Draft" / "Publish", chained
// create-then-publish per issue #6), each independently validating and reading the
// same form state via form.handleSubmit.
export interface EventFormAction {
  label: string
  onSubmit: (values: EventFormValues) => void
  isSubmitting: boolean
  variant?: 'default' | 'outline'
}

interface EventFormProps {
  defaultValues?: EventFormValues
  // Absent on the create page (nothing exists yet for a new image to attach to);
  // present on the edit page, threaded down to EventImageGallery so an existing
  // image's thumbnail can be fetched. See EventImageRow/useEventImage.
  eventId?: string
  // An empty array means "read-only": no submit buttons render, and every field
  // (including ticket-type rows and the add/remove controls) is disabled -- the
  // terminal-status case (CANCELLED/COMPLETED) from issue #6, where the backend
  // itself rejects any update once an event is terminal.
  actions: EventFormAction[]
}

export function EventForm({ defaultValues, eventId, actions }: EventFormProps) {
  const form = useForm<EventFormValues>({
    resolver: zodResolver(eventFormSchema),
    defaultValues: defaultValues ?? emptyValues,
  })

  const ticketTypes = useFieldArray({
    control: form.control,
    name: 'ticketTypes',
  })

  // A two-step form, not two separate pages: step 1 is every field this form has
  // always had, step 2 is the image gallery. Purely a local view toggle -- switching
  // steps never touches the server, and Save as Draft/Publish/Save Changes (below)
  // always submits the whole form regardless of which step is currently showing, so
  // "Next" is independent of them rather than gating them.
  const [step, setStep] = useState<1 | 2>(1)

  // .at(0), not array destructuring -- destructuring types actions[0] as always-defined
  // (TS doesn't add | undefined without noUncheckedIndexedAccess), which would hide the
  // real possibility of an empty actions array (the read-only case) from the type checker.
  const primaryAction = actions.at(0)
  const secondaryActions = actions.slice(1)
  const readOnly = actions.length === 0
  const anySubmitting = actions.some((action) => action.isSubmitting)

  return (
    <Form {...form}>
      <form
        onSubmit={
          primaryAction
            ? form.handleSubmit(primaryAction.onSubmit)
            : (event) => event.preventDefault()
        }
        className="grid max-w-2xl gap-5"
      >
        {step === 1 && (
          <>
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Name</FormLabel>
                  <FormControl>
                    <Input disabled={readOnly} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-2 gap-5">
              <FormField
                control={form.control}
                name="start"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Start (optional)</FormLabel>
                    <FormControl>
                      <Input
                        type="datetime-local"
                        disabled={readOnly}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="end"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>End (optional)</FormLabel>
                    <FormControl>
                      <Input
                        type="datetime-local"
                        disabled={readOnly}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="venueId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Venue</FormLabel>
                  <FormControl>
                    <VenueCombobox
                      value={field.value}
                      onChange={field.onChange}
                      disabled={readOnly}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-2 gap-5">
              <FormField
                control={form.control}
                name="salesStart"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Sales Start (optional)</FormLabel>
                    <FormControl>
                      <Input
                        type="datetime-local"
                        disabled={readOnly}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="salesEnd"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Sales End (optional)</FormLabel>
                    <FormControl>
                      <Input
                        type="datetime-local"
                        disabled={readOnly}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="grid gap-4">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold text-(--sea-ink)">
                  Ticket Types
                </h2>
                {readOnly ? null : (
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => ticketTypes.append(emptyTicketType)}
                  >
                    + Add Ticket Type
                  </Button>
                )}
              </div>

              {ticketTypes.fields.map((field, index) => (
                <TicketTypeRow
                  key={field.id}
                  control={form.control}
                  index={index}
                  canRemove={ticketTypes.fields.length > 1}
                  onRemove={() => ticketTypes.remove(index)}
                  disabled={readOnly}
                />
              ))}

              {form.formState.errors.ticketTypes?.root ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.ticketTypes.root.message}
                </p>
              ) : null}
            </div>
          </>
        )}

        {step === 2 && (
          <EventImageGallery
            control={form.control}
            eventId={eventId}
            disabled={readOnly}
          />
        )}

        {/* Independent of Save as Draft/Publish/Save Changes below -- a pure
                    local view toggle, never a submit. Kept visible even when readOnly
                    (a terminal event) so its images can still be browsed to. */}
        <div className="flex gap-3">
          {step === 2 && (
            <Button type="button" variant="outline" onClick={() => setStep(1)}>
              Back
            </Button>
          )}
          {step === 1 && (
            <Button type="button" variant="outline" onClick={() => setStep(2)}>
              Next
            </Button>
          )}
        </div>

        {primaryAction ? (
          <div className="flex gap-3">
            <Button
              type="submit"
              variant={primaryAction.variant ?? 'default'}
              disabled={anySubmitting}
            >
              {primaryAction.isSubmitting ? 'Saving...' : primaryAction.label}
            </Button>
            {secondaryActions.map((action) => (
              <Button
                key={action.label}
                type="button"
                variant={action.variant ?? 'outline'}
                disabled={anySubmitting}
                onClick={form.handleSubmit(action.onSubmit)}
              >
                {action.isSubmitting ? 'Saving...' : action.label}
              </Button>
            ))}
          </div>
        ) : null}
      </form>
    </Form>
  )
}
