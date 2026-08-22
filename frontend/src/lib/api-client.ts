import { getUserManager } from './oidc'

// Every feature's api.ts calls this instead of raw fetch. Attaches Authorization and
// Accept-Language on every request, and centralizes 401 handling in one place.
export async function apiFetch(input: string, init: RequestInit = {}): Promise<Response> {
  const userManager = getUserManager()
  const user = await userManager.getUser()

  const headers = new Headers(init.headers)
  if (user?.access_token) {
    headers.set('Authorization', `Bearer ${user.access_token}`)
  }
  // Hardcoded until issue #11 (i18n) is actually built -- nothing implements language
  // switching yet, so there's no active-language source to read from.
  headers.set('Accept-Language', 'en')

  const response = await fetch(input, { ...init, headers })

  if (response.status !== 401) {
    return response
  }

  // Try a silent refresh once, retry the request; fall back to a full sign-in redirect
  // if that fails.
  try {
    await userManager.signinSilent()
  } catch {
    await userManager.signinRedirect()
    return response
  }

  const retryUser = await userManager.getUser()
  const retryHeaders = new Headers(init.headers)
  if (retryUser?.access_token) {
    retryHeaders.set('Authorization', `Bearer ${retryUser.access_token}`)
  }
  retryHeaders.set('Accept-Language', 'en')

  return fetch(input, { ...init, headers: retryHeaders })
}
