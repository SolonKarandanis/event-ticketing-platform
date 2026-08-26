import { Link, createFileRoute, useNavigate } from '@tanstack/react-router'
import { ConfirmButton } from '#/components/ConfirmButton'
import { Button } from '#/components/ui/button'
import {
    EventForm,
    eventToFormValues,
    formValuesToRequest,
} from '#/features/events/components/EventForm'
import type { EventFormValues } from '#/features/events/components/EventForm'
import {
    useCancelEvent,
    useCompleteEvent,
    useDeleteEvent,
    useEvent,
    usePublishEvent,
    useUpdateEvent,
} from '#/features/events/hooks'

export const Route = createFileRoute('/_organizer/events/$eventId')({
    component: RouteComponent,
})

function RouteComponent() {
    const { eventId } = Route.useParams()
    const navigate = useNavigate()
    const { data: event, isPending, isError } = useEvent(eventId)

    const updateEvent = useUpdateEvent(eventId)
    const publishEvent = usePublishEvent(eventId)
    const cancelEvent = useCancelEvent(eventId)
    const completeEvent = useCompleteEvent(eventId)
    const deleteEvent = useDeleteEvent(eventId)

    function handleSubmit(values: EventFormValues) {
        updateEvent.mutate(
            { ...formValuesToRequest(values), id: eventId },
            { onSuccess: () => void navigate({ to: '/events' }) },
        )
    }

    function handleDelete() {
        deleteEvent.mutate(undefined, {
            onSuccess: () => void navigate({ to: '/events' }),
        })
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
                <>
                    {/* Terminal per EventServiceImpl.updateEventForOrganizer -- the backend itself
                        rejects any field update once an event is CANCELLED/COMPLETED, so the form
                        renders fully read-only (actions=[]) rather than letting an edit fail at
                        submit time. */}
                    {event.status === 'CANCELLED' || event.status === 'COMPLETED' ? (
                        <p className="mb-4 text-sm text-(--sea-ink-soft)">
                            This event is {event.status.toLowerCase()} and can no longer be edited.
                        </p>
                    ) : null}

                    <EventForm
                        defaultValues={eventToFormValues(event)}
                        actions={
                            event.status === 'CANCELLED' || event.status === 'COMPLETED'
                                ? []
                                : [
                                      {
                                          label: 'Save Changes',
                                          onSubmit: handleSubmit,
                                          isSubmitting: updateEvent.isPending,
                                      },
                                  ]
                        }
                    />

                    {/* Publish/Cancel/Complete/Delete are independent of the form's own save --
                        publishing a draft, say, applies to whatever's already persisted, not
                        unsaved edits sitting in the form above. Matches EventServiceImpl exactly:
                        publish only from DRAFT, cancel/complete only from PUBLISHED. Delete has no
                        status guard on the backend at all, but stays Draft-only here per the
                        decided design -- a published event with real ticket sales shouldn't be
                        one click from disappearing. */}
                    {event.status === 'DRAFT' ? (
                        <div className="mt-4 flex gap-3">
                            <Button
                                type="button"
                                onClick={() => publishEvent.mutate()}
                                disabled={publishEvent.isPending}
                            >
                                {publishEvent.isPending ? 'Publishing...' : 'Publish'}
                            </Button>
                            <ConfirmButton
                                label={deleteEvent.isPending ? 'Deleting...' : 'Delete'}
                                title="Delete this event?"
                                description="This permanently removes the event and its ticket types. This can't be undone."
                                confirmLabel="Delete"
                                variant="destructive"
                                disabled={deleteEvent.isPending}
                                onConfirm={handleDelete}
                            />
                        </div>
                    ) : null}

                    {event.status === 'PUBLISHED' ? (
                        <div className="mt-4 flex gap-3">
                            <Button
                                type="button"
                                onClick={() => completeEvent.mutate()}
                                disabled={completeEvent.isPending}
                            >
                                {completeEvent.isPending ? 'Completing...' : 'Complete'}
                            </Button>
                            <ConfirmButton
                                label={cancelEvent.isPending ? 'Cancelling...' : 'Cancel Event'}
                                title="Cancel this event?"
                                description="Attendees who already have tickets aren't notified automatically -- this only changes the event's status. This can't be undone."
                                confirmLabel="Cancel Event"
                                variant="destructive"
                                disabled={cancelEvent.isPending}
                                onConfirm={() => cancelEvent.mutate()}
                            />
                        </div>
                    ) : null}
                </>
            )}
        </main>
    )
}
