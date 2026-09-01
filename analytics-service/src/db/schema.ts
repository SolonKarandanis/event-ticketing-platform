import {
  pgTable,
  bigserial,
  uuid,
  doublePrecision,
  timestamp,
} from 'drizzle-orm/pg-core';

export const ticketSales = pgTable('ticket_sales', {
  id: bigserial('id', { mode: 'number' }).primaryKey(),
  ticketId: uuid('ticket_id').notNull().unique(),
  ticketTypeId: uuid('ticket_type_id').notNull(),
  eventId: uuid('event_id').notNull(),
  organizerId: uuid('organizer_id').notNull(),
  purchaserId: uuid('purchaser_id').notNull(),
  price: doublePrecision('price').notNull(),
  purchasedAt: timestamp('purchased_at', { withTimezone: true }).notNull(),
  recordedAt: timestamp('recorded_at', { withTimezone: true })
    .notNull()
    .defaultNow(),
  // Nullable -- the row is kept, not deleted, on cancellation (see recordCancellation).
  // A cancelled sale still happened; getSummaryForEvent filters it out of revenue/
  // ticketsSold via `IS NULL` rather than the row's absence, preserving the full
  // history for a later gross-vs-net breakdown without another schema change.
  cancelledAt: timestamp('cancelled_at', { withTimezone: true }),
});

export type TicketSale = typeof ticketSales.$inferSelect;
