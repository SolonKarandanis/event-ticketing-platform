// A first pass at testing this app's React Query hooks -- demonstrates the pattern
// (MSW intercepting apiFetch's real network calls, a fresh per-test QueryClient, a
// renderHook + waitFor assertion) rather than covering every hook in this file.
// useEvents/useEvent cover a plain query and its `enabled` guard; useCreateEvent covers
// a mutation whose request body is multipart (the trickiest contract in this feature --
// see forms.ts/api.ts) plus both its success (cache invalidation) and error paths.
import { describe, expect, it } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { server } from '#/test/msw/server'
import { createQueryWrapper, createTestQueryClient } from '#/test/test-utils'
import { useCreateEvent, useEvent, useEvents } from './hooks'
import type { CreateEventRequest } from './types'

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/events`

const testVenue = {
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
}

describe('useEvents', () => {
  it('returns the page of events from GET /api/v1/events', async () => {
    server.use(
      http.get(BASE_URL, () =>
        HttpResponse.json({
          content: [
            {
              id: 'event-1',
              name: 'Summer Fest',
              start: null,
              end: null,
              venue: testVenue,
              salesStart: null,
              salesEnd: null,
              status: 'DRAFT',
              ticketTypes: [],
            },
          ],
          totalElements: 1,
          totalPages: 1,
          number: 0,
          size: 10,
        }),
      ),
    )

    const { result } = renderHook(() => useEvents({ page: 0, size: 10 }), {
      wrapper: createQueryWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.content).toHaveLength(1)
    expect(result.current.data?.content[0].name).toBe('Summer Fest')
  })

  it("surfaces the backend's real error message on failure", async () => {
    server.use(
      http.get(BASE_URL, () =>
        HttpResponse.json({ error: 'Something went wrong' }, { status: 500 }),
      ),
    )

    const { result } = renderHook(() => useEvents({ page: 0, size: 10 }), {
      wrapper: createQueryWrapper(),
    })

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect(result.current.error?.message).toBe('Something went wrong')
  })
})

describe('useEvent', () => {
  it('never calls the network when eventId is empty -- the enabled guard', async () => {
    // No handler registered at all for GET /api/v1/events/:eventId -- if this fired a
    // real request, MSW's onUnhandledRequest: 'error' setup would fail the test.
    const { result } = renderHook(() => useEvent(''), {
      wrapper: createQueryWrapper(),
    })

    expect(result.current.isPending).toBe(true)
    expect(result.current.fetchStatus).toBe('idle')
  })
})

describe('useCreateEvent', () => {
  const request: CreateEventRequest = {
    name: 'New Event',
    venueId: testVenue.id,
    ticketTypes: [{ name: 'General', price: 10 }],
    images: [{ newImageIndex: 0, altText: 'A cover photo' }],
  }
  const newImageFiles = [new File(['fake-image-bytes'], 'cover.jpg', { type: 'image/jpeg' })]

  it('sends the event as a JSON blob part and the file as a matching newImages part', async () => {
    let receivedEvent: CreateEventRequest | undefined
    let receivedFileCount: number | undefined

    server.use(
      http.post(BASE_URL, async ({ request: httpRequest }) => {
        const formData = await httpRequest.formData()
        const eventPart = formData.get('event') as File
        receivedEvent = JSON.parse(await eventPart.text()) as CreateEventRequest
        receivedFileCount = formData.getAll('newImages').length

        return HttpResponse.json(
          {
            id: 'created-event-id',
            name: receivedEvent.name,
            start: null,
            end: null,
            venue: testVenue,
            salesStart: null,
            salesEnd: null,
            status: 'DRAFT',
            ticketTypes: [],
            images: [],
            createdAt: '2026-01-01T00:00:00',
            updatedAt: '2026-01-01T00:00:00',
          },
          { status: 201 },
        )
      }),
    )

    const { result } = renderHook(() => useCreateEvent(), {
      wrapper: createQueryWrapper(),
    })

    result.current.mutate({ request, newImageFiles })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    // The JSON part has to survive the FormData/Blob round-trip intact -- this is
    // exactly the bit that broke when it was first sent as a raw string instead of a
    // Blob with an explicit application/json type (see api.ts's buildEventFormData).
    expect(receivedEvent).toEqual(request)
    expect(receivedFileCount).toBe(1)
  })

  it('invalidates the events list cache on success, refetching any active list query', async () => {
    let listRequestCount = 0
    server.use(
      http.get(BASE_URL, () => {
        listRequestCount += 1
        return HttpResponse.json({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 })
      }),
      http.post(BASE_URL, () =>
        HttpResponse.json(
          {
            id: 'created-event-id',
            name: request.name,
            start: null,
            end: null,
            venue: testVenue,
            salesStart: null,
            salesEnd: null,
            status: 'DRAFT',
            ticketTypes: [],
            images: [],
            createdAt: '2026-01-01T00:00:00',
            updatedAt: '2026-01-01T00:00:00',
          },
          { status: 201 },
        ),
      ),
    )

    // Both hooks share one QueryClient/wrapper on purpose -- that's what makes this a
    // real test of cross-hook cache behavior rather than just "the mutation succeeded".
    const queryClient = createTestQueryClient()
    const wrapper = createQueryWrapper(queryClient)

    const list = renderHook(() => useEvents({ page: 0, size: 10 }), { wrapper })
    await waitFor(() => expect(list.result.current.isSuccess).toBe(true))
    expect(listRequestCount).toBe(1)

    const create = renderHook(() => useCreateEvent(), { wrapper })
    create.result.current.mutate({ request, newImageFiles })
    await waitFor(() => expect(create.result.current.isSuccess).toBe(true))

    // invalidateQueries refetches any query with an active observer -- the still-mounted
    // useEvents hook above is exactly that, so the list endpoint should be hit again
    // with nothing explicitly calling refetch().
    await waitFor(() => expect(listRequestCount).toBe(2))
  })

  it('surfaces a 409 (too many images) as a real error, not a silent failure', async () => {
    server.use(
      http.post(BASE_URL, () =>
        HttpResponse.json({ error: 'An event can have at most 8 images' }, { status: 409 }),
      ),
    )

    const { result } = renderHook(() => useCreateEvent(), {
      wrapper: createQueryWrapper(),
    })

    result.current.mutate({ request, newImageFiles })

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect(result.current.error?.message).toBe('An event can have at most 8 images')
  })
})
