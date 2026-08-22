import { createFileRoute, Link } from '@tanstack/react-router'
import { useAuth } from 'react-oidc-context'

// AuthProvider (mounted in __root.tsx) auto-detects the code/state query params and
// processes the callback itself via userManager.signinCallback() -- the actual redirect
// to the signed-in user's role home happens in onSigninCallback there. This route just
// needs to exist as a valid page for Keycloak to land on, with something reasonable to
// show for the brief moment before that redirect fires -- or if signinCallback fails,
// so a broken handshake doesn't just hang here forever with no way out.
export const Route = createFileRoute('/callback')({
  ssr: false,
  component: Callback,
})

function Callback() {
  const auth = useAuth()

  if (auth.error) {
    return (
      <main className="page-wrap px-4 py-12">
        <p className="island-kicker mb-2">Sign-in failed</p>
        <p className="text-sm text-[var(--sea-ink-soft)]">{auth.error.message}</p>
        <Link to="/" className="nav-link mt-4 inline-block">
          Back to Home
        </Link>
      </main>
    )
  }

  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker">Signing you in...</p>
    </main>
  )
}
