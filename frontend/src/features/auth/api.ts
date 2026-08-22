// Auth isn't a REST resource on ticket-service (it's all Keycloak-based), so there's no
// typed fetch functions to put here. The actual OIDC mechanics (issue #3) live in
// #/lib/oidc.ts (the shared UserManager) and #/lib/api-client.ts (the apiFetch()
// wrapper) instead -- see hooks.ts in this folder for the one small feature-facing
// convenience wrapper (useUserRoles).
