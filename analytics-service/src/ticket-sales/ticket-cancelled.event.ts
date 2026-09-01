export interface TicketCancelledEvent {
  ticketId: string;
  ticketTypeId: string;
  eventId: string;
  organizerId: string;
  purchaserId: string;
  cancelledAt: string;
  cancelReason: string;
}
