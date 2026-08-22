import { Link, createFileRoute, useNavigate } from '@tanstack/react-router'
import { ApiError } from '#/lib/api-client'
import {
  VenueForm,
  formValuesToRequest,
} from '#/features/venues/components/VenueForm'
import type { VenueFormValues } from '#/features/venues/components/VenueForm'
import { useCreateVenue } from '#/features/venues/hooks'

export const Route = createFileRoute('/_organizer/venues/new')({
  component: NewVenue,
})

function NewVenue() {
  const navigate = useNavigate()
  const createVenue = useCreateVenue()

  function handleSubmit(values: VenueFormValues) {
    createVenue.mutate(formValuesToRequest(values), {
      onSuccess: () => void navigate({ to: '/venues' }),
    })
  }

  return (
    <main className="page-wrap px-4 py-12">
      <Link to="/venues" className="nav-link mb-4 inline-block">
        &larr; Back to Venues
      </Link>
      <p className="island-kicker mb-2">Organizer</p>
      <h1 className="display-title mb-6 text-3xl font-bold text-[var(--sea-ink)]">
        Add Venue
      </h1>
      <VenueForm
        onSubmit={handleSubmit}
        isSubmitting={createVenue.isPending}
        submitLabel="Save Venue"
        submitError={
          createVenue.error instanceof ApiError
            ? createVenue.error.message
            : null
        }
      />
    </main>
  )
}
