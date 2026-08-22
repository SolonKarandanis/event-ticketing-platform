import { createRouter as createTanStackRouter } from '@tanstack/react-router'
import { routeTree } from './routeTree.gen'

import { setupRouterSsrQueryIntegration } from '@tanstack/react-router-ssr-query'
import { getContext } from './integrations/tanstack-query/root-provider'
import { getUserManager } from '#/lib/oidc'

export function getRouter() {
  const context = getContext()

  const router = createTanStackRouter({
    routeTree,
    context: {
      ...context,
      auth: {
        getUser: () =>
          typeof window !== 'undefined' ? getUserManager().getUser() : Promise.resolve(null),
      },
    },
    scrollRestoration: true,
    defaultPreload: 'intent',
    defaultPreloadStaleTime: 0,
  })

  setupRouterSsrQueryIntegration({ router, queryClient: context.queryClient })

  // Client-only: re-evaluate beforeLoad guards the instant login/logout happens, not
  // just on the next navigation.
  if (typeof window !== 'undefined') {
    const userManager = getUserManager()
    userManager.events.addUserLoaded(() => router.invalidate())
    userManager.events.addUserUnloaded(() => router.invalidate())
  }

  return router
}

declare module '@tanstack/react-router' {
  interface Register {
    router: ReturnType<typeof getRouter>
  }
}
