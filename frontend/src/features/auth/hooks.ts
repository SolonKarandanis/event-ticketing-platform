import { useAuth } from 'react-oidc-context'
import { getRoles } from './roles'

// Thin convenience wrapper -- components that just need "what roles does the current
// user have" don't need to know about the realm_access claim shape.
export function useUserRoles(): string[] {
  const auth = useAuth()
  return getRoles(auth.user)
}
