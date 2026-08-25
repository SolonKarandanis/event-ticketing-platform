import {createFileRoute} from '@tanstack/react-router'

export const Route = createFileRoute('/_organizer/events/$eventId')({
    component: RouteComponent,
})

function RouteComponent() {
    return <div>Hello "/_organizer/events/$eventId"!</div>
}
