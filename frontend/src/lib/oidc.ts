import { UserManager, type StateStore, type User } from 'oidc-client-ts'

// Genuinely in-memory -- oidc-client-ts's default stores are sessionStorage-backed, which
// survives a reload. The design here calls for state that doesn't, recovered instead via
// signinSilent() on bootstrap (see AuthProvider's onSigninCallback usage in __root.tsx and
// the bootstrap effect that calls it).
class InMemoryStateStore implements StateStore {
  private store = new Map<string, string>()

  async set(key: string, value: string): Promise<void> {
    this.store.set(key, value)
  }

  async get(key: string): Promise<string | null> {
    return this.store.get(key) ?? null
  }

  async remove(key: string): Promise<string | null> {
    const value = this.store.get(key) ?? null
    this.store.delete(key)
    return value
  }

  async getAllKeys(): Promise<string[]> {
    return Array.from(this.store.keys())
  }
}

let userManagerInstance: UserManager | undefined

// Lazily constructed, client-only. oidc-client-ts's UserManager touches browser APIs;
// this module can still be imported by server-rendered code paths (e.g. via __root.tsx),
// so the instance is never constructed at module scope -- only on first real call, which
// only ever happens client-side (the routes/components that call this are all `ssr: false`).
export function getUserManager(): UserManager {
  if (typeof window === 'undefined') {
    throw new Error('getUserManager() must only be called on the client')
  }

  if (!userManagerInstance) {
    const store = new InMemoryStateStore()

    userManagerInstance = new UserManager({
      authority: import.meta.env.VITE_KEYCLOAK_ISSUER,
      client_id: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
      redirect_uri: `${window.location.origin}/callback`,
      post_logout_redirect_uri: window.location.origin,
      response_type: 'code',
      scope: 'openid profile email',
      userStore: store,
      stateStore: store,
      automaticSilentRenew: true,
      // No silent_redirect_uri is set -- signinSilent() tries a refresh token first and
      // only falls back to an iframe if one isn't configured/available. Omitting it
      // entirely means there's no iframe fallback path at all: renewal is refresh-token-only.
    })
  }

  return userManagerInstance
}

const ROLE_ORGANIZER = 'ROLE_ORGANIZER'
const ROLE_ATTENDEE = 'ROLE_ATTENDEE'
const ROLE_STAFF = 'ROLE_STAFF'

// Keycloak's realm_access claim isn't a standard OIDC claim, so oidc-client-ts types
// UserProfile's extra fields as `unknown` -- this is the one place that gets cast.
export function getRoles(user: User | null | undefined): string[] {
  if (!user) return []
  const realmAccess = user.profile.realm_access as { roles?: string[] } | undefined
  return realmAccess?.roles ?? []
}

// Literal union, not a bare string -- TanStack Router's navigate()/redirect() are typed
// against known route paths, so this has to match one of them exactly to type-check.
export type RoleHomeRoute = '/dashboard' | '/browse' | '/scan' | '/'

// Per issue #3: role mismatch redirects to the user's own role's home, not a Forbidden
// page -- the three roles are effectively different apps sharing one codebase. Falls back
// to the public landing page for a user with none of the three roles.
export function getRoleHomeRoute(roles: string[]): RoleHomeRoute {
  if (roles.includes(ROLE_ORGANIZER)) return '/dashboard'
  if (roles.includes(ROLE_ATTENDEE)) return '/browse'
  if (roles.includes(ROLE_STAFF)) return '/scan'
  return '/'
}

export { ROLE_ORGANIZER, ROLE_ATTENDEE, ROLE_STAFF }
