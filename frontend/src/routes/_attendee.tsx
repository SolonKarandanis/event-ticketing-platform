import { createFileRoute, Outlet, redirect } from '@tanstack/react-router'
import { getRoleHomeRoute, getRoles, getUserManager, ROLE_ATTENDEE } from '#/lib/oidc'

export const Route = createFileRoute('/_attendee')({
  ssr: false,
  beforeLoad: async ({ context }) => {
    const user = await context.auth.getUser()

    if (!user) {
      await getUserManager().signinRedirect()
      throw redirect({ to: '/' })
    }

    const roles = getRoles(user)
    if (!roles.includes(ROLE_ATTENDEE)) {
      throw redirect({ to: getRoleHomeRoute(roles) })
    }
  },
  component: () => <Outlet />,
})
