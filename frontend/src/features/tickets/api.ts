// Typed fetch functions for the tickets resource -- populated when building the
// attendee's purchased-tickets/QR code views and the staff validation screen (issue #9).
import type {PaginationParams} from "#/lib/pagination.ts";
import {apiFetch, parseBlobOrThrow, parseJsonOrThrow} from "#/lib/api-client.ts";
import type {Page} from "#/lib/api-client.ts";
import type {
    CancelTicketRequest,
    CancelTicketResponse,
    GetTicketResponse,
    ListTicketResponse,
    TicketValidationRequest,
    TicketValidationResponse
} from "#/features/tickets/types.ts";

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/tickets`

export async function listTickets({ page, size }: PaginationParams):Promise<Page<ListTicketResponse>>{
    const response = await apiFetch(`${BASE_URL}?page=${page}&size=${size}`);
    return parseJsonOrThrow<Page<ListTicketResponse>>(response);
}

export async function getTicket(ticketId: string):Promise<GetTicketResponse>{
    const response = await apiFetch(`${BASE_URL}/${ticketId}`);
    return parseJsonOrThrow<GetTicketResponse>(response);
}

export async function getTicketQrCode(ticketId: string): Promise<Blob> {
    const response = await apiFetch(`${BASE_URL}/${ticketId}/qr-codes`);
    return parseBlobOrThrow(response);
}

export async function cancelTicket(ticketId: string, request: CancelTicketRequest): Promise<CancelTicketResponse> {
    const response = await apiFetch(`${BASE_URL}/${ticketId}/cancel`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
    });
    return parseJsonOrThrow<CancelTicketResponse>(response);
}

export async function validateTicket(request:TicketValidationRequest):Promise<TicketValidationResponse>{
    const response = await apiFetch(`${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/ticket-validations`,{
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request)
    });
    return parseJsonOrThrow<TicketValidationResponse>(response);
}