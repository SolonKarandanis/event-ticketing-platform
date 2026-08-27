import { Link, createFileRoute } from '@tanstack/react-router'
import { usePublishedEvent } from '#/features/published-events/hooks'

export const Route = createFileRoute('/browse/$eventId')({
    component: EventDetails,
})

function formatDateTime(value: string | null): string {
    if (!value) {
        return 'TBA'
    }
    return new Date(value).toLocaleString(undefined, {
        dateStyle: 'medium',
        timeStyle: 'short',
    })
}

function EventDetails() {
    const { eventId } = Route.useParams()
    const { data: event, isPending, isError } = usePublishedEvent(eventId)

    return (
        <main className="page-wrap px-4 py-12">
            <Link to="/browse" className="nav-link mb-4 inline-block">
                &larr; Back to Events
            </Link>
            {isPending &&<p className="text-sm text-(--sea-ink-soft)">Loading event...</p>}
            {!isPending && isError &&(
                <p className="text-sm text-destructive">
                    Couldn't find this event. It may no longer be published.
                </p>
            )}
            {!isPending && !isError &&(
                <>
                    <h1 className="display-title mb-2 text-3xl font-bold text-(--sea-ink)">
                        {event.name}
                    </h1>
                    <p className="text-sm text-(--sea-ink-soft)">
                        {formatDateTime(event.start)}
                        {event.end ? ` - ${formatDateTime(event.end)}` : ''}
                    </p>
                    <p className="mb-8 text-sm text-(--sea-ink-soft)">
                        {event.venue.name}, {event.venue.addressLine1}, {event.venue.city}
                    </p>

                    <h2 className="mb-4 text-lg font-semibold text-(--sea-ink)">Ticket Types</h2>
                    <div className="grid max-w-xl gap-3">
                        {event.ticketTypes.map((ticketType) => (
                            <div key={ticketType.id} className="island-shell rounded-xl p-4">
                                <div className="flex items-center justify-between">
                                    <span className="font-medium text-(--sea-ink)">{ticketType.name}</span>
                                    <span className="text-(--sea-ink)">${ticketType.price.toFixed(2)}</span>
                                </div>
                                {ticketType.description ? (
                                    <p className="mt-1 text-sm text-(--sea-ink-soft)">
                                        {ticketType.description}
                                    </p>
                                ) : null}
                            </div>
                        ))}
                    </div>
                </>
            )}
        </main>
    )
}
