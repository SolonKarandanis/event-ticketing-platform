import { Link, createFileRoute } from '@tanstack/react-router'
import { Button } from '#/components/ui/button'
import { PaginatedTable } from '#/components/PaginatedTable'
import { useTickets } from '#/features/tickets/hooks'
import {
  DEFAULT_PAGE,
  DEFAULT_SIZE,
  paginationSearchSchema,
} from '#/lib/pagination'

export const Route = createFileRoute('/_attendee/tickets/')({
  validateSearch: paginationSearchSchema.parse,
  component: MyTickets,
})

function MyTickets() {
  const search = Route.useSearch()
  const page = search.page ?? DEFAULT_PAGE
  const size = search.size ?? DEFAULT_SIZE
  const { data, isPending, isError } = useTickets({ page: page - 1, size })

  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker mb-2">Attendee</p>
      <h1 className="display-title mb-6 text-3xl font-bold text-(--sea-ink)">
        My Tickets
      </h1>

      <PaginatedTable
        items={data?.content ?? []}
        isPending={isPending}
        isError={isError}
        page={page}
        size={size}
        totalPages={data?.totalPages ?? 0}
        totalElements={data?.totalElements ?? 0}
        itemLabel="ticket"
        getRowKey={(ticket) => ticket.id}
        emptyMessage="No tickets yet. Browse events to find something to attend."
        loadingMessage="Loading tickets..."
        errorMessage="Couldn't load tickets. Try refreshing."
        columns={[
          { header: 'Ticket Type', cell: (ticket) => ticket.ticketType.name },
          {
            header: 'Price',
            cell: (ticket) => `$${ticket.ticketType.price.toFixed(2)}`,
          },
          { header: 'Status', cell: (ticket) => ticket.status },
          {
            header: 'Actions',
            className: 'text-right',
            cell: (ticket) => (
              <Button asChild variant="outline" size="sm">
                <Link to="/tickets/$ticketId" params={{ ticketId: ticket.id }}>
                  View
                </Link>
              </Button>
            ),
          },
        ]}
      />
    </main>
  )
}
