import { Link, createFileRoute } from '@tanstack/react-router'
import { Button } from '#/components/ui/button'
import { PaginatedTable } from '#/components/PaginatedTable'
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
          <h1 className="display-title text-3xl font-bold text-(--sea-ink)">
            Venues
          </h1>
        </div>
        <Button asChild>
          <Link to="/venues/new">+ Add Venue</Link>
        </Button>
      </div>

      <PaginatedTable
        items={data?.content ?? []}
        isPending={isPending}
        isError={isError}
        page={page}
        size={size}
        totalPages={data?.totalPages ?? 0}
        totalElements={data?.totalElements ?? 0}
        itemLabel="venue"
        getRowKey={(venue) => venue.id}
        emptyMessage="No venues yet. Add one before creating an event."
        loadingMessage="Loading venues..."
        errorMessage="Couldn't load venues. Try refreshing."
        columns={[
          { header: 'Name', cell: (venue) => venue.name },
          { header: 'City', cell: (venue) => venue.city },
          { header: 'Capacity', cell: (venue) => venue.capacity ?? '—' },
          {
            header: 'Actions',
            className: 'text-right',
            cell: (venue) => (
              <Button asChild variant="outline" size="sm">
                <Link to="/venues/$venueId" params={{ venueId: venue.id }}>
                  Edit
                </Link>
              </Button>
            ),
          },
        ]}
      />
    </main>
  )
}
