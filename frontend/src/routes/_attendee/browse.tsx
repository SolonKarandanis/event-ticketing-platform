import { createFileRoute } from '@tanstack/react-router'
import { useAuth } from 'react-oidc-context'

// Placeholder -- the real published-events search/browse screen is issue #10.
export const Route = createFileRoute('/_attendee/browse')({
  component: AttendeeBrowse,
})

function AttendeeBrowse() {
  const auth = useAuth()
  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker mb-2">Attendee</p>
      <h1 className="display-title mb-3 text-3xl font-bold text-[var(--sea-ink)]">
        Browse Events
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
