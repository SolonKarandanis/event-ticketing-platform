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
// localized server-side via Accept-Language). analytics-service has no custom exception
// filter, so it falls back to NestJS's own default shape instead:
// { statusCode, message: string | string[], error? } -- there, `error` is just the HTTP
// reason phrase ("Forbidden"), not a real message; `message` is the part worth showing
// (and can be a string[] from a ValidationPipe). Checked in that order so a ticket-service
// body's `error` string always wins over incidentally also having a `message` field.
function extractErrorMessage(body: unknown, status: number): string {
  if (body && typeof body === 'object') {
    if ('error' in body && typeof body.error === 'string') {
      return body.error
    }
    if ('message' in body) {
      const { message } = body
      if (typeof message === 'string') {
        return message
      }
      if (Array.isArray(message) && message.every((entry) => typeof entry === 'string')) {
        return message.join(', ')
      }
    }
  }
  return `Request failed with status ${status}`
}

// This is the one place either backend's error body actually gets parsed, shared by
// parseJsonOrThrow, parseBlobOrThrow, and throwIfNotOk below.
async function throwApiError(response: Response): Promise<never> {
  const body: unknown = await response.json().catch(() => null)
  throw new ApiError(response.status, extractErrorMessage(body, response.status))
}

export async function parseJsonOrThrow<T>(response: Response): Promise<T> {
  if (!response.ok) {
    return throwApiError(response)
  }
  return await response.json() as Promise<T>
}

// For endpoints whose success body isn't JSON (the ticket QR code image, image/png) --
// same failure handling as parseJsonOrThrow, but reads the success body as a Blob
// instead of calling response.json() on it.
export async function parseBlobOrThrow(response: Response): Promise<Blob> {
  if (!response.ok) {
    return throwApiError(response)
  }
  return await response.blob()
}

// For endpoints that succeed with 204 No Content -- same failure handling as
// parseJsonOrThrow, but never calls response.json() on an empty success body (which
// throws a SyntaxError: an empty body isn't valid JSON, so parseJsonOrThrow<void>
// would fail on the success path, not just the error one).
export async function throwIfNotOk(response: Response): Promise<void> {
  if (!response.ok) {
    return throwApiError(response)
  }
}

// For a mutation's onError handler: surfaces the backend's real, localized message when
// the failure came from parseJsonOrThrow, falls back to a feature-supplied string for
// anything else (a network failure, an unexpected exception).
export function toastErrorMessage(error: unknown, fallback: string): string {
  return error instanceof ApiError ? error.message : fallback
}
