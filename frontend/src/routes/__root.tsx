import { useEffect } from 'react'
import {
  HeadContent,
  Scripts,
  createRootRouteWithContext,
  useRouter,
} from '@tanstack/react-router'
import { TanStackRouterDevtoolsPanel } from '@tanstack/react-router-devtools'
import { TanStackDevtools } from '@tanstack/react-devtools'
import { AuthProvider } from 'react-oidc-context'
import type { User } from 'oidc-client-ts'
import Footer from '../components/Footer'
import Header from '../components/Header'
import { getUserManager } from '#/lib/oidc'
import { getRoleHomeRoute, getRoles } from '#/features/auth/roles'

import TanStackQueryDevtools from '../integrations/tanstack-query/devtools'

import appCss from '../styles.css?url'

import type { QueryClient } from '@tanstack/react-query'

interface MyRouterContext {
  queryClient: QueryClient
  auth: {
    getUser: () => Promise<User | null>
  }
}

const THEME_INIT_SCRIPT = `(function(){try{var stored=window.localStorage.getItem('theme');var mode=(stored==='light'||stored==='dark'||stored==='auto')?stored:'auto';var prefersDark=window.matchMedia('(prefers-color-scheme: dark)').matches;var resolved=mode==='auto'?(prefersDark?'dark':'light'):mode;var root=document.documentElement;root.classList.remove('light','dark');root.classList.add(resolved);if(mode==='auto'){root.removeAttribute('data-theme')}else{root.setAttribute('data-theme',mode)}root.style.colorScheme=resolved;}catch(e){}})();`

export const Route = createRootRouteWithContext<MyRouterContext>()({
  head: () => ({
    meta: [
      {
        charSet: 'utf-8',
      },
      {
        name: 'viewport',
        content: 'width=device-width, initial-scale=1',
      },
      {
        title: 'TanStack Start Starter',
      },
    ],
    links: [
      {
        rel: 'stylesheet',
        href: appCss,
      },
    ],
  }),
  shellComponent: RootDocument,
})

function RootDocument({ children }: { children: React.ReactNode }) {
  const router = useRouter()

  // Client-only -- server renders with no manager (AuthProvider tolerates this,
  // falling back to an inert stub since its own initialization work is deferred to an
  // effect that never runs during SSR anyway).
  const userManager = typeof window !== 'undefined' ? getUserManager() : undefined

  const onSigninCallback = (user: User | undefined) => {
    const destination = getRoleHomeRoute(getRoles(user))
    void router.navigate({ to: destination, replace: true })
  }

  useEffect(() => {
    // Bootstrap recovery: if Keycloak's own SSO session cookie is still valid, this
    // recovers the session invisibly (no redirect) -- otherwise it's a no-op. Needed
    // because tokens are held in memory only and don't survive a reload on their own.
    getUserManager()
      .signinSilent()
      .catch(() => {
        // No existing session to recover -- expected for a first visit / logged-out user.
      })
  }, [])

  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: THEME_INIT_SCRIPT }} />
        <HeadContent />
      </head>
      <body className="font-sans antialiased wrap-anywhere selection:bg-[rgba(79,184,178,0.24)]">
        <AuthProvider userManager={userManager} onSigninCallback={onSigninCallback}>
          <Header />
          {children}
          <Footer />
        </AuthProvider>
        <TanStackDevtools
          config={{
            position: 'bottom-right',
          }}
          plugins={[
            {
              name: 'Tanstack Router',
              render: <TanStackRouterDevtoolsPanel />,
            },
            TanStackQueryDevtools,
          ]}
        />
        <Scripts />
      </body>
    </html>
  )
}
