import { UserManager, WebStorageStateStore, type StateStore, type User } from 'oidc-client-ts'

// Genuinely in-memory -- used ONLY for userStore (the signed-in user's tokens). This is
// what "in-memory tokens" in the design actually means: keep the access/refresh tokens
// out of persistent browser storage, recovered after a reload via signinSilent() instead
// (see the bootstrap effect in __root.tsx).
//
// stateStore is deliberately NOT this -- it can't be. signinRedirect() does a real,
// full-page browser navigation to Keycloak and back; that wipes all JS memory in the tab,
// so a pure in-memory store has no record of the PKCE verifier/state by the time the
// browser lands back on /callback (this is exactly the "stuck on /callback" bug this
// comment is here to stop someone from reintroducing). The state store only ever holds a
// short-lived, single-use handshake value anyway -- sessionStorage (tab-scoped, cleared
// on tab close) is the right, and only working, place for it.
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
    userManagerInstance = new UserManager({
      authority: import.meta.env.VITE_KEYCLOAK_ISSUER,
      client_id: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
      redirect_uri: `${window.location.origin}/callback`,
      post_logout_redirect_uri: window.location.origin,
      response_type: 'code',
      scope: 'openid profile email',
      userStore: new InMemoryStateStore(),
      stateStore: new WebStorageStateStore({ store: window.sessionStorage }),
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

// This realm's "realm roles" protocol mapper only adds realm_access.roles to the access
// token (confirmed via the admin API -- no id.token.claim/userinfo.token.claim config at
// all), not the ID token. oidc-client-ts's user.profile comes from the ID token, so
// realm_access is never there to read -- roles have to come from decoding the access
// token itself instead. This matches ticket-service's own JwtAuthenticationConverter,
// which reads the identical claim off the identical token (the one sent as the bearer
// token), so frontend and backend agree on where roles live by construction, not by luck.
function decodeJwtPayload(token: string): Record<string, unknown> {
  const base64Url = token.split('.')[1]
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
  return JSON.parse(atob(padded))
}

export function getRoles(user: User | null | undefined): string[] {
  if (!user?.access_token) return []
  try {
    const payload = decodeJwtPayload(user.access_token)
    const realmAccess = payload.realm_access as { roles?: string[] } | undefined
    return realmAccess?.roles ?? []
  } catch {
    return []
  }
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
