// Same pattern as features/events/hooks.test.ts, extended to two things that file
// didn't need: a plain JSON mutation (create/update here are JSON, not multipart) and
// an infinite query (useInfiniteVenues, backing the venue picker combobox).
import { describe, expect, it } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { server } from '#/test/msw/server'
import { createQueryWrapper, createTestQueryClient } from '#/test/test-utils'
import { useCreateVenue, useInfiniteVenues, useVenue, useVenues } from './hooks'
import type { CreateVenueRequest, Venue } from './types'

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/venues`

function venueFixture(overrides: Partial<Venue> = {}): Venue {
  return {
    id: 'venue-1',
    name: 'Test Venue',
    addressLine1: '1 Main St',
    addressLine2: null,
    city: 'Testville',
    postalCode: '12345',
    country: 'Testland',
    latitude: null,
    longitude: null,
    capacity: null,
    accessibilityInfo: null,
    ...overrides,
  }
}

describe('useVenues', () => {
  it('returns the page of venues from GET /api/v1/venues', async () => {
    server.use(
      http.get(BASE_URL, () =>
        HttpResponse.json({
          content: [venueFixture()],
          totalElements: 1,
          totalPages: 1,
          number: 0,
          size: 10,
        }),
      ),
    )

    const { result } = renderHook(() => useVenues({ page: 0, size: 10 }), {
      wrapper: createQueryWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.content).toHaveLength(1)
    expect(result.current.data?.content[0].name).toBe('Test Venue')
  })
})

describe('useVenue', () => {
  it('never calls the network when venueId is empty -- the enabled guard', () => {
    // No handler registered for GET /api/v1/venues/:venueId -- a real request here
    // would fail the test outright (onUnhandledRequest: 'error', see test/setup.ts).
    const { result } = renderHook(() => useVenue(''), {
      wrapper: createQueryWrapper(),
    })

    expect(result.current.isPending).toBe(true)
    expect(result.current.fetchStatus).toBe('idle')
  })
})

describe('useCreateVenue', () => {
  const request: CreateVenueRequest = {
    name: 'New Venue',
    addressLine1: '2 Side St',
    city: 'Otherville',
    postalCode: '54321',
    country: 'Testland',
  }

  it('sends a plain JSON body (unlike events, venues have no images to carry)', async () => {
    let receivedBody: CreateVenueRequest | undefined
    let receivedContentType: string | null = null

    server.use(
      http.post(BASE_URL, async ({ request: httpRequest }) => {
        receivedContentType = httpRequest.headers.get('content-type')
        receivedBody = (await httpRequest.json()) as CreateVenueRequest
        return HttpResponse.json(venueFixture({ id: 'created-venue-id', ...receivedBody }), {
          status: 201,
        })
      }),
    )

    const { result } = renderHook(() => useCreateVenue(), {
      wrapper: createQueryWrapper(),
    })

    result.current.mutate(request)

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(receivedContentType).toBe('application/json')
    expect(receivedBody).toEqual(request)
  })

  it('invalidates the venues list cache on success, refetching any active list query', async () => {
    let listRequestCount = 0
    server.use(
      http.get(BASE_URL, () => {
        listRequestCount += 1
        return HttpResponse.json({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 })
      }),
      http.post(BASE_URL, () =>
        HttpResponse.json(venueFixture({ id: 'created-venue-id' }), { status: 201 }),
      ),
    )

    const queryClient = createTestQueryClient()
    const wrapper = createQueryWrapper(queryClient)

    const list = renderHook(() => useVenues({ page: 0, size: 10 }), { wrapper })
    await waitFor(() => expect(list.result.current.isSuccess).toBe(true))
    expect(listRequestCount).toBe(1)

    const create = renderHook(() => useCreateVenue(), { wrapper })
    create.result.current.mutate(request)
    await waitFor(() => expect(create.result.current.isSuccess).toBe(true))

    await waitFor(() => expect(listRequestCount).toBe(2))
  })

  it('surfaces the backend’s real error message on failure', async () => {
    server.use(
      http.post(BASE_URL, () =>
        HttpResponse.json({ error: 'name: Name is required' }, { status: 400 }),
      ),
    )

    const { result } = renderHook(() => useCreateVenue(), {
      wrapper: createQueryWrapper(),
    })

    result.current.mutate(request)

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect(result.current.error?.message).toBe('name: Name is required')
  })
})

describe('useInfiniteVenues', () => {
  it('fetches subsequent pages and reports hasNextPage correctly', async () => {
    server.use(
      http.get(BASE_URL, ({ request }) => {
        const page = Number(new URL(request.url).searchParams.get('page'))
        const content = page === 0 ? [venueFixture({ id: 'venue-1' })] : [venueFixture({ id: 'venue-2' })]
        return HttpResponse.json({ content, totalElements: 2, totalPages: 2, number: page, size: 20 })
      }),
    )

    const { result } = renderHook(() => useInfiniteVenues(''), {
      wrapper: createQueryWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.pages).toHaveLength(1)
    expect(result.current.data?.pages[0].content[0].id).toBe('venue-1')
    // page 0 of 2 total -- getNextPageParam (see hooks.ts) still has somewhere to go.
    expect(result.current.hasNextPage).toBe(true)

    await result.current.fetchNextPage()

    await waitFor(() => expect(result.current.data?.pages).toHaveLength(2))
    expect(result.current.data?.pages[1].content[0].id).toBe('venue-2')
    // Now on page 1 of 2 (0-indexed) -- nothing left to fetch.
    expect(result.current.hasNextPage).toBe(false)
  })
})
