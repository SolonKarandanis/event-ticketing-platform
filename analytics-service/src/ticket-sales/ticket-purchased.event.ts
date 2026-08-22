export interface TicketPurchasedEvent {
  ticketId: string;
  ticketTypeId: string;
  eventId: string;
  organizerId: string;
  purchaserId: string;
  price: number;
  purchasedAt: string;
}
