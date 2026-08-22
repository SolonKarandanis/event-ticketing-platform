import { Link, createFileRoute } from '@tanstack/react-router'
import { Button } from '#/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '#/components/ui/table'
import { useVenues } from '#/features/venues/hooks'

export const Route = createFileRoute('/_organizer/venues/')({
  component: VenuesList,
})

function VenuesList() {
  const { data: venues, isPending, isError } = useVenues()

  return (
    <main className="page-wrap px-4 py-12">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <p className="island-kicker mb-2">Organizer</p>
          <h1 className="display-title text-3xl font-bold text-[var(--sea-ink)]">
            Venues
          </h1>
        </div>
        <Button asChild>
          <Link to="/venues/new">+ Add Venue</Link>
        </Button>
      </div>

      {isPending ? (
        <p className="text-sm text-[var(--sea-ink-soft)]">Loading venues...</p>
      ) : isError ? (
        <p className="text-sm text-destructive">
          Couldn't load venues. Try refreshing.
        </p>
      ) : venues.length === 0 ? (
        <p className="text-sm text-[var(--sea-ink-soft)]">
          No venues yet. Add one before creating an event.
        </p>
      ) : (
        <div className="island-shell rounded-xl">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>City</TableHead>
                <TableHead>Capacity</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {venues.map((venue) => (
                <TableRow key={venue.id}>
                  <TableCell>{venue.name}</TableCell>
                  <TableCell>{venue.city}</TableCell>
                  <TableCell>{venue.capacity ?? '—'}</TableCell>
                  <TableCell className="text-right">
                    <Button asChild variant="outline" size="sm">
                      <Link
                        to="/venues/$venueId"
                        params={{ venueId: venue.id }}
                      >
                        Edit
                      </Link>
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </main>
  )
}
