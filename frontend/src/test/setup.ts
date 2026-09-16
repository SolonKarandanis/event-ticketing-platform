import { Blob, File } from 'node:buffer'
import { FormData, Headers, Request, Response, fetch } from 'undici'
import '@testing-library/jest-dom/vitest'
import { afterAll, afterEach, beforeAll, vi } from 'vitest'
import { server } from './msw/server'

// jsdom's own Blob/File/FormData don't round-trip correctly through MSW's Node-based
// request interception (a FormData part built with jsdom's Blob comes back on the
// "server" side with a working-looking .text() method that actually resolves to the
// literal string "undefined" -- confirmed directly, not assumed).
//
// The full WHATWG set (fetch/Headers/Request/Response too, not just Blob/File/FormData)
// has to come from the same package for the same reason: Node's own global `fetch` is
// undici-based, but it's Node's *internal* bundled copy, not the same class objects the
// separately-installed `undici` npm package exports -- `instanceof` checks between the
// two fail even though they're structurally identical. A multipart POST reproduced this
// exactly: apiFetch's own `body instanceof FormData` check (against this file's patched
// FormData) passed, but MSW's own Request reconstruction -- built from *its* captured
// Headers/Request/Response references, resolved when `msw/node` itself first imports,
// independently of whatever we patch onto `fetch` -- didn't recognize the same object as
// FormData, silently defaulted the outgoing Content-Type to `text/plain`, and the
// receiving side's `request.formData()` then rejected it outright ("Content-Type was
// not one of multipart/form-data or application/x-www-form-urlencoded"). Patching the
// complete set from one consistent module closes that gap everywhere at once, not just
// at the one call site that happened to be checked.
//
// This has to run before server.listen() below actually patches `fetch` -- it does,
// since this executes as this module loads, and server.listen() only runs later, inside
// beforeAll.
Object.assign(globalThis, { Blob, File, FormData, Headers, Request, Response, fetch })

// apiFetch (see #/lib/api-client) calls #/lib/oidc's getUserManager().getUser() on
// every request. Mocked globally so no test ever constructs a real oidc-client-ts
// UserManager (which touches sessionStorage and sets up automaticSilentRenew timers) --
// every hook test just gets an "anonymous" apiFetch, which is all MSW-mocked endpoints
// need, since none of them actually inspect the Authorization header's contents.
vi.mock('#/lib/oidc', () => ({
  getUserManager: () => ({
    getUser: () => Promise.resolve(null),
    signinSilent: () => Promise.reject(new Error('not implemented in tests')),
    signinRedirect: () => Promise.resolve(),
  }),
}))

// onUnhandledRequest: 'error' -- a request MSW wasn't told to expect fails the test
// loudly instead of either hitting the real network or hanging silently. Every test
// registers exactly the handlers it needs via server.use(...); resetHandlers()
// between tests means one test's mocked endpoint can never leak into the next.
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())
