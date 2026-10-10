export interface TicketCancelledEvent {
  ticketId: string;
  ticketTypeId: string;
  eventId: string;
  organizerId: string;
  purchaserId: string;
  // Unused by recordCancellation today (it only ever updates an already-recorded
  // sale's cancelledAt) -- carried for symmetry with TicketPurchasedEvent.orderId,
  // same "capture now, no consumer needed yet" reasoning.
  orderId: string | null;
  cancelledAt: string;
  cancelReason: string;
}
