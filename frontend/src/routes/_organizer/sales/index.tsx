import { Link, createFileRoute } from '@tanstack/react-router'
import { PaginatedTable } from '#/components/PaginatedTable'
import { organizerTicketSalesQueryOptions, useOrganizerTicketSales } from '#/features/ticket-sales/hooks'
import { DEFAULT_PAGE, DEFAULT_SIZE, paginationSearchSchema } from '#/lib/pagination'

// Named /sales, not /tickets -- the attendee-facing "My Tickets" route already owns
// /tickets (_attendee/tickets/), and while these live under different pathless layouts
// (_organizer vs _attendee), both are pathless (no URL segment of their own), so the
// two routes' actual URLs would collide if this used the same name.
export const Route = createFileRoute('/_organizer/sales/')({
    validateSearch: paginationSearchSchema.parse,
    loaderDeps: ({ search }) => ({
        page: search.page ?? DEFAULT_PAGE,
        size: search.size ?? DEFAULT_SIZE,
    }),
    loader: async ({ context, deps }) => {
        try {
            await context.queryClient.ensureQueryData(
                organizerTicketSalesQueryOptions({ page: deps.page - 1, size: deps.size }),
            )
        } catch {
            // Handled by useOrganizerTicketSales()'s isError below.
        }
    },
    component: OrganizerTicketSales,
})

function OrganizerTicketSales() {
    const search = Route.useSearch()
    const page = search.page ?? DEFAULT_PAGE
    const size = search.size ?? DEFAULT_SIZE
    const { data, isPending, isError } = useOrganizerTicketSales({ page: page - 1, size })

    return (
        <main className="page-wrap px-4 py-12">
            <h1 className="display-title mb-6 text-3xl font-bold text-(--sea-ink)">
                Ticket Sales
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
                emptyMessage="No tickets sold yet."
                loadingMessage="Loading ticket sales..."
                errorMessage="Couldn't load ticket sales. Try refreshing."
                columns={[
                    { header: 'Reference', cell: (ticket) => ticket.referenceCode },
                    {
                        header: 'Event',
                        cell: (ticket) => (
                            <Link
                                to="/events/$eventId/tickets"
                                params={{ eventId: ticket.eventId }}
                                className="nav-link"
                            >
                                {ticket.eventName}
                            </Link>
                        ),
                    },
                    { header: 'Ticket Type', cell: (ticket) => ticket.ticketType.name },
                    { header: 'Purchaser', cell: (ticket) => ticket.purchaserName },
                    { header: 'Status', cell: (ticket) => ticket.status },
                ]}
            />
        </main>
    )
}
