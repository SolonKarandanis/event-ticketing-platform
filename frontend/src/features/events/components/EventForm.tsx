import { zodResolver } from '@hookform/resolvers/zod'
import { useFieldArray, useForm } from 'react-hook-form'
import { z } from 'zod'
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
import { isIntegerOrEmpty, isNonNegativeDecimal } from '#/lib/validation'
import { TicketTypeRow } from './TicketTypeRow'
import type { GetEventDetailsResponse, UpdateEventRequest } from '../types'

// Mirrors CreateTicketTypeRequestDto/UpdateTicketTypeRequestDto -- id is absent for a
// ticket type being added, present (and matched against the existing row) for one
// being edited. `limitedQuantity` isn't a backend field at all: it's the "Limited
// quantity" toggle from issue #6, driving whether totalAvailable gets sent at all when
// building the request.
const ticketTypeFormSchema = z
    .object({
        id: z.string().optional(),
        name: z.string().trim().min(1, 'Name is required'),
        price: z.string().trim().refine(isNonNegativeDecimal, 'Must be a non-negative number'),
        description: z.string().trim(),
        limitedQuantity: z.boolean(),
        totalAvailable: z.string().trim(),
    })
    .superRefine((ticketType, ctx) => {
        if (ticketType.limitedQuantity && ticketType.totalAvailable === '') {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                path: ['totalAvailable'],
                message: 'Required when quantity is limited',
            })
            return
        }
        if (!isIntegerOrEmpty(ticketType.totalAvailable)) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                path: ['totalAvailable'],
                message: 'Must be a whole number',
            })
        }
    })

// start/end/salesStart/salesEnd hold whatever an <input type="datetime-local"> hands
// back ("" or "YYYY-MM-DDTHH:mm") -- none are required on CreateEventRequestDto, so
// blank is valid for each individually; the cross-field rules below only fire once the
// relevant pair is actually present, mirroring EventServiceImpl.validateEventDates
// exactly (including that it's a >/<=  comparison, not >=/<, so equal timestamps are
// only rejected where the backend itself rejects them).
const eventFormSchema = z
    .object({
        name: z.string().trim().min(1, 'Name is required'),
        start: z.string(),
        end: z.string(),
        venueId: z.string().min(1, 'Venue is required'),
        salesStart: z.string(),
        salesEnd: z.string(),
        ticketTypes: z.array(ticketTypeFormSchema).min(1, 'At least one ticket type is required'),
    })
    .superRefine((event, ctx) => {
        const { start, end, salesStart, salesEnd } = event

        if (start && end && end <= start) {
            ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['end'], message: 'End must be after start' })
        }

        if (start && salesEnd && salesEnd > start) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                path: ['salesEnd'],
                message: 'Sales must end by the event start',
            })
        }

        if (salesStart && salesEnd && salesEnd <= salesStart) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                path: ['salesEnd'],
                message: 'Sales end must be after sales start',
            })
        }
    })

export type EventFormValues = z.infer<typeof eventFormSchema>
export type TicketTypeFormValues = z.infer<typeof ticketTypeFormSchema>

// Seeds a freshly-added row (via useFieldArray().append) and the create form's initial
// single row -- ticketTypes needs at least one entry to satisfy the .min(1) above, so
// "no ticket types yet" has to be "one blank one", not an empty array.
const emptyTicketType: TicketTypeFormValues = {
    id: undefined,
    name: '',
    price: '',
    description: '',
    limitedQuantity: false,
    totalAvailable: '',
}

const emptyValues: EventFormValues = {
    name: '',
    start: '',
    end: '',
    venueId: '',
    salesStart: '',
    salesEnd: '',
    ticketTypes: [emptyTicketType],
}

