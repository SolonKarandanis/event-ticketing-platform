import { createFileRoute, Link, useNavigate } from '@tanstack/react-router'
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { toastErrorMessage } from '#/lib/api-client'
import { publishEvent } from '#/features/events/api'
import { useCreateEvent } from '#/features/events/hooks'
import { EventForm, formValuesToRequest } from '#/features/events/components/EventForm'
import type { EventFormValues } from '#/features/events/components/EventForm'

export const Route = createFileRoute('/_organizer/events/new')({
    component: RouteComponent,
})

function RouteComponent() {
    const navigate = useNavigate()

    // Two separate useCreateEvent() instances rather than one shared between both
    // buttons -- each tracks its own isPending independently, so "Save as Draft" and
    // "Publish" can show accurate individual loading state instead of both lighting up
    // together whenever either is clicked.
    const saveDraft = useCreateEvent()
    const createForPublish = useCreateEvent()

    // createEvent always creates in DRAFT status (see EventServiceImpl.createEvent) --
    // there's no "create as published" endpoint, so Publish is create-then-publish,
    // using the id the create call just returned. usePublishEvent doesn't fit here: it
    // takes eventId as a hook argument, which requires knowing the id before the hook
    // is even called -- impossible for an event that doesn't exist yet.
    const publishNewEvent = useMutation({
        mutationFn: (eventId: string) => publishEvent(eventId),
        onSuccess: () => toast.success('Event published'),
        onError: (error) => {
            toast.error(toastErrorMessage(error, "Couldn't publish event"))
        },
    })

    function handleSaveDraft(values: EventFormValues) {
        saveDraft.mutate(formValuesToRequest(values), {
            onSuccess: () => void navigate({ to: '/events' }),
        })
    }

    function handlePublish(values: EventFormValues) {
        createForPublish.mutate(formValuesToRequest(values), {
            onSuccess: (event) => {
                publishNewEvent.mutate(event.id, {
                    onSuccess: () => void navigate({ to: '/events' }),
                })
            },
        })
    }

    return (
        <main className="page-wrap px-4 py-12">
            <Link to="/events" className="nav-link mb-4 inline-block">
                &larr; Back to Events
            </Link>
            <h1 className="display-title mb-6 text-3xl font-bold text-(--sea-ink)">
                Add Event
            </h1>
            <EventForm
                actions={[
                    {
                        label: 'Save as Draft',
                        onSubmit: handleSaveDraft,
                        isSubmitting: saveDraft.isPending,
                    },
                    {
                        label: 'Publish',
                        onSubmit: handlePublish,
                        isSubmitting: createForPublish.isPending || publishNewEvent.isPending,
                        variant: 'default',
                    },
                ]}
            />
        </main>
    )
}
