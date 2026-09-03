import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'

// Retries disabled -- React Query's default exponential backoff would otherwise make
// a test asserting on an error response wait out several real retry delays before the
// assertion ever gets a chance to run. A fresh client per call (not a shared module-level
// singleton) so one test's cache can never leak into another's.
export function createTestQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })
}

// Pass an existing client when a test needs to assert against it directly (e.g.
// spying on invalidateQueries, or reading back what's in the cache after a mutation);
// otherwise a wrapper-scoped one is created for you.
export function createQueryWrapper(queryClient: QueryClient = createTestQueryClient()) {
  return function QueryWrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  }
}
