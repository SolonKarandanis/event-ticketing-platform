import { Link, createFileRoute, useNavigate } from '@tanstack/react-router'
import { VenueForm } from '#/features/venues/components/VenueForm'
import { formValuesToRequest, venueToFormValues } from '#/features/venues/forms'
import type { VenueFormValues } from '#/features/venues/forms'
import { useUpdateVenue, useVenue, venueQueryOptions } from '#/features/venues/hooks'

export const Route = createFileRoute('/_organizer/venues/$venueId')({
  // Same warm-cache-on-navigation/intent-preload trick as venues/index.tsx. Swallowed
  // for the same reason -- no errorComponent, so useVenue()'s own isError branch below
  // should be what renders on failure, not the router's generic error boundary.
  loader: async ({ context, params }) => {
    try {
      await context.queryClient.ensureQueryData(venueQueryOptions(params.venueId))
    } catch {
      // Handled by useVenue()'s isError below.
    }
  },
  component: EditVenue,
})

function EditVenue() {
  const { venueId } = Route.useParams()
  const navigate = useNavigate()
  const { data: venue, isPending, isError } = useVenue(venueId)
  const updateVenue = useUpdateVenue(venueId)

  function handleSubmit(values: VenueFormValues) {
    updateVenue.mutate(
      { ...formValuesToRequest(values), id: venueId },
      { onSuccess: () => void navigate({ to: '/venues' }) },
    )
  }

  return (
    <main className="page-wrap px-4 py-12">
      <Link to="/venues" className="nav-link mb-4 inline-block">
        &larr; Back to Venues
      </Link>
      <p className="island-kicker mb-2">Organizer</p>
      <h1 className="display-title mb-6 text-3xl font-bold text-(--sea-ink)">
        Edit Venue
      </h1>
        {isPending && <p className="text-sm text-(--sea-ink-soft)">Loading venue...</p>}
        {!isPending && isError && <p className="text-sm text-destructive">Couldn't load this venue.</p>}
        {!isPending && !isError &&(
            <VenueForm
                defaultValues={venueToFormValues(venue)}
                onSubmit={handleSubmit}
                isSubmitting={updateVenue.isPending}
                submitLabel="Save Venue"
            />
        )}
    </main>
  )
}
