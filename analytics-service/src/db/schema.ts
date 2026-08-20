import { pgTable, bigserial, uuid, doublePrecision, timestamp } from 'drizzle-orm/pg-core';

export const ticketSales = pgTable('ticket_sales', {
  id: bigserial('id', { mode: 'number' }).primaryKey(),
  ticketId: uuid('ticket_id').notNull().unique(),
  ticketTypeId: uuid('ticket_type_id').notNull(),
  eventId: uuid('event_id').notNull(),
  purchaserId: uuid('purchaser_id').notNull(),
  price: doublePrecision('price').notNull(),
  purchasedAt: timestamp('purchased_at', { withTimezone: true }).notNull(),
  recordedAt: timestamp('recorded_at', { withTimezone: true }).notNull().defaultNow(),
});

export type TicketSale = typeof ticketSales.$inferSelect;
