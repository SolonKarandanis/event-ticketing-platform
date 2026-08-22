// Auth isn't a REST resource on ticket-service (it's all Keycloak-based), so there's no
// typed fetch functions to put here. Generic OIDC/Keycloak mechanics (issue #3) live in
// #/lib/oidc.ts (the shared UserManager) and #/lib/api-client.ts (the apiFetch()
// wrapper); this app's own identity model (role constants, role-to-route mapping) lives
// in types.ts and roles.ts in this folder instead -- see hooks.ts for the one small
// feature-facing convenience wrapper (useUserRoles).
