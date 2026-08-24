// Typed fetch functions for the events resource -- populated when building the
// screens from GitHub issue #6 (Event creation/edit form) and #10 (published events
// search & browse), against ticket-service's /api/v1/events and /api/v1/published-events.
import type {
    CreateEventRequest,
    CreateEventResponse,
    GetEventDetailsResponse,
    ListEventResponse, UpdateEventRequest, UpdateEventResponse
} from "#/features/events/types.ts";
import type {PaginationParams} from "#/lib/pagination.ts";
import {apiFetch, type Page, parseJsonOrThrow} from "#/lib/api-client.ts";

const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/events`

export async function listEvents({ page, size }: PaginationParams): Promise<Page<ListEventResponse>> {
    const response = await apiFetch(`${BASE_URL}?page=${page}&size=${size}`);
    return parseJsonOrThrow<Page<ListEventResponse>>(response);
}

export async function getEvent(eventId: string):Promise<GetEventDetailsResponse>{
    const response = await apiFetch(`${BASE_URL}/${eventId}`);
    return parseJsonOrThrow<GetEventDetailsResponse>(response);
}

export async function createEvent(request:CreateEventRequest):Promise<CreateEventResponse>{
    const response = await apiFetch(BASE_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
    });
    return parseJsonOrThrow<CreateEventResponse>(response);
}

export async function updateEvent(eventId: string, request:UpdateEventRequest):Promise<UpdateEventResponse>{
    const response = await apiFetch(`${BASE_URL}/${eventId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
    });
    return parseJsonOrThrow<UpdateEventResponse>(response);
}

export async function deleteEvent(eventId: string):Promise<void>{
    const response = await apiFetch(`${BASE_URL}/${eventId}`, {
        method: 'DELETE',
    });
    return parseJsonOrThrow<void>(response);
}

export async function publishEvent(eventId: string):Promise<void> {
    const response = await apiFetch(`${BASE_URL}/${eventId}/publish`, {
        method: 'POST',
    });
    return parseJsonOrThrow<void>(response);
}

export async function cancelEvent(eventId: string):Promise<void> {
    const response = await apiFetch(`${BASE_URL}/${eventId}/cancel`, {
        method: 'POST',
    });
    return parseJsonOrThrow<void>(response);
}

export async function completeEvent(eventId: string):Promise<void> {
    const response = await apiFetch(`${BASE_URL}/${eventId}/complete`, {
        method: 'POST',
    });
    return parseJsonOrThrow<void>(response);
}