// event.start/end/salesStart/salesEnd are already "YYYY-MM-DDTHH:mm[:ss]" LocalDateTime
// strings -- that's exactly what <input type="datetime-local"> accepts as a value, so
// no reformatting is needed, just the null -> '' fallback every optional field gets.
export function eventToFormValues(event: GetEventDetailsResponse): EventFormValues {
    return {
        name: event.name,
        start: event.start ?? '',
        end: event.end ?? '',
        venueId: event.venue.id,
        salesStart: event.salesStart ?? '',
        salesEnd: event.salesEnd ?? '',
        ticketTypes: event.ticketTypes.map((ticketType) => ({
            id: ticketType.id,
            name: ticketType.name,
            price: String(ticketType.price),
            description: ticketType.description ?? '',
            limitedQuantity: ticketType.totalAvailable !== null,
            totalAvailable: ticketType.totalAvailable !== null ? String(ticketType.totalAvailable) : '',
        })),
    }
}

// Shaped like UpdateEventRequest minus `id` (which the route adds on top, the same way
// VenueForm's callers do) rather than CreateEventRequest -- CreateTicketTypeRequest has
// no `id` field at all, so a ticket type row carrying one (from an existing row being
// edited) wouldn't satisfy it, while UpdateTicketTypeRequest's optional `id` covers both
// "new row" (undefined) and "existing row" (set) in one shape. A plain CreateEventRequest
// is still assignable from this when creating, since a variable with an extra optional
// property is fine where the narrower type is expected -- it's only a fresh object
// literal that would trip the excess-property check.
export function formValuesToRequest(values: EventFormValues): Omit<UpdateEventRequest, 'id'> {
    return {
        name: values.name,
        start: values.start || undefined,
        end: values.end || undefined,
        venueId: values.venueId,
        salesStart: values.salesStart || undefined,
        salesEnd: values.salesEnd || undefined,
        ticketTypes: values.ticketTypes.map((ticketType) => ({
            id: ticketType.id,
            name: ticketType.name,
            price: Number(ticketType.price),
            description: ticketType.description || undefined,
            totalAvailable: ticketType.limitedQuantity ? Number(ticketType.totalAvailable) : undefined,
        })),
    }
}

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
    actions: EventFormAction[]
}

export function EventForm({ defaultValues, actions }: EventFormProps) {
    const form = useForm<EventFormValues>({
        resolver: zodResolver(eventFormSchema),
        defaultValues: defaultValues ?? emptyValues,
    })

    const ticketTypes = useFieldArray({ control: form.control, name: 'ticketTypes' })

    const [primaryAction, ...secondaryActions] = actions
    const anySubmitting = actions.some((action) => action.isSubmitting)

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(primaryAction.onSubmit)} className="grid max-w-2xl gap-5">
                <FormField
                    control={form.control}
                    name="name"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Name</FormLabel>
                            <FormControl>
                                <Input {...field} />
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
                                    <Input type="datetime-local" {...field} />
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
                                    <Input type="datetime-local" {...field} />
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
                                <VenueCombobox value={field.value} onChange={field.onChange} />
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
                                    <Input type="datetime-local" {...field} />
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
                                    <Input type="datetime-local" {...field} />
                                </FormControl>
                                <FormMessage />
                            </FormItem>
                        )}
                    />
                </div>

                <div className="grid gap-4">
                    <div className="flex items-center justify-between">
                        <h2 className="text-lg font-semibold text-(--sea-ink)">Ticket Types</h2>
                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => ticketTypes.append(emptyTicketType)}
                        >
                            + Add Ticket Type
                        </Button>
                    </div>

                    {ticketTypes.fields.map((field, index) => (
                        <TicketTypeRow
                            key={field.id}
                            control={form.control}
                            index={index}
                            canRemove={ticketTypes.fields.length > 1}
                            onRemove={() => ticketTypes.remove(index)}
                        />
                    ))}

                    {form.formState.errors.ticketTypes?.root ? (
                        <p className="text-sm text-destructive">
                            {form.formState.errors.ticketTypes.root.message}
                        </p>
                    ) : null}
                </div>

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
            </form>
        </Form>
    )
}