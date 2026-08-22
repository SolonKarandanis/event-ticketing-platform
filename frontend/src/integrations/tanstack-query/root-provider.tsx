import { QueryClient } from '@tanstack/react-query'

export function getContext() {
  // Default staleTime is 0, which means any remount (e.g. router.invalidate() firing after
  // the auth bootstrap's signinSilent() resolves) refetches unconditionally even seconds
  // after the same data just loaded. 30s means a second mount that soon reuses the cache.
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30_000,
      },
    },
  })

  return {
    queryClient,
  }
}
export default function TanstackQueryProvider() {}
