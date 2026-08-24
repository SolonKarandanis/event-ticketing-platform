import { Link, Outlet, createFileRoute, redirect } from '@tanstack/react-router'
import { getRoleHomeRoute, getRoles } from '#/features/auth/roles'
import { ROLE_ORGANIZER } from '#/features/auth/types'

// Pathless layout -- contributes no URL segment of its own, just wraps its children
// (organizer screens) with this one role check. ssr: false means beforeLoad never runs
// server-side, matching the client-only UserManager it depends on.
//
// No user redirects to '/' rather than calling signinRedirect() directly -- this guard
// re-runs via router.invalidate() on every auth event (see router.tsx), including
// addUserUnloaded when someone signs out. Triggering a fresh signinRedirect() from here
// used to race the actual sign-out navigation and win, so clicking "Log Out" silently
// logged the user right back in instead. Redirecting to '/' has no such race -- the
// existing "Log In" button there is what actually starts a new session.
export const Route = createFileRoute('/_organizer')({
  ssr: false,
  beforeLoad: async ({ context }) => {
    const user = await context.auth.getUser()

    if (!user) {
      throw redirect({ to: '/' })
    }

    const roles = getRoles(user)
    if (!roles.includes(ROLE_ORGANIZER)) {
      throw redirect({ to: getRoleHomeRoute(roles) })
    }
  },
  component: OrganizerLayout,
})

function OrganizerLayout() {
  return (
    <>
      <nav className="page-wrap flex gap-4 px-4 pt-6 text-sm font-semibold">
        <Link to="/dashboard" className="nav-link" activeProps={{ className: 'nav-link is-active' }}>
          Dashboard
        </Link>
        <Link
          to="/venues"
          className="nav-link"
          activeProps={{ className: 'nav-link is-active' }}
          activeOptions={{ exact: false }}
        >
          Venues
        </Link>
        <Link
            to="/events"
            className="nav-link"
            activeProps={{ className: 'nav-link is-active' }}
            activeOptions={{ exact: false }}
        >
          Events
        </Link>
      </nav>
      <Outlet />
    </>
  )
}
