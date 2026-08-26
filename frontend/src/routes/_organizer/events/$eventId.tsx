import { Link, createFileRoute, useNavigate } from '@tanstack/react-router'
import {
    EventForm,
    eventToFormValues,
    formValuesToRequest,
} from '#/features/events/components/EventForm'
import type { EventFormValues } from '#/features/events/components/EventForm'
import { useEvent, useUpdateEvent } from '#/features/events/hooks'

export const Route = createFileRoute('/_organizer/events/$eventId')({
    component: RouteComponent,
})

function RouteComponent() {
    const { eventId } = Route.useParams()
    const navigate = useNavigate()
    const { data: event, isPending, isError } = useEvent(eventId)
    const updateEvent = useUpdateEvent(eventId)

    function handleSubmit(values: EventFormValues) {
        updateEvent.mutate(
            { ...formValuesToRequest(values), id: eventId },
            { onSuccess: () => void navigate({ to: '/events' }) },
        )
    }

    return (
        <main className="page-wrap px-4 py-12">
            <Link to="/events" className="nav-link mb-4 inline-block">
                &larr; Back to Events
            </Link>
            <h1 className="display-title mb-6 text-3xl font-bold text-(--sea-ink)">
                Edit Event
            </h1>

            {isPending ? (
                <p className="text-sm text-(--sea-ink-soft)">Loading event...</p>
            ) : isError ? (
                <p className="text-sm text-destructive">Couldn't load this event.</p>
            ) : (
                <EventForm
                    defaultValues={eventToFormValues(event)}
                    actions={[
                        {
                            label: 'Save Changes',
                            onSubmit: handleSubmit,
                            isSubmitting: updateEvent.isPending,
                        },
                    ]}
                />
            )}
        </main>
    )
}
