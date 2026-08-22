import type { User } from 'oidc-client-ts'
import { decodeJwtPayload } from '#/lib/oidc'
import { ROLE_ATTENDEE, ROLE_ORGANIZER, ROLE_STAFF } from './types'
import type { RoleHomeRoute } from './types'

// This realm's "realm roles" protocol mapper only adds realm_access.roles to the access
// token (confirmed via the admin API -- no id.token.claim/userinfo.token.claim config at
// all), not the ID token. oidc-client-ts's user.profile comes from the ID token, so
// realm_access is never there to read -- roles have to come from decoding the access
// token itself instead. This matches ticket-service's own JwtAuthenticationConverter,
// which reads the identical claim off the identical token (the one sent as the bearer
// token), so frontend and backend agree on where roles live by construction, not by luck.
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

// Per issue #3: role mismatch redirects to the user's own role's home, not a Forbidden
// page -- the three roles are effectively different apps sharing one codebase. Falls back
// to the public landing page for a user with none of the three roles.
export function getRoleHomeRoute(roles: string[]): RoleHomeRoute {
  if (roles.includes(ROLE_ORGANIZER)) return '/dashboard'
  if (roles.includes(ROLE_ATTENDEE)) return '/browse'
  if (roles.includes(ROLE_STAFF)) return '/scan'
  return '/'
}
