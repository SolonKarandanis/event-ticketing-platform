import { Blob, File } from 'node:buffer'
import { FormData } from 'undici'
import '@testing-library/jest-dom/vitest'
import { afterAll, afterEach, beforeAll, vi } from 'vitest'
import { server } from './msw/server'

// jsdom's own Blob/File/FormData don't round-trip correctly through MSW's Node-based
// request interception (a FormData part built with jsdom's Blob comes back on the
// "server" side with a working-looking .text() method that actually resolves to the
// literal string "undefined" -- confirmed directly, not assumed). This has to run
// before server.listen() below patches the global fetch -- it does, since this
// executes as this module loads, and server.listen() only runs later, inside
// beforeAll. fetch/Request/Response are left alone on purpose: those are what MSW
// itself needs to patch, and Node's global fetch is already undici-based regardless of
// the jsdom environment, so only Blob/File/FormData -- the pieces jsdom actually
// shadows with its own DOM-spec implementations -- need pointing at consistent
// (Node-native) ones.
Object.assign(globalThis, { Blob, File, FormData })

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
