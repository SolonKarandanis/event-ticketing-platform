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

// The subset of Spring Data's Page<T> JSON envelope every paginated list screen
// actually needs -- not the full shape (sort, pageable, first/last, etc).
export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message)
  }
}

// ticket-service's GlobalExceptionHandler always responds with { error: string } (already
// localized server-side via Accept-Language) on 4xx/5xx -- this is the one place that shape
// gets parsed, so every feature's api.ts gets a real, localized message instead of a bare
// status code.
export async function parseJsonOrThrow<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body: unknown = await response.json().catch(() => null)
    const message =
      body && typeof body === 'object' && 'error' in body && typeof body.error === 'string'
        ? body.error
        : `Request failed with status ${response.status}`
    throw new ApiError(response.status, message)
  }
  return await response.json() as Promise<T>
}
