import { setupServer } from 'msw/node'

// No default handlers registered here -- each test file registers exactly the
// endpoints it needs via server.use(...) (see src/test/setup.ts's resetHandlers,
// which clears them back out after every test). Keeps a test's own file the single
// place you look to see what it actually depends on the network for.
export const server = setupServer()
