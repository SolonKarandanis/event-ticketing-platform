// Named React Query hooks wrapping api.ts -- query keys and invalidation live here
// once, not duplicated at call sites. See issue #9.
import type {PaginationParams} from "#/lib/pagination.ts";
import {queryOptions, useMutation, useQuery} from "@tanstack/react-query";
import {getTicket, getTicketQrCode, listTickets, validateTicket} from "#/features/tickets/api.ts";
import {toast} from "sonner";
import {toastErrorMessage} from "#/lib/api-client.ts";

const ticketsKey = ['tickets'] as const

// Exported so route loaders can ensureQueryData against the exact same cache entry
// these hooks read -- see features/events/hooks.ts for the pattern this follows.
export function ticketsQueryOptions(params: PaginationParams) {
    return queryOptions({
        queryKey: [...ticketsKey, params],
        queryFn: () => listTickets(params),
    });
}

export function ticketQueryOptions(ticketId: string) {
    return queryOptions({
        queryKey: [...ticketsKey, ticketId],
        queryFn: () => getTicket(ticketId),
        enabled: Boolean(ticketId),
    });
}

export function ticketQrCodeQueryOptions(ticketId: string) {
    return queryOptions({
        queryKey: [...ticketsKey, ticketId, 'qr-code'],
        queryFn: () => getTicketQrCode(ticketId),
        enabled: Boolean(ticketId),
    });
}

export function useTickets(params: PaginationParams) {
    return useQuery(ticketsQueryOptions(params));
}

export function useTicket(ticketId: string) {
    return useQuery(ticketQueryOptions(ticketId));
}

export function useTicketQrCode(ticketId: string) {
    return useQuery(ticketQrCodeQueryOptions(ticketId));
}

// No onSuccess toast/invalidation here: a validation call resolves with HTTP 200 for a
// legitimate INVALID/EXPIRED result just as much as for VALID (only a genuinely missing
// QR code/ticket is an error) -- a blanket "success" toast would say the same thing
// whether staff should admit or deny someone. The validation screen (issue #9) renders
// its own full-screen ADMIT/ALREADY-USED/NOT-FOUND result straight off `data.status`.
// Nothing in ListTicketResponse/GetTicketResponse reflects validation state either, so
// there's no tickets query left stale to invalidate.
export function useValidateTicket() {
    return useMutation({
        mutationFn: validateTicket,
        onError: (error) => {
            toast.error(toastErrorMessage(error, "Couldn't validate ticket"));
        },
    });
}