import { Injectable, Inject } from '@nestjs/common';
import { and, eq, sql } from 'drizzle-orm';
import { PostgresJsDatabase } from 'drizzle-orm/postgres-js';
import { DRIZZLE } from '../db/drizzle.provider';
import { ticketSales } from '../db/schema';
import { TicketPurchasedEvent } from './ticket-purchased.event';
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
        ),
      );

    return { eventId, ...summary };
  }
}
