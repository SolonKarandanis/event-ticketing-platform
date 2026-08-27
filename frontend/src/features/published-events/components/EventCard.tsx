import { Link } from '@tanstack/react-router'
import type { ListPublishedEventResponse } from '../types'

// Text-only, no image -- Event has no image field at all yet (flagged for future
// while resolving issue #17, not built).
function formatEventDate(start: string | null): string {
    if (!start) {
        return 'Date TBA'
    }
    return new Date(start).toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
    })
}

export function EventCard({ event }: { event: ListPublishedEventResponse }) {
    return (
        <Link
            to="/browse/$eventId"
            params={{ eventId: event.id }}
            className="island-shell block rounded-xl p-5 transition hover:-translate-y-0.5"
        >
            <h3 className="font-semibold text-(--sea-ink)">{event.name}</h3>
            <p className="mt-1 text-sm text-(--sea-ink-soft)">{formatEventDate(event.start)}</p>
            <p className="text-sm text-(--sea-ink-soft)">
                {event.venue.name}, {event.venue.city}
            </p>
        </Link>
    )
}
