import { Injectable, Inject } from '@nestjs/common';
import { and, eq, gte, isNull, sql } from 'drizzle-orm';
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
        // count(*) alone comes back as Postgres bigint, which postgres.js
        // deserializes as a *string* to avoid silently losing precision above
        // Number.MAX_SAFE_INTEGER -- sql<number>() is a compile-time-only type
        // assertion, it doesn't cast anything at runtime. ::int makes Postgres do the
        // narrowing itself, so the driver hands back a real JS number (a ticket count
        // safely fits in 4 bytes).
        ticketsSold: sql<number>`count(*)::int`,
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

  // Organizer-wide rollup -- same shape as getSummaryForEvent, just without the eventId
  // filter, so it sums across every event the organizer has ever sold a ticket for.
  async getSummaryForOrganizer(organizerId: string) {
    const [summary] = await this.db
      .select({
        ticketsSold: sql<number>`count(*)::int`,
        revenue: sql<number>`coalesce(sum(${ticketSales.price}), 0)`,
      })
      .from(ticketSales)
      .where(
        and(
          eq(ticketSales.organizerId, organizerId),
          isNull(ticketSales.cancelledAt),
        ),
      );

    return summary;
  }

  // Daily revenue/tickets for the last `days` calendar days (UTC), organizer-wide,
  // cancelled sales excluded -- same filtering as the two summaries above. Returns one
  // point per day in the window, oldest first, zero-filled for days with no sales: a
  // sparse query result (only days that actually have rows) would otherwise leave gaps
  // in the trend line instead of an honest flat stretch at zero.
  async getSalesOverTime(organizerId: string, days = 30) {
    const since = new Date();
    since.setUTCHours(0, 0, 0, 0);
    since.setUTCDate(since.getUTCDate() - (days - 1));

    // AT TIME ZONE 'UTC' on a timestamptz column converts it to the UTC wall-clock
    // reading before truncating to a day -- bucketing has to be timezone-fixed, or
    // which calendar day a sale near midnight lands in would depend on the database
    // server's local timezone setting instead of a value this service controls.
    const dateExpr = sql<string>`to_char(${ticketSales.purchasedAt} at time zone 'UTC', 'YYYY-MM-DD')`;

    const rows = await this.db
      .select({
        date: dateExpr,
        ticketsSold: sql<number>`count(*)::int`,
        revenue: sql<number>`coalesce(sum(${ticketSales.price}), 0)`,
      })
      .from(ticketSales)
      .where(
        and(
          eq(ticketSales.organizerId, organizerId),
          isNull(ticketSales.cancelledAt),
          gte(ticketSales.purchasedAt, since),
        ),
      )
      .groupBy(dateExpr)
      .orderBy(dateExpr);

    const byDate = new Map(rows.map((row) => [row.date, row]));
    const series: { date: string; ticketsSold: number; revenue: number }[] = [];
    for (let i = 0; i < days; i++) {
      const day = new Date(since);
      day.setUTCDate(day.getUTCDate() + i);
      const date = day.toISOString().slice(0, 10);
      const found = byDate.get(date);
      series.push({
        date,
        ticketsSold: found?.ticketsSold ?? 0,
        revenue: found?.revenue ?? 0,
      });
    }
    return series;
  }
}
