import { createFileRoute, Outlet, redirect } from '@tanstack/react-router'
import { getRoleHomeRoute, getRoles } from '#/features/auth/roles'
import { ROLE_ATTENDEE } from '#/features/auth/types'

// See _organizer.tsx for why "no user" redirects to '/' instead of calling
// signinRedirect() directly -- doing that here raced sign-out and won, undoing it.
export const Route = createFileRoute('/_attendee')({
  ssr: false,
  beforeLoad: async ({ context }) => {
    const user = await context.auth.getUser()

    if (!user) {
      throw redirect({ to: '/' })
    }

    const roles = getRoles(user)
    if (!roles.includes(ROLE_ATTENDEE)) {
      throw redirect({ to: getRoleHomeRoute(roles) })
    }
  },
  component: () => <Outlet />,
})
