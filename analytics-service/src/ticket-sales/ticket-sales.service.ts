import { Injectable, Inject } from '@nestjs/common';
import { and, eq, isNull, sql } from 'drizzle-orm';
import { PostgresJsDatabase } from 'drizzle-orm/postgres-js';
import { DRIZZLE } from '../db/drizzle.provider';
import { ticketSales } from '../db/schema';
import { TicketPurchasedEvent } from './ticket-purchased.event';
import { TicketCancelledEvent } from './ticket-cancelled.event';
import * as schema from '../db/schema';

@Injectable()
export class TicketSalesService {
  constructor(
    @Inject(DRIZZLE) private readonly db: PostgresJsDatabase<typeof schema>,
  ) {}

  async recordSale(event: TicketPurchasedEvent): Promise<void> {
    await this.db
      .insert(ticketSales)
      .values({
        ticketId: event.ticketId,
        ticketTypeId: event.ticketTypeId,
        eventId: event.eventId,
        organizerId: event.organizerId,
        purchaserId: event.purchaserId,
        price: event.price,
        purchasedAt: new Date(event.purchasedAt),
      })
      .onConflictDoNothing({ target: ticketSales.ticketId });
  }

  // A plain UPDATE, not a delete -- the row (and its original price/purchasedAt) stays,
  // just marked cancelled. Idempotent in the same spirit as recordSale's
  // onConflictDoNothing: a redelivered ticket.cancelled message re-runs this against a
  // ticketId that's either not there yet (no-op, nothing to update -- the purchase
  // message hasn't been processed yet) or already has cancelledAt set (harmless re-set
  // to the same value), never an error either way.
  async recordCancellation(event: TicketCancelledEvent): Promise<void> {
    await this.db
      .update(ticketSales)
      .set({ cancelledAt: new Date(event.cancelledAt) })
      .where(eq(ticketSales.ticketId, event.ticketId));
  }

  async getSummaryForEvent(eventId: string, organizerId: string) {
    const [summary] = await this.db
      .select({
        ticketsSold: sql<number>`count(*)`,
        revenue: sql<number>`coalesce(sum(${ticketSales.price}), 0)`,
      })
      .from(ticketSales)
      .where(
        and(
          eq(ticketSales.eventId, eventId),
          eq(ticketSales.organizerId, organizerId),
          isNull(ticketSales.cancelledAt),
        ),
      );

    return { eventId, ...summary };
  }
}
