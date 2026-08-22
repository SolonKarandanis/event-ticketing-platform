import { createFileRoute, Outlet, redirect } from '@tanstack/react-router'
import { getRoleHomeRoute, getRoles, getUserManager, ROLE_ORGANIZER } from '#/lib/oidc'

// Pathless layout -- contributes no URL segment of its own, just wraps its children
// (organizer screens) with this one role check. ssr: false means beforeLoad never runs
// server-side, matching the client-only UserManager it depends on.
export const Route = createFileRoute('/_organizer')({
  ssr: false,
  beforeLoad: async ({ context }) => {
    const user = await context.auth.getUser()

    if (!user) {
      await getUserManager().signinRedirect()
      throw redirect({ to: '/' })
    }

    const roles = getRoles(user)
    if (!roles.includes(ROLE_ORGANIZER)) {
      throw redirect({ to: getRoleHomeRoute(roles) })
    }
  },
  component: () => <Outlet />,
})
