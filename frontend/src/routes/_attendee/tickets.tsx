import { createFileRoute } from '@tanstack/react-router'
import { useAuth } from 'react-oidc-context'

// Placeholder -- the real purchased-tickets list (with QR codes) is a future pass;
// features/tickets/{api,hooks}.ts already exist (useTickets/useTicket/useTicketQrCode).
// This is the attendee's authenticated landing page now that /browse (issue #10) moved
// out to a public route -- browsing events never needed a login, only viewing tickets
// you've actually bought does.
export const Route = createFileRoute('/_attendee/tickets')({
  component: MyTickets,
})

function MyTickets() {
  const auth = useAuth()
  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker mb-2">Attendee</p>
      <h1 className="display-title mb-3 text-3xl font-bold text-[var(--sea-ink)]">
        My Tickets
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
