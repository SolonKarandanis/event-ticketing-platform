import type { Server } from 'node:http';
import { Controller, Get, INestApplication, Param } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import request from 'supertest';
import { MetricsModule } from './metrics.module';

@Controller('things')
class ThingsController {
  @Get(':id')
  get(@Param('id') id: string) {
    return { id };
  }
}

// Over real HTTP, like HealthController's spec: checks /metrics needs no token and that
// requests are recorded by route pattern rather than raw URL.
describe('MetricsModule', () => {
  let app: INestApplication;

  beforeAll(async () => {
    const moduleRef = await Test.createTestingModule({
      imports: [MetricsModule],
      controllers: [ThingsController],
    }).compile();

    app = moduleRef.createNestApplication({ logger: false });
    await app.init();
  });

  afterAll(async () => {
    await app.close();
  });

  const scrape = async () =>
    (
      await request(app.getHttpServer() as Server)
        .get('/metrics')
        .expect(200)
    ).text;

  it('serves Node process metrics without a token', async () => {
    const body = await scrape();

    expect(body).toContain('process_resident_memory_bytes');
    expect(body).toContain('nodejs_eventloop_lag_seconds');
    expect(body).toContain('nodejs_gc_duration_seconds');
  });

  it('records requests by route pattern, not by raw URL', async () => {
    const server = app.getHttpServer() as Server;
    await request(server).get('/things/42').expect(200);
    await request(server).get('/things/43').expect(200);

    const body = await scrape();

    expect(body).toMatch(
      /http_server_requests_seconds_count\{[^}]*route="\/things\/:id"[^}]*\} 2/,
    );
    expect(body).not.toContain('/things/42');
  });

  it('groups unmatched requests under one label', async () => {
    await request(app.getHttpServer() as Server)
      .get('/nope/123')
      .expect(404);

    const body = await scrape();

    expect(body).toContain('route="UNMATCHED"');
    expect(body).not.toContain('/nope/123');
  });

  it('does not record its own scrapes', async () => {
    await scrape();

    expect(await scrape()).not.toContain('route="/metrics"');
  });
});
