export interface TicketPurchasedEvent {
  ticketId: string;
  ticketTypeId: string;
  eventId: string;
  organizerId: string;
  purchaserId: string;
  orderId: string | null;
  price: number;
  currency: string;
  purchasedAt: string;
}
