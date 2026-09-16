import { randomUUID } from 'node:crypto';
import path from 'node:path';
import postgres from 'postgres';
import { drizzle, PostgresJsDatabase } from 'drizzle-orm/postgres-js';
import { migrate } from 'drizzle-orm/postgres-js/migrator';
import {
  PostgreSqlContainer,
  StartedPostgreSqlContainer,
} from '@testcontainers/postgresql';
import { TicketSalesService } from './ticket-sales.service';
import { TicketPurchasedEvent } from './ticket-purchased.event';
import { TicketCancelledEvent } from './ticket-cancelled.event';
import * as schema from '../db/schema';

// Real Postgres via Testcontainers, migrated with the project's own drizzle/ SQL files --
// same reasoning as ticket-service's AbstractPostgresContainerTest: recordSale's
// onConflictDoNothing and getSummaryForEvent's aggregate/filter behavior are exactly the
// kind of thing a mocked query builder would let through unverified. No PostGIS needed
// here (ticket_sales has no geo columns), so a plain postgres image is enough.
describe('TicketSalesService', () => {
  jest.setTimeout(60_000);

  let container: StartedPostgreSqlContainer;
  let sql: postgres.Sql;
  let db: PostgresJsDatabase<typeof schema>;
  let service: TicketSalesService;

  beforeAll(async () => {
    container = await new PostgreSqlContainer('postgres:14-alpine').start();
    sql = postgres(container.getConnectionUri());
    db = drizzle(sql, { schema });
    await migrate(db, {
      migrationsFolder: path.join(__dirname, '..', '..', 'drizzle'),
    });
    service = new TicketSalesService(db);
  });

  afterAll(async () => {
    await sql.end();
    await container.stop();
  });

  function purchasedEvent(
    overrides: Partial<TicketPurchasedEvent> = {},
  ): TicketPurchasedEvent {
    return {
      ticketId: randomUUID(),
      ticketTypeId: randomUUID(),
      eventId: randomUUID(),
      organizerId: randomUUID(),
      purchaserId: randomUUID(),
      price: 25,
      purchasedAt: '2026-01-01T00:00:00.000Z',
      ...overrides,
    };
  }

  async function findByTicketId(ticketId: string) {
    return db.query.ticketSales.findFirst({
      where: (row, { eq }) => eq(row.ticketId, ticketId),
    });
  }

  describe('recordSale', () => {
    it('inserts a row with every field mapped, uncancelled', async () => {
      const event = purchasedEvent();

      await service.recordSale(event);

      const row = await findByTicketId(event.ticketId);
      expect(row).toMatchObject({
        ticketId: event.ticketId,
        ticketTypeId: event.ticketTypeId,
        eventId: event.eventId,
        organizerId: event.organizerId,
        purchaserId: event.purchaserId,
        price: event.price,
        cancelledAt: null,
      });
      expect(row?.purchasedAt.toISOString()).toBe(event.purchasedAt);
    });

    it('is idempotent for a redelivered message with the same ticketId', async () => {
      const event = purchasedEvent();

      await service.recordSale(event);
      // A genuine redelivery, not a retry with different data -- same ticketId, same
      // payload, exactly what RabbitMQ's at-least-once delivery can produce.
      await service.recordSale(event);

      const rows = await db.query.ticketSales.findMany({
        where: (row, { eq }) => eq(row.ticketId, event.ticketId),
      });
      expect(rows).toHaveLength(1);
    });
  });

  describe('recordCancellation', () => {
    it('sets cancelledAt without touching the original sale data', async () => {
      const event = purchasedEvent();
      await service.recordSale(event);

      const cancelledAt = '2026-02-01T00:00:00.000Z';
      await service.recordCancellation({
        ticketId: event.ticketId,
        ticketTypeId: event.ticketTypeId,
        eventId: event.eventId,
        organizerId: event.organizerId,
        purchaserId: event.purchaserId,
        cancelledAt,
        cancelReason: 'ATTENDEE_REQUEST',
      } satisfies TicketCancelledEvent);

      const row = await findByTicketId(event.ticketId);
      expect(row?.cancelledAt?.toISOString()).toBe(cancelledAt);
      // The row is kept, not deleted -- price/purchasedAt survive cancellation intact
      // for a later gross-vs-net breakdown.
      expect(row?.price).toBe(event.price);
      expect(row?.purchasedAt.toISOString()).toBe(event.purchasedAt);
    });

    it('is a harmless no-op when the purchase message has not landed yet', async () => {
      const ticketId = randomUUID();

      await expect(
        service.recordCancellation({
          ticketId,
          ticketTypeId: randomUUID(),
          eventId: randomUUID(),
          organizerId: randomUUID(),
          purchaserId: randomUUID(),
          cancelledAt: '2026-02-01T00:00:00.000Z',
          cancelReason: 'ATTENDEE_REQUEST',
        }),
      ).resolves.not.toThrow();

      expect(await findByTicketId(ticketId)).toBeUndefined();
    });
  });

  describe('getSummaryForEvent', () => {
    it('sums price and counts tickets for the given event and organizer', async () => {
      const eventId = randomUUID();
      const organizerId = randomUUID();
      await service.recordSale(
        purchasedEvent({ eventId, organizerId, price: 10 }),
      );
      await service.recordSale(
        purchasedEvent({ eventId, organizerId, price: 15 }),
      );

      const summary = await service.getSummaryForEvent(eventId, organizerId);

      expect(summary).toMatchObject({ eventId, ticketsSold: 2, revenue: 25 });
    });

    it('excludes cancelled tickets from both ticketsSold and revenue', async () => {
      const eventId = randomUUID();
      const organizerId = randomUUID();
      const kept = purchasedEvent({ eventId, organizerId, price: 10 });
      const cancelled = purchasedEvent({ eventId, organizerId, price: 40 });
      await service.recordSale(kept);
      await service.recordSale(cancelled);
      await service.recordCancellation({
        ticketId: cancelled.ticketId,
        ticketTypeId: cancelled.ticketTypeId,
        eventId,
        organizerId,
        purchaserId: cancelled.purchaserId,
        cancelledAt: '2026-02-01T00:00:00.000Z',
        cancelReason: 'ORGANIZER_ACTION',
      });

      const summary = await service.getSummaryForEvent(eventId, organizerId);

      expect(summary).toMatchObject({ eventId, ticketsSold: 1, revenue: 10 });
    });

    it('never returns null revenue for an event with no sales', async () => {
      const summary = await service.getSummaryForEvent(
        randomUUID(),
        randomUUID(),
      );

      expect(summary).toMatchObject({ ticketsSold: 0, revenue: 0 });
    });

    it("does not count another organizer's sales for the same event", async () => {
      // The whole point of issue #15's fix: eventId alone used to be enough to read
      // someone else's summary. Two different organizerIds, same eventId.
      const eventId = randomUUID();
      const ownerOrganizerId = randomUUID();
      const otherOrganizerId = randomUUID();
      await service.recordSale(
        purchasedEvent({ eventId, organizerId: ownerOrganizerId, price: 50 }),
      );

      const attackerView = await service.getSummaryForEvent(
        eventId,
        otherOrganizerId,
      );

      expect(attackerView).toMatchObject({ ticketsSold: 0, revenue: 0 });
    });
  });

  describe('getSummaryForOrganizer', () => {
    it('sums across every event the organizer has sold tickets for', async () => {
      const organizerId = randomUUID();
      await service.recordSale(
        purchasedEvent({ organizerId, eventId: randomUUID(), price: 10 }),
      );
      await service.recordSale(
        purchasedEvent({ organizerId, eventId: randomUUID(), price: 15 }),
      );

      const summary = await service.getSummaryForOrganizer(organizerId);

      expect(summary).toMatchObject({ ticketsSold: 2, revenue: 25 });
    });

    it("excludes cancelled tickets and other organizers' sales", async () => {
      const organizerId = randomUUID();
      const kept = purchasedEvent({ organizerId, price: 10 });
      const cancelled = purchasedEvent({ organizerId, price: 40 });
      const otherOrganizer = purchasedEvent({ price: 1000 });
      await service.recordSale(kept);
      await service.recordSale(cancelled);
      await service.recordSale(otherOrganizer);
      await service.recordCancellation({
        ticketId: cancelled.ticketId,
        ticketTypeId: cancelled.ticketTypeId,
        eventId: cancelled.eventId,
        organizerId,
        purchaserId: cancelled.purchaserId,
        cancelledAt: '2026-02-01T00:00:00.000Z',
        cancelReason: 'ORGANIZER_ACTION',
      });

      const summary = await service.getSummaryForOrganizer(organizerId);

      expect(summary).toMatchObject({ ticketsSold: 1, revenue: 10 });
    });

    it('returns zero, not null, for an organizer with no sales', async () => {
      const summary = await service.getSummaryForOrganizer(randomUUID());

      expect(summary).toMatchObject({ ticketsSold: 0, revenue: 0 });
    });
  });

  describe('getSalesOverTime', () => {
    // Fixed midday UTC so a sale lands solidly inside its calendar day regardless of
    // what time this suite happens to run.
    function daysAgoIso(daysAgo: number): string {
      const date = new Date();
      date.setUTCHours(12, 0, 0, 0);
      date.setUTCDate(date.getUTCDate() - daysAgo);
      return date.toISOString();
    }

    function dateOnly(iso: string): string {
      return iso.slice(0, 10);
    }

    it('returns one zero-filled point per day, oldest first, for the default 30-day window', async () => {
      const series = await service.getSalesOverTime(randomUUID());

      expect(series).toHaveLength(30);
      expect(
        series.every((point) => point.ticketsSold === 0 && point.revenue === 0),
      ).toBe(true);
      expect(series[0].date).toBe(dateOnly(daysAgoIso(29)));
      expect(series[29].date).toBe(dateOnly(daysAgoIso(0)));
    });

    it('buckets sales onto their own day and sums same-day sales together', async () => {
      const organizerId = randomUUID();
      const today = daysAgoIso(0);
      await service.recordSale(
        purchasedEvent({ organizerId, price: 10, purchasedAt: today }),
      );
      await service.recordSale(
        purchasedEvent({ organizerId, price: 15, purchasedAt: today }),
      );
      await service.recordSale(
        purchasedEvent({ organizerId, price: 20, purchasedAt: daysAgoIso(5) }),
      );

      const series = await service.getSalesOverTime(organizerId);

      const todayPoint = series.find((point) => point.date === dateOnly(today));
      const fiveDaysAgoPoint = series.find(
        (point) => point.date === dateOnly(daysAgoIso(5)),
      );
      expect(todayPoint).toMatchObject({ ticketsSold: 2, revenue: 25 });
      expect(fiveDaysAgoPoint).toMatchObject({ ticketsSold: 1, revenue: 20 });
    });

    it("excludes cancelled sales, other organizers' sales, and sales outside the window", async () => {
      const organizerId = randomUUID();
      const cancelled = purchasedEvent({
        organizerId,
        price: 99,
        purchasedAt: daysAgoIso(1),
      });
      await service.recordSale(cancelled);
      await service.recordCancellation({
        ticketId: cancelled.ticketId,
        ticketTypeId: cancelled.ticketTypeId,
        eventId: cancelled.eventId,
        organizerId,
        purchaserId: cancelled.purchaserId,
        cancelledAt: daysAgoIso(0),
        cancelReason: 'ATTENDEE_REQUEST',
      });
      // Someone else's sale, same day.
      await service.recordSale(
        purchasedEvent({ price: 500, purchasedAt: daysAgoIso(1) }),
      );
      // Comfortably outside the default 30-day window.
      await service.recordSale(
        purchasedEvent({
          organizerId,
          price: 1000,
          purchasedAt: daysAgoIso(45),
        }),
      );

      const series = await service.getSalesOverTime(organizerId);

      expect(
        series.every((point) => point.ticketsSold === 0 && point.revenue === 0),
      ).toBe(true);
    });

    it('honors a custom days window', async () => {
      const series = await service.getSalesOverTime(randomUUID(), 7);

      expect(series).toHaveLength(7);
      expect(series[0].date).toBe(dateOnly(daysAgoIso(6)));
      expect(series[6].date).toBe(dateOnly(daysAgoIso(0)));
    });
  });
});
