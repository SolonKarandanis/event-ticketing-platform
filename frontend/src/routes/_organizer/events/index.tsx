import {createFileRoute, Link} from '@tanstack/react-router'
import {DEFAULT_PAGE, DEFAULT_SIZE, paginationSearchSchema} from "#/lib/pagination.ts";
import {useEvents} from "#/features/events/hooks.ts";
import {Button} from "#/components/ui/button.tsx";
import {PaginatedTable} from "#/components/PaginatedTable.tsx";

export const Route = createFileRoute('/_organizer/events/')({
    validateSearch: paginationSearchSchema.parse,
    component: RouteComponent,
})

function RouteComponent() {
    const search = Route.useSearch()
    const page = search.page ?? DEFAULT_PAGE
    const size = search.size ?? DEFAULT_SIZE
    const { data, isPending, isError } = useEvents({ page: page - 1, size })

    return (
        <main className="page-wrap px-4 py-12">
            <div className="mb-6 flex items-center justify-between">
                <div>
                    <h1 className="display-title text-3xl font-bold text-(--sea-ink)">
                        Events
                    </h1>
                </div>
                <Button asChild>
                    <Link to="/events/new">+ Add Event</Link>
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
                itemLabel="event"
                getRowKey={(event) => event.id}
                emptyMessage="No events yet. Create one to get started."
                loadingMessage="Loading events..."
                errorMessage="Couldn't load events. Try refreshing."
                columns={[
                    { header: 'Name', cell: (event) => event.name },
                    { header: 'Start Date', cell: (event) => event.start ?? '—' },
                    { header: 'End Date', cell: (event) => event.end ?? '—' },
                    { header: 'Venue', cell: (event) => event.venue.name },
                    { header: 'Status', cell: (event) => event.status },
                    {
                        header: 'Actions',
                        className: 'text-right',
                        cell: (event) => (
                            <Button asChild variant="outline" size="sm">
                                <Link to="/events/$eventId" params={{ eventId: event.id }}>
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
