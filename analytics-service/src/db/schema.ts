import {
  pgTable,
  bigserial,
  uuid,
  integer,
  varchar,
  timestamp,
} from 'drizzle-orm/pg-core';

export const ticketSales = pgTable('ticket_sales', {
  id: bigserial('id', { mode: 'number' }).primaryKey(),
  ticketId: uuid('ticket_id').notNull().unique(),
  ticketTypeId: uuid('ticket_type_id').notNull(),
  eventId: uuid('event_id').notNull(),
  organizerId: uuid('organizer_id').notNull(),
  purchaserId: uuid('purchaser_id').notNull(),
  // Cheap to add alongside the minor-units change below, matching #23's own
  // "capture now, expensive to backfill later" reasoning (same precedent as Venue's
  // lat/long). Nullable forever: no consumer needs it yet, and rows recorded before
  // this column existed have no order to backfill it from.
  orderId: uuid('order_id'),
  // Integer minor units (e.g. cents), matching #23's ticket-service representation --
  // was doublePrecision major units. A single ticket's price safely fits in 4 bytes;
  // see ticket-sales.service.ts's own getSummaryForEvent/getSummaryForOrganizer/
  // getSalesOverTime comments for why their SUM(price) aggregates are explicitly cast
  // back to ::int rather than left as Postgres's promoted bigint.
  price: integer('price').notNull(),
  // ISO 4217, single fixed value from ticket-service's own config -- no multi-currency
  // UI, matching TicketType.currency's own precedent.
  currency: varchar('currency', { length: 3 }).notNull(),
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
