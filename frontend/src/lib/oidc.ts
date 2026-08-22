import { UserManager, WebStorageStateStore } from 'oidc-client-ts'

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
      // sessionStorage, not pure in-memory: a real reload -- not just a redirect round-trip
      // -- wipes JS memory too, and signinSilent()'s refresh-token path needs an *existing*
      // refresh token already loaded to renew. There's nothing to renew after a from-scratch
      // reload with nothing in memory, so a reload always dropped the session and bounced to
      // '/' with no way back in. sessionStorage (tab-scoped, cleared on tab close -- not
      // localStorage) is the standard trade-off here: the refresh token is the genuinely
      // sensitive one of the two either way, so this isn't meaningfully less safe than the
      // in-memory version was, and it actually works across a reload.
      userStore: new WebStorageStateStore({ store: window.sessionStorage }),
      stateStore: new WebStorageStateStore({ store: window.sessionStorage }),
      automaticSilentRenew: true,
      // No silent_redirect_uri is set -- signinSilent() tries a refresh token first and
      // only falls back to an iframe if one isn't configured/available. Omitting it
      // entirely means there's no iframe fallback path at all: renewal is refresh-token-only.
    })
  }

  return userManagerInstance
}

// Generic JWT payload decode -- base64url (not plain base64), no Keycloak- or app-specific
// knowledge of what's inside. Anything that needs a particular claim (e.g. roles, see
// features/auth/roles.ts) builds on top of this rather than parsing tokens itself.
export function decodeJwtPayload(token: string): Record<string, unknown> {
  const base64Url = token.split('.')[1]
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
  return JSON.parse(atob(padded))
}
