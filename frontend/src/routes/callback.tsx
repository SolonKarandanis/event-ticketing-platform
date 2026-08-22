import { createFileRoute } from '@tanstack/react-router'

// AuthProvider (mounted in __root.tsx) auto-detects the code/state query params and
// processes the callback itself via userManager.signinCallback() -- the actual redirect
// to the signed-in user's role home happens in onSigninCallback there. This route just
// needs to exist as a valid page for Keycloak to land on, with something reasonable to
// show for the brief moment before that redirect fires.
export const Route = createFileRoute('/callback')({
  ssr: false,
  component: Callback,
})

function Callback() {
  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker">Signing you in...</p>
    </main>
  )
}
