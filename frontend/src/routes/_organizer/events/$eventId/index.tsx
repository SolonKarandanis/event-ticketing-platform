import { Link, createFileRoute, useNavigate } from '@tanstack/react-router'
import { ConfirmButton } from '#/components/ConfirmButton'
import { Button } from '#/components/ui/button'
import { EventForm } from '#/features/events/components/EventForm'
import { eventToFormValues, formValuesToRequest } from '#/features/events/forms'
import type { EventFormValues } from '#/features/events/forms'
import {
  eventQueryOptions,
  useCancelEvent,
  useCompleteEvent,
  useDeleteEvent,
  useEvent,
  usePublishEvent,
  useUpdateEvent,
} from '#/features/events/hooks'
import { EventStatus } from '#/features/events/types'
import { venueSearchInfiniteQueryOptions } from '#/features/venues/hooks'

export const Route = createFileRoute('/_organizer/events/$eventId/')({
  // Two independent prefetches: the event itself (same warm-cache trick as the list
  // route -- fires on navigation and on intent-preload, hovering an "Edit" link), and
  // VenueCombobox's own first page (EventForm renders it regardless of read-only
  // status -- see events/new.tsx for why that's ensureInfiniteQueryData, not
  // ensureQueryData). Each caught independently: no errorComponent here, so an
  // uncaught rejection from either would bypass its own component-level handling
  // (useEvent()'s isError message, VenueCombobox's own loading/empty state) and hit
  // the router's generic error boundary instead -- and one failing shouldn't cancel
  // the other's prefetch.
  loader: ({ context, params }) =>
    Promise.all([
      context.queryClient
        .ensureQueryData(eventQueryOptions(params.eventId))
        .catch(() => {
          // Handled by useEvent()'s isError below.
        }),
      context.queryClient
        .ensureInfiniteQueryData(venueSearchInfiniteQueryOptions(''))
        .catch(() => {
          // Handled by VenueCombobox's own isPending/empty-result UI.
        }),
    ]),
  component: RouteComponent,
})

function RouteComponent() {
  const { eventId } = Route.useParams()
  const navigate = useNavigate()
  const { data: event, isPending, isError } = useEvent(eventId)
  const isTerminal =
    event?.status === EventStatus.CANCELLED ||
    event?.status === EventStatus.COMPLETED

  const updateEvent = useUpdateEvent(eventId)
  const publishEvent = usePublishEvent(eventId)
  const cancelEvent = useCancelEvent(eventId)
  const completeEvent = useCompleteEvent(eventId)
  const deleteEvent = useDeleteEvent(eventId)

  function handleSubmit(values: EventFormValues) {
    const { request, newImageFiles } = formValuesToRequest(values)
    updateEvent.mutate(
      { request: { ...request, id: eventId }, newImageFiles },
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
      <div className="mb-6 flex items-center justify-between">
        <h1 className="display-title text-3xl font-bold text-(--sea-ink)">
          Edit Event
        </h1>
        <Link
          to="/events/$eventId/tickets"
          params={{ eventId }}
          className="nav-link"
        >
          Ticket Sales
        </Link>
      </div>
      {isPending && (
        <p className="text-sm text-(--sea-ink-soft)">Loading event...</p>
      )}
      {!isPending && isError && (
        <p className="text-sm text-destructive">Couldn't load this event.</p>
      )}
      {!isPending && !isError && (
        <>
          {/* Terminal per EventServiceImpl.updateEventForOrganizer -- the backend itself
                        rejects any field update once an event is CANCELLED/COMPLETED, so the form
                        renders fully read-only (actions=[]) rather than letting an edit fail at
                        submit time. */}
          {isTerminal && (
            <p className="mb-4 text-sm text-(--sea-ink-soft)">
              This event is {event.status.toLowerCase()} and can no longer be
              edited.
            </p>
          )}
          <EventForm
            defaultValues={eventToFormValues(event)}
            eventId={eventId}
            actions={
              isTerminal
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
          {event.status === EventStatus.DRAFT && (
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
          )}
          {event.status === EventStatus.PUBLISHED && (
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
                description="Every ticket already sold for this event is cancelled too (already-admitted attendees are left alone). Attendees aren't notified automatically. This can't be undone."
                confirmLabel="Cancel Event"
                variant="destructive"
                disabled={cancelEvent.isPending}
                onConfirm={() => cancelEvent.mutate()}
              />
            </div>
          )}
        </>
      )}
    </main>
  )
}
