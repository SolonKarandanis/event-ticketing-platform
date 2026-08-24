import {createFileRoute, Link} from '@tanstack/react-router'
import {DEFAULT_PAGE, DEFAULT_SIZE, paginationSearchSchema} from "#/lib/pagination.ts";
import {useEvents} from "#/features/events/hooks.ts";
import {Button} from "#/components/ui/button.tsx";

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
        </main>
    )
}
