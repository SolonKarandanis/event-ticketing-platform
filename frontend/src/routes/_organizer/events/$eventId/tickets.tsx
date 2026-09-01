import { Link, createFileRoute } from '@tanstack/react-router'
import { ConfirmButton } from '#/components/ConfirmButton'
import { PaginatedTable } from '#/components/PaginatedTable'
import { eventTicketSalesQueryOptions, useCancelTicketForOrganizer, useEventTicketSales } from '#/features/ticket-sales/hooks'
import type { TicketSaleResponse } from '#/features/ticket-sales/types'
import { TicketStatus } from '#/features/tickets/types'
import { DEFAULT_PAGE, DEFAULT_SIZE, paginationSearchSchema } from '#/lib/pagination'

// Sibling of $eventId/index.tsx (the edit page), not a dot-nested child of it -- a
// dot-nested route (`$eventId.tickets.tsx`) would make $eventId.tsx an implicit layout,
// requiring an <Outlet/> it doesn't have. Same directory-of-independent-siblings shape
// browse/index.tsx + browse/$eventId.tsx already use.
export const Route = createFileRoute('/_organizer/events/$eventId/tickets')({
    validateSearch: paginationSearchSchema.parse,
    loaderDeps: ({ search }) => ({
        page: search.page ?? DEFAULT_PAGE,
        size: search.size ?? DEFAULT_SIZE,
    }),
    // Same warm-cache-on-navigation/intent-preload trick as every other paginated list
    // route. Swallowed -- no errorComponent, so useEventTicketSales()'s own isError
    // below should be what renders on failure.
    loader: async ({ context, params, deps }) => {
        try {
            await context.queryClient.ensureQueryData(
                eventTicketSalesQueryOptions(params.eventId, { page: deps.page - 1, size: deps.size }),
            )
        } catch {
            // Handled by useEventTicketSales()'s isError below.
        }
    },
    component: EventTicketSales,
})

function CancelTicketAction({ eventId, ticket }: { eventId: string; ticket: TicketSaleResponse }) {
    const cancelTicket = useCancelTicketForOrganizer(eventId, ticket.id)

    if (ticket.status === TicketStatus.CANCELLED) {
        return <span className="text-sm text-(--sea-ink-soft)">Cancelled</span>
    }

    return (
        <ConfirmButton
            label={cancelTicket.isPending ? 'Cancelling...' : 'Cancel'}
            title="Cancel this ticket?"
            description={`This cancels ${ticket.purchaserName}'s ticket (${ticket.referenceCode}). This can't be undone.`}
            confirmLabel="Cancel Ticket"
            variant="destructive"
            disabled={cancelTicket.isPending}
            onConfirm={() => cancelTicket.mutate({})}
        />
    )
}

function EventTicketSales() {
    const { eventId } = Route.useParams()
    const search = Route.useSearch()
    const page = search.page ?? DEFAULT_PAGE
    const size = search.size ?? DEFAULT_SIZE
    const { data, isPending, isError } = useEventTicketSales(eventId, { page: page - 1, size })

    return (
        <main className="page-wrap px-4 py-12">
            <Link to="/events/$eventId" params={{ eventId }} className="nav-link mb-4 inline-block">
                &larr; Back to Event
            </Link>
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
                emptyMessage="No tickets sold for this event yet."
                loadingMessage="Loading ticket sales..."
                errorMessage="Couldn't load ticket sales. Try refreshing."
                columns={[
                    { header: 'Reference', cell: (ticket) => ticket.referenceCode },
                    { header: 'Ticket Type', cell: (ticket) => ticket.ticketType.name },
                    { header: 'Purchaser', cell: (ticket) => ticket.purchaserName },
                    { header: 'Status', cell: (ticket) => ticket.status },
                    {
                        header: 'Actions',
                        className: 'text-right',
                        cell: (ticket) => <CancelTicketAction eventId={eventId} ticket={ticket} />,
                    },
                ]}
            />
        </main>
    )
}
