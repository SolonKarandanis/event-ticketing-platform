import { createFileRoute } from '@tanstack/react-router'
import { useAuth } from 'react-oidc-context'

// Placeholder -- the real organizer dashboard is built in a future pass (issues #6, #7, #8).
// This exists to verify the auth/routing shell: an organizer landing here after login,
// and getting bounced back here from a route that isn't theirs.
export const Route = createFileRoute('/_organizer/dashboard')({
  component: OrganizerDashboard,
})

function OrganizerDashboard() {
  const auth = useAuth()
  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker mb-2">Organizer</p>
      <h1 className="display-title mb-3 text-3xl font-bold text-[var(--sea-ink)]">
        Dashboard
      </h1>
      <p className="text-sm text-[var(--sea-ink-soft)]">
        Signed in as {auth.user?.profile.preferred_username ?? 'unknown'}
      </p>
      <button
        type="button"
        className="nav-link mt-4"
        onClick={() => auth.signoutRedirect()}
      >
        Log Out
      </button>
    </main>
  )
}
