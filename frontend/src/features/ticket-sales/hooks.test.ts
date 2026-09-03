// Same pattern as the other feature test suites. New here: two queries that share one
// base URL but resolve to genuinely different endpoints (per-event vs. cross-event
// ticket sales), and a mutation whose invalidation key is deliberately broader than
// what it operates on -- cancelling one event's ticket should refetch the
// organizer-wide list too, not just that one event's.
import { describe, expect, it } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { server } from '#/test/msw/server'
import { createQueryWrapper, createTestQueryClient } from '#/test/test-utils'
import { useCancelTicketForOrganizer, useEventTicketSales, useOrganizerTicketSales } from './hooks'
import { TicketCancelReason, TicketStatus } from '#/features/tickets/types'
import type { TicketSaleResponse } from './types'

const EVENTS_BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/events`

function ticketSaleFixture(overrides: Partial<TicketSaleResponse> = {}): TicketSaleResponse {
  return {
    id: 'ticket-1',
    referenceCode: 'XY3P9KRT',
    status: TicketStatus.PURCHASED,
    ticketType: { id: 'tt-1', name: 'General', price: 25 },
    purchaserName: 'Jane Attendee',
    purchaserEmail: 'jane@example.com',
    eventId: 'event-1',
    eventName: 'Summer Fest',
    createdAt: '2026-01-01T00:00:00',
    ...overrides,
  }
}

describe('useEventTicketSales', () => {
  it('returns the page of sales from GET /api/v1/events/:eventId/tickets', async () => {
    server.use(
      http.get(`${EVENTS_BASE_URL}/:eventId/tickets`, () =>
        HttpResponse.json({
          content: [ticketSaleFixture()],
          totalElements: 1,
          totalPages: 1,
          number: 0,
          size: 10,
        }),
      ),
    )

    const { result } = renderHook(() => useEventTicketSales('event-1', { page: 0, size: 10 }), {
      wrapper: createQueryWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.content).toHaveLength(1)
    expect(result.current.data?.content[0].purchaserName).toBe('Jane Attendee')
  })

  it('never calls the network when eventId is empty -- the enabled guard', () => {
    const { result } = renderHook(() => useEventTicketSales('', { page: 0, size: 10 }), {
      wrapper: createQueryWrapper(),
    })

    expect(result.current.isPending).toBe(true)
    expect(result.current.fetchStatus).toBe('idle')
  })
})

describe('useOrganizerTicketSales', () => {
  it('returns the page of sales from GET /api/v1/events/tickets, across every event', async () => {
    server.use(
      http.get(`${EVENTS_BASE_URL}/tickets`, () =>
        HttpResponse.json({
          content: [ticketSaleFixture({ id: 'ticket-2', eventId: 'event-2', eventName: 'Winter Gala' })],
          totalElements: 1,
          totalPages: 1,
          number: 0,
          size: 10,
        }),
      ),
    )

    const { result } = renderHook(() => useOrganizerTicketSales({ page: 0, size: 10 }), {
      wrapper: createQueryWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.content[0].eventName).toBe('Winter Gala')
  })
})

describe('useCancelTicketForOrganizer', () => {
  it('invalidates the whole ticket-sales cache on success -- both per-event and organizer-wide lists', async () => {
    let eventListRequestCount = 0
    let organizerListRequestCount = 0

    server.use(
      http.get(`${EVENTS_BASE_URL}/:eventId/tickets`, () => {
        eventListRequestCount += 1
        return HttpResponse.json({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 })
      }),
      http.get(`${EVENTS_BASE_URL}/tickets`, () => {
        organizerListRequestCount += 1
        return HttpResponse.json({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 })
      }),
      http.post(`${EVENTS_BASE_URL}/:eventId/tickets/:ticketId/cancel`, () =>
        HttpResponse.json({
          id: 'ticket-1',
          status: TicketStatus.CANCELLED,
          cancelledAt: '2026-01-01T00:00:00',
          cancelReason: TicketCancelReason.ORGANIZER_ACTION,
          cancelNote: null,
        }),
      ),
    )

    const queryClient = createTestQueryClient()
    const wrapper = createQueryWrapper(queryClient)

    const eventList = renderHook(() => useEventTicketSales('event-1', { page: 0, size: 10 }), { wrapper })
    const organizerList = renderHook(() => useOrganizerTicketSales({ page: 0, size: 10 }), { wrapper })
    await waitFor(() => expect(eventList.result.current.isSuccess).toBe(true))
    await waitFor(() => expect(organizerList.result.current.isSuccess).toBe(true))
    expect(eventListRequestCount).toBe(1)
    expect(organizerListRequestCount).toBe(1)

    const cancel = renderHook(() => useCancelTicketForOrganizer('event-1', 'ticket-1'), { wrapper })
    cancel.result.current.mutate({})
    await waitFor(() => expect(cancel.result.current.isSuccess).toBe(true))

    // Both share the ['ticket-sales'] key prefix (see hooks.ts) -- a cancel scoped to
    // one event still refetches the cross-event list too, not just that event's.
    await waitFor(() => expect(eventListRequestCount).toBe(2))
    await waitFor(() => expect(organizerListRequestCount).toBe(2))
  })

  it("surfaces the backend's real error message on failure", async () => {
    server.use(
      http.post(`${EVENTS_BASE_URL}/:eventId/tickets/:ticketId/cancel`, () =>
        HttpResponse.json({ error: 'This ticket has already been cancelled' }, { status: 409 }),
      ),
    )

    const { result } = renderHook(() => useCancelTicketForOrganizer('event-1', 'ticket-1'), {
      wrapper: createQueryWrapper(),
    })

    result.current.mutate({ note: 'Refund requested' })

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect(result.current.error?.message).toBe('This ticket has already been cancelled')
  })
})
