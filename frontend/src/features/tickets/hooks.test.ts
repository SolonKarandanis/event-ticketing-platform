// Same pattern as features/events/hooks.test.ts, extended to two things that file
// didn't need: a query whose success body is a raw Blob, not JSON (useTicketQrCode),
// and a mutation with deliberately no onSuccess/invalidation at all (useValidateTicket --
// see its own comment in hooks.ts for why a 200 response isn't necessarily "success").
import { describe, expect, it } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { server } from '#/test/msw/server'
import { createQueryWrapper, createTestQueryClient } from '#/test/test-utils'
import { useCancelTicket, useTicket, useTicketQrCode, useTickets, useValidateTicket } from './hooks'
import { TicketCancelReason, TicketStatus, TicketValidationMethod, TicketValidationStatus } from './types'
import type { GetTicketResponse, ListTicketResponse } from './types'

const TICKETS_BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/tickets`
const VALIDATIONS_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/ticket-validations`

function ticketFixture(overrides: Partial<GetTicketResponse> = {}): GetTicketResponse {
  return {
    id: 'ticket-1',
    referenceCode: 'XY3P9KRT',
    status: TicketStatus.PURCHASED,
    price: 25,
    description: 'General admission',
    eventName: 'Summer Fest',
    eventVenueName: 'Test Venue',
    eventStart: null,
    eventEnd: null,
    ...overrides,
  }
}

describe('useTickets', () => {
  it('returns the page of tickets from GET /api/v1/tickets', async () => {
    const listItem: ListTicketResponse = {
      id: 'ticket-1',
      status: TicketStatus.PURCHASED,
      ticketType: { id: 'tt-1', name: 'General', price: 25 },
    }
    server.use(
      http.get(TICKETS_BASE_URL, () =>
        HttpResponse.json({ content: [listItem], totalElements: 1, totalPages: 1, number: 0, size: 10 }),
      ),
    )

    const { result } = renderHook(() => useTickets({ page: 0, size: 10 }), {
      wrapper: createQueryWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.content).toHaveLength(1)
    expect(result.current.data?.content[0].ticketType.name).toBe('General')
  })
})

describe('useTicket', () => {
  it('returns the ticket details from GET /api/v1/tickets/:ticketId', async () => {
    server.use(
      http.get(`${TICKETS_BASE_URL}/:ticketId`, () => HttpResponse.json(ticketFixture())),
    )

    const { result } = renderHook(() => useTicket('ticket-1'), {
      wrapper: createQueryWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.referenceCode).toBe('XY3P9KRT')
  })

  it('never calls the network when ticketId is empty -- the enabled guard', () => {
    const { result } = renderHook(() => useTicket(''), {
      wrapper: createQueryWrapper(),
    })

    expect(result.current.isPending).toBe(true)
    expect(result.current.fetchStatus).toBe('idle')
  })
})

describe('useTicketQrCode', () => {
  it('resolves with the raw PNG bytes as a Blob, not JSON', async () => {
    const pngBytes = new Uint8Array([0x89, 0x50, 0x4e, 0x47])
    server.use(
      http.get(`${TICKETS_BASE_URL}/:ticketId/qr-codes`, () =>
        HttpResponse.arrayBuffer(pngBytes.buffer, { headers: { 'Content-Type': 'image/png' } }),
      ),
    )

    const { result } = renderHook(() => useTicketQrCode('ticket-1'), {
      wrapper: createQueryWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toBeInstanceOf(Blob)
    expect(result.current.data?.type).toBe('image/png')
    const bytes = new Uint8Array(await result.current.data!.arrayBuffer())
    expect(Array.from(bytes)).toEqual(Array.from(pngBytes))
  })
})

describe('useValidateTicket', () => {
  it('resolves normally on a 200 with an ALREADY-USED-style INVALID result -- not an error', async () => {
    server.use(
      http.post(VALIDATIONS_URL, () =>
        HttpResponse.json({ ticketId: 'ticket-1', status: TicketValidationStatus.INVALID }),
      ),
    )

    const { result } = renderHook(() => useValidateTicket(), {
      wrapper: createQueryWrapper(),
    })

    result.current.mutate({ id: 'some-qr-domain-id', method: TicketValidationMethod.QR_SCAN })

    // The whole point of this hook (see its comment in hooks.ts): INVALID is a normal,
    // successful HTTP 200 response, not a thrown error -- the caller reads
    // data.status itself to decide ADMIT vs ALREADY-USED.
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.status).toBe(TicketValidationStatus.INVALID)
    expect(result.current.isError).toBe(false)
  })

  it('does still surface a genuine error -- a ticket/QR code that truly does not exist', async () => {
    server.use(
      http.post(VALIDATIONS_URL, () =>
        HttpResponse.json({ error: 'QR code not found' }, { status: 400 }),
      ),
    )

    const { result } = renderHook(() => useValidateTicket(), {
      wrapper: createQueryWrapper(),
    })

    result.current.mutate({ id: 'unknown-id', method: TicketValidationMethod.QR_SCAN })

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect(result.current.error?.message).toBe('QR code not found')
  })
})

describe('useCancelTicket', () => {
  it('invalidates the tickets cache on success, refetching any active list query', async () => {
    let listRequestCount = 0
    server.use(
      http.get(TICKETS_BASE_URL, () => {
        listRequestCount += 1
        return HttpResponse.json({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 })
      }),
      http.post(`${TICKETS_BASE_URL}/:ticketId/cancel`, () =>
        HttpResponse.json({
          id: 'ticket-1',
          status: TicketStatus.CANCELLED,
          cancelledAt: '2026-01-01T00:00:00',
          cancelReason: TicketCancelReason.ATTENDEE_REQUEST,
          cancelNote: 'Changed my mind',
        }),
      ),
    )

    const queryClient = createTestQueryClient()
    const wrapper = createQueryWrapper(queryClient)

    const list = renderHook(() => useTickets({ page: 0, size: 10 }), { wrapper })
    await waitFor(() => expect(list.result.current.isSuccess).toBe(true))
    expect(listRequestCount).toBe(1)

    const cancel = renderHook(() => useCancelTicket('ticket-1'), { wrapper })
    cancel.result.current.mutate({ note: 'Changed my mind' })
    await waitFor(() => expect(cancel.result.current.isSuccess).toBe(true))

    await waitFor(() => expect(listRequestCount).toBe(2))
  })
})
