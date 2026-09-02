import { Link } from '@tanstack/react-router'
import { publishedEventImageUrl } from '../api'
import type { ListPublishedEventResponse } from '../types'

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
      className="island-shell block overflow-hidden rounded-xl transition hover:-translate-y-0.5"
    >
      {event.coverImageId ? (
        <img
          src={publishedEventImageUrl(event.id, event.coverImageId)}
          alt=""
          className="h-40 w-full object-cover"
        />
      ) : null}
      <div className="p-5">
        <h3 className="font-semibold text-(--sea-ink)">{event.name}</h3>
        <p className="mt-1 text-sm text-(--sea-ink-soft)">
          {formatEventDate(event.start)}
        </p>
        <p className="text-sm text-(--sea-ink-soft)">
          {event.venue.name}, {event.venue.city}
        </p>
      </div>
    </Link>
  )
}
