// Same pattern as the other feature test suites. This one's entirely read-only queries
// (no mutations, see hooks.ts's own comment on why) -- the interesting contract to
// verify is usePublishedEvents' request shape: page/size/sortBy get flattened into a
// nested `paging` object server-side, distinct from every other list endpoint in this
// app, which just sends page/size as query params.
import { describe, expect, it } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { server } from '#/test/msw/server'
import { createQueryWrapper } from '#/test/test-utils'
import { usePublishedEvent, usePublishedEventCities, usePublishedEvents } from './hooks'
import type { GetPublishedEventDetailsResponse, ListPublishedEventResponse } from './types'

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/published-events`

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

describe('usePublishedEvents', () => {
  it('sends page/size/sortBy nested under paging, and filters at the top level', async () => {
    let receivedBody: Record<string, unknown> | undefined

    server.use(
      http.post(`${BASE_URL}/search`, async ({ request }) => {
        receivedBody = (await request.json()) as Record<string, unknown>
        const events: ListPublishedEventResponse[] = [
          { id: 'event-1', name: 'Summer Fest', start: null, end: null, venue: testVenue, coverImageId: null },
        ]
        return HttpResponse.json({ content: events, totalElements: 1, totalPages: 1, number: 0, size: 10 })
      }),
    )

    const { result } = renderHook(
      () => usePublishedEvents({ page: 0, size: 10, q: 'fest', city: 'Testville', sortBy: 'priceAsc' }),
      { wrapper: createQueryWrapper() },
    )

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.content[0].name).toBe('Summer Fest')
    expect(receivedBody).toEqual({
      q: 'fest',
      city: 'Testville',
      paging: { page: 0, limit: 10, sortField: 'priceAsc' },
    })
  })
})

describe('usePublishedEvent', () => {
  it('returns the event details, including its ordered image gallery', async () => {
    const details: GetPublishedEventDetailsResponse = {
      id: 'event-1',
      name: 'Summer Fest',
      start: null,
      end: null,
      venue: testVenue,
      ticketTypes: [{ id: 'tt-1', name: 'General', price: 25, description: null }],
      images: [{ id: 'image-1', altText: 'Main stage' }],
    }
    server.use(http.get(`${BASE_URL}/:eventId`, () => HttpResponse.json(details)))

    const { result } = renderHook(() => usePublishedEvent('event-1'), {
      wrapper: createQueryWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.images).toEqual([{ id: 'image-1', altText: 'Main stage' }])
  })

  it('never calls the network when eventId is empty -- the enabled guard', () => {
    const { result } = renderHook(() => usePublishedEvent(''), {
      wrapper: createQueryWrapper(),
    })

    expect(result.current.isPending).toBe(true)
    expect(result.current.fetchStatus).toBe('idle')
  })
})

describe('usePublishedEventCities', () => {
  it('returns the distinct city list from GET /api/v1/published-events/cities', async () => {
    server.use(http.get(`${BASE_URL}/cities`, () => HttpResponse.json(['Athens', 'Testville'])))

    const { result } = renderHook(() => usePublishedEventCities(), {
      wrapper: createQueryWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual(['Athens', 'Testville'])
  })
})
