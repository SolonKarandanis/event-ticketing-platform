// This app's identity model -- not OIDC concepts, just what roles exist here and which
// URL each one belongs at. Keycloak/token mechanics live in #/lib/oidc.ts instead; see
// roles.ts in this folder for the functions that connect the two.
export const ROLE_ORGANIZER = 'ROLE_ORGANIZER'
export const ROLE_ATTENDEE = 'ROLE_ATTENDEE'
export const ROLE_STAFF = 'ROLE_STAFF'

// Literal union, not a bare string -- TanStack Router's navigate()/redirect() are typed
// against known route paths, so this has to match one of them exactly to type-check.
export type RoleHomeRoute = '/dashboard' | '/tickets' | '/scan' | '/'
