export interface TicketPurchasedEvent {
  ticketId: string;
  ticketTypeId: string;
  eventId: string;
  purchaserId: string;
  price: number;
  purchasedAt: string;
}
