import { createFileRoute } from '@tanstack/react-router'
import { useAuth } from 'react-oidc-context'

// Placeholder -- the real QR/manual ticket validation screen is issue #9.
export const Route = createFileRoute('/_staff/scan')({
  component: StaffScan,
})

function StaffScan() {
  const auth = useAuth()
  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker mb-2">Staff</p>
      <h1 className="display-title mb-3 text-3xl font-bold text-[var(--sea-ink)]">
        Scan Tickets
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
