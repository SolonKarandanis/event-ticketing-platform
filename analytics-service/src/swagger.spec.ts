import type { Server } from 'node:http';
import { INestApplication } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import request from 'supertest';
import { EventAnalyticsController } from './ticket-sales/event-analytics.controller';
import { OrganizerAnalyticsController } from './ticket-sales/organizer-analytics.controller';
import { TicketSalesService } from './ticket-sales/ticket-sales.service';
import { setupSwagger } from './swagger';

// A minimal module with just the two controllers Swagger needs to reflect, rather than
// the full AppModule -- that would also construct RabbitMqConsumerService (a real AMQP
// connection attempt) and the Drizzle/Postgres provider, neither of which
// SwaggerModule.createDocument() reads at all (it only introspects route/DTO metadata).
// AuthGuard('jwt')/RolesGuard on the controllers are never invoked here either: Swagger's
// own routes are mounted directly on the HTTP adapter, bypassing Nest's guarded routes
// entirely, so no PassportModule/strategy is needed just to serve the document.
describe('setupSwagger', () => {
  let app: INestApplication;

  beforeAll(async () => {
    const moduleRef = await Test.createTestingModule({
      controllers: [EventAnalyticsController, OrganizerAnalyticsController],
      providers: [
        {
          provide: TicketSalesService,
          useValue: {
            getSummaryForEvent: () => Promise.resolve(undefined),
            getSummaryForOrganizer: () => Promise.resolve(undefined),
            getSalesOverTime: () => Promise.resolve(undefined),
          },
        },
      ],
    }).compile();

    app = moduleRef.createNestApplication();
    setupSwagger(app);
    await app.init();
  });

  afterAll(async () => {
    await app.close();
  });

  it('serves the OpenAPI document publicly, listing known routes', async () => {
    const response = await request(app.getHttpServer() as Server)
      .get('/api-docs-json')
      .expect(200);
    const document = response.body as { openapi?: string; paths: object };

    expect(document.openapi).toBeDefined();
    // Known, stable paths -- confirms the document reflects the real controllers, not
    // just that some JSON came back.
    expect(document.paths).toHaveProperty('/analytics/organizer/summary');
    expect(document.paths).toHaveProperty(
      '/analytics/organizer/sales-over-time',
    );
    expect(document.paths).toHaveProperty(
      '/analytics/events/{eventId}/summary',
    );
  });

  it('serves the Swagger UI page publicly', async () => {
    await request(app.getHttpServer() as Server)
      .get('/api-docs')
      .expect(200);
  });
});
