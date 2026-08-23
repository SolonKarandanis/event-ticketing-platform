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
import { PaginationControls } from '#/components/PaginationControls'
import { useVenues } from '#/features/venues/hooks'
import { DEFAULT_PAGE, DEFAULT_SIZE, paginationSearchSchema } from '#/lib/pagination'

export const Route = createFileRoute('/_organizer/venues/')({
  validateSearch: paginationSearchSchema.parse,
  component: VenuesList,
})

function VenuesList() {
  const search = Route.useSearch()
  const page = search.page ?? DEFAULT_PAGE
  const size = search.size ?? DEFAULT_SIZE
  const { data, isPending, isError } = useVenues({ page: page - 1, size })

  return (
    <main className="page-wrap px-4 py-12">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <p className="island-kicker mb-2">Organizer</p>
          <h1 className="display-title text-3xl font-bold text-(--sea-ink)">
            Venues
          </h1>
        </div>
        <Button asChild>
          <Link to="/venues/new">+ Add Venue</Link>
        </Button>
      </div>

      {isPending ? (
        <p className="text-sm text-(--sea-ink-soft)">Loading venues...</p>
      ) : isError ? (
        <p className="text-sm text-destructive">
          Couldn't load venues. Try refreshing.
        </p>
      ) : data.content.length === 0 && page === 1 ? (
        <p className="text-sm text-(--sea-ink-soft)">
          No venues yet. Add one before creating an event.
        </p>
      ) : (
        <>
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
                {data.content.map((venue) => (
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

          <PaginationControls
            page={page}
            size={size}
            totalPages={data.totalPages}
            totalElements={data.totalElements}
            itemLabel="venue"
          />
        </>
      )}
    </main>
  )
}
